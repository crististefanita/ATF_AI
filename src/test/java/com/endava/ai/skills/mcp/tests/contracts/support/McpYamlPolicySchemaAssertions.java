package com.endava.ai.skills.mcp.tests.contracts.support;

import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalogSync;
import com.endava.ai.skills.mcp.contracts.execution.McpAgenticExecutionStepSpec;
import com.endava.ai.skills.mcp.contracts.governance.McpGovernanceMapEntry;
import com.endava.ai.skills.mcp.contracts.model.ContractSpec;
import com.endava.ai.skills.mcp.contracts.model.KpiSpec;
import com.endava.ai.skills.mcp.contracts.policy.McpCloseoutArtifactSpec;
import com.endava.ai.skills.mcp.contracts.policy.McpTestNgProfileSpec;
import org.testng.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.endava.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.*;

public final class McpYamlPolicySchemaAssertions {

    private McpYamlPolicySchemaAssertions() {
    }

    public static void assertStableMinimumSchema() {
        McpYamlPackagePolicySchemaAssertions.assertStableMinimumSchema();
    }

    public static void assertClaimActionContracts() {
        McpYamlClaimActionSchemaAssertions.assertClaimActionContracts();
    }

    public static void assertSourceEvolutionKpisHaveCompleteSchema() {
        List<KpiSpec> specs = ContractCatalog.sourceEvolutionKpis();

        Assert.assertTrue(specs.size() >= 30, "Expected rich source-evolution KPI catalog, got " + specs.size());
        for (KpiSpec spec : specs) {
            Assert.assertFalse(spec.id().isBlank(), "id must be present");
            Assert.assertFalse(spec.profile().isBlank(), "profile must be present for " + spec.id());
            Assert.assertFalse(spec.category().isBlank(), "category must be present for " + spec.id());
            Assert.assertFalse(spec.severity().isBlank(), "severity must be present for " + spec.id());
            Assert.assertFalse(spec.sourceArtifact().isBlank(), "sourceArtifact must be present for " + spec.id());
            Assert.assertFalse(spec.formula().isBlank(), "formula must be present for " + spec.id());
            Assert.assertFalse(spec.repair().observedArtifact().isBlank(),
                    "repair.observedArtifact must be present for " + spec.id());
            Assert.assertFalse(spec.repair().primaryArtifact().isBlank(),
                    "repair.primaryArtifact must be present for " + spec.id());
            Assert.assertFalse(spec.repair().supportingArtifact().isBlank(),
                    "repair.supportingArtifact must be present for " + spec.id());
            Assert.assertFalse(spec.repair().route().isBlank(), "repair.route must be present for " + spec.id());
            Assert.assertFalse(spec.why().isBlank(), "why must be present for " + spec.id());
            Assert.assertFalse(spec.qualityMeaning().isBlank(), "qualityMeaning must be present for " + spec.id());
            Assert.assertFalse(spec.agentAction().isBlank(), "agentAction must be present for " + spec.id());
            Assert.assertFalse(spec.repairHint().isBlank(), "repairHint must be present for " + spec.id());
            Assert.assertFalse(spec.repairCommand().isBlank(), "repairCommand must be present for " + spec.id());
            Assert.assertFalse(spec.capImpact().isBlank(), "capImpact must be present for " + spec.id());
            Assert.assertFalse(spec.publishabilityImpact().isBlank(),
                    "publishabilityImpact must be present for " + spec.id());
        }
    }

    public static void assertSourceEvolutionKpisHaveStableIdentity() {
        List<KpiSpec> specs = ContractCatalog.sourceEvolutionKpis();
        Assert.assertTrue(ContractCatalogSync.duplicateIds(specs).isEmpty(),
                "Duplicate YAML KPI ids: " + ContractCatalogSync.duplicateIds(specs));
        Assert.assertTrue(ContractCatalogSync.invalidSeverities(specs).isEmpty(),
                "Invalid YAML KPI severities: " + ContractCatalogSync.invalidSeverities(specs));
        Assert.assertTrue(ContractCatalogSync.invalidSourceEvolutionProfiles(specs).isEmpty(),
                "Invalid YAML KPI profiles: " + ContractCatalogSync.invalidSourceEvolutionProfiles(specs));

        Set<String> blockerIds = specs.stream()
                .filter(spec -> "BLOCKER".equals(spec.severity()))
                .map(KpiSpec::id)
                .collect(Collectors.toSet());
        Assert.assertTrue(blockerIds.containsAll(REQUIRED_ROOT_KPIS),
                "Missing required close-out blocker KPI ids: " + REQUIRED_ROOT_KPIS + " in " + blockerIds);
    }

