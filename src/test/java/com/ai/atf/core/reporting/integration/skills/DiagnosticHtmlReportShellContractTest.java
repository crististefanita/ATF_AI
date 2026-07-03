package com.ai.atf.core.reporting.integration.skills;

import com.ai.atf.core.reporting.architecture.support.ArchitectureSourceScanner;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.aggregate.internal.ExtentFinalAuditHtml;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDomIds;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.ai.core.support.html.HtmlTable;
import com.ai.core.support.html.HtmlText;
import com.ai.skills.mcp.contracts.checks.McpCheckRefEvidenceRenderer;
import com.ai.skills.mcp.rules.source.kpi.SourceEvolutionKpiCatalog;
import com.ai.skills.mcp.rules.source.kpi.SourceEvolutionKpiRow;
import com.ai.skills.mcp.rules.source.kpi.render.KpiMatrixHtmlRenderer;
import com.ai.skills.mcp.rules.source.kpi.render.KpiRepairBoardRenderer;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.DIAGNOSTIC_HTML_SHELL)
@Listeners(TestListener.class)
public class DiagnosticHtmlReportShellContractTest {
    private static final String ATLAS_RULE_MATRIX_RENDERER =
            "com.ai.skills.atlas.validation.AtlasRuleMatrixRenderer";
    private static final String MCP_CHECK_REF_EVIDENCE_RENDERER =
            "com.ai.skills.mcp.contracts.checks.McpCheckRefEvidenceRenderer";
    private static final String MCP_KPI_MATRIX_RENDERER =
            "com.ai.skills.mcp.rules.source.kpi.render.KpiMatrixHtmlRenderer";
    private static final String MCP_KPI_REPAIR_BOARD_RENDERER =
            "com.ai.skills.mcp.rules.source.kpi.render.KpiRepairBoardRenderer";

