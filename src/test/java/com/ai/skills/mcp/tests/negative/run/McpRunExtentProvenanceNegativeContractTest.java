package com.ai.skills.mcp.tests.negative.run;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contract.RequiresArtifacts;
import com.ai.skills.mcp.contract.ValidatesMcpContract;
import com.ai.skills.mcp.rules.run.RunExtentProvenanceRule;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.skills.mcp.tests.support.McpRunNegativeSupport.firstExtentReport;
import static com.ai.skills.mcp.tests.support.McpRunNegativeSupport.extentOrigin;
import static com.ai.skills.mcp.tests.support.McpRunNegativeSupport.sha256;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.cloneLatestPackageFirstRunOnly;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.validateWithRules;
import static com.ai.skills.mcp.tests.support.McpNegativeFixture.write;
import static com.ai.skills.mcp.validation.McpNegativeScenarioValidation.logValidatorScenario;
import static com.ai.skills.mcp.validation.McpViolationValidation.requireCode;

@Listeners(TestListener.class)
@ValidatesMcpContract("run-extent-provenance-negative-contracts")
@RequiresArtifacts({
        "runs/run_##/validation/reports/ExtentReport.html",
        "runs/run_##/validation/reports/ExtentReport.sha256",
        "runs/run_##/validation/reports/extent-origin.json"
})
@McpSuiteRef("mcp.suite.mcp-run-extent-provenance-negative-contract-test")
public class McpRunExtentProvenanceNegativeContractTest {
    private static final RunExtentProvenanceRule EXTENT_PROVENANCE_RULE = new RunExtentProvenanceRule();

    @Test
    @ValidatesMcpContract("run.extent.origin-sha256")
    @McpCheckRef("run.extent.origin-sha256.invalid.negative")
    public void rejectsOriginWithoutValidSha256() throws IOException {
        Path packageRoot = cloneLatestPackageFirstRunOnly();
        Path extentReport = firstExtentReport(packageRoot);
        writeOrigin(extentReport, "\"not-a-sha256\"", Files.size(extentReport), 0,
                "2026-06-28T11:25:44+03:00", "mvn -q -Dtest=PositiveUserTests test");

        McpPackageValidationResult result = validateWithRules(packageRoot, EXTENT_PROVENANCE_RULE);

        logValidatorScenario(
                "Reject Extent origin without valid SHA-256",
                packageRoot,
                "Replaced extent-origin.json sha256 with a non-hash value while keeping the report present.",
                "Expected violation: run.extent.origin-sha256.invalid.",
                "Asserted artifacts: extent-origin.json + ExtentReport.html.",
                result);
        requireCode(result, "run.extent.origin-sha256.invalid");
    }

    @Test
    @ValidatesMcpContract("run.extent.origin-size")
    @McpCheckRef("run.extent.origin-size-mismatch.negative")
    public void rejectsOriginSizeThatDoesNotMatchReportBytes() throws IOException {
        Path packageRoot = cloneLatestPackageFirstRunOnly();
        Path extentReport = firstExtentReport(packageRoot);
        writeOrigin(extentReport, "\"" + sha256(extentReport) + "\"", Files.size(extentReport) + 17, 0,
                "2026-06-28T11:25:44+03:00", "mvn -q -Dtest=PositiveUserTests test");

        McpPackageValidationResult result = validateWithRules(packageRoot, EXTENT_PROVENANCE_RULE);

        logValidatorScenario(
                "Reject Extent origin size mismatch",
                packageRoot,
                "Published a correct origin SHA but a stale byte size.",
                "Expected violation: run.extent.origin-size-mismatch.",
                "Asserted artifacts: extent-origin.json + ExtentReport.html.",
                result);
        requireCode(result, "run.extent.origin-size-mismatch");
    }

    @Test
    @ValidatesMcpContract("run.extent.origin-exit")
    @McpCheckRef("run.extent.origin-exit.invalid.negative")
    public void rejectsOriginWithNonZeroExitCode() throws IOException {
        Path packageRoot = cloneLatestPackageFirstRunOnly();
        Path extentReport = firstExtentReport(packageRoot);
        writeOrigin(extentReport, "\"" + sha256(extentReport) + "\"", Files.size(extentReport), 1,
                "2026-06-28T11:25:44+03:00", "mvn -q -Dtest=PositiveUserTests test");

        McpPackageValidationResult result = validateWithRules(packageRoot, EXTENT_PROVENANCE_RULE);

        logValidatorScenario(
                "Reject Extent origin non-zero exit",
                packageRoot,
                "Marked Maven test execution as exit=1 in extent-origin.json.",
                "Expected violation: run.extent.origin-exit.invalid.",
                "Asserted artifacts: extent-origin.json.",
                result);
        requireCode(result, "run.extent.origin-exit.invalid");
    }

    @Test
    @ValidatesMcpContract("run.extent.origin-command")
    @McpCheckRef("run.extent.origin.not-maven-test.negative")
    public void rejectsOriginCommandThatDoesNotRunMavenTests() throws IOException {
        Path packageRoot = cloneLatestPackageFirstRunOnly();
        Path extentReport = firstExtentReport(packageRoot);
        writeOrigin(extentReport, "\"" + sha256(extentReport) + "\"", Files.size(extentReport), 0,
                "2026-06-28T11:25:44+03:00", "mvn -q test-compile");

        McpPackageValidationResult result = validateWithRules(packageRoot, EXTENT_PROVENANCE_RULE);

        logValidatorScenario(
                "Reject Extent origin compile-only command",
                packageRoot,
                "Used a Maven compile-only command as Extent provenance.",
                "Expected violation: run.extent.origin.not-maven-test.",
                "Asserted artifacts: extent-origin.json.",
                result);
        requireCode(result, "run.extent.origin.not-maven-test");
    }

    @Test
    @ValidatesMcpContract("run.extent.origin-timestamp")
    @McpCheckRef("run.extent.origin-timestamp.invalid.negative")
    public void rejectsOriginWithUnparseableTimestamp() throws IOException {
        Path packageRoot = cloneLatestPackageFirstRunOnly();
        Path extentReport = firstExtentReport(packageRoot);
        writeOrigin(extentReport, "\"" + sha256(extentReport) + "\"", Files.size(extentReport), 0,
                "after tests maybe", "mvn -q -Dtest=PositiveUserTests test");

        McpPackageValidationResult result = validateWithRules(packageRoot, EXTENT_PROVENANCE_RULE);

        logValidatorScenario(
                "Reject Extent origin invalid timestamp",
                packageRoot,
                "Replaced timestamp with free text instead of parseable freeze metadata.",
                "Expected violation: run.extent.origin-timestamp.invalid.",
                "Asserted artifacts: extent-origin.json.",
                result);
        requireCode(result, "run.extent.origin-timestamp.invalid");
    }

    private static void writeOrigin(Path extentReport,
                                    String shaLiteral,
                                    long size,
                                    int exit,
                                    String timestamp,
                                    String command) throws IOException {
        write(extentOrigin(extentReport),
                "{\n"
                        + "  \"command\": \"" + command + "\",\n"
                        + "  \"timestamp\": \"" + timestamp + "\",\n"
                        + "  \"generated_at\": \"" + timestamp + "\",\n"
                        + "  \"finished_at\": \"" + timestamp + "\",\n"
                        + "  \"exit\": " + exit + ",\n"
                        + "  \"artifact\": \"ExtentReport.html\",\n"
                        + "  \"sha256\": " + shaLiteral + ",\n"
                        + "  \"size\": " + size + "\n"
                        + "}\n");
    }
}
