package com.ai.skills.mcp.tests.contracts;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.contract.McpContractMatrix;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.contracts.advisor.McpAiTaskAdvisorEvaluator;
import com.ai.skills.mcp.contracts.advisor.McpAiTaskAdvisorInput;
import com.ai.skills.mcp.contracts.advisor.McpAiTaskAdvisorRenderer;
import com.ai.skills.mcp.contracts.advisor.McpAiTaskAdvisorSpec;
import com.ai.skills.mcp.rules.source.kpi.SourceEvolutionKpiRow;
import com.ai.skills.mcp.rules.source.kpi.SourceEvolutionKpiCatalog;
import com.ai.skills.mcp.rules.source.kpi.render.KpiMatrixHtmlRenderer;
import com.ai.skills.mcp.rules.source.kpi.render.KpiMatrixMarkdownRenderer;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-yaml-contract-matrix-test")
public class McpYamlContractMatrixTest {
    private static final Path TARGET_ADVISOR_MD = Path.of("target/mcp-ai-task-advisor.md");
    private static final Path TARGET_ADVISOR_HTML = Path.of("target/mcp-ai-task-advisor.html");
    private static final Path TARGET_ADVISOR_SECTION = Path.of("target/mcp-ai-task-advisor-final-report-section.html");

    @Test
    @McpCheckRef("mcp.yaml-contract.kpi-matrix.metadata-columns")
    public void yamlBackedKpiMatrixCarriesRepairAndScoreMetadata() {
        List<SourceEvolutionKpiRow> rows = sampleRows();
        String markdown = KpiMatrixMarkdownRenderer.render(rows);
        String html = KpiMatrixHtmlRenderer.renderMatrix(rows);

        Assert.assertTrue(markdown.contains("root_diff_files_count"), "Matrix must include close-out blocker KPI.");
        Assert.assertTrue(markdown.contains("final_report_mcp_link_completeness"),
                "Matrix must include final report link completeness KPI.");
        Assert.assertTrue(markdown.contains("Primary repair artifact"), "Matrix must expose repair artifact column.");
        Assert.assertTrue(markdown.contains("Cap impact"), "Matrix must expose cap impact column.");
        Assert.assertTrue(markdown.contains("Publishability impact"), "Matrix must expose publishability impact column.");
        Assert.assertTrue(html.contains("<th>Route</th>"), "HTML matrix must expose repair route.");
        Assert.assertTrue(html.contains("package-closeout"), "HTML matrix must carry YAML profile metadata.");

        String ruleMatrix = McpContractMatrix.allProfilesRuleMatrix();
        Assert.assertTrue(ruleMatrix.contains("profile | rule_id | family | artifact_family | cost | category"),
                "Rule matrix must expose profile-level MCP rule routing with typed family/artifact-family/cost metadata.");
        Assert.assertTrue(ruleMatrix.contains("FULL_PACKAGE | mcp.gate.package-closeout"),
                "Rule matrix must name package close-out gate under the full/package profile.");
        Assert.assertTrue(ruleMatrix.contains("HTML_EXAMPLE_CANDIDATE"),
                "Rule matrix must keep candidate promotion rules visible beside package rules.");
        Assert.assertTrue(ruleMatrix.contains("repair_route"),
                "Rule matrix must tell agents where to repair a failing rule.");
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.ai-task-advisor.rendering")
    public void aiTaskAdvisorRendersNonBlockingCreditEfficientGuidance() throws IOException {
        String markdown = McpAiTaskAdvisorRenderer.renderMarkdown(ContractCatalog.aiTaskAdvisorContracts());
        String html = McpAiTaskAdvisorRenderer.renderHtml(ContractCatalog.aiTaskAdvisorContracts());
        String section = McpAiTaskAdvisorRenderer.renderFinalReportSection(ContractCatalog.aiTaskAdvisorContracts());

        Files.writeString(TARGET_ADVISOR_MD, markdown);
        Files.writeString(TARGET_ADVISOR_HTML, html);
        Files.writeString(TARGET_ADVISOR_SECTION, section);

        Assert.assertTrue(markdown.contains("Model tier"),
                "Advisor markdown must expose model-tier guidance.");
        Assert.assertTrue(markdown.contains("Acceptance criteria"),
                "Advisor markdown must expose acceptance criteria.");
        Assert.assertTrue(markdown.contains("Stop condition"),
                "Advisor markdown must expose stop condition guidance.");
        Assert.assertTrue(html.contains(McpAiTaskAdvisorRenderer.SECTION_ANCHOR),
                "Advisor HTML must expose a stable final-report anchor.");
        Assert.assertTrue(section.contains("does not override MCP blockers"),
                "Advisor final-report section must stay advisory-only.");
        Assert.assertTrue(section.contains("<details id=\"" + McpAiTaskAdvisorRenderer.SECTION_ANCHOR + "\""),
                "Advisor final-report section must be a collapsed details block.");
        Assert.assertFalse(section.contains("<section id=\"" + McpAiTaskAdvisorRenderer.SECTION_ANCHOR + "\""),
                "Advisor final-report section must not be rendered as an always-open section.");
        Assert.assertFalse(markdown.contains("BLOCKER") || html.contains("BLOCKER") || section.contains("BLOCKER"),
                "Advisor output must not publish blocker severity; integrity blockers live in executable MCP rules.");
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.ai-task-advisor.contextual-nonblocking")
    public void aiTaskAdvisorEvaluatesRunShapeWithoutCreatingBlockers() {
        McpAiTaskAdvisorInput incompleteRun = McpAiTaskAdvisorInput.builder()
                .taskScope("analyze everything in all modules and improve the whole system")
                .modelTier("extra high")
                .build();

        List<McpAiTaskAdvisorSpec> findings = McpAiTaskAdvisorEvaluator.evaluate(incompleteRun);

        Assert.assertTrue(findings.stream().anyMatch(spec -> spec.id().equals("advisor.prompt.scope.too-broad")),
                "Broad run scope should produce advisor feedback.");
        Assert.assertTrue(findings.stream().anyMatch(spec -> spec.id().equals("advisor.acceptance-criteria.missing")),
                "Missing acceptance criteria should produce advisor feedback.");
        Assert.assertTrue(findings.stream().anyMatch(spec -> spec.id().equals("advisor.stop-condition.missing")),
                "Missing stop condition should produce advisor feedback.");
        Assert.assertTrue(findings.stream().noneMatch(McpAiTaskAdvisorSpec::blocking),
                "Contextual advisor findings must remain non-blocking.");

        McpAiTaskAdvisorInput completeRun = McpAiTaskAdvisorInput.builder()
                .taskScope("update MCP advisor report section")
                .modelTier("light")
                .acceptanceCriteria("advisor section appears in governance map")
                .stopCondition("target advisor artifacts are generated")
                .valueRationale("reduces agent calibration time")
                .build();

        Assert.assertTrue(McpAiTaskAdvisorEvaluator.evaluate(completeRun).isEmpty(),
                "A scoped run with acceptance criteria, stop condition and value rationale should not need advisor feedback.");
    }

    private static List<SourceEvolutionKpiRow> sampleRows() {
        List<SourceEvolutionKpiRow> rows = new ArrayList<>();
        SourceEvolutionKpiCatalog.KpiSpec root = SourceEvolutionKpiCatalog.rootKpis().stream()
                .filter(spec -> "root_diff_files_count".equals(spec.id()))
                .findFirst()
                .orElseThrow();
        SourceEvolutionKpiCatalog.KpiSpec report = SourceEvolutionKpiCatalog.diagnosticKpis().stream()
                .filter(spec -> "final_report_mcp_link_completeness".equals(spec.id()))
                .findFirst()
                .orElseThrow();
        rows.add(SourceEvolutionKpiRow.of(root, 4, 4, "PASS", ""));
        rows.add(SourceEvolutionKpiRow.of(report, 100, 80, "RISK", "expected 100 but observed 80"));
        return rows;
    }
}
