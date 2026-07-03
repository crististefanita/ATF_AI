package com.ai.skills.mcp.tests.negative.run;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contract.RequiresArtifacts;
import com.ai.skills.mcp.contract.ValidatesMcpContract;
import com.ai.skills.mcp.rules.run.RunExtentProofRule;
import com.ai.skills.mcp.rules.run.RunExtentProvenanceRule;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.skills.mcp.tests.support.McpRunNegativeSupport.firstExtentReport;
import static com.ai.skills.mcp.tests.support.McpRunNegativeSupport.extentOrigin;
import static com.ai.skills.mcp.tests.support.McpRunNegativeSupport.extentSha256;
import static com.ai.skills.mcp.tests.support.McpRunNegativeSupport.extentSha256Line;
import static com.ai.skills.mcp.tests.support.McpRunNegativeSupport.sha256;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.cloneLatestPackageFirstRunOnly;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.write;
import static com.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.ai.skills.mcp.validation.McpViolationValidation.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("run-extent-negative-contracts")
@RequiresArtifacts({
        "runs/run_##/validation/reports/ExtentReport.html",
        "runs/run_##/validation/reports/ExtentReport.sha256",
        "runs/run_##/validation/reports/extent-origin.json"
})
@McpSuiteRef("mcp.suite.mcp-run-extent-negative-contract-test")
public class McpRunExtentNegativeContractTest {
    private static final RunExtentProofRule EXTENT_PROOF_RULE = new RunExtentProofRule();
    private static final RunExtentProvenanceRule EXTENT_PROVENANCE_RULE = new RunExtentProvenanceRule();

    @Test
    @ValidatesMcpContract("run.extent.maven-provenance")
    @McpCheckRef("run.extent.manual-proof-injection.negative")
    public void rejectsPackageWhenExtentReportIsAlteredAfterMavenOutput() throws IOException {
        Path packageRoot = cloneLatestPackageFirstRunOnly();
        Path extentReport = firstExtentReport(packageRoot);
        String alteredHtml = Files.readString(extentReport)
                + "\n<!-- manual proof injection: Business Flow Graph / Score Overview / Proof Mapping -->\n";
        write(extentReport, alteredHtml);

        McpPackageValidationResult result = validateWithRules(packageRoot, EXTENT_PROVENANCE_RULE);

        logValidatorScenario(
                "Reject altered Extent report",
                packageRoot,
                "Appended final-report proof vocabulary into a frozen Extent artifact after the run.",
                "Expected violations: run.extent.manual-proof-injection and/or hash provenance blockers.",
                "Asserted artifacts: runs/run_##/validation/reports/ExtentReport.html + sha256/origin chain.",
                result);
        requireCode(result, "run.extent.manual-proof-injection");
    }

    @Test
    @ValidatesMcpContract("run.extent.no-post-run-audit-injection")
    @McpCheckRef("run.extent.post-run-audit-injection.negative")
    public void rejectsPackageWhenExtentReportWasRehashedAfterAuditPanelInjection() throws IOException {
        Path packageRoot = cloneLatestPackageFirstRunOnly();
        Path extentReport = firstExtentReport(packageRoot);
        String alteredHtml = Files.readString(extentReport)
                + "\n<section>ATF Runtime Tag Audit</section>"
                + "\n<section>Failure signatures</section>"
                + "\n<!-- post-run report repair -->\n";
        write(extentReport, alteredHtml);
        String alteredHash = sha256(extentReport);
        write(extentSha256(extentReport), extentSha256Line(alteredHash));
        write(extentOrigin(extentReport),
                "{\n"
                        + "  \"command\": \"mvn -q test -Dtest=PostRunCosmeticRepairFixture\",\n"
                        + "  \"source\": \"" + extentReport.toString().replace("\\", "\\\\") + "\",\n"
                        + "  \"sha256\": \"" + alteredHash + "\",\n"
                        + "  \"timestamp\": \"2026-06-27T19:00:00\",\n"
                        + "  \"generated_at\": \"2026-06-27T19:00:00\",\n"
                        + "  \"exit\": 0,\n"
                        + "  \"size\": " + Files.size(extentReport) + "\n"
                        + "}\n");

        McpPackageValidationResult result = validateWithRules(packageRoot, EXTENT_PROVENANCE_RULE);

        logValidatorScenario(
                "Reject rehashed Extent audit injection",
                packageRoot,
                "Injected runtime-looking audit panels plus an explicit post-run repair marker, then recomputed sha256/origin.",
                "Expected violation: run.extent.manual-proof-injection. Reason: explicit repair marker, not framework runtime audit.",
                "Asserted artifacts: ExtentReport.html, ExtentReport.sha256, extent-origin.json.",
                result);
        requireCode(result, "run.extent.manual-proof-injection");
    }

