package com.endava.ai.skills.mcp.tests.core.calibration;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpRunArtifacts;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.skills.mcp.provenance.McpExtentProvenanceVerifier;
import com.endava.ai.skills.mcp.validation.run.McpTestNgXmlMethods;
import com.endava.ai.skills.mcp.validation.run.extent.McpExtentReportMode;
import com.endava.ai.skills.mcp.validation.run.extent.McpExtentScreenshotEvidence;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.endava.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.nativeSparkShellHtml;
import static com.endava.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.testNgXml;
import static com.endava.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.writeReport;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-run-evidence-calibration-contract-test")
public class McpRunEvidenceCalibrationContractTest {

    @Test
    @McpCheckRef("run.extent.screenshot-native-anchor.accepted")
    public void screenshotEvidenceAcceptsNativeExtentAnchor() {
        String image = "data:image/png;base64," + "A".repeat(900);
        String html = "<a class=\"screenshot\" data-featherlight=\"image\" href=\"" + image + "\">Final state</a>";

        Assert.assertTrue(McpExtentScreenshotEvidence.containsUsefulEmbeddedScreenshot(html),
                "Native Extent anchor media should count as embedded screenshot evidence.");
    }

    @Test
    @McpCheckRef("run.extent.screenshot-img.accepted")
    public void screenshotEvidenceAcceptsEmbeddedImg() {
        String image = "data:image/png;base64," + "B".repeat(900);
        String html = "<img alt=\"Final state UI proof\" src=\"" + image + "\">";

        Assert.assertTrue(McpExtentScreenshotEvidence.containsUsefulEmbeddedScreenshot(html),
                "Embedded img data URI should remain valid screenshot evidence.");
    }

    @Test
    @McpCheckRef("run.extent.screenshot-missing.rejected")
    public void screenshotEvidenceRejectsMissingMedia() {
        String html = "<html><body><p>Execution details only</p></body></html>";

        Assert.assertFalse(McpExtentScreenshotEvidence.containsUsefulEmbeddedScreenshot(html),
                "Missing screenshot media must not satisfy UI proof.");
    }

    @Test
    @McpCheckRef("run.xml.config-methods-ignored")
    public void testNgXmlCountabilityIgnoresConfigMethods() {
        String xml = testNgXml(5, true);

        Assert.assertEquals(McpTestNgXmlMethods.passedRealMethodCount(xml), 5,
                "Five real @Test methods plus config methods must count as five.");
    }

    @Test
    @McpCheckRef("run.xml.real-method-count-drift.rejected")
    public void testNgXmlCountabilityKeepsRealMethodCountStrict() {
        String xml = testNgXml(4, true);

        Assert.assertNotEquals(McpTestNgXmlMethods.passedRealMethodCount(xml), 5,
                "Four real @Test methods must not satisfy a five-test target.");
    }

    @Test
    @McpCheckRef("run.extent.framework-runtime-audit.accepted")
    public void manualProofDetectorAllowsFrameworkRuntimeAudit() throws IOException {
        Path report = writeReport("<section>ATF Runtime Tag Audit</section>"
                + "<section>ATF Runtime Failure Groups</section>"
                + "<section>Test cases</section>");

        List<McpPackageViolation> violations = new McpExtentProvenanceVerifier()
                .validateNoManualProofInjection(report);

        Assert.assertTrue(violations.isEmpty(),
                "Framework runtime audit tokens should not be classified as manual proof injection.");
    }

    @Test
    @McpCheckRef("run.extent.manual-proof-section.rejected")
    public void manualProofDetectorRejectsInjectedFinalReportProof() throws IOException {
        Path report = writeReport("<section>Proof Mapping</section>"
                + "<section>Score Overview</section>"
                + "<section>Business Flow Graph</section>");

        List<McpPackageViolation> violations = new McpExtentProvenanceVerifier()
                .validateNoManualProofInjection(report);

        Assert.assertTrue(violations.stream()
                        .anyMatch(violation -> "run.extent.manual-proof-injection".equals(violation.code())),
                "Manual proof vocabulary must remain a blocker.");
    }