    public static void assertMcpProfilesDeclarePrimaryExecutionStages() {
        List<McpTestNgProfileSpec> profiles = ContractCatalog.testNgProfiles();
        Set<String> ids = profiles.stream().map(McpTestNgProfileSpec::id).collect(Collectors.toSet());

        Assert.assertTrue(ids.containsAll(PRIMARY_PROFILE_IDS),
                "Missing primary MCP profile ids from YAML: " + PRIMARY_PROFILE_IDS + " in " + ids);
        Assert.assertEquals(profiles.stream().filter(McpTestNgProfileSpec::finalArtifact).count(), 1,
                "Exactly one profile must be the final artifact basis.");
        McpTestNgProfileSpec closeout = ContractCatalog.requireTestNgProfile(PROFILE_PACKAGE_CLOSEOUT);
        Assert.assertTrue(closeout.finalArtifact(), "package-closeout must be the final artifact profile.");
        Assert.assertEquals(closeout.allowedSkipped(), 0, "package-closeout must stay skipped-free by contract.");
        Assert.assertEquals(closeout.mirrorOf(), PROFILE_PACKAGE_AUDIT,
                "package-closeout must mirror package-audit checks and differ only by final evidence role.");
        McpTestNgProfileSpec full = ContractCatalog.requireTestNgProfile(PROFILE_FULL);
        Assert.assertTrue(full.excludedGroups().contains(PROFILE_FINAL_REPORT_CLOSEOUT),
                "full diagnostic sweep must exclude final-only browser/render report validation.");
    }

    public static void assertAgenticExecutionFlowSeparatesCalibrationCloseoutAndPromotionStages() {
        List<McpAgenticExecutionStepSpec> steps = ContractCatalog.agenticExecutionFlow();

        Assert.assertEquals(steps.stream()
                        .map(McpAgenticExecutionStepSpec::profileId)
                        .collect(Collectors.toUnmodifiableList()),
                EXPECTED_AGENTIC_FLOW,
                "Agentic MCP flow must keep the explicit low-churn order.");
        Assert.assertEquals(steps.stream().map(McpAgenticExecutionStepSpec::order).distinct().count(),
                steps.size(), "Agentic MCP flow orders must be unique.");
        for (McpAgenticExecutionStepSpec step : steps) {
            McpTestNgProfileSpec profile = ContractCatalog.requireTestNgProfile(step.profileId());
            Assert.assertEquals(step.suiteXml(), profile.suiteXml(),
                    "Agentic execution flow suite must reuse the profile catalog: " + step.id());
            Assert.assertEquals(step.allowedSkipped(), profile.allowedSkipped(),
                    "Agentic execution flow skipped policy must mirror profile YAML: " + step.id());
            Assert.assertTrue(Files.isRegularFile(Path.of(step.suiteXml())),
                    "Agentic execution flow suite must exist: " + step.suiteXml());
            Assert.assertFalse(step.role().isBlank(), "Missing role for " + step.id());
            Assert.assertFalse(step.stopCondition().isBlank(), "Missing stop condition for " + step.id());
            Assert.assertFalse(step.notFor().isBlank(), "Missing notFor boundary for " + step.id());
            Assert.assertTrue(step.mustPass(), "Agentic execution flow stages must be green before advancing: "
                    + step.id());
        }

        Assert.assertEquals(flowIndex(PROFILE_PACKAGE_CLOSEOUT), 3,
                "Package close-out must be the frozen package artifact basis.");
        Assert.assertTrue(flowIndex(PROFILE_FINAL_REPORT_CLOSEOUT) > flowIndex(PROFILE_PACKAGE_CLOSEOUT),
                "Final report close-out must run only after package artifacts are frozen.");
        Assert.assertTrue(flowIndex(PROFILE_LANGGRAPH_FINAL_REPORT) > flowIndex(PROFILE_FINAL_REPORT_CLOSEOUT),
                "LangGraph final report validation must run only after final report surface close-out.");
        Assert.assertFalse(ContractCatalog.requireTestNgProfile(PROFILE_FINAL_REPORT_CLOSEOUT).finalArtifact(),
                "Final report close-out validates the frozen report surface; package-closeout remains final artifact.");
        Assert.assertFalse(ContractCatalog.requireTestNgProfile(PROFILE_LANGGRAPH_FINAL_REPORT).finalArtifact(),
                "LangGraph final report validation reads the final report; package-closeout remains final artifact.");
    }