    @Test
    @ValidatesMcpContract("run.extent.origin-post-run-edit")
    @McpCheckRef("run.extent.origin-post-run-edit.negative")
    public void rejectsPackageWhenOriginDeclaresAgentEditedExtentEvenWithConsistentHash() throws IOException {
        Path packageRoot = cloneLatestPackageFirstRunOnly();
        Path extentReport = firstExtentReport(packageRoot);
        String alteredHtml = Files.readString(extentReport)
                + "\n<!-- harmless looking whitespace normalization marker -->\n";
        write(extentReport, alteredHtml);
        String alteredHash = sha256(extentReport);
        write(extentSha256(extentReport), extentSha256Line(alteredHash));
        write(extentOrigin(extentReport),
                "{\n"
                        + "  \"artifact\": \"ExtentReport.html\",\n"
                        + "  \"command\": \"mvn \\\"-Dtest=SupportAttachmentPolicyTests\\\" \\\"-DforkCount=0\\\" test\",\n"
                        + "  \"source_command\": \"mvn \\\"-Dtest=SupportAttachmentPolicyTests\\\" \\\"-DforkCount=0\\\" test\",\n"
                        + "  \"timestamp\": \"2026-06-27T18:16:00+03:00\",\n"
                        + "  \"finished_at\": \"2026-06-27T18:18:09+03:00\",\n"
                        + "  \"exit\": 0,\n"
                        + "  \"exit_code\": 0,\n"
                        + "  \"size\": " + Files.size(extentReport) + ",\n"
                        + "  \"sha256\": \"" + alteredHash + "\",\n"
                        + "  \"generated_by\": \"Maven/TestNG Extent reporter\",\n"
                        + "  \"edited_by_agent\": true,\n"
                        + "  \"edit_reason\": \"Removed framework runtime audit overlay tokens from per-run execution evidence\"\n"
                        + "}\n");

        McpPackageValidationResult result = validateWithRules(packageRoot, EXTENT_PROVENANCE_RULE);

        logValidatorScenario(
                "Reject Extent origin that admits agent edit",
                packageRoot,
                "Changed Extent bytes, recomputed sha/origin consistently, and declared edited_by_agent=true like a post-run tamper failure mode.",
                "Expected violation: run.extent.origin-post-run-edit even when hash and origin SHA match the edited file.",
                "Asserted artifacts: ExtentReport.html, ExtentReport.sha256, extent-origin.json.",
                result);
        requireCode(result, "run.extent.origin-post-run-edit");
    }

    @Test
    @ValidatesMcpContract("run.extent.native-testlistener-provenance")
    @McpCheckRef("run.extent.origin-post-run-writer.negative")
    public void rejectsPackageWhenExtentOriginUsesPostRunExecutionProofWriter() throws IOException {
        Path packageRoot = cloneLatestPackageFirstRunOnly();
        Path extentReport = firstExtentReport(packageRoot);
        String hash = sha256(extentReport);
        write(extentOrigin(extentReport),
                "{\n"
                        + "  \"command\": \"mvn -q -DskipTests test-compile exec:java -Dexec.classpathScope=test "
                        + "-Dexec.mainClass=com.ai.ui.tests.support.ExtentExecutionProofWriter\",\n"
                        + "  \"timestamp\": \"2026-06-28T11:25:44+03:00\",\n"
                        + "  \"exit\": 0,\n"
                        + "  \"artifact\": \"ExtentReport.html\",\n"
                        + "  \"sha256\": \"" + hash + "\",\n"
                        + "  \"size\": " + Files.size(extentReport) + ",\n"
                        + "  \"generator\": \"com.ai.ui.tests.support.ExtentExecutionProofWriter\"\n"
                        + "}\n");

        McpPackageValidationResult result = validateWithRules(packageRoot, EXTENT_PROVENANCE_RULE);

        logValidatorScenario(
                "Reject Extent origin produced by post-run writer",
                packageRoot,
                "Replaced extent-origin.json with a post-run exec:java ExtentExecutionProofWriter command while keeping a valid report hash.",
                "Expected violation: run.extent.origin.post-run-writer.",
                "Asserted artifacts: ExtentReport.html and extent-origin.json.",
                result);
        requireCode(result, "run.extent.origin.post-run-writer");
    }

