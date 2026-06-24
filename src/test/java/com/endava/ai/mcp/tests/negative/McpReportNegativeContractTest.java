package com.endava.ai.mcp.tests.negative;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.mcp.McpGeneratedPackageValidator;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.contract.RequiresArtifacts;
import com.endava.ai.mcp.contract.ValidatesMcpContract;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.endava.ai.mcp.tests.support.HtmlDetailsMutator.closeDetailsSection;
import static com.endava.ai.mcp.tests.support.HtmlDetailsMutator.replaceDetailsSection;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.cloneLatestPackage;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.mutateReport;
import static com.endava.ai.mcp.tests.support.McpNegativeFixture.projectRoot;
import static com.endava.ai.mcp.tests.support.McpNegativeScenarioLogger.logValidatorScenario;
import static com.endava.ai.mcp.tests.support.McpViolationAssert.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("report-negative-contracts")
@RequiresArtifacts({
        "analysis/langgraph-business-understanding.html",
        "analysis/shared-report-reference.css"
})
public class McpReportNegativeContractTest {
    @Test
    @ValidatesMcpContract("report.graph.default-open")
    public void rejectsPackageWhenBusinessFlowGraphIsNotDefaultOpen() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        mutateReport(packageRoot, html -> closeDetailsSection(html, "Business Flow Graph"));

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject final report with closed Business Flow Graph",
                packageRoot,
                "Changed analysis/langgraph-business-understanding.html so Business Flow Graph is not open by default.",
                "Expected violation: report.graph.default-open.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.graph.default-open");
        StepLogger.pass("Validator rejected non-default-open Business Flow Graph");
    }

    @Test
    public void rejectsPackageWhenFinalReportLeaksExternalCalibrationLanguage() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        mutateReport(packageRoot, html -> html.replace("</header>", "</header><p>html-ex benchmark comparison</p>"));

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject final report leaking calibration language",
                packageRoot,
                "Injected html-ex / benchmark / comparison language into the final report.",
                "Expected violation: report.external-calibration-leak.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.external-calibration-leak");
        StepLogger.pass("Validator rejected external calibration language in final report");
    }

    @Test
    @ValidatesMcpContract("report.css.shared-reference-copy-drift")
    public void rejectsPackageWhenAnalysisSharedCssDriftsFromProjectReference() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        Path css = packageRoot.resolve("analysis/shared-report-reference.css");
        Files.writeString(css, Files.readString(css)
                + "\n/* local drift: generated package must not patch report CSS inside analysis */\n"
                + ".local-drift { color: hotpink; }\n");

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject analysis shared CSS drift",
                packageRoot,
                "Changed analysis/shared-report-reference.css without changing the project-level canonical CSS.",
                "Expected violation: report.css.shared-reference-copy-drift.",
                "Asserted artifact: analysis/shared-report-reference.css.",
                result);
        requireCode(result, "report.css.shared-reference-copy-drift");
        StepLogger.pass("Validator rejected local analysis CSS drift");
    }

    @Test
    public void rejectsPackageWhenFinalReportHasBrokenLocalLink() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        mutateReport(packageRoot, html -> html.replace("../runs/run_01/validation/reports/ExtentReport.html",
                "../runs/run_99/validation/reports/missing.html"));

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject final report broken local hyperlink",
                packageRoot,
                "Changed analysis/langgraph-business-understanding.html to link to ../runs/run_99/validation/reports/missing.html.",
                "Expected violation: report.link.broken.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.link.broken");
        StepLogger.pass("Validator rejected broken local report link");
    }

    @Test
    public void rejectsPackageWhenRunAuditDoesNotExposeDistinctClaims() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        mutateReport(packageRoot, html -> replaceDetailsSection(html, "Run Audit",
                "<ul class=\"run-audit-list\"><li>run_01: same claim</li><li>run_02: same claim</li></ul>"));

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject repetitive Run Audit claims",
                packageRoot,
                "Collapsed Run Audit to repeated bookkeeping instead of distinct business claims.",
                "Expected violation: report.run-audit.claims-too-repetitive.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.run-audit.claims-too-repetitive");
        StepLogger.pass("Validator rejected repetitive run audit");
    }

    @Test
    public void rejectsPackageWhenCloseOutDoesNotNameConcreteNextBusinessObject() throws IOException {
        Path projectRoot = projectRoot();
        Path packageRoot = cloneLatestPackage();
        mutateReport(packageRoot, html -> replaceDetailsSection(html, "Close-Out",
                "<p class=\"closeout-summary\">Continue later with a valuable flow.</p>"));

        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);

        logValidatorScenario(
                "Reject generic Close-Out next step",
                packageRoot,
                "Changed Close-Out to generic continuation without a named business object.",
                "Expected violation: report.close-out.next-object-missing.",
                "Asserted artifact: analysis/langgraph-business-understanding.html.",
                result);
        requireCode(result, "report.close-out.next-object-missing");
        StepLogger.pass("Validator rejected generic Close-Out");
    }
}
