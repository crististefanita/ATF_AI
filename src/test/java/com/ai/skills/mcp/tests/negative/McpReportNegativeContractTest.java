package com.ai.skills.mcp.tests.negative;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contract.RequiresArtifacts;
import com.ai.skills.mcp.contract.ValidatesMcpContract;
import com.ai.skills.mcp.rules.report.BusinessGraphReportRule;
import com.ai.skills.mcp.rules.report.CloseOutReportRule;
import com.ai.skills.mcp.rules.report.LocalLinksRule;
import com.ai.skills.mcp.rules.report.ProofMappingReportRule;
import com.ai.skills.mcp.rules.report.ReportChromeRule;
import com.ai.skills.mcp.tests.support.McpTestDigests;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.skills.mcp.tests.support.HtmlDetailsMutator.closeDetailsSection;
import static com.ai.skills.mcp.tests.support.HtmlDetailsMutator.replaceDetailsSection;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.cloneLatestPackage;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.clonePrimaryHtmlExamplePackage;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.clonePrimaryHtmlExampleReportSpine;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.mutateReport;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.write;
import static com.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.ai.skills.mcp.validation.McpViolationValidation.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("report-negative-contracts")
@RequiresArtifacts({
        McpArtifactPaths.FINAL_REPORT,
        McpArtifactPaths.REPORT_CSS
})
@McpSuiteRef("mcp.suite.mcp-report-negative-contract-test")
public class McpReportNegativeContractTest {
    @Test
    @ValidatesMcpContract("report.graph.default-open")
    @McpCheckRef("report.business-graph.default-open.negative")
    public void rejectsPackageWhenBusinessFlowGraphIsNotDefaultOpen() throws IOException {
        Path packageRoot = cloneLatestPackage();
        mutateReport(packageRoot, html -> closeDetailsSection(html, "Business Flow Graph"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new BusinessGraphReportRule());

        logValidatorScenario(
                "Reject final report with closed Business Flow Graph",
                packageRoot,
                "Changed analysis/langgraph-business-understanding.html so Business Flow Graph is not open by default.",
                "Expected violation: report.graph.default-open.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.graph.default-open");
    }

    @Test
    @ValidatesMcpContract("report.section-hint.decorative-collapse-state")
    public void rejectsPackageWhenSectionHintDoesNotExplainSection() throws IOException {
        Path packageRoot = cloneLatestPackage();
        mutateReport(packageRoot, html -> html.replaceFirst(
                "class=\"sumhint\">[^<]+",
                "class=\"sumhint\">closed"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new ReportChromeRule());

        logValidatorScenario(
                "Reject decorative collapse-state section hint",
                packageRoot,
                "Changed a section hint to say closed instead of explaining the section contents.",
                "Expected violation: report.section-hint.decorative-collapse-state.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.section-hint.decorative-collapse-state");
    }

    @Test
    public void rejectsPackageWhenFinalReportLeaksExternalCalibrationLanguage() throws IOException {
        Path packageRoot = cloneLatestPackage();
        mutateReport(packageRoot, html -> html.replace("</header>", "</header><p>html-ex benchmark comparison</p>"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new CloseOutReportRule());

        logValidatorScenario(
                "Reject final report leaking calibration language",
                packageRoot,
                "Injected html-ex / benchmark / comparison language into the final report.",
                "Expected violation: report.external-calibration-leak.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.external-calibration-leak");
    }

    @Test
    @ValidatesMcpContract("report.css.shared-reference-copy-drift")
    public void rejectsPackageWhenAnalysisSharedCssDriftsFromProjectReference() throws IOException {
        Path packageRoot = cloneLatestPackage();
        Path css = packageRoot.resolve(McpArtifactPaths.REPORT_CSS);
        Files.writeString(css, Files.readString(css)
                + "\n/* local drift: generated package must not patch report CSS inside analysis */\n"
                + ".local-drift { color: hotpink; }\n");

        McpPackageValidationResult result = validateWithRules(packageRoot, new ReportChromeRule());

        logValidatorScenario(
                "Reject analysis shared CSS drift",
                packageRoot,
                "Changed " + McpArtifactPaths.REPORT_CSS + " without changing the project-level canonical CSS.",
                "Expected violation: report.css.shared-reference-copy-drift.",
                "Asserted artifact: " + McpArtifactPaths.REPORT_CSS + ".",
                result);
        requireCode(result, "report.css.shared-reference-copy-drift");
    }

    @Test
    public void rejectsPackageWhenFinalReportHasBrokenLocalLink() throws IOException {
        Path packageRoot = clonePrimaryHtmlExamplePackage();
        mutateReport(packageRoot, html -> html.replace("../runs/run_01/validation/reports/ExtentReport.html",
                "../runs/run_99/validation/reports/missing.html"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new LocalLinksRule());

        logValidatorScenario(
                "Reject final report broken local hyperlink",
                packageRoot,
                "Changed analysis/langgraph-business-understanding.html to link to ../runs/run_99/validation/reports/missing.html.",
                "Expected violation: report.link.broken.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.link.broken");
    }

    @Test
    @ValidatesMcpContract("report.framework-properties-link.missing")
    public void rejectsPackageWhenFinalReportDoesNotLinkFrameworkPropertiesSnapshot() throws IOException {
        Path packageRoot = clonePrimaryHtmlExampleReportSpine();
        mutateReport(packageRoot, html -> html
                .replace(doubleQuotedHref(McpArtifactPaths.FRAMEWORK_PROPERTIES_SNAPSHOT),
                        doubleQuotedHref(McpArtifactPaths.PACKAGE_STATE))
                .replace(singleQuotedHref(McpArtifactPaths.FRAMEWORK_PROPERTIES_SNAPSHOT),
                        singleQuotedHref(McpArtifactPaths.PACKAGE_STATE)));

        McpPackageValidationResult result = validateWithRules(packageRoot, new ProofMappingReportRule());

        logValidatorScenario(
                "Reject final report without audited framework.properties hyperlink",
                packageRoot,
                "Removed the Artifact Index hyperlink to "
                        + McpArtifactPaths.linkFromFinalReport(McpArtifactPaths.FRAMEWORK_PROPERTIES_SNAPSHOT) + ".",
                "Expected violation: report.framework-properties-link.missing.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.framework-properties-link.missing");
    }

    @Test
    @ValidatesMcpContract("report.artifact-index.core-link.missing")
    public void rejectsPackageWhenArtifactIndexDoesNotLinkRootDiff() throws IOException {
        Path packageRoot = clonePrimaryHtmlExampleReportSpine();
        mutateReport(packageRoot, html -> html
                .replace(doubleQuotedHref(McpArtifactPaths.ROOT_DIFF), doubleQuotedHref(McpArtifactPaths.PACKAGE_STATE))
                .replace(singleQuotedHref(McpArtifactPaths.ROOT_DIFF), singleQuotedHref(McpArtifactPaths.PACKAGE_STATE)));

        McpPackageValidationResult result = validateWithRules(packageRoot, new ProofMappingReportRule());

        logValidatorScenario(
                "Reject final report without root diff hyperlink in Artifact Index",
                packageRoot,
                "Removed the Artifact Index hyperlink to "
                        + McpArtifactPaths.linkFromFinalReport(McpArtifactPaths.ROOT_DIFF) + ".",
                "Expected violation: report.artifact-index.core-link.missing.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.artifact-index.core-link.missing");
    }

    @Test
    @ValidatesMcpContract("report.mcp-evidence.missing")
    public void rejectsPackageWhenFinalReportDoesNotLinkCompleteMcpExtentReport() throws IOException {
        Path packageRoot = clonePrimaryHtmlExampleReportSpine();
        publishCompleteMcpEvidenceFixture(packageRoot);
        mutateReport(packageRoot, html -> html
                .replace(doubleQuotedHref(McpArtifactPaths.MCP_EXTENT_REPORT),
                        doubleQuotedHref(McpArtifactPaths.MCP_GATE_RESULT))
                .replace(singleQuotedHref(McpArtifactPaths.MCP_EXTENT_REPORT),
                        singleQuotedHref(McpArtifactPaths.MCP_GATE_RESULT)));

        McpPackageValidationResult result = validateWithRules(packageRoot, new ProofMappingReportRule());

        logValidatorScenario(
                "Reject final report without complete MCP Extent hyperlink",
                packageRoot,
                "Removed the Artifact Index hyperlink to "
                        + McpArtifactPaths.linkFromFinalReport(McpArtifactPaths.MCP_EXTENT_REPORT) + ".",
                "Expected violation: report.mcp-evidence.missing.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.mcp-evidence.missing");
    }

    @Test
    @ValidatesMcpContract("report.mcp-evidence.missing")
    public void rejectsPackageWhenFinalReportDoesNotLinkCompleteMcpMethodLedger() throws IOException {
        Path packageRoot = clonePrimaryHtmlExampleReportSpine();
        publishCompleteMcpEvidenceFixture(packageRoot);
        mutateReport(packageRoot, html -> html
                .replace(doubleQuotedHref(McpArtifactPaths.MCP_METHOD_LEDGER),
                        doubleQuotedHref(McpArtifactPaths.MCP_GATE_RESULT))
                .replace(singleQuotedHref(McpArtifactPaths.MCP_METHOD_LEDGER),
                        singleQuotedHref(McpArtifactPaths.MCP_GATE_RESULT)));

        McpPackageValidationResult result = validateWithRules(packageRoot, new ProofMappingReportRule());

        logValidatorScenario(
                "Reject final report without complete MCP method ledger hyperlink",
                packageRoot,
                "Removed the Artifact Index hyperlink to "
                        + McpArtifactPaths.linkFromFinalReport(McpArtifactPaths.MCP_METHOD_LEDGER) + ".",
                "Expected violation: report.mcp-evidence.missing.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.mcp-evidence.missing");
    }

    @Test
    public void rejectsPackageWhenRunAuditDoesNotExposeDistinctClaims() throws IOException {
        Path packageRoot = clonePrimaryHtmlExampleReportSpine();
        mutateReport(packageRoot, html -> replaceDetailsSection(html, "Run Audit",
                "<ul class=\"run-audit-list\"><li>run_01: same claim</li><li>run_02: same claim</li></ul>"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new CloseOutReportRule());

        logValidatorScenario(
                "Reject repetitive Run Audit claims",
                packageRoot,
                "Collapsed Run Audit to repeated bookkeeping instead of distinct business claims.",
                "Expected violation: report.run-audit.claims-too-repetitive.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.run-audit.claims-too-repetitive");
    }

    @Test
    public void rejectsPackageWhenCloseOutDoesNotNameConcreteNextBusinessObject() throws IOException {
        Path packageRoot = clonePrimaryHtmlExampleReportSpine();
        mutateReport(packageRoot, html -> replaceDetailsSection(html, "Close-Out",
                "<p class=\"closeout-summary\">Continue later with a valuable flow.</p>"));

        McpPackageValidationResult result = validateWithRules(packageRoot, new CloseOutReportRule());

        logValidatorScenario(
                "Reject generic Close-Out next step",
                packageRoot,
                "Changed Close-Out to generic continuation without a named business object.",
                "Expected violation: report.close-out.next-object-missing.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.close-out.next-object-missing");
    }

    private void publishCompleteMcpEvidenceFixture(Path packageRoot) throws IOException {
        Path mcp = packageRoot.resolve(McpArtifactPaths.MCP_VALIDATION_ROOT);
        Files.createDirectories(mcp);
        Path extent = packageRoot.resolve(McpArtifactPaths.MCP_EXTENT_REPORT);
        if (!Files.isRegularFile(extent)) {
            write(extent, completeMcpExtentHtml(78, 99));
        }
        String hash = McpTestDigests.sha256(extent);
        write(packageRoot.resolve(McpArtifactPaths.MCP_EXTENT_SHA256),
                hash + "  " + McpArtifactPaths.fileName(McpArtifactPaths.MCP_EXTENT_REPORT) + "\n");
        write(packageRoot.resolve(McpArtifactPaths.MCP_EXTENT_ORIGIN),
                "{\n"
                        + "  \"command\": \"mvn -q -Dtest=Mcp*Test test -Dmcp.package.root=" + packageRoot.getFileName() + "\",\n"
                        + "  \"timestamp\": \"2026-06-29T10:00:00+03:00\",\n"
                        + "  \"exit\": 0,\n"
                        + "  \"artifact\": \"" + McpArtifactPaths.fileName(McpArtifactPaths.MCP_EXTENT_REPORT) + "\",\n"
                        + "  \"sha256\": \"" + hash + "\",\n"
                        + "  \"size\": " + Files.size(extent) + "\n"
                        + "}\n");
        write(packageRoot.resolve(McpArtifactPaths.MCP_TESTNG_XML),
                "<testsuite name=\"MCP\" tests=\"177\" failures=\"0\" errors=\"0\" skipped=\"99\"></testsuite>\n");
        write(packageRoot.resolve(McpArtifactPaths.MCP_METHOD_LEDGER),
                "# Full MCP Method Ledger\n\n"
                        + "Tests run: 177\n"
                        + "Passed: 78\n"
                        + "Skipped: 99\n"
                        + "Failures: 0\n"
                        + "Errors: 0\n\n"
                        + "Maven full-suite outcome: discovered=177, executed/pass=78, skipped=99, failures=0, errors=0.\n\n"
                        + "Skipped profile classes:\n"
                        + "- profile/precondition group: package-audit profile methods not active for this validator slice; reason: TestNG profile/precondition skip; accepted=true.\n");
        write(packageRoot.resolve(McpArtifactPaths.MCP_GATE_RESULT),
                "# MCP Gate Result\n\n"
                        + "- verdict: PASS\n"
                        + "- command: mvn -q -Dtest=Mcp*Test test -Dmcp.package.root=" + packageRoot.getFileName() + "\n"
                        + "- package_root: " + packageRoot.getFileName() + "\n"
                        + "- Maven full-suite outcome: discovered=177 executed/pass=78 skipped=99 failures=0 errors=0\n"
                        + "- Tests run: 177\n"
                        + "- Passed: 78\n"
                        + "- Failures: 0\n"
                        + "- Errors: 0\n"
                        + "- Skipped: 99\n"
                        + "- latest_extent_report: " + McpArtifactPaths.MCP_EXTENT_REPORT + "\n"
                        + "- latest_extent_sha256: " + McpArtifactPaths.MCP_EXTENT_SHA256 + "\n"
                        + "- latest_extent_origin: " + McpArtifactPaths.MCP_EXTENT_ORIGIN + "\n"
                        + "- complete_testng_xml: " + McpArtifactPaths.MCP_TESTNG_XML + "\n"
                        + "- full_mcp_method_ledger: " + McpArtifactPaths.MCP_METHOD_LEDGER + "\n");
        mutateReport(packageRoot, html -> replaceDetailsSection(html, "Artifact Index",
                "<ul>"
                        + artifactItem(McpArtifactPaths.README, McpArtifactPaths.README)
                        + artifactItem(McpArtifactPaths.PACKAGE_STATE, "package-state.json")
                        + artifactItem(McpArtifactPaths.ROOT_DIFF, "root-source-thin-no-index.diff")
                        + artifactItem(McpArtifactPaths.LOCAL_LINK_CHECK, "local-link-check.txt")
                        + artifactItem(McpArtifactPaths.RENDER_PROOF, "canonical-render-check.png")
                        + artifactItem(McpArtifactPaths.EXECUTION_GATE_CARD, "execution-gate-card.md")
                        + artifactItem(McpArtifactPaths.PACKAGE_CLOSE_OUT, "package-close-out.md")
                        + artifactItem(McpArtifactPaths.FRAMEWORK_PROPERTIES_SNAPSHOT, "framework.properties")
                        + artifactItem(McpArtifactPaths.MCP_EXTENT_REPORT,
                                "MCP execution evidence ExtentReport.html")
                        + artifactItem(McpArtifactPaths.MCP_EXTENT_SHA256,
                                "MCP execution evidence SHA-256")
                        + artifactItem(McpArtifactPaths.MCP_EXTENT_ORIGIN,
                                "MCP execution evidence origin")
                        + artifactItem(McpArtifactPaths.MCP_GATE_RESULT, "MCP gate result")
                        + artifactItem(McpArtifactPaths.MCP_TESTNG_XML, "Complete MCP TestNG XML")
                        + artifactItem(McpArtifactPaths.MCP_METHOD_LEDGER, "Full MCP method ledger")
                        + "</ul>"));
    }

    private static String artifactItem(String artifact, String label) {
        return "<li><a href=\"" + McpArtifactPaths.linkFromFinalReport(artifact) + "\">" + label + "</a></li>";
    }

    private static String doubleQuotedHref(String artifact) {
        return "href=\"" + McpArtifactPaths.linkFromFinalReport(artifact) + "\"";
    }

    private static String singleQuotedHref(String artifact) {
        return "href='" + McpArtifactPaths.linkFromFinalReport(artifact) + "'";
    }

    private String completeMcpExtentHtml(int passed, int skipped) {
        StringBuilder html = new StringBuilder("<html><body class=\"spa -report dark\">")
                .append("<a id=\"nav-dashboard\"></a><div class=\"dashboard-view\"><span>Tests run</span>")
                .append("<span>Passed ").append(passed).append("</span><span>Skipped ").append(skipped).append("</span></div>")
                .append("<script>var statusGroup = { parentCount: ").append(passed + skipped)
                .append(", passParent: ").append(passed)
                .append(", failParent: 0, skipParent: ").append(skipped).append(" };</script>")
                .append("<h1>Complete MCP generated package audit</h1>")
                .append("<p>McpGeneratedPackageValidatorTest MCP contract suite complete MCP report</p>")
                .append("<ul class=\"test-list-item\">");
        for (int index = 1; index <= passed; index++) {
            html.append("<li class=\"test-item\" status=\"pass\" test-id=\"").append(index).append("\">")
                    .append("<p class=\"name\">McpGeneratedPackageValidatorTest pass method ").append(index).append("</p>")
                    .append("<span class=\"badge pass-bg log\">Pass</span></li>");
        }
        for (int index = 1; index <= skipped; index++) {
            html.append("<li class=\"test-item\" status=\"skip\" test-id=\"").append(passed + index).append("\">")
                    .append("<p class=\"name\">Mcp precondition skipped method ").append(index).append("</p>")
                    .append("<span class=\"badge skip-bg log\">Skip</span></li>");
        }
        html.append("</ul>");
        html.append(" MCP contract test pass skip ".repeat(4000));
        html.append("</body></html>");
        return html.toString();
    }

}