    @Test
    @ValidatesMcpContract("run.extent.native-testlistener-provenance")
    @McpCheckRef("run.extent.native-spark-shell.negative")
    public void rejectsPackageWhenExtentReportOnlyImitatesSparkShell() throws IOException {
        Path packageRoot = cloneLatestPackageFirstRunOnly();
        Path extentReport = firstExtentReport(packageRoot);
        String fakeImage = "data:image/png;base64," + "A".repeat(900);
        String fakeSparkHtml = "<html><head><style id=\"spark-style.css\">"
                + ".dashboard-view{}.test-view{}.extent-framework{}</style></head>"
                + "<body><main class=\"extent-framework\">"
                + "<section class=\"dashboard-view\">status duration step details expected actual log</section>"
                + "<section class=\"test-view\">PASS PASS execution detail</section>"
                + "<img class=\"final-state-proof\" alt=\"Final state UI proof screenshot\" src=\"" + fakeImage + "\">"
                + "<p>" + "execution detail status duration expected actual log ".repeat(400) + "</p>"
                + "</main></body></html>";
        write(extentReport, fakeSparkHtml);
        String hash = sha256(extentReport);
        write(extentSha256(extentReport), extentSha256Line(hash));
        write(extentOrigin(extentReport),
                "{\n"
                        + "  \"command\": \"mvn test\",\n"
                        + "  \"timestamp\": \"2026-06-28T11:25:44+03:00\",\n"
                        + "  \"exit\": 0,\n"
                        + "  \"artifact\": \"ExtentReport.html\",\n"
                        + "  \"sha256\": \"" + hash + "\",\n"
                        + "  \"size\": " + Files.size(extentReport) + "\n"
                        + "}\n");

        McpPackageValidationResult result = validateWithRules(packageRoot, EXTENT_PROOF_RULE, EXTENT_PROVENANCE_RULE);

        logValidatorScenario(
                "Reject fake Spark shell",
                packageRoot,
                "Replaced ExtentReport.html with a custom proof page that copies weak Spark token names but lacks the native Spark shell.",
                "Expected violation: run.extent.native-spark-shell.missing.",
                "Asserted artifacts: ExtentReport.html, ExtentReport.sha256 and extent-origin.json.",
                result);
        requireCode(result, "run.extent.native-spark-shell.missing");
    }

    @Test
    @ValidatesMcpContract("run.extent.hash-mismatch")
    @McpCheckRef("run.extent.hash-mismatch.negative")
    public void rejectsPackageWhenExtentHashSnapshotDoesNotMatchReport() throws IOException {
        Path packageRoot = cloneLatestPackageFirstRunOnly();
        write(packageRoot.resolve("runs/run_01/validation/reports/ExtentReport.sha256"),
                "0000000000000000000000000000000000000000000000000000000000000000  ExtentReport.html\n");

        McpPackageValidationResult result = validateWithRules(packageRoot, EXTENT_PROVENANCE_RULE);

        logValidatorScenario(
                "Reject Extent hash mismatch",
                packageRoot,
                "Published a SHA-256 snapshot that cannot match the canonical run_01 ExtentReport.html.",
                "Expected violation: run.extent.hash-mismatch.",
                "Asserted artifacts: runs/run_01/validation/reports/ExtentReport.html + ExtentReport.sha256.",
                result);
        requireCode(result, "run.extent.hash-mismatch");
    }
}
