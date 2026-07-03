package com.ai.skills.mcp.tests.core.profiles;

import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.contracts.execution.McpArtifactFamily;
import com.ai.skills.mcp.contracts.execution.McpCloseoutEvidence;
import com.ai.skills.mcp.contracts.execution.McpCloseoutEvidenceStep;
import com.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import com.ai.skills.mcp.contracts.policy.McpTestNgProfileSpec;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-test-ng-profile-contract-test")
public class McpTestNgProfileContractTest {
    @Test
    @McpCheckRef("mcp.testng.sanity-profile.scope")
    public void sanityProfileContainsOnlyMcpValidatorHealthChecks() throws Exception {
        assertProfileClasses(McpExecutionProfile.SANITY.id());
        assertProfilePolicy(McpExecutionProfile.SANITY.id());
    }

    @Test
    @McpCheckRef("mcp.testng.run-profile.scope")
    public void runProfileContainsOnlyRunEvidenceContracts() throws Exception {
        assertProfileClasses(McpExecutionProfile.RUN.id());
    }

    @Test
    @McpCheckRef("mcp.testng.run-preflight-profile.scope")
    public void runPreflightProfileAddsRuntimePackageAccounting() throws Exception {
        assertProfileClasses(McpExecutionProfile.RUN_PREFLIGHT.id());
    }

    @Test
    @McpCheckRef("mcp.testng.candidate-profile.scope")
    public void candidateProfileContainsOnlyTeacherGradePromotionGate() throws Exception {
        McpTestNgProfileSpec spec = ContractCatalog.requireTestNgProfile(McpExecutionProfile.CANDIDATE.id());

        assertProfileClasses(McpExecutionProfile.CANDIDATE.id());
        assertProfilePolicy(McpExecutionProfile.CANDIDATE.id());
        Assert.assertEquals(methodIncludes(spec.suiteXml()), spec.expectedMethods(),
                "Candidate promotion must stay isolated; package-closeout and final-report-closeout run before it.");
        Assert.assertTrue(spec.expectedMethods().contains("latestPackageIsTeacherGradeHtmlExampleCandidate"),
                "Candidate promotion must still include the teacher-grade candidate score gate.");
        Assert.assertEquals(spec.expectedMethods().size(), 1,
                "Final report, post-sync and browser-render checks belong in mcp-final-report-closeout, not candidate.");
    }

    @Test
    @McpCheckRef("mcp.testng.html-ex-corpus-profile.scope")
    public void htmlExampleCorpusProfileContainsOnlyCorpusClassifier() throws Exception {
        assertProfileClasses(McpExecutionProfile.HTML_EX_CORPUS.id());
    }

    @Test
    @McpCheckRef("mcp.testng.self-profile.no-package-audit")
    public void selfProfileExcludesPackageAuditContracts() throws Exception {
        assertProfilePolicy(McpExecutionProfile.SELF.id());
    }

    @Test
    @McpCheckRef("mcp.testng.self-profile.contract-health")
    public void selfProfileIncludesEveryMcpContractHealthTest() throws Exception {
        List<String> actual = classes(ContractCatalog.requireTestNgProfile(McpExecutionProfile.SELF.id()).suiteXml());
        List<String> contractHealthClasses = javaClassesUnder(AtfSourcePaths.TEST_MCP_TESTS_ROOT.resolve("contracts"));

        Assert.assertTrue(actual.containsAll(contractHealthClasses),
                "MCP self-health must include every tests.contracts class so YAML/catalog/routing drift is caught early: "
                        + contractHealthClasses.stream()
                        .filter(name -> !actual.contains(name))
                        .collect(Collectors.toUnmodifiableList()));
    }

    @Test
    @McpCheckRef("mcp.testng.package-audit-profile.scope")
    public void packageAuditProfileContainsOnlyPackageAuditContracts() throws Exception {
        assertProfilePolicy(McpExecutionProfile.PACKAGE_AUDIT.id());
    }