    private static final Path MAIN_JAVA = AtfSourcePaths.MAIN_AI_ROOT;
    private static final Path HTML_TEXT = AtfSourcePaths.mainJava(HtmlText.class);
    private static final List<Path> STANDALONE_DIAGNOSTIC_RENDERERS = List.of(
            AtfSourcePaths.mainJava(ATLAS_RULE_MATRIX_RENDERER),
            AtfSourcePaths.mainJava(MCP_CHECK_REF_EVIDENCE_RENDERER),
            AtfSourcePaths.mainJava(MCP_KPI_MATRIX_RENDERER),
            AtfSourcePaths.mainJava(MCP_KPI_REPAIR_BOARD_RENDERER)
    );

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.DIAGNOSTIC_HTML_SHELL_DOCUMENT)
    public void diagnosticDocumentPublishesTheAgreedSmallReportShell() {
        String html = HtmlText.diagnosticDocument("Demo <Report>", "<h1>Demo</h1><table></table>");

        assertDiagnosticShell("ATF diagnostic document", html);
        Assert.assertTrue(html.contains("<title>Demo &lt;Report&gt;</title>"));
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.DIAGNOSTIC_HTML_SHELL_FAMILIES)
    public void smallDiagnosticReportFamiliesShareTheAgreedShell() throws Exception {
        Map<String, String> reports = new LinkedHashMap<>();
        reports.put("ATF diagnostic document",
                HtmlText.diagnosticDocument("ATF Core Diagnostic", "<h1>ATF Core Diagnostic</h1><table></table>"));
        reports.put("Atlas rule matrix", atlasRuleMatrixHtml());
        reports.put("MCP CheckRef matrix", McpCheckRefEvidenceRenderer.renderHtmlMatrix(sampleMcpCheckIds()));
        reports.put("MCP KPI matrix", KpiMatrixHtmlRenderer.renderMatrix(sampleKpiRows()));
        reports.put("MCP KPI repair board", KpiRepairBoardRenderer.renderHtml(sampleKpiRows()));

        for (Map.Entry<String, String> report : reports.entrySet()) {
            assertDiagnosticShell(report.getKey(), report.getValue());
            assertNoExtentShellMarkup(report.getKey(), report.getValue());
        }
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.DIAGNOSTIC_HTML_SHELL_FRAGMENTS)
    public void finalReportFragmentsDoNotPretendToBeStandaloneDiagnosticDocuments() {
        Map<String, String> fragments = Map.of(
                "MCP CheckRef final-report section",
                McpCheckRefEvidenceRenderer.renderFinalReportSection(sampleMcpCheckIds()),
                "ATF core Extent final audit section",
                ExtentFinalAuditHtml.finalAuditSection(2, 2, 2, 0, 0, "unsafe <inventory>")
        );

        Assert.assertTrue(fragments.get("MCP CheckRef final-report section")
                .startsWith("<section id=\"mcp-check-ref-evidence\">"));
        Assert.assertTrue(fragments.get("ATF core Extent final audit section")
                .contains("<section id=\"atf-core-final-audit\""));
        Assert.assertTrue(fragments.get("ATF core Extent final audit section")
                .contains("unsafe &lt;inventory&gt;"),
                "ATF core final audit fragment must escape inventory text before embedding in Extent.");

        for (Map.Entry<String, String> fragment : fragments.entrySet()) {
            assertFragmentDoesNotOwnDocumentShell(fragment.getKey(), fragment.getValue());
        }
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.DIAGNOSTIC_HTML_STATUS_PALETTE)
    public void diagnosticStatusClassesStayCanonicalAcrossSmallReportRenderers() {
        String atfStatusTable = HtmlText.diagnosticDocument(
                "ATF Status Palette",
                HtmlTable.withHeadings("Status", "Meaning")
                        .row(HtmlTable.Cell.classed("pass", "PASS"), HtmlTable.Cell.text("green"))
                        .row(HtmlTable.Cell.classed("fail", "FAIL"), HtmlTable.Cell.text("red"))
                        .row(HtmlTable.Cell.classed("risk", "RISK"), HtmlTable.Cell.text("amber"))
                        .row(HtmlTable.Cell.classed("info", "INFO"), HtmlTable.Cell.text("blue"))
                        .render()
        );
        String kpiMatrix = KpiMatrixHtmlRenderer.renderMatrix(sampleKpiRowsWithStatuses());
        String kpiRepairBoard = KpiRepairBoardRenderer.renderHtml(sampleKpiRowsWithStatuses());

        assertDiagnosticShell("ATF status table", atfStatusTable);
        assertStatusClass("ATF status table", atfStatusTable, "pass", "PASS");
        assertStatusClass("ATF status table", atfStatusTable, "fail", "FAIL");
        assertStatusClass("ATF status table", atfStatusTable, "risk", "RISK");
        assertStatusClass("ATF status table", atfStatusTable, "info", "INFO");
        assertStatusClass("MCP KPI matrix", kpiMatrix, "pass", "PASS");
        assertStatusClass("MCP KPI matrix", kpiMatrix, "fail", "FAIL");
        assertStatusClass("MCP KPI matrix", kpiMatrix, "risk", "RISK");
        assertStatusClass("MCP KPI matrix", kpiMatrix, "info", "INFO");
        assertStatusClass("MCP KPI repair board", kpiRepairBoard, "fail", "FAIL");
        assertStatusClass("MCP KPI repair board", kpiRepairBoard, "risk", "RISK");

        String atlasRenderer = ArchitectureSourceScanner.readUnchecked(
                AtfSourcePaths.mainJava(ATLAS_RULE_MATRIX_RENDERER));
        String htmlTableColumn = ArchitectureSourceScanner.readUnchecked(
                AtfSourcePaths.mainJava("com.ai.core.support.html.HtmlTableColumn"));
        Assert.assertTrue(atlasRenderer.contains("HtmlTableColumn.status(\"Status\", AtlasRuleMatrixRow::status)")
                        && htmlTableColumn.contains("HtmlTable.Cell.classed(rendered.toLowerCase(Locale.ROOT), rendered)"),
                "Atlas matrix status cells must route through HtmlTable status classes.");
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.DIAGNOSTIC_HTML_SHELL_RENDERERS)
    public void diagnosticHtmlRenderersUseTheSharedDocumentShell() throws IOException {
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(MAIN_JAVA)) {
            files.filter(AtfSourcePaths::isJavaSource)
                    .filter(path -> !path.equals(HTML_TEXT))
                    .forEach(path -> inspectRendererSource(path, offenders));
        }

        Assert.assertTrue(
                offenders.isEmpty(),
                "Small diagnostic HTML reports must use HtmlText.diagnosticDocument; offenders: " + offenders
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.DIAGNOSTIC_HTML_TABLE_MODEL)
    public void standaloneDiagnosticRenderersUseTheSharedTableModel() {
        List<String> offenders = new ArrayList<>();
        for (Path renderer : STANDALONE_DIAGNOSTIC_RENDERERS) {
            String source = ArchitectureSourceScanner.readUnchecked(renderer);
            if (source.contains("<table") || source.contains("<thead>") || source.contains("<tbody>")) {
                offenders.add(renderer.toString());
            }
            if (source.contains("diagnosticDocument(") && !source.contains("HtmlTable")) {
                offenders.add(renderer + " does not use HtmlTable for diagnostic content.");
            }
        }

        Assert.assertTrue(
                offenders.isEmpty(),
                "Standalone small diagnostic reports must use the shared HtmlTable model; offenders: " + offenders
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.DIAGNOSTIC_HTML_YAML_STANDARDS)
    public void reportingShellYamlDeclaresSmallDiagnosticReportStandards() {
        Assert.assertEquals(
                AtfCoreYamlContractCatalog
                        .requireReportingShellContract("core.reporting-shell.small-diagnostic-document-shell")
                        .owner(),
                "HtmlText.diagnosticDocument"
        );
        Assert.assertEquals(
                AtfCoreYamlContractCatalog
                        .requireReportingShellContract("core.reporting-shell.small-diagnostic-table-model")
                        .owner(),
                "HtmlTable and HtmlTableColumn"
        );
        Assert.assertEquals(
                AtfCoreYamlContractCatalog
                        .requireReportingShellContract("core.reporting-shell.small-diagnostic-status-palette")
                        .surface(),
                "Small diagnostic HTML"
        );
        Assert.assertEquals(
                AtfCoreYamlContractCatalog
                        .requireReportingShellContract("core.reporting-shell.no-extent-shell-in-small-diagnostics")
                        .kind(),
                "forbidden-visible-fallback"
        );
    }

    private static void inspectRendererSource(Path path, List<String> offenders) {
        String source = ArchitectureSourceScanner.readUnchecked(path);
        if (source.contains("<!doctype html><html") || source.contains("HtmlText.document(")) {
            offenders.add(path.toString());
        }
    }

    private static String atlasRuleMatrixHtml() throws Exception {
        Class<?> renderer = Class.forName(ATLAS_RULE_MATRIX_RENDERER);
        Method toHtml = renderer.getDeclaredMethod("toHtml", List.class);
        toHtml.setAccessible(true);
        return (String) toHtml.invoke(null, List.of());
    }

    private static List<String> sampleMcpCheckIds() {
        return List.of("mcp.yaml-contract.kpi-schema.required-fields");
    }

    private static List<SourceEvolutionKpiRow> sampleKpiRows() {
        SourceEvolutionKpiCatalog.KpiSpec root = SourceEvolutionKpiCatalog.rootKpis().stream()
                .filter(spec -> "root_diff_files_count".equals(spec.id()))
                .findFirst()
                .orElseThrow();
        SourceEvolutionKpiCatalog.KpiSpec report = SourceEvolutionKpiCatalog.diagnosticKpis().stream()
                .filter(spec -> "final_report_mcp_link_completeness".equals(spec.id()))
                .findFirst()
                .orElseThrow();
        return List.of(
                SourceEvolutionKpiRow.of(root, 4, 4, "PASS", ""),
                SourceEvolutionKpiRow.of(report, 100, 80, "RISK", "expected 100 but observed 80")
        );
    }

    private static List<SourceEvolutionKpiRow> sampleKpiRowsWithStatuses() {
        SourceEvolutionKpiCatalog.KpiSpec root = SourceEvolutionKpiCatalog.rootKpis().stream()
                .filter(spec -> "root_diff_files_count".equals(spec.id()))
                .findFirst()
                .orElseThrow();
        SourceEvolutionKpiCatalog.KpiSpec report = SourceEvolutionKpiCatalog.diagnosticKpis().stream()
                .filter(spec -> "final_report_mcp_link_completeness".equals(spec.id()))
                .findFirst()
                .orElseThrow();
        return List.of(
                SourceEvolutionKpiRow.of(root, 4, 4, "PASS", ""),
                SourceEvolutionKpiRow.of(root, 4, 3, "FAIL", "expected 4 but observed 3"),
                SourceEvolutionKpiRow.of(report, 100, 80, "RISK", "expected 100 but observed 80"),
                SourceEvolutionKpiRow.of(report, 100, null, "INFO", "missing optional diagnostic")
        );
    }

    private static void assertDiagnosticShell(String label, String html) {
        Assert.assertTrue(html.startsWith("<!doctype html><html><head><meta charset=\"utf-8\"><title>"),
                label + " must start with the canonical diagnostic document shell.");
        Assert.assertTrue(html.contains("font-family:Segoe UI,Arial,sans-serif"),
                label + " must use the shared diagnostic typography.");
        Assert.assertTrue(html.contains("margin:24px"),
                label + " must use the shared diagnostic page spacing.");
        Assert.assertTrue(html.contains("color:#172033"),
                label + " must use the shared diagnostic text color.");
        Assert.assertTrue(html.contains("table{border-collapse:collapse;width:100%;font-size:13px}"),
                label + " must use the shared diagnostic table shape.");
        Assert.assertTrue(html.contains(".pass{color:#166534;font-weight:700}"),
                label + " must publish the shared PASS status class.");
        Assert.assertTrue(html.contains(".fail{color:#991b1b;font-weight:700}"),
                label + " must publish the shared FAIL status class.");
        Assert.assertTrue(html.contains(".risk{color:#b45309;font-weight:700}"),
                label + " must publish the shared RISK status class.");
        Assert.assertTrue(html.contains(".info{color:#1d4ed8;font-weight:700}"),
                label + " must publish the shared INFO status class.");
        Assert.assertTrue(html.contains("code,pre{white-space:normal;color:#334155}"),
                label + " must use the shared code/pre styling.");
    }

    private static void assertNoExtentShellMarkup(String label, String html) {
        Assert.assertFalse(html.contains("<script " + ExtentMarkers.scriptId(ExtentMarkers.REPORT_MODE)),
                label + " must not inject an Extent Spark report-mode script.");
        Assert.assertFalse(html.contains(ExtentDataAttributes.REPORT_MODE + "="),
                label + " must not inject Extent Spark report-mode attributes.");
        Assert.assertFalse(html.contains("class=\"" + ExtentHtmlStructure.DASHBOARD_VIEW_CLASS)
                        || html.contains("class=\"" + ExtentDomIds.DASHBOARD_VIEW),
                label + " must not reuse Extent dashboard markup.");
    }

    private static void assertFragmentDoesNotOwnDocumentShell(String label, String html) {
        Assert.assertFalse(html.contains("<!doctype html>"),
                label + " must not include a standalone document wrapper.");
        Assert.assertFalse(html.contains("font-family:Segoe UI,Arial,sans-serif"),
                label + " must inherit its host report shell instead of injecting diagnostic CSS.");
        Assert.assertFalse(html.contains("<script " + ExtentMarkers.scriptId(ExtentMarkers.REPORT_MODE)),
                label + " must not inject the Extent report-mode marker itself.");
    }

    private static void assertStatusClass(String label, String html, String cssClass, String text) {
        Assert.assertTrue(html.contains("class=\"" + cssClass + "\">" + text + "</td>"),
                label + " must render " + text + " with class=\"" + cssClass + "\".");
    }
}