    public static void assertAtlasQualityBridgeKeepsMcpCalibratedWithoutBecomingAtlasGate() {
        List<McpGovernanceMapEntry.AtlasQualityBridgeSpec> bridge = ContractCatalog.atlasQualityBridgeContracts();
        Set<String> bridgeLanes = bridge.stream()
                .map(McpGovernanceMapEntry.AtlasQualityBridgeSpec::atlasLane)
                .collect(Collectors.toUnmodifiableSet());

        Assert.assertEquals(bridgeLanes, REQUIRED_ATLAS_QUALITY_LANES,
                "MCP final-report closeout must be calibrated against every Atlas quality lane.");
        Assert.assertEquals(bridgeLanes, atlasQualityLaneIds(),
                "MCP bridge lane ids must stay synchronized with skills/atlas/contracts/agentic-quality-lanes.yml.");
        for (McpGovernanceMapEntry.AtlasQualityBridgeSpec spec : bridge) {
            Assert.assertEquals(spec.mcpProfileId(), PROFILE_LANGGRAPH_FINAL_REPORT,
                    "Atlas quality bridge belongs to LangGraph final-report validation, not package-closeout: "
                            + spec.id());
            Assert.assertFalse(spec.blocking(),
                    "Bridge entries are calibration guidance; executable MCP/Atlas rules own blockers: " + spec.id());
            Assert.assertFalse(spec.finalReportSurface().isBlank(), "Missing final report surface for " + spec.id());
            Assert.assertTrue(spec.mcpResponsibility().contains("require")
                            || spec.mcpResponsibility().contains("reject"),
                    "MCP side must name a concrete mechanical/report responsibility: " + spec.id());
            Assert.assertTrue(spec.atlasResponsibility().contains("judge"),
                    "Atlas side must remain business-quality judgment, not MCP artifact gating: " + spec.id());
            Assert.assertFalse(spec.agentAction().isBlank(), "Missing agent action for " + spec.id());
        }
    }

    public static void assertPackageCloseoutArtifactSpine() {
        McpCloseoutArtifactSpec spec = ContractCatalog.primaryCloseoutArtifactSpec();

        Assert.assertEquals(spec.profile(), "package-closeout");
        Assert.assertEquals(spec.severity(), "BLOCKER");
        Assert.assertTrue(containsAlternative(spec.requiredArtifacts(), "validation/mcp/TEST-TestSuite.xml",
                "validation/mcp/testng-results.xml"), "Close-out XML alternatives must be declared.");
        Assert.assertTrue(containsAlternative(spec.requiredGateResultMentions(), "validation/mcp/TEST-TestSuite.xml",
                "validation/mcp/testng-results.xml"), "Gate result XML mention alternatives must be declared.");
        for (String artifact : requiredCloseoutArtifacts()) {
            Assert.assertTrue(contains(spec.requiredArtifacts(), artifact),
                    "Missing required close-out artifact: " + artifact);
            Assert.assertTrue(contains(spec.requiredArtifactIndexLinks(), artifact),
                    "Missing required Artifact Index link: " + artifact);
            Assert.assertTrue(contains(spec.requiredFinalReportLinks(), artifact),
                    "Missing required final report link: " + artifact);
        }
        for (String artifact : List.of(
                McpArtifactPaths.MCP_EXTENT_REPORT,
                McpArtifactPaths.MCP_EXTENT_SHA256,
                McpArtifactPaths.MCP_EXTENT_ORIGIN,
                McpArtifactPaths.MCP_METHOD_LEDGER)) {
            Assert.assertTrue(contains(spec.requiredGateResultMentions(), artifact),
                    "Missing required gate-result mention: " + artifact);
        }
    }

    public static void assertPackagePolicyArtifactConstants() {
        McpYamlPackagePolicySchemaAssertions.assertPackagePolicyArtifactConstants();
    }

    public static void assertReportTemplatePackageSpine() {
        McpYamlReportTemplateSchemaAssertions.assertReportTemplatePackageSpine();
    }