    @Test
    @McpCheckRef("mcp.testng.post-sync-smoke-profile.scope")
    public void postSyncSmokeProfileContainsOnlyFastArtifactSynchronizationChecks() throws Exception {
        McpTestNgProfileSpec spec = ContractCatalog.requireTestNgProfile(McpExecutionProfile.POST_SYNC_SMOKE.id());

        assertProfileClasses(McpExecutionProfile.POST_SYNC_SMOKE.id());
        assertProfilePolicy(McpExecutionProfile.POST_SYNC_SMOKE.id());
        Assert.assertEquals(methodIncludes(spec.suiteXml()), spec.expectedMethods(),
                "Post-sync smoke must stay method-scoped so artifact-copy verification remains fast and constructive.");
        Assert.assertFalse(spec.finalArtifact(),
                "Post-sync smoke is a follow-up confidence gate; package-closeout remains the final artifact profile.");
        Assert.assertEquals(spec.allowedSkipped(), 0,
                "Post-sync smoke must not hide skipped artifact synchronization checks.");
    }

    @Test
    @McpCheckRef("mcp.testng.final-report-closeout-profile.scope")
    public void finalReportCloseoutProfileRunsOnlyAfterPackageArtifactsAreFrozen() throws Exception {
        McpTestNgProfileSpec spec = ContractCatalog.requireTestNgProfile(McpExecutionProfile.FINAL_REPORT_CLOSEOUT.id());

        assertProfileClasses(McpExecutionProfile.FINAL_REPORT_CLOSEOUT.id());
        assertProfilePolicy(McpExecutionProfile.FINAL_REPORT_CLOSEOUT.id());
        Assert.assertEquals(methodIncludes(spec.suiteXml()), spec.expectedMethods(),
                "Final report close-out must keep post-sync checks method-scoped while adding report-surface checks.");
        Assert.assertFalse(spec.finalArtifact(),
                "Final report close-out verifies the published report after package-closeout; it must not replace package-closeout evidence.");
        Assert.assertEquals(spec.allowedSkipped(), 0,
                "Final report close-out must not hide skipped report-surface/browser checks.");
    }

    @Test
    @McpCheckRef("mcp.testng.langgraph-final-report-profile.scope")
    public void langgraphFinalReportProfileRunsAfterFinalReportSurfaceCloseout() throws Exception {
        McpTestNgProfileSpec spec = ContractCatalog.requireTestNgProfile(McpExecutionProfile.LANGGRAPH_FINAL_REPORT.id());

        assertProfileClasses(McpExecutionProfile.LANGGRAPH_FINAL_REPORT.id());
        assertProfilePolicy(McpExecutionProfile.LANGGRAPH_FINAL_REPORT.id());
        Assert.assertEquals(methodIncludes(spec.suiteXml()), spec.expectedMethods(),
                "LangGraph final report validation must stay method-scoped to the final report semantic checks.");
        Assert.assertFalse(spec.finalArtifact(),
                "LangGraph final report validation reads the final report; package-closeout remains the final artifact profile.");
        Assert.assertEquals(spec.allowedSkipped(), 0,
                "LangGraph final report validation must not hide skipped semantic report checks.");
        Assert.assertFalse(classes(spec.suiteXml()).contains("com.ai.skills.mcp.tests.smoke.McpPostSyncSmokeContractTest"),
                "LangGraph report validation must not repeat frozen artifact sync smoke checks.");
    }

    @Test
    @McpCheckRef("mcp.testng.package-closeout-profile.scope")
    public void packageCloseoutProfileMirrorsCompletePackageAuditContracts() throws Exception {
        McpTestNgProfileSpec closeoutSpec = ContractCatalog.requireTestNgProfile(McpExecutionProfile.PACKAGE_CLOSEOUT.id());
        McpTestNgProfileSpec auditSpec = ContractCatalog.requireTestNgProfile(closeoutSpec.mirrorOf());
        List<String> closeout = classes(closeoutSpec.suiteXml());
        List<String> audit = classes(auditSpec.suiteXml());

        Assert.assertEquals(closeout, audit,
                "Package-closeout profile must mirror package-audit so final Extent lists all concrete publishability checks.");
        assertProfilePolicy(McpExecutionProfile.PACKAGE_CLOSEOUT.id());
        Assert.assertTrue(closeoutSpec.finalArtifact(), "Package-closeout profile must be marked finalArtifact=true in YAML.");
        Assert.assertEquals(closeoutSpec.allowedSkipped(), 0, "Package-closeout profile must declare allowedSkipped=0.");
    }

