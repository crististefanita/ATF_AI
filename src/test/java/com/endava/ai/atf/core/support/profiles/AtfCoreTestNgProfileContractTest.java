package com.endava.ai.atf.core.support.profiles;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlProfileSpec;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CORE_TEST_NG_PROFILE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_TEST_NG_PROFILE_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class AtfCoreTestNgProfileContractTest {
    private static final int MAX_TEST_CLASSES_PER_PACKAGE = 8;
    private static final List<Path> CORE_SANITY_SURFACES = List.of(
            AtfSourcePaths.TEST_ATF_ROOT,
            AtfSourcePaths.testResource("atf/core")
    );
    private static final List<String> PUBLISHED_ARTIFACT_FOLDERS = List.of(
            "docs" + "/out",
            "docs" + "\\out",
            "docs" + "/atlas/validation",
            "docs" + "\\atlas\\validation"
    );

    @Test
    public void coreProfilesAreYamlDrivenAndMatchTestNgXml() throws Exception {
        for (AtfCoreYamlProfileSpec spec : AtfCoreYamlContractCatalog.profiles()) {
            Document document = profile(spec.suiteXml());

            if (!spec.expectedClasses().isEmpty()) {
                Assert.assertEquals(attributes(document, "class", "name"), spec.expectedClasses(),
                        spec.id() + " class order/scope drifted from profiles.yml");
            }
            if (!spec.expectedPackages().isEmpty()) {
                Assert.assertEquals(attributes(document, "package", "name"), spec.expectedPackages(),
                        spec.id() + " package discovery scope drifted from profiles.yml");
            }
            Assert.assertTrue(attributes(document, "include", "name").containsAll(spec.includedGroups()),
                    spec.id() + " missing included TestNG groups from profiles.yml");
            Assert.assertTrue(attributes(document, "exclude", "name").containsAll(spec.excludedGroups()),
                    spec.id() + " missing excluded TestNG groups from profiles.yml");
            assertRequiredClasses(spec, document);
            assertForbiddenClasses(spec, document);
        }
    }

    @Test
    public void coreProfilePurposeSeparatesFastVisualAggregateAndSmokeEvidence() {
        AtfCoreYamlProfileSpec smoke = AtfCoreYamlContractCatalog.requireProfile("atf-core-smoke");
        AtfCoreYamlProfileSpec fast = AtfCoreYamlContractCatalog.requireProfile("atf-core-fast");
        AtfCoreYamlProfileSpec visual = AtfCoreYamlContractCatalog.requireProfile("atf-core-visual");
        AtfCoreYamlProfileSpec aggregate = AtfCoreYamlContractCatalog.requireProfile("atf-core-aggregate");
        AtfCoreYamlProfileSpec full = AtfCoreYamlContractCatalog.requireProfile("atf-core-full");
        AtfCoreYamlProfileSpec browserSmoke =
                AtfCoreYamlContractCatalog.requireProfile("atf-ui-engine-browser-smoke");
        AtfCoreYamlProfileSpec seleniumExtentSmoke =
                AtfCoreYamlContractCatalog.requireProfile("atf-ui-engine-selenium-extent-smoke");

        Assert.assertEquals(smoke.stability(), "smoke");
        Assert.assertTrue(fast.expectedClasses().size() > smoke.expectedClasses().size(),
                "Core smoke must stay smaller than fast regression confidence.");
        Assert.assertEquals(fast.browser(), Boolean.FALSE.toString());
        Assert.assertEquals(visual.browser(), "partial");
        Assert.assertEquals(browserSmoke.browser(), Boolean.TRUE.toString());
        Assert.assertTrue(aggregate.finalArtifact(), "Aggregate profile is the ATF core public artifact profile.");
        Assert.assertFalse(full.finalArtifact(), "Full core sweep is health evidence, not a final artifact profile.");
        Assert.assertTrue(visual.includedGroups().contains(AtfTestGroups.EXTENT_ONLY));
        Assert.assertTrue(visual.includedGroups().contains(AtfTestGroups.ALLURE_ONLY));
        Assert.assertTrue(full.excludedGroups().contains(AtfTestGroups.CONTROLLED_FAILURE_FIXTURE));
        Assert.assertTrue(seleniumExtentSmoke.excludedGroups().contains(AtfTestGroups.ALLURE_ONLY));
    }

    @Test
    public void coreProfileTestPackagesStaySmallEnoughToRemainReadable() throws Exception {
        Map<String, Integer> counts = new LinkedHashMap<>();
        try (var files = Files.walk(AtfSourcePaths.TEST_ATF_ROOT)) {
            files.filter(AtfSourcePaths::isJavaSource)
                    .filter(AtfCoreTestNgProfileContractTest::isTestClass)
                    .map(AtfCoreTestNgProfileContractTest::packageName)
                    .sorted()
                    .forEach(packageName -> counts.merge(packageName, 1, Integer::sum));
        }

        List<String> overloaded = counts.entrySet().stream()
                .filter(entry -> entry.getValue() > MAX_TEST_CLASSES_PER_PACKAGE)
                .map(entry -> entry.getKey() + " has " + entry.getValue()
                        + " test classes, max " + MAX_TEST_CLASSES_PER_PACKAGE)
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(overloaded.isEmpty(),
                "Split overloaded ATF test packages into focused subpackages: " + overloaded);
    }

    @Test
    public void coreSanityAndSmokeTestsDoNotAuditPublishedPackageFolders() throws Exception {
        List<String> violations = CORE_SANITY_SURFACES.stream()
                .filter(Files::exists)
                .flatMap(AtfCoreTestNgProfileContractTest::filesUnder)
                .filter(path -> AtfSourcePaths.isJavaSource(path) || AtfSourcePaths.isXmlFile(path))
                .filter(AtfCoreTestNgProfileContractTest::containsPublishedArtifactFolder)
                .map(Path::toString)
                .collect(Collectors.toList());

        Assert.assertTrue(violations.isEmpty(),
                "ATF core sanity/smoke tests must not audit published package folders. "
                        + "Those folders belong to MCP/Atlas package validators: " + violations);
    }

    private static void assertRequiredClasses(AtfCoreYamlProfileSpec spec, Document document) {
        List<String> classes = attributes(document, "class", "name");
        for (String required : spec.requiredClasses()) {
            Assert.assertTrue(classes.contains(required), spec.id() + " missing required class " + required);
        }
    }

    private static void assertForbiddenClasses(AtfCoreYamlProfileSpec spec, Document document) {
        List<String> classes = attributes(document, "class", "name");
        List<String> packages = attributes(document, "package", "name");
        for (String forbidden : spec.forbiddenClasses()) {
            Assert.assertFalse(classes.contains(forbidden), spec.id() + " must not include " + forbidden);
        }
        for (String fragment : spec.forbiddenClassFragments()) {
            Assert.assertTrue(classes.stream().noneMatch(name -> name.contains(fragment)),
                    spec.id() + " must not include class fragment " + fragment + ": " + classes);
            Assert.assertTrue(packages.stream().noneMatch(name -> name.contains(fragment)),
                    spec.id() + " must not include package fragment " + fragment + ": " + packages);
        }
    }

    private static Document profile(String suiteXml) throws Exception {
        Path profile = Path.of(suiteXml);
        Assert.assertTrue(Files.isRegularFile(profile), "Missing ATF core TestNG profile: " + profile);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(profile.toFile());
    }

    private static List<String> attributes(Document document, String tagName, String attribute) {
        NodeList nodes = document.getElementsByTagName(tagName);
        List<String> values = new ArrayList<>();
        for (int index = 0; index < nodes.getLength(); index++) {
            values.add(nodes.item(index).getAttributes().getNamedItem(attribute).getNodeValue());
        }
        return values;
    }

    private static java.util.stream.Stream<Path> filesUnder(Path root) {
        try {
            return Files.walk(root).filter(Files::isRegularFile);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot scan " + root, e);
        }
    }

    private static boolean containsPublishedArtifactFolder(Path path) {
        try {
            String source = Files.readString(path);
            return PUBLISHED_ARTIFACT_FOLDERS.stream().anyMatch(source::contains);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }

    private static boolean isTestClass(Path path) {
        try {
            String source = Files.readString(path);
            return source.contains("@Test") && source.contains(" class ");
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }

    private static String packageName(Path path) {
        try {
            String source = Files.readString(path);
            int start = source.indexOf("package ");
            int end = source.indexOf(';', start);
            return source.substring(start + "package ".length(), end).trim();
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }
}
