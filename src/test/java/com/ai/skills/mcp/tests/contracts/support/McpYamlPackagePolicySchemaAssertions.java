package com.ai.skills.mcp.tests.contracts.support;

import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.contracts.advisor.McpAiTaskAdvisorSpec;
import com.ai.skills.mcp.contracts.candidate.McpHtmlExCorpusSpec;
import com.ai.skills.mcp.contracts.policy.McpPackagePolicySpec;
import com.ai.skills.mcp.contracts.policy.McpProtectedRootSpec;
import com.ai.skills.mcp.contracts.policy.McpReportHyperlinkSpec;
import com.ai.skills.mcp.contracts.policy.McpScoreCapSpec;
import com.ai.skills.mcp.contracts.policy.McpScoreTruthSyncSpec;
import com.ai.skills.mcp.contracts.policy.McpSizeBudgetSpec;
import com.ai.skills.mcp.contracts.policy.McpSourceEvolutionLedgerSpec;
import com.ai.skills.mcp.context.McpArtifactPaths;
import org.testng.Assert;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class McpYamlPackagePolicySchemaAssertions {
    private static final Set<String> SOURCE_EVOLUTION_LEDGER_KINDS = Set.of(
            "required-section",
            "required-kpi-label");
    private static final Set<String> PACKAGE_POLICY_KINDS = Set.of(
            "runtime-required-key",
            "runtime-decimal-key",
            "framework-audit-key",
            "published-noise-token",
            "kpi-closeout-artifact");
    private static final Set<String> ADVISOR_SEVERITIES = Set.of("INFO", "WARN");
    private static final Set<String> REQUIRED_ADVISOR_IDS = Set.of(
            "advisor.prompt.scope.too-broad",
            "advisor.model-tier.too-high",
            "advisor.acceptance-criteria.missing",
            "advisor.stop-condition.missing",
            "advisor.value-rationale.missing");

    private McpYamlPackagePolicySchemaAssertions() {
    }

    static void assertStableMinimumSchema() {
        assertReportHyperlinkContracts();
        assertScoreCapContracts();
        assertScoreTruthSyncContracts();
        assertAiTaskAdvisorContracts();
        assertProtectedRootContracts();
        assertSourceEvolutionLedgerContracts();
        assertPackagePolicyContracts();
        assertHtmlExCorpusContracts();
        assertSizeBudgetContracts();
    }

    static void assertPackagePolicyArtifactConstants() {
        Assert.assertTrue(ContractCatalog.packagePolicyTokens("runtime-required-key")
                        .containsAll(List.of("RUN_EXPECTED_DELTA_QUALITY", "ROUND_TARGET_TEST_COUNT",
                                "META_ITERATION_COUNT")),
                "Runtime required keys must remain declarative.");
        Assert.assertTrue(ContractCatalog.packagePolicyTokens("framework-audit-key")
                        .containsAll(List.of("ui.screenshots.enabled", "reporting.engine", "reports.dir")),
                "Framework snapshot audit keys must remain declarative.");
        Assert.assertTrue(ContractCatalog.packagePolicyTokens("published-noise-token")
                        .containsAll(List.of("TODO", "FIXME", "undefined")),
                "Published artifact noise tokens must remain declarative.");

        Set<String> expectedKpiArtifacts = Set.of(
                McpArtifactPaths.MCP_KPI_MATRIX,
                McpArtifactPaths.MCP_KPI_MATRIX_HTML,
                McpArtifactPaths.MCP_KPI_FORMULA_TRACE,
                McpArtifactPaths.MCP_KPI_REPAIR_BOARD,
                McpArtifactPaths.MCP_KPI_REPAIR_BOARD_HTML,
                McpArtifactPaths.MCP_SCORE_CAP_RATIONALE);
        Assert.assertEquals(
                Set.copyOf(ContractCatalog.packagePolicyTokens("kpi-closeout-artifact")),
                expectedKpiArtifacts,
                "package-policy.yml KPI artifacts must match McpArtifactPaths constants.");
    }

    private static void assertReportHyperlinkContracts() {
        Assert.assertFalse(ContractCatalog.reportHyperlinkContracts().isEmpty(),
                "report-hyperlinks.yml must declare final report link contracts.");
        for (McpReportHyperlinkSpec spec : ContractCatalog.reportHyperlinkContracts()) {
            Assert.assertFalse(spec.id().isBlank(), "report hyperlink id must be present");
            Assert.assertFalse(spec.profile().isBlank(), "report hyperlink profile must be present for " + spec.id());
            Assert.assertFalse(spec.severity().isBlank(), "report hyperlink severity must be present for " + spec.id());
            Assert.assertFalse(spec.report().isBlank(), "report target must be present for " + spec.id());
            Assert.assertFalse(spec.section().isBlank(), "report section must be present for " + spec.id());
            Assert.assertFalse(spec.requiredLinks().isEmpty(), "required links missing for " + spec.id());
            Assert.assertFalse(spec.validator().isBlank(), "report hyperlink validator missing for " + spec.id());
            Assert.assertFalse(spec.repairTarget().isBlank(), "report hyperlink repair target missing for " + spec.id());
            Assert.assertFalse(spec.capImpact().isBlank(), "report hyperlink cap impact missing for " + spec.id());
            Assert.assertFalse(spec.publishabilityImpact().isBlank(),
                    "report hyperlink publishability impact missing for " + spec.id());
        }
    }

    private static void assertScoreCapContracts() {
        Assert.assertFalse(ContractCatalog.scoreCapContracts().isEmpty(),
                "score-caps.yml must declare score cap contracts.");
        for (McpScoreCapSpec spec : ContractCatalog.scoreCapContracts()) {
            Assert.assertFalse(spec.id().isBlank(), "score cap id must be present");
            Assert.assertFalse(spec.profile().isBlank(), "score cap profile must be present for " + spec.id());
            Assert.assertFalse(spec.severity().isBlank(), "score cap severity must be present for " + spec.id());
            Assert.assertFalse(spec.trigger().isBlank(), "score cap trigger must be present for " + spec.id());
            Assert.assertTrue(spec.maxScore() > 0 && spec.maxScore() <= 100,
                    "score cap maxScore must be 1..100 for " + spec.id());
            Assert.assertFalse(spec.validator().isBlank(), "score cap validator missing for " + spec.id());
            Assert.assertFalse(spec.repairTarget().isBlank(), "score cap repair target missing for " + spec.id());
            Assert.assertFalse(spec.capImpact().isBlank(), "score cap cap impact missing for " + spec.id());
            Assert.assertFalse(spec.publishabilityImpact().isBlank(),
                    "score cap publishability impact missing for " + spec.id());
        }
    }

    private static void assertScoreTruthSyncContracts() {
        Assert.assertFalse(ContractCatalog.scoreTruthSyncContracts().isEmpty(),
                "score-truth-sync.yml must declare score truth contracts.");
        for (McpScoreTruthSyncSpec spec : ContractCatalog.scoreTruthSyncContracts()) {
            Assert.assertFalse(spec.id().isBlank(), "score truth id must be present");
            Assert.assertFalse(spec.profile().isBlank(), "score truth profile must be present for " + spec.id());
            Assert.assertFalse(spec.severity().isBlank(), "score truth severity must be present for " + spec.id());
            Assert.assertFalse(spec.truthSource().isBlank(), "score truth source must be present for " + spec.id());
            Assert.assertFalse(spec.synchronizedArtifacts().isEmpty(),
                    "score truth synchronized artifacts missing for " + spec.id());
            Assert.assertFalse(spec.requiredEvidence().isEmpty(), "score truth evidence missing for " + spec.id());
            Assert.assertFalse(spec.validator().isBlank(), "score truth validator missing for " + spec.id());
            Assert.assertFalse(spec.repairTarget().isBlank(), "score truth repair target missing for " + spec.id());
            Assert.assertFalse(spec.capImpact().isBlank(), "score truth cap impact missing for " + spec.id());
            Assert.assertFalse(spec.publishabilityImpact().isBlank(),
                    "score truth publishability impact missing for " + spec.id());
        }
    }

    private static void assertAiTaskAdvisorContracts() {
        Assert.assertFalse(ContractCatalog.aiTaskAdvisorContracts().isEmpty(),
                "ai-task-advisor.yml must declare non-blocking agent guidance.");
        Set<String> ids = ContractCatalog.aiTaskAdvisorContracts().stream()
                .map(McpAiTaskAdvisorSpec::id)
                .collect(Collectors.toSet());
        Assert.assertTrue(ids.containsAll(REQUIRED_ADVISOR_IDS),
                "ai-task-advisor.yml must declare the primary advisor loop entries.");
        for (McpAiTaskAdvisorSpec spec : ContractCatalog.aiTaskAdvisorContracts()) {
            Assert.assertFalse(spec.id().isBlank(), "advisor id must be present");
            Assert.assertTrue(ADVISOR_SEVERITIES.contains(spec.severity()),
                    "advisor severity must stay INFO/WARN for " + spec.id());
            Assert.assertFalse(spec.signal().isBlank(), "advisor signal missing for " + spec.id());
            Assert.assertFalse(spec.recommendation().isBlank(), "advisor recommendation missing for " + spec.id());
            Assert.assertFalse(spec.modelTier().isBlank(), "advisor modelTier missing for " + spec.id());
            Assert.assertFalse(spec.acceptanceCriteria().isBlank(),
                    "advisor acceptanceCriteria missing for " + spec.id());
            Assert.assertFalse(spec.stopCondition().isBlank(), "advisor stopCondition missing for " + spec.id());
            Assert.assertFalse(spec.valueRationale().isBlank(), "advisor valueRationale missing for " + spec.id());
            Assert.assertFalse(spec.reportAnchor().isBlank(), "advisor reportAnchor missing for " + spec.id());
            Assert.assertFalse(spec.blocking(), "advisor entries must never become blockers: " + spec.id());
        }
    }

    private static void assertProtectedRootContracts() {
        Assert.assertFalse(ContractCatalog.protectedRootContracts().isEmpty(),
                "protected-roots.yml must declare protected roots.");
        for (McpProtectedRootSpec spec : ContractCatalog.protectedRootContracts()) {
            Assert.assertFalse(spec.id().isBlank(), "protected root id must be present");
            Assert.assertFalse(spec.path().isBlank(), "protected root path missing for " + spec.id());
            Assert.assertFalse(spec.severity().isBlank(), "protected root severity missing for " + spec.id());
            Assert.assertFalse(spec.owner().isBlank(), "protected root owner missing for " + spec.id());
            Assert.assertFalse(spec.reason().isBlank(), "protected root reason missing for " + spec.id());
            Assert.assertFalse(spec.allowedAgentAction().isBlank(),
                    "protected root allowedAgentAction missing for " + spec.id());
        }
    }

    private static void assertSourceEvolutionLedgerContracts() {
        Assert.assertFalse(ContractCatalog.sourceEvolutionLedgerContracts().isEmpty(),
                "source-evolution-ledger.yml must not be empty.");
        Set<String> kinds = ContractCatalog.sourceEvolutionLedgerContracts().stream()
                .map(McpSourceEvolutionLedgerSpec::kind)
                .collect(Collectors.toSet());
        Assert.assertTrue(SOURCE_EVOLUTION_LEDGER_KINDS.containsAll(kinds),
                "Invalid source-evolution ledger kinds: " + kinds);
        Assert.assertTrue(kinds.containsAll(SOURCE_EVOLUTION_LEDGER_KINDS),
                "source-evolution-ledger.yml must declare required sections and KPI labels.");
    }

    private static void assertPackagePolicyContracts() {
        Assert.assertFalse(ContractCatalog.packagePolicyContracts().isEmpty(),
                "package-policy.yml must declare runtime, artifact and published-noise policies.");
        Set<String> kinds = ContractCatalog.packagePolicyContracts().stream()
                .map(McpPackagePolicySpec::kind)
                .collect(Collectors.toSet());
        Assert.assertTrue(PACKAGE_POLICY_KINDS.containsAll(kinds),
                "Invalid package-policy kinds: " + kinds);
        Assert.assertTrue(kinds.containsAll(PACKAGE_POLICY_KINDS),
                "package-policy.yml must declare all package policy token families.");
        for (McpPackagePolicySpec spec : ContractCatalog.packagePolicyContracts()) {
            Assert.assertFalse(spec.id().isBlank(), "package policy id must be present");
            Assert.assertFalse(spec.kind().isBlank(), "package policy kind missing for " + spec.id());
            Assert.assertFalse(spec.token().isBlank(), "package policy token missing for " + spec.id());
            Assert.assertFalse(spec.message().isBlank(), "package policy message missing for " + spec.id());
        }
    }

    private static void assertHtmlExCorpusContracts() {
        Assert.assertFalse(ContractCatalog.htmlExCorpusContracts().isEmpty(),
                "html-ex-corpus.yml must declare candidate corpus contracts.");
        for (McpHtmlExCorpusSpec spec : ContractCatalog.htmlExCorpusContracts()) {
            Assert.assertFalse(spec.id().isBlank(), "html-ex corpus id must be present");
            Assert.assertFalse(spec.profile().isBlank(), "html-ex corpus profile missing for " + spec.id());
            Assert.assertFalse(spec.severity().isBlank(), "html-ex corpus severity missing for " + spec.id());
            Assert.assertFalse(spec.corpusRoot().isBlank(), "html-ex corpus root missing for " + spec.id());
            Assert.assertFalse(Path.of(spec.corpusRoot()).isAbsolute(),
                    "html-ex corpus default root must be repo-relative for " + spec.id());
            Assert.assertTrue(spec.corpusRoot().replace('\\', '/').startsWith("docs/html-ex"),
                    "html-ex corpus default root must use the local docs/html-ex corpus for " + spec.id());
            Assert.assertFalse(spec.rootProperty().isBlank(), "html-ex corpus root property missing for " + spec.id());
            Assert.assertFalse(spec.primaryStructuralReference().isBlank(),
                    "html-ex corpus primary structural reference missing for " + spec.id());
            Assert.assertEquals(spec.primaryStructuralReference(), "auto",
                    "html-ex corpus structural reference must be auto-discovered from docs/html-ex; "
                            + "do not pin a dated ATF folder in YAML for " + spec.id());
            Assert.assertFalse(Path.of(spec.primaryStructuralReference()).isAbsolute(),
                    "html-ex corpus primary structural reference must be repo-relative for " + spec.id());
            Assert.assertTrue(spec.minimumExamples() > 0,
                    "html-ex corpus minimumExamples must be positive for " + spec.id());
            Assert.assertFalse(spec.matrixArtifact().isBlank(),
                    "html-ex corpus matrixArtifact missing for " + spec.id());
            Assert.assertFalse(Path.of(spec.matrixArtifact()).isAbsolute(),
                    "html-ex corpus matrixArtifact must be repo-relative for " + spec.id());
            Assert.assertFalse(spec.validator().isBlank(), "html-ex corpus validator missing for " + spec.id());
            Assert.assertFalse(spec.repairTarget().isBlank(), "html-ex corpus repair target missing for " + spec.id());
            Assert.assertEquals(spec.repairTarget(), spec.matrixArtifact(),
                    "html-ex corpus repair target must stay aligned with matrixArtifact for " + spec.id());
            Assert.assertFalse(spec.capImpact().isBlank(), "html-ex corpus cap impact missing for " + spec.id());
            Assert.assertFalse(spec.publishabilityImpact().isBlank(),
                    "html-ex corpus publishability impact missing for " + spec.id());
        }
    }

    private static void assertSizeBudgetContracts() {
        Assert.assertFalse(ContractCatalog.sizeBudgetContracts().isEmpty(),
                "size-budgets.yml must declare package/class budgets.");
        for (McpSizeBudgetSpec spec : ContractCatalog.sizeBudgetContracts()) {
            Assert.assertFalse(spec.id().isBlank(), "size budget id must be present");
            Assert.assertFalse(spec.type().isBlank(), "size budget type missing for " + spec.id());
            Assert.assertFalse(spec.reason().isBlank(), "size budget reason missing for " + spec.id());
            if ("package-default".equals(spec.type())) {
                Assert.assertFalse(spec.root().isBlank(), "package-default root missing for " + spec.id());
                Assert.assertTrue(spec.maxClasses() > 0, "package-default maxClasses missing for " + spec.id());
            } else if ("package-override".equals(spec.type())) {
                Assert.assertFalse(spec.path().isBlank(), "package-override path missing for " + spec.id());
                Assert.assertTrue(spec.maxClasses() > 0, "package-override maxClasses missing for " + spec.id());
            } else if ("line-cap".equals(spec.type())) {
                Assert.assertFalse(spec.path().isBlank(), "line-cap path missing for " + spec.id());
                Assert.assertTrue(spec.maxLines() > 0, "line-cap maxLines missing for " + spec.id());
            } else if ("resource-max-bytes".equals(spec.type())) {
                Assert.assertFalse(spec.path().isBlank(), "resource-max-bytes path missing for " + spec.id());
                Assert.assertTrue(spec.maxBytes() > 0, "resource-max-bytes maxBytes missing for " + spec.id());
            } else if ("recursive-package-cap".equals(spec.type())) {
                Assert.assertFalse(spec.path().isBlank(), "recursive-package-cap path missing for " + spec.id());
                Assert.assertTrue(spec.maxClasses() > 0, "recursive-package-cap maxClasses missing for " + spec.id());
            } else {
                Assert.fail("Unknown size budget type for " + spec.id() + ": " + spec.type());
            }
        }
    }
}