    @Test
    @McpCheckRef("run.extent.mode.component-adapter.accepted")
    public void reportModeClassifierAcceptsComponentAdapterMarker() {
        String html = "<html><body data-atf-report-mode=\"component-adapter\" data-atf-runtime-audit=\"false\">"
                + "<script id=\"ATF_EXTENT_REPORT_MODE\" type=\"application/json\">"
                + "{\"mode\":\"component-adapter\",\"runtimeAudit\":false}</script>"
                + "</body></html>";

        McpExtentReportMode mode = McpExtentReportMode.fromHtml(html);

        Assert.assertEquals(mode.mode(), McpExtentReportMode.Mode.COMPONENT_ADAPTER,
                "Component/adapter marker must be classified explicitly.");
        Assert.assertTrue(mode.consistencyViolations(
                        Path.of(McpArtifactPaths.fileName(McpRunArtifacts.EXTENT_REPORT)), html).isEmpty(),
                "Component/adapter reports with the mode marker must not require runtime audit sections.");
    }

    @Test
    @McpCheckRef("run.extent.mode.component-adapter-without-fallback.accepted")
    public void reportModeClassifierAcceptsComponentAdapterWithoutFallback() {
        String html = "<html><body data-atf-report-mode=\"component-adapter\" data-atf-runtime-audit=\"false\">"
                + "<script id=\"ATF_EXTENT_REPORT_MODE\" type=\"application/json\">"
                + "{\"mode\":\"component-adapter\",\"runtimeAudit\":false}</script>"
                + "<div class=\"dashboard-view\">Native Spark dashboard</div>"
                + "</body></html>";

        List<McpPackageViolation> violations = McpExtentReportMode.fromHtml(html)
                .consistencyViolations(Path.of(McpArtifactPaths.fileName(McpRunArtifacts.EXTENT_REPORT)), html);

        Assert.assertTrue(violations.isEmpty(),
                "Component/adapter marker without fallback must stay valid.");
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.runtime-audited-extent-required")
    public void finalMcpExtentReportRejectsComponentAdapterMode() {
        String html = "<html><body data-atf-report-mode=\"component-adapter\" data-atf-runtime-audit=\"false\">"
                + "<script id=\"ATF_EXTENT_REPORT_MODE\" type=\"application/json\">"
                + "{\"mode\":\"component-adapter\",\"runtimeAudit\":false}</script>"
                + "<div id=\"dashboard-view\" class=\"dashboard-view\">"
                + "<h6 class=\"card-title\">Test cases</h6>"
                + "<h6 class=\"card-title\">Tests Passed</h6>"
                + "<h6 class=\"card-title\">Tests Failed</h6>"
                + "<table data-atf-component-tag-table=\"classes\"></table>"
                + "<table data-atf-component-tag-table=\"packages\"></table>"
                + "<table data-atf-component-tag-table=\"annotations\"></table>"
                + "</div>"
                + "<div id=\"status-toggle\"><span class=\"atf-status-filter-label\">Status</span></div>"
                + "<script id=\"ATF_EXTENT_NAVIGATION_GUARD\">activate(visibleView());</script>"
                + "<script>function collapseCard(node){return node;} var statusGroup = {parentCount:1};</script>"
                + "</body></html>";

        List<McpPackageViolation> violations = McpExtentReportMode.fromHtml(html)
                .publishedValidationViolations(Path.of(McpArtifactPaths.MCP_EXTENT_REPORT), html);

        Assert.assertTrue(violations.stream()
                        .anyMatch(violation -> "mcp.extent.published-overview.invalid".equals(violation.code())
                                && violation.message().contains("must be runtime-audited")),
                "Final MCP Extent report must reject component-adapter mode: " + violations);
    }

    @Test
    @McpCheckRef("run.extent.mode.runtime-audited.requires-ledger")
    public void reportModeClassifierRejectsRuntimeModeWithoutAuditSections() {
        String html = "<html><body data-atf-report-mode=\"runtime-audited\" data-atf-runtime-audit=\"true\">"
                + "<script id=\"ATF_EXTENT_REPORT_MODE\" type=\"application/json\">"
                + "{\"mode\":\"runtime-audited\",\"runtimeAudit\":true}</script>"
                + "<div class=\"dashboard-view\">Native Spark dashboard</div>"
                + "</body></html>";

        List<McpPackageViolation> violations = McpExtentReportMode.fromHtml(html)
                .consistencyViolations(Path.of(McpArtifactPaths.fileName(McpRunArtifacts.EXTENT_REPORT)), html);

        Assert.assertTrue(violations.stream()
                        .anyMatch(violation -> "run.extent.mode-runtime-audit.missing".equals(violation.code())),
                "Runtime-audited marker without runtime sections must be rejected.");
    }

    @Test
    @McpCheckRef("run.extent.origin.skip-tests-false.accepted")
    public void originCommandAcceptsExplicitSkipTestsFalse() throws IOException {
        Path runDirectory = Files.createTempDirectory("mcp-origin-command-run-");
        Path report = runDirectory.resolve(McpRunArtifacts.EXTENT_REPORT);
        Files.createDirectories(report.getParent());
        Files.writeString(report, nativeSparkShellHtml());
        Files.writeString(runDirectory.resolve(McpRunArtifacts.EXTENT_ORIGIN),
                "{\n"
                        + "  \"command\": \"mvn -q -DskipTests=false -Dtest=PositiveUserTests test\",\n"
                        + "  \"timestamp\": \"2026-06-28T11:25:44+03:00\",\n"
                        + "  \"exit\": 0,\n"
                        + "  \"artifact\": \"" + McpArtifactPaths.fileName(McpRunArtifacts.EXTENT_REPORT) + "\",\n"
                        + "  \"sha256\": \""
                        + "0".repeat(64) + "\",\n"
                        + "  \"size\": " + Files.size(report) + "\n"
                        + "}\n");

        List<McpPackageViolation> violations = new McpExtentProvenanceVerifier()
                .validateNativeTestListenerProvenance(runDirectory, report);

        Assert.assertTrue(violations.stream().noneMatch(violation ->
                        "run.extent.origin.not-maven-test".equals(violation.code())
                                || "run.extent.origin.post-run-writer".equals(violation.code())),
                "skipTests=false must not be treated as disabled-test provenance: " + violations);
    }

    @Test
    @McpCheckRef("run.extent.origin-exit-code-alias.accepted")
    public void originSnapshotAcceptsCamelCaseExitCode() throws IOException {
        Path runDirectory = Files.createTempDirectory("mcp-origin-exit-code-run-");
        Path report = runDirectory.resolve(McpRunArtifacts.EXTENT_REPORT);
        Files.createDirectories(report.getParent());
        Files.writeString(report, "<html><body>native Extent bytes</body></html>");
        String hash = com.endava.ai.skills.mcp.tests.support.McpRunNegativeSupport.sha256(report);
        Files.writeString(runDirectory.resolve(McpRunArtifacts.EXTENT_SHA256),
                hash + "  " + McpArtifactPaths.fileName(McpRunArtifacts.EXTENT_REPORT) + "\n");
        Files.writeString(runDirectory.resolve(McpRunArtifacts.EXTENT_ORIGIN),
                "{\n"
                        + "  \"command\": \"mvn -q -Dtest=PositiveUserTests test\",\n"
                        + "  \"timestamp\": \"2026-06-28T11:25:44+03:00\",\n"
                        + "  \"exitCode\": 0,\n"
                        + "  \"artifact\": \"" + McpArtifactPaths.fileName(McpRunArtifacts.EXTENT_REPORT) + "\",\n"
                        + "  \"sha256\": \"" + hash + "\",\n"
                        + "  \"size\": " + Files.size(report) + "\n"
                        + "}\n");

        List<McpPackageViolation> violations = new McpExtentProvenanceVerifier()
                .validateMavenProvenance(runDirectory, report);

        Assert.assertTrue(violations.isEmpty(), "Camel-case exitCode origin metadata should be accepted: " + violations);
    }
}