    @Test
    @McpCheckRef("mcp.testng.full-profile.package-scope")
    public void fullProfileUsesCompleteMcpTestPackage() throws Exception {
        McpTestNgProfileSpec spec = ContractCatalog.requireTestNgProfile(McpExecutionProfile.FULL.id());
        Assert.assertEquals(packages(spec.suiteXml()), spec.expectedPackages());
        assertMcpOnlyPackages(packages(spec.suiteXml()), spec.suiteXml());
        Assert.assertEquals(groupExcludes(spec.suiteXml()), spec.excludedGroups(),
                "Full MCP can stay package-discovered, but final-only browser/render groups must be excluded.");
        Assert.assertTrue(spec.excludedGroups().contains("mcp-final-report-closeout"),
                "Full MCP is diagnostic/calibration; browser-render final report checks belong only to final-report-closeout.");
        Assert.assertTrue(classes(spec.suiteXml()).isEmpty(),
                "Full MCP profile should use package discovery instead of hand-maintained class lists.");
    }

    @Test
    @McpCheckRef("mcp.testng.profiles.resources-only")
    public void mcpTestNgProfilesLiveOnlyInMcpResources() {
        for (McpTestNgProfileSpec spec : ContractCatalog.testNgProfiles()) {
            if (!spec.resourcesOnly()) {
                continue;
            }
            String suiteName = Path.of(spec.suiteXml()).getFileName().toString();
            Path rootCopy = Path.of(suiteName).toAbsolutePath().normalize();
            Assert.assertFalse(Files.exists(rootCopy),
                    "Root-level MCP TestNG profile must not exist: " + rootCopy);
        }
    }

    @Test
    @McpCheckRef("mcp.testng.profiles.yaml-schema")
    public void mcpTestNgProfileYamlCoversAllCanonicalProfiles() {
        List<String> ids = ContractCatalog.testNgProfiles().stream()
                .map(McpTestNgProfileSpec::id)
                .collect(Collectors.toList());
        Assert.assertEquals(ids, McpExecutionProfile.ids(),
                "profiles.yml order must stay aligned with McpExecutionProfile so code, docs and agents use one vocabulary.");
        for (McpTestNgProfileSpec spec : ContractCatalog.testNgProfiles()) {
            McpExecutionProfile profile = McpExecutionProfile.fromId(spec.id());
            Assert.assertEquals(spec.suiteXml(), profile.suiteXml(),
                    "Canonical profile enum and profiles.yml suiteXml drifted for " + spec.id());
            Assert.assertTrue(Files.isRegularFile(Path.of(spec.suiteXml())),
                    "YAML suiteXml must point to an existing TestNG profile: " + spec.suiteXml());
            Assert.assertFalse(spec.role().isBlank(), "YAML role must explain profile purpose: " + spec.id());
        }
    }

