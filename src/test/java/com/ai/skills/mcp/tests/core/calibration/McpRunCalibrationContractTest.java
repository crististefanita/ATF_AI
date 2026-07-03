package com.ai.skills.mcp.tests.core.calibration;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpViolationSeverity;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import com.ai.skills.mcp.rules.packageaudit.closeout.FullMcpGateRule;
import com.ai.skills.mcp.rules.packageaudit.ReadmeStateSyncRule;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.publishCleanCloseoutEvidence;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.publishCompleteMcpExtent;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.publishCompleteMcpExtentWithSkippedPackageAuditClass;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.publishFinalReportWithoutSkippedMcpSummary;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.publishGateXmlAndLedger;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.publishGateXmlAndLedgerWithoutSkipDisclosure;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.publishPackageState;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.rewriteMcpGateCommand;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.rewriteMcpOriginCommand;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationFixtures.temporaryContext;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-run-calibration-contract-test")
public class McpRunCalibrationContractTest {

    @AfterMethod(alwaysRun = true)
    public void restoreFrozenGateProperties() {
        System.clearProperty(FullMcpGateRule.REQUIRE_FROZEN_EVIDENCE_PROPERTY);
        System.clearProperty(FullMcpGateRule.ALLOW_PHASE_ONE_PENDING_EVIDENCE_PROPERTY);
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.missing-frozen-evidence-blocked")
    public void fullMcpGateBlocksMissingFrozenEvidenceByDefault() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.gate-result.missing".equals(violation.code())
                                && violation.severity() == McpViolationSeverity.BLOCKER),
                "Default final validation must block missing frozen MCP evidence.");
    }

    @Test
    @McpCheckRef("mcp.gate.two-phase.phase1-explicit-warning")
    public void fullMcpGateAllowsPhaseOneOnlyWhenExplicitlyEnabled() throws IOException {
        System.setProperty(FullMcpGateRule.ALLOW_PHASE_ONE_PENDING_EVIDENCE_PROPERTY, "true");
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);

        new FullMcpGateRule().validate(context, result);

        Assert.assertFalse(result.hasBlockers(), "Explicit phase-1 validation may record pending frozen evidence as a warning.");
        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.gate-result.pending-freeze".equals(violation.code())
                                && violation.severity() == McpViolationSeverity.WARNING),
                "Explicit phase-1 mode should keep missing frozen evidence visible as a warning.");
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.required-overrides-phase1")
    public void frozenEvidenceRequiredOverridesExplicitPhaseOneFlag() throws IOException {
        System.setProperty(FullMcpGateRule.ALLOW_PHASE_ONE_PENDING_EVIDENCE_PROPERTY, "true");
        System.setProperty(FullMcpGateRule.REQUIRE_FROZEN_EVIDENCE_PROPERTY, "true");
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.gate-result.missing".equals(violation.code())
                                && violation.severity() == McpViolationSeverity.BLOCKER),
                "Required frozen-evidence mode must block even if the phase-1 flag is set.");
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.extent-required")
    public void fullMcpGateBlocksPassWhenCompleteExtentReportIsMissing() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.gate-result.complete-artifacts-missing".equals(violation.code())
                                && violation.message().contains("Extent report")),
                "MCP PASS must block when " + McpArtifactPaths.MCP_EXTENT_REPORT + " is missing.");
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.extent-substantive")
    public void fullMcpGateRejectsToyMcpExtentReport() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());
        Path extent = context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_REPORT);
        Files.writeString(extent, "<html><body>MCP PASS</body></html>");

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.extent-report.incomplete".equals(violation.code())),
                "Toy MCP Extent HTML must not satisfy final close-out.");
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.extent-hash-provenance")
    public void fullMcpGateRejectsMcpExtentHashMismatch() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());
        publishCompleteMcpExtent(context.packageRoot(), 78, 99);
        Files.writeString(context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_SHA256),
                "0".repeat(64) + "  ExtentReport.html\n");

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.extent.hash-mismatch".equals(violation.code())),
                "MCP Extent hash mismatch must block final close-out.");
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.counts-cross-check")
    public void fullMcpGateRejectsMcpExtentXmlLedgerCountMismatch() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());
        publishCompleteMcpExtent(context.packageRoot(), 77, 100);

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.evidence-counts.mismatch".equals(violation.code())),
                "MCP Extent/XML/ledger count drift must block final close-out.");
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.summary-visible-in-final-report")
    public void fullMcpGateRejectsCleanCloseoutWhenFinalReportDoesNotNameSubsetAndCounts() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishCleanCloseoutEvidence(context.packageRoot(), false);

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "report.mcp-closeout-summary.missing".equals(violation.code())),
                "Clean MCP close-out still needs a visible final-report subset/count summary.");
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.skipped-profile-disclosure-required")
    public void fullMcpGateRejectsSkippedMcpWithoutProfileDisclosure() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedgerWithoutSkipDisclosure(context.packageRoot());
        publishCompleteMcpExtent(context.packageRoot(), 78, 99);

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.skipped-profile.unexplained".equals(violation.code())),
                "Skipped MCP close-out evidence must explain profile/class groups.");
    }

    @Test
    @McpCheckRef("mcp.gate.full-sweep.skipped-profile-disclosed-secondary-only")
    public void fullMcpGateAcceptsSkippedMcpWhenProfileDisclosureIsPublished() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());
        publishCompleteMcpExtent(context.packageRoot(), 78, 99);

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.gate-result.not-package-closeout".equals(violation.code())
                                || "mcp.extent.origin.not-package-closeout".equals(violation.code())
                || "mcp.extent.origin.not-full-mcp-suite".equals(violation.code())),
                "Explained full-sweep skips may be health evidence, but final close-out must come from "
                        + McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName() + ": "
                        + result.violations());
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.skipped-profile-visible-in-final-report")
    public void fullMcpGateRejectsSkippedMcpWhenFinalReportDoesNotExplainIt() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());
        publishCompleteMcpExtent(context.packageRoot(), 78, 99);
        publishFinalReportWithoutSkippedMcpSummary(context.packageRoot());

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "report.mcp-skipped-explanation.missing".equals(violation.code())),
                "Accepted MCP skips must be visible in final report, not only in ledger.");
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.skipped-package-class-named")
    public void fullMcpGateRejectsSkippedPackageAuditClassWhenLedgerDoesNotNameIt() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());
        publishCompleteMcpExtentWithSkippedPackageAuditClass(context.packageRoot(), 78, 99);

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.skipped-profile.class-missing".equals(violation.code())),
                "Skipped McpLatestPackage* class names must be published in the ledger.");
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.skipped-cap-required")
    public void fullMcpGateRejectsAcceptedSkipsWhenPackageCapIsTooHigh() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());
        publishPackageState(context.packageRoot(), 95);
        publishCompleteMcpExtent(context.packageRoot(), 78, 99);

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.skipped-profile.cap-missing".equals(violation.code())),
                "Accepted skipped MCP methods must keep package cap below 95.");
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.ledger-placeholders-rejected")
    public void fullMcpGateRejectsMethodLedgerPlaceholders() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());
        Files.writeString(context.packageRoot().resolve(McpArtifactPaths.MCP_METHOD_LEDGER),
                Files.readString(context.packageRoot().resolve(McpArtifactPaths.MCP_METHOD_LEDGER))
                        + "\n- $class.$method placeholder\n");
        publishCompleteMcpExtent(context.packageRoot(), 78, 99);

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.method-ledger.placeholder".equals(violation.code())),
                "Unresolved ledger placeholders must block final close-out.");
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.skip-reason-command-drift")
    public void fullMcpGateRejectsPackageRootCommandContradiction() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());
        Files.writeString(context.packageRoot().resolve(McpArtifactPaths.MCP_TESTNG_XML),
                "<testsuite name=\"MCP\" tests=\"177\" failures=\"0\" errors=\"0\" skipped=\"99\">"
                        + "<system-out>MCP package-audit skipped: missing -Dmcp.package.root</system-out>"
                        + "</testsuite>\n");
        publishCompleteMcpExtent(context.packageRoot(), 78, 99);

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.skip-reason.command-contradiction".equals(violation.code())),
                "Contradictory command vs skip reason must block final close-out.");
    }

    @Test
    @McpCheckRef("mcp.gate.full-sweep.skip-reason-not-command-proof")
    public void fullMcpGateDoesNotTreatMissingRootSkipTextAsCommandProof() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());
        publishCompleteMcpExtent(context.packageRoot(), 78, 99);
        rewriteMcpOriginCommand(context.packageRoot(),
                McpExecutionProfile.FULL.mavenCommand());
        rewriteMcpGateCommand(context.packageRoot(),
                McpExecutionProfile.FULL.mavenCommand());
        Files.writeString(context.packageRoot().resolve(McpArtifactPaths.MCP_TESTNG_XML),
                "<testsuite name=\"MCP\" tests=\"177\" failures=\"0\" errors=\"0\" skipped=\"99\">"
                        + "<system-out>MCP package-audit skipped: missing -Dmcp.package.root</system-out>"
                        + "</testsuite>\n");

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .noneMatch(violation -> "mcp.skip-reason.command-contradiction".equals(violation.code())),
                "Missing-root skip text alone must not be interpreted as proof that the Maven command received -Dmcp.package.root: "
                        + result.violations());
        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.gate-result.not-package-closeout".equals(violation.code())
                                || "mcp.extent.origin.not-package-closeout".equals(violation.code())
                                || "mcp.extent.origin.not-full-mcp-suite".equals(violation.code())),
                "Accepted full MCP sweep skips remain health evidence, not final validation/mcp close-out evidence: "
                        + result.violations());
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.xml-profile-required")
    public void fullMcpGateRejectsFullSuiteXmlAsFinalCloseoutEvidence() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishCleanCloseoutEvidence(context.packageRoot(), true);
        Files.writeString(context.packageRoot().resolve(McpArtifactPaths.MCP_TESTNG_XML),
                "<testsuite name=\"MCP Full Gate\" tests=\"89\" failures=\"0\" errors=\"0\" skipped=\"0\">"
                        + "<properties><property name=\"surefire.suiteXmlFiles\" "
                        + "value=\"" + McpExecutionProfile.FULL.suiteXml() + "\"/></properties>"
                        + "</testsuite>\n");

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.xml.not-package-closeout".equals(violation.code())),
                "Full-suite XML must not be accepted as final validation/mcp close-out evidence: "
                        + result.violations());
    }

    @Test
    @McpCheckRef("mcp.gate.package-closeout.skipped-forbidden")
    public void fullMcpGateRejectsCloseoutSkipsAboveHardCap() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());
        publishCompleteMcpExtent(context.packageRoot(), 78, 99);
        rewriteMcpOriginCommand(context.packageRoot(),
                "mvn -q -Dsurefire.suiteXmlFiles=" + McpExecutionProfile.PACKAGE_CLOSEOUT.suiteXml() + " "
                        + "-Dmcp.package.root=" + context.packageRoot() + " test");

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "mcp.closeout.skipped-profile.forbidden".equals(violation.code())),
                "Package close-out with skipped methods must be blocked regardless of cap: " + result.violations());
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.origin-command-visible")
    public void finalReportMustPublishAuditedMcpExtentOriginCommand() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishCleanCloseoutEvidence(context.packageRoot(), true);
        Path report = context.packageRoot().resolve(McpArtifactPaths.FINAL_REPORT);
        String commandText = "MCP Maven command for ExtentReport.html: "
                + "mvn -q -Dsurefire.suiteXmlFiles=" + McpExecutionProfile.PACKAGE_CLOSEOUT.suiteXml() + " "
                + "-Dmcp.package.root=" + context.packageRoot() + " test. ";
        Files.writeString(report, Files.readString(report).replace(commandText,
                "MCP Extent evidence is linked but the command is not published. "));

        new FullMcpGateRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "report.mcp-origin-command.missing".equals(violation.code())),
                "Final report must expose the audited Maven command from "
                        + McpArtifactPaths.MCP_EXTENT_ORIGIN + ": "
                        + result.violations());
    }

    @Test
    @McpCheckRef("mcp.gate.final-closeout.readme-links-required")
    public void readmeMustLinkFinalMcpCloseoutEvidence() throws IOException {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = temporaryContext(result);
        publishGateXmlAndLedger(context.packageRoot());
        Files.writeString(context.packageRoot().resolve(McpArtifactPaths.README),
                "# Package\n\nRuns completed: 5. Tests produced: 25. MCP PASS.\n");

        new ReadmeStateSyncRule().validate(context, result);

        Assert.assertTrue(result.violations().stream()
                        .anyMatch(violation -> "readme.mcp-evidence-link.missing".equals(violation.code())),
                "README without final MCP artifact links must be blocked.");
    }

}
