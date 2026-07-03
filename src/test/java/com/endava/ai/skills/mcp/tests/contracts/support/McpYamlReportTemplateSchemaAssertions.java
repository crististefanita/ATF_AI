package com.endava.ai.skills.mcp.tests.contracts.support;

import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.policy.McpReportTemplateSpec;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import org.testng.Assert;

import java.util.Set;
import java.util.stream.Collectors;

final class McpYamlReportTemplateSchemaAssertions {
    private static final Set<String> REPORT_TEMPLATE_KINDS = Set.of(
            "package-artifact",
            "run-artifact",
            "proof-family",
            "proof-mapping-token",
            "opening-marker",
            "score-card-label",
            "contract-artifact-label",
            "executive-context-evidence",
            "runtime-token",
            "forbidden-calibration-token",
            "run-audit-evidence-token",
            "artifact-index-token",
            "reusable-visual-class",
            "score-overview-any");

    private McpYamlReportTemplateSchemaAssertions() {
    }

    static void assertReportTemplatePackageSpine() {
        Set<String> yamlPackageArtifacts = ContractCatalog.reportTemplateContracts().stream()
                .filter(spec -> "package-artifact".equals(spec.kind()))
                .map(McpReportTemplateSpec::token)
                .collect(Collectors.toSet());
        Set<String> expected = Set.of(
                McpArtifactPaths.README,
                McpArtifactPaths.CALIBRATION,
                McpArtifactPaths.PACKAGE_MANIFEST,
                McpArtifactPaths.INPUT_SNAPSHOT,
                McpArtifactPaths.FINAL_REPORT,
                McpArtifactPaths.REPORT_CSS,
                McpArtifactPaths.RENDER_PROOF,
                McpArtifactPaths.PACKAGE_STATE,
                McpArtifactPaths.EXECUTION_GATE_CARD,
                McpArtifactPaths.QUALITY_ACCOUNTING_VERDICT,
                McpArtifactPaths.FRAMEWORK_PROPERTIES_SNAPSHOT,
                McpArtifactPaths.PACKAGE_CLOSE_OUT,
                McpArtifactPaths.SOURCE_EVOLUTION_LEDGER,
                McpArtifactPaths.ROOT_DIFF);

        Assert.assertEquals(yamlPackageArtifacts, expected,
                "report-template.yml package-artifact tokens must match McpArtifactPaths.");
    }

    static void assertReportTemplateCanonicalSurfaces() {
        Assert.assertFalse(ContractCatalog.reportTemplateContracts().isEmpty(),
                "report-template.yml must not be empty.");
        Set<String> reportTemplateKinds = ContractCatalog.reportTemplateContracts().stream()
                .map(McpReportTemplateSpec::kind)
                .collect(Collectors.toSet());
        Assert.assertTrue(REPORT_TEMPLATE_KINDS.containsAll(reportTemplateKinds),
                "Invalid report-template kinds: " + reportTemplateKinds);
        Assert.assertTrue(reportTemplateKinds.containsAll(REPORT_TEMPLATE_KINDS),
                "report-template.yml must declare the canonical report profile surfaces.");
        for (McpReportTemplateSpec spec : ContractCatalog.reportTemplateContracts()) {
            Assert.assertFalse(spec.id().isBlank(), "report-template id missing");
            Assert.assertFalse(spec.kind().isBlank(), "report-template kind missing for " + spec.id());
            Assert.assertFalse(spec.message().isBlank(), "report-template message missing for " + spec.id());
            if ("score-overview-any".equals(spec.kind())) {
                Assert.assertFalse(spec.tokens().isEmpty(), "score-overview-any tokens missing for " + spec.id());
            } else {
                Assert.assertTrue(spec.hasTokenField(), "token missing for " + spec.id());
            }
        }
    }
}