    @Test
    @McpCheckRef("mcp.testng.closeout-evidence.facade")
    public void packageCloseoutEvidencePublishesCanonicalCommandAndArtifactFamilies() {
        McpCloseoutEvidence closeout = ContractCatalog.closeoutEvidence();

        Assert.assertEquals(closeout.profile(), McpExecutionProfile.PACKAGE_CLOSEOUT);
        Assert.assertEquals(closeout.postSyncProfile(), McpExecutionProfile.POST_SYNC_SMOKE);
        Assert.assertEquals(closeout.finalReportProfile(), McpExecutionProfile.FINAL_REPORT_CLOSEOUT);
        Assert.assertEquals(closeout.langgraphFinalReportProfile(), McpExecutionProfile.LANGGRAPH_FINAL_REPORT);

        String command = closeout.packageCloseoutCommandTemplate();
        Assert.assertTrue(command.contains(McpExecutionProfile.PACKAGE_CLOSEOUT.suiteXml()),
                "Close-out command must name the canonical package-closeout suite: " + command);
        Assert.assertTrue(command.contains("-Dmcp.package.root=<package-root>"),
                "Close-out command must force an explicit package root: " + command);
        Assert.assertTrue(command.contains("-Dmcp.project.root=<project-root>"),
                "Close-out command must force an explicit project root: " + command);
        Assert.assertTrue(command.contains("-D" + ReportingConfigKeys.REPORTING_ENGINE
                        + "=" + ReportingConfigKeys.ENGINE_EXTENT),
                "Close-out command must generate the final MCP Extent artifact via the reporting runtime: " + command);
        Assert.assertTrue(command.contains("-D" + ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED
                        + "=" + ReportingConfigKeys.TIMESTAMP_DISABLED),
                "Close-out command must freeze " + McpArtifactPaths.MCP_EXTENT_REPORT
                        + " under a stable name: " + command);

        for (List<String> alternatives : closeout.artifactSpec().requiredArtifacts()) {
            Assert.assertTrue(alternatives.stream().anyMatch(McpArtifactFamily::coversAny),
                    "Close-out required artifact must be owned by a typed artifact family: " + alternatives);
        }
        Assert.assertTrue(closeout.artifactFamilies().contains(McpArtifactFamily.EXTENT));
        Assert.assertTrue(closeout.artifactFamilies().contains(McpArtifactFamily.TESTNG_XML));
        Assert.assertTrue(closeout.artifactFamilies().contains(McpArtifactFamily.KPI));
        Assert.assertTrue(closeout.artifactFamilies().contains(McpArtifactFamily.SOURCE_EVOLUTION));

        List<McpCloseoutEvidenceStep> plan = closeout.closeoutPlan();
        Assert.assertEquals(plan.stream().map(McpCloseoutEvidenceStep::profile).collect(Collectors.toList()),
                List.of(McpExecutionProfile.PACKAGE_CLOSEOUT,
                        McpExecutionProfile.POST_SYNC_SMOKE,
                        McpExecutionProfile.FINAL_REPORT_CLOSEOUT,
                        McpExecutionProfile.LANGGRAPH_FINAL_REPORT),
                "Close-out plan must make the final MCP sequence explicit for agents.");
        Assert.assertEquals(plan.get(0).repairTarget(), McpArtifactPaths.MCP_VALIDATION_ROOT,
                "Primary close-out repair target must be the frozen " + McpArtifactPaths.MCP_VALIDATION_ROOT
                        + " spine.");
        Assert.assertTrue(plan.get(2).artifactFamilies().contains(McpArtifactFamily.FINAL_REPORT),
                "Final report close-out step must own final-report link and summary evidence.");
        Assert.assertTrue(plan.get(3).artifactFamilies().contains(McpArtifactFamily.FINAL_REPORT),
                "LangGraph final report step must own the semantic final report validation evidence.");
        String planMarkdown = closeout.closeoutPlanMarkdown();
        Assert.assertTrue(planMarkdown.contains(McpExecutionProfile.PACKAGE_CLOSEOUT.id())
                        && planMarkdown.contains(McpExecutionProfile.FINAL_REPORT_CLOSEOUT.id())
                        && planMarkdown.contains(McpExecutionProfile.LANGGRAPH_FINAL_REPORT.id()),
                "Close-out plan markdown must be usable in agent-facing diagnostics.");
        Assert.assertTrue(planMarkdown.contains("Artifact families") && planMarkdown.contains("Repair target"),
                "Close-out plan markdown must expose artifact families and repair targets.");
    }

    private static void assertProfileClasses(String profileId) throws Exception {
        McpTestNgProfileSpec spec = ContractCatalog.requireTestNgProfile(profileId);
        List<String> actual = classes(spec.suiteXml());
        Assert.assertEquals(actual, spec.expectedClasses(), spec.suiteXml() + " class order/scope drifted");
        Set<String> unique = new LinkedHashSet<>(actual);
        Assert.assertEquals(unique.size(), actual.size(), spec.suiteXml() + " contains duplicate class entries");
        assertMcpOnlyClasses(actual, spec.suiteXml());
    }

