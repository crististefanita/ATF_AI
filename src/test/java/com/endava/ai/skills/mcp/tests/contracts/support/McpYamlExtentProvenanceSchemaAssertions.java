package com.endava.ai.skills.mcp.tests.contracts.support;

import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.provenance.McpExtentProvenanceSpec;
import org.testng.Assert;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class McpYamlExtentProvenanceSchemaAssertions {
    private static final Set<String> EXTENT_PROVENANCE_KINDS = Set.of(
            "native-spark-shell-token",
            "native-spark-shell-threshold",
            "framework-runtime-audit-token",
            "manual-proof-injection-token",
            "origin-edit-token",
            "origin-required-field",
            "origin-forbidden-writer-token");

    private McpYamlExtentProvenanceSchemaAssertions() {
    }

    static void assertExtentProvenanceBoundaries() {
        Assert.assertFalse(ContractCatalog.extentProvenanceContracts().isEmpty(),
                "extent-provenance.yml must not be empty.");
        Set<String> kinds = ContractCatalog.extentProvenanceContracts().stream()
                .map(McpExtentProvenanceSpec::kind)
                .collect(Collectors.toSet());
        Assert.assertTrue(EXTENT_PROVENANCE_KINDS.containsAll(kinds),
                "Invalid extent-provenance kinds: " + kinds);
        Assert.assertTrue(kinds.containsAll(EXTENT_PROVENANCE_KINDS),
                "extent-provenance.yml must declare native shell, manual proof, runtime audit and origin contracts.");

        for (McpExtentProvenanceSpec spec : ContractCatalog.extentProvenanceContracts()) {
            Assert.assertFalse(spec.id().isBlank(), "extent-provenance id missing");
            Assert.assertFalse(spec.kind().isBlank(), "extent-provenance kind missing for " + spec.id());
            Assert.assertFalse(spec.message().isBlank(), "extent-provenance message missing for " + spec.id());
            if ("native-spark-shell-threshold".equals(spec.kind())) {
                Assert.assertNotNull(spec.threshold(), "native Spark threshold missing for " + spec.id());
                Assert.assertTrue(spec.threshold() > 0, "native Spark threshold must be positive for " + spec.id());
            } else {
                Assert.assertTrue(spec.hasTokenField(), "token missing for " + spec.id());
            }
        }
    }

    static void assertExtentProvenanceAntiSurgeryMarkers() {
        Assert.assertTrue(ContractCatalog.extentProvenanceTokens("native-spark-shell-token")
                        .contains("body class=\"spa -report"),
                "Native Spark shell body token must remain declarative.");
        Assert.assertTrue(ContractCatalog.extentProvenanceTokens("manual-proof-injection-token")
                        .contains("business flow graph"),
                "Final-report vocabulary must remain blocked from Maven Extent evidence.");
        Assert.assertTrue(ContractCatalog.extentProvenanceTokens("framework-runtime-audit-token")
                        .contains("atf runtime tag audit"),
                "Framework runtime audit vocabulary must remain classified as allowed runtime evidence.");
        Assert.assertTrue(ContractCatalog.extentProvenanceTokens("origin-edit-token")
                        .contains("edited_by_agent"),
                "Origin edit markers must remain declarative.");
        Assert.assertTrue(ContractCatalog.extentProvenanceTokens("origin-required-field")
                        .containsAll(List.of("command", "timestamp", "exit", "sha256", "size")),
                "Origin required fields must remain declarative.");
        Assert.assertTrue(ContractCatalog.extentProvenanceTokens("origin-forbidden-writer-token")
                        .contains("exec:java"),
                "Post-run writer markers must remain declarative.");
        Assert.assertEquals(ContractCatalog.extentProvenanceThreshold("native-spark-shell-threshold", -1), 4,
                "Native Spark shell acceptance threshold must be declared in YAML.");
    }
}
