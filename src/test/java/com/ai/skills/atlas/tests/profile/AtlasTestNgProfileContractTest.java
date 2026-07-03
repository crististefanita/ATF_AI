package com.ai.skills.atlas.tests.profile;

import com.ai.skills.atlas.contract.AtlasContractSuiteRef;
import com.ai.skills.atlas.contract.AtlasSuiteIds;
import com.ai.skills.atlas.contract.execution.AtlasExecutionProfile;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Listeners(TestListener.class)
@AtlasContractSuiteRef(AtlasSuiteIds.TESTNG_PROFILE_TAXONOMY)
public class AtlasTestNgProfileContractTest {
    private static final int MAX_TEST_CLASSES_PER_PACKAGE = 8;
    private static final Path ATLAS_RESOURCES = AtfSourcePaths.testResource("skills/atlas");
    private static final Set<String> SMOKE_CLASSES = Set.of(
            "com.ai.skills.atlas.tests.profile.AtlasTestNgProfileContractTest",
            "com.ai.skills.atlas.tests.report.AtlasReportModelContractTest",
            "com.ai.skills.atlas.tests.catalog.AtlasRuleCatalogContractTest",
            "com.ai.skills.atlas.tests.architecture.AtlasPackageShapeContractTest",
            "com.ai.skills.atlas.tests.yaml.AtlasBusinessUnderstandingConsumerContractTest",
            "com.ai.skills.atlas.tests.yaml.AtlasYamlRulePolicyContractTest",
            "com.ai.skills.atlas.tests.yaml.AtlasYamlSuiteContractTest"
    );
    private static final Set<String> FULL_REQUIRED_CLASSES = Set.of(
            "com.ai.skills.atlas.tests.profile.AtlasTestNgProfileContractTest",
            "com.ai.skills.atlas.tests.candidate.AtlasCandidateBusinessComprehensionContractTest",
            "com.ai.skills.atlas.tests.candidate.AtlasCandidateGraphContractTest",
            "com.ai.skills.atlas.tests.candidate.AtlasCandidateScoreContractTest",
            "com.ai.skills.atlas.tests.candidate.AtlasCandidateSkillReadinessContractTest",
            "com.ai.skills.atlas.tests.candidate.AtlasCandidateStructureContractTest",
            "com.ai.skills.atlas.tests.candidate.AtlasGeneratedOutputContractTest",
            "com.ai.skills.atlas.tests.report.AtlasReportModelContractTest",
            "com.ai.skills.atlas.tests.catalog.AtlasRuleCatalogContractTest",
            "com.ai.skills.atlas.tests.architecture.AtlasPackageShapeContractTest",
            "com.ai.skills.atlas.tests.yaml.AtlasBusinessUnderstandingConsumerContractTest",
            "com.ai.skills.atlas.tests.yaml.AtlasYamlRulePolicyContractTest",
            "com.ai.skills.atlas.tests.yaml.AtlasYamlSuiteContractTest"
    );

    @Test
    public void atlasSmokeProfileContainsOnlyFastAtlasSanityContracts() throws Exception {
        Document sanity = profile(AtlasExecutionProfile.SANITY);
        Document smoke = profile(AtlasExecutionProfile.SMOKE);

        Assert.assertEquals(sanity.getDocumentElement().getAttribute("name"), "Atlas Sanity");
        Assert.assertEquals(classes(sanity), SMOKE_CLASSES,
                "Atlas sanity must stay a small Atlas-only profile.");
        assertAtlasOnly(sanity);
        assertNoHeavyCandidateOrPublishedArtifactContracts(sanity);

        Assert.assertEquals(smoke.getDocumentElement().getAttribute("name"), "Framework Atlas Smoke Contracts");
        Assert.assertEquals(classes(smoke), SMOKE_CLASSES,
                "Atlas smoke must stay a small Atlas-only sanity profile.");
        assertAtlasOnly(smoke);
        assertNoHeavyCandidateOrPublishedArtifactContracts(smoke);
    }

    @Test
    public void atlasFullProfileContainsOnlyAtlasContracts() throws Exception {
        Document full = profile(AtlasExecutionProfile.FULL);
        Document businessCloseout = profile(AtlasExecutionProfile.BUSINESS_CLOSEOUT);

        Assert.assertEquals(full.getDocumentElement().getAttribute("name"), "Framework Atlas Contracts");
        Assert.assertTrue(classes(full).containsAll(FULL_REQUIRED_CLASSES),
                "Atlas full profile is missing required Atlas contract classes: "
                        + missing(FULL_REQUIRED_CLASSES, classes(full)));
        assertAtlasOnly(full);

        Assert.assertEquals(businessCloseout.getDocumentElement().getAttribute("name"),
                "Framework Atlas Business Closeout Contracts");
        Assert.assertTrue(classes(businessCloseout).containsAll(FULL_REQUIRED_CLASSES),
                "Atlas business closeout profile must keep the full business/report contract surface: "
                        + missing(FULL_REQUIRED_CLASSES, classes(businessCloseout)));
        Assert.assertTrue(excludesGroup(businessCloseout, "atlas-adapter-parity"),
                "Atlas business closeout must exclude adapter parity; Selenium belongs to full diagnostics.");
        assertAtlasOnly(businessCloseout);
    }