    private static void assertProfilePolicy(String profileId) throws Exception {
        McpTestNgProfileSpec spec = ContractCatalog.requireTestNgProfile(profileId);
        List<String> actual = classes(spec.suiteXml());
        for (String required : spec.requiredClasses()) {
            Assert.assertTrue(actual.contains(required), spec.id() + " missing required class " + required);
        }
        for (String requiredFragment : spec.requiredClassFragments()) {
            Assert.assertTrue(actual.stream().anyMatch(name -> name.contains(requiredFragment)),
                    spec.id() + " missing required class fragment " + requiredFragment + ": " + actual);
        }
        for (String forbidden : spec.forbiddenClasses()) {
            Assert.assertFalse(actual.contains(forbidden), spec.id() + " must not include " + forbidden);
        }
        for (String forbiddenFragment : spec.forbiddenClassFragments()) {
            Assert.assertTrue(actual.stream().noneMatch(name -> name.contains(forbiddenFragment)),
                    spec.id() + " must not include class fragment " + forbiddenFragment + ": " + actual);
        }
        assertMcpOnlyClasses(actual, spec.suiteXml());
    }

    private static void assertMcpOnlyClasses(List<String> classes, String suiteXml) {
        List<String> foreign = classes.stream()
                .filter(name -> !name.startsWith(AtfSourcePaths.MCP_ROOT_PACKAGE + "."))
                .collect(Collectors.toList());
        Assert.assertTrue(foreign.isEmpty(),
                "MCP TestNG profile must not route ATF core, Atlas or UI product tests: "
                        + suiteXml + " -> " + foreign);
    }

    private static void assertMcpOnlyPackages(List<String> packages, String suiteXml) {
        List<String> foreign = packages.stream()
                .filter(name -> !name.startsWith(AtfSourcePaths.MCP_ROOT_PACKAGE + "."))
                .collect(Collectors.toList());
        Assert.assertTrue(foreign.isEmpty(),
                "MCP TestNG profile must not discover ATF core, Atlas or UI product packages: "
                        + suiteXml + " -> " + foreign);
    }

    private static List<String> classes(String suiteXml) throws Exception {
        return attributes(suiteXml, "class", "name");
    }

    private static List<String> packages(String suiteXml) throws Exception {
        return attributes(suiteXml, "package", "name");
    }

    private static List<String> methodIncludes(String suiteXml) throws Exception {
        return attributes(suiteXml, "include", "name");
    }

    private static List<String> groupExcludes(String suiteXml) throws Exception {
        return attributes(suiteXml, "exclude", "name");
    }

    private static List<String> attributes(String suiteXml, String element, String attribute) throws Exception {
        Document document = readSuite(suiteXml);
        NodeList nodes = document.getElementsByTagName(element);
        List<String> values = new ArrayList<>();
        for (int index = 0; index < nodes.getLength(); index++) {
            values.add(nodes.item(index).getAttributes().getNamedItem(attribute).getNodeValue());
        }
        return values;
    }

    private static Document readSuite(String suiteXml)
            throws ParserConfigurationException, IOException, SAXException {
        Path path = suiteXml.startsWith("src/")
                ? Path.of(suiteXml).toAbsolutePath().normalize()
                : AtfSourcePaths.testResource(suiteXml).toAbsolutePath().normalize();
        Assert.assertTrue(Files.isRegularFile(path), "Missing TestNG profile: " + path);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return factory.newDocumentBuilder().parse(path.toFile());
    }

    private static List<String> javaClassesUnder(Path root) throws IOException {
        try (var files = Files.walk(root)) {
            return files
                    .filter(AtfSourcePaths::isJavaSource)
                    .filter(McpTestNgProfileContractTest::isExecutableContractClass)
                    .map(path -> AtfSourcePaths.className(AtfSourcePaths.TEST_JAVA_ROOT, path))
                    .sorted()
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    private static boolean isExecutableContractClass(Path path) {
        String normalized = path.toString().replace('\\', '/');
        return !normalized.contains("/support/");
    }
}