    public static void assertReportTemplateCanonicalSurfaces() {
        McpYamlReportTemplateSchemaAssertions.assertReportTemplateCanonicalSurfaces();
    }

    public static void assertExtentProvenanceBoundaries() {
        McpYamlExtentProvenanceSchemaAssertions.assertExtentProvenanceBoundaries();
    }

    public static void assertExtentProvenanceAntiSurgeryMarkers() {
        McpYamlExtentProvenanceSchemaAssertions.assertExtentProvenanceAntiSurgeryMarkers();
    }

    public static void assertSchemaHelpersRejectBadMetadata() {
        KpiSpec valid = spec("duplicate_id", "package-closeout", "BLOCKER");
        KpiSpec duplicate = spec("duplicate_id", "package-closeout", "BLOCKER");
        KpiSpec badSeverity = spec("bad_severity", "package-closeout", "CRITICAL");
        KpiSpec badProfile = spec("bad_profile", "full-sweep", "INFO");

        Assert.assertEquals(ContractCatalogSync.duplicateIds(List.of(valid, duplicate)), List.of("duplicate_id"));
        Assert.assertEquals(ContractCatalogSync.invalidSeverities(List.of(badSeverity)),
                List.of("bad_severity=CRITICAL"));
        Assert.assertEquals(ContractCatalogSync.invalidSourceEvolutionProfiles(List.of(badProfile)),
                List.of("bad_profile=full-sweep"));
        Assert.assertThrows(IllegalArgumentException.class, () -> KpiSpec.builder()
                .id("missing_artifact")
                .profile("package-closeout")
                .category("source")
                .severity("BLOCKER")
                .formula("some formula")
                .repair(ContractSpec.RepairSpec.of(
                        "review/root-source-thin-no-index.diff",
                        "review/source-evolution-ledger.md",
                        "validation/mcp/mcp-kpi-matrix.md",
                        "source-evolution-closeout"))
                .why("why")
                .qualityMeaning("quality")
                .agentAction("act")
                .repairHint("hint")
                .repairCommand("command")
                .capImpact("cap")
                .publishabilityImpact("impact")
                .build());
    }

    private static KpiSpec spec(String id, String profile, String severity) {
        return KpiSpec.builder()
                .id(id)
                .profile(profile)
                .category("source")
                .severity(severity)
                .sourceArtifact("review/root-source-thin-no-index.diff")
                .formula("count evidence")
                .repair(ContractSpec.RepairSpec.of(
                        "review/root-source-thin-no-index.diff",
                        "review/source-evolution-ledger.md",
                        "validation/mcp/mcp-kpi-matrix.md",
                        "source-evolution-closeout"))
                .why("why")
                .qualityMeaning("quality")
                .agentAction("act")
                .repairHint("hint")
                .repairCommand("command")
                .capImpact("cap")
                .publishabilityImpact("impact")
                .build();
    }

    private static boolean contains(List<List<String>> groups, String artifact) {
        return groups.stream().anyMatch(group -> group.contains(artifact));
    }

    private static List<String> requiredCloseoutArtifacts() {
        return List.of(
                McpArtifactPaths.MCP_EXTENT_REPORT,
                McpArtifactPaths.MCP_EXTENT_SHA256,
                McpArtifactPaths.MCP_EXTENT_ORIGIN,
                McpArtifactPaths.MCP_GATE_RESULT,
                McpArtifactPaths.MCP_METHOD_LEDGER,
                McpArtifactPaths.MCP_KPI_MATRIX,
                McpArtifactPaths.MCP_KPI_MATRIX_HTML,
                McpArtifactPaths.MCP_KPI_FORMULA_TRACE,
                McpArtifactPaths.MCP_KPI_REPAIR_BOARD,
                McpArtifactPaths.MCP_KPI_REPAIR_BOARD_HTML,
                McpArtifactPaths.MCP_SCORE_CAP_RATIONALE);
    }

    private static int flowIndex(String profileId) {
        return EXPECTED_AGENTIC_FLOW.indexOf(profileId);
    }

    private static Set<String> atlasQualityLaneIds() {
        return SimpleYamlContractLoader.load("skills/atlas/contracts/agentic-quality-lanes.yml").stream()
                .map(entry -> entry.get("id"))
                .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean containsAlternative(List<List<String>> groups, String first, String second) {
        return groups.stream().anyMatch(group -> group.contains(first) && group.contains(second));
    }
}