    @Test
    public void atlasExecutionProfilesExposeTypedRunnableSuites() {
        Assert.assertEquals(
                AtlasExecutionProfile.ids(),
                List.of("atlas-sanity", "atlas-smoke", "atlas-business-closeout", "atlas-full"),
                "Atlas execution profile vocabulary must stay small and agent-readable.");

        for (AtlasExecutionProfile profile : AtlasExecutionProfile.values()) {
            Assert.assertTrue(Files.isRegularFile(Path.of(profile.suiteXml())),
                    "Atlas execution profile must point to an executable suite: " + profile.suiteXml());
            Assert.assertTrue(profile.mavenCommand().contains(profile.suiteXml()),
                    "Atlas execution profile must expose a runnable Maven command: " + profile.mavenCommand());
            Assert.assertEquals(AtlasExecutionProfile.fromId(profile.id()), profile,
                    "Atlas execution profile id lookup must round-trip: " + profile.id());
        }
    }

    @Test
    public void atlasTestPackagesStayFocused() throws IOException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        try (var files = Files.walk(AtfSourcePaths.TEST_ATLAS_ROOT)) {
            files.filter(AtfSourcePaths::isJavaSource)
                    .filter(AtlasTestNgProfileContractTest::isTestClass)
                    .map(AtlasTestNgProfileContractTest::packageName)
                    .sorted()
                    .forEach(packageName -> counts.merge(packageName, 1, Integer::sum));
        }

        List<String> overloaded = counts.entrySet().stream()
                .filter(entry -> entry.getValue() > MAX_TEST_CLASSES_PER_PACKAGE)
                .map(entry -> entry.getKey() + " has " + entry.getValue()
                        + " test classes, max " + MAX_TEST_CLASSES_PER_PACKAGE)
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(overloaded.isEmpty(),
                "Split overloaded Atlas test packages into candidate/report/yaml/profile/catalog lanes: "
                        + overloaded);
    }

    private static Document profile(AtlasExecutionProfile executionProfile) throws Exception {
        Path profile = ATLAS_RESOURCES.resolve(executionProfile.suiteFileName());
        Assert.assertTrue(Files.isRegularFile(profile), "Missing Atlas TestNG profile: " + profile);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(profile.toFile());
    }

    private static Set<String> classes(Document document) {
        NodeList nodes = document.getElementsByTagName("class");
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (int index = 0; index < nodes.getLength(); index++) {
            values.add(nodes.item(index).getAttributes().getNamedItem("name").getNodeValue());
        }
        return values;
    }

    private static boolean excludesGroup(Document document, String group) {
        NodeList nodes = document.getElementsByTagName("exclude");
        for (int index = 0; index < nodes.getLength(); index++) {
            if (group.equals(nodes.item(index).getAttributes().getNamedItem("name").getNodeValue())) {
                return true;
            }
        }
        return false;
    }

    private static void assertAtlasOnly(Document document) {
        Set<String> foreignClasses = classes(document).stream()
                .filter(name -> !name.startsWith(AtfSourcePaths.ATLAS_ROOT_PACKAGE + ".tests."))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Assert.assertTrue(foreignClasses.isEmpty(),
                "Atlas profiles must not route ATF core, MCP, UI product or external validator tests: "
                        + foreignClasses);
    }

    private static void assertNoHeavyCandidateOrPublishedArtifactContracts(Document document) {
        Set<String> forbidden = classes(document).stream()
                .filter(name -> name.contains("Candidate")
                        || name.contains("GeneratedOutput")
                        || name.contains("PublishedExtent")
                        || name.contains("UiAgentic"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Assert.assertTrue(forbidden.isEmpty(),
                "Atlas smoke must stay fast and must not run candidate or published-artifact audits: " + forbidden);
    }

    private static Set<String> missing(Set<String> expected, Set<String> actual) {
        return expected.stream()
                .filter(value -> !actual.contains(value))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean isTestClass(Path path) {
        String source = read(path);
        return source.contains("@Test") && source.contains(" class ");
    }

    private static String packageName(Path path) {
        String source = read(path);
        int start = source.indexOf("package ");
        int end = source.indexOf(';', start);
        return source.substring(start + "package ".length(), end).trim();
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot inspect Atlas test source: " + path, exception);
        }
    }
}
