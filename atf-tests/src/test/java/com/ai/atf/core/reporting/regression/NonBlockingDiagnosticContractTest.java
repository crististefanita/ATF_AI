package com.ai.atf.core.reporting.regression;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.listener.internal.runtime.audit.RuntimeAuditFailureReporter;
import com.ai.core.reporting.diagnostic.NonBlockingDiagnostic;
import com.ai.core.reporting.diagnostic.NonBlockingWarning;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.TestStatus;
import com.ai.core.reporting.utils.ConsoleLogger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.NON_BLOCKING_DIAGNOSTIC_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.NON_BLOCKING_DIAGNOSTIC_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class NonBlockingDiagnosticContractTest {
    private static final String TARGET_REPORT_HTML = "target/report.html";

    @AfterMethod(alwaysRun = true)
    public void resetDiagnostics() {
        NonBlockingDiagnostic.clear();
        NonBlockingDiagnostic.resetSinkForTests();
        ConsoleLogger.resetSinkForTests();
        ReportingManager.reset();
    }

    @Test
    public void customSinkReceivesStructuredWarningAndBufferKeepsMessage() {
        List<NonBlockingWarning> received = new ArrayList<>();
        NonBlockingDiagnostic.setSinkForTests(received::add);

        NonBlockingDiagnostic.warn("contract diagnostic", new IllegalStateException("not fatal"));

        Assert.assertEquals(received.size(), 1,
                "The configured diagnostic sink must receive one structured warning.");
        Assert.assertEquals(received.get(0).severity(), NonBlockingWarning.Severity.WARN);
        Assert.assertEquals(received.get(0).operation(), "contract diagnostic");
        Assert.assertEquals(received.get(0).failureType(), "IllegalStateException");
        Assert.assertEquals(received.get(0).exceptionClass(), "IllegalStateException");
        Assert.assertTrue(received.get(0).message().contains("not fatal"));
        Assert.assertEquals(NonBlockingDiagnostic.warnings(), List.of(received.get(0).message()),
                "The in-memory warning ledger must preserve the published warning text.");
        Assert.assertEquals(NonBlockingDiagnostic.warningEvents(), List.of(received.get(0)),
                "The structured warning ledger must preserve the full diagnostic event.");
    }

    @Test
    public void structuredWarningCarriesComponentArtifactAndRepairHintForRuntimeAuditability() {
        List<NonBlockingWarning> received = new ArrayList<>();
        NonBlockingDiagnostic.setSinkForTests(received::add);

        NonBlockingDiagnostic.warn(
                "runtime Extent audit update",
                new IllegalStateException("locked report"),
                "ExtentRuntimeAudit",
                TARGET_REPORT_HTML,
                "Release file handles before post-processing the Extent report."
        );

        NonBlockingWarning warning = received.get(0);
        Assert.assertEquals(warning.severity(), NonBlockingWarning.Severity.WARN);
        Assert.assertEquals(warning.component(), "ExtentRuntimeAudit");
        Assert.assertEquals(warning.artifact(), TARGET_REPORT_HTML);
        Assert.assertEquals(warning.repairHint(),
                "Release file handles before post-processing the Extent report.");
        Assert.assertEquals(warning.exceptionClass(), "IllegalStateException");
        Assert.assertTrue(warning.message().contains("runtime Extent audit update"));
    }

    @Test
    public void structuredWarningBehavesLikeAValueObject() {
        List<NonBlockingWarning> received = new ArrayList<>();
        NonBlockingDiagnostic.setSinkForTests(received::add);

        NonBlockingDiagnostic.warn("value diagnostic", new IllegalStateException("same"));
        NonBlockingDiagnostic.warn("value diagnostic", new IllegalStateException("same"));

        Assert.assertEquals(received.size(), 2);
        Assert.assertNotSame(received.get(0), received.get(1),
                "The two events must be different instances so equality is not accidental identity.");
        Assert.assertEquals(received.get(0), received.get(1),
                "Structured diagnostics must compare by value.");
        Assert.assertEquals(received.get(0).hashCode(), received.get(1).hashCode(),
                "Equal structured diagnostics must have equal hash codes.");
    }

    @Test
    public void structuredWarningNormalizesNullInputsWithoutNullText() {
        List<NonBlockingWarning> received = new ArrayList<>();
        NonBlockingDiagnostic.setSinkForTests(received::add);

        NonBlockingDiagnostic.warn(null, (Throwable) null);
        NonBlockingDiagnostic.warn(null, (String) null);

        Assert.assertEquals(received.size(), 2);
        for (NonBlockingWarning warning : received) {
            Assert.assertNotNull(warning.operation());
            Assert.assertNotNull(warning.failureMessage());
            Assert.assertNotNull(warning.message());
            Assert.assertFalse(warning.message().contains("null"),
                    "Warning output must normalize null values instead of leaking literal null text.");
        }
        Assert.assertEquals(received.get(0).exceptionClass(), "IllegalStateException");
        Assert.assertEquals(received.get(1).exceptionClass(), "Diagnostic");
    }

    @Test
    public void runtimeAuditFailureReporterCapturesLinkageErrorsAsNonBlockingDiagnostics() {
        List<NonBlockingWarning> received = new ArrayList<>();
        NonBlockingDiagnostic.setSinkForTests(received::add);

        RuntimeAuditFailureReporter.report("runtime Extent audit update", new LinkageError("classpath drift"));

        Assert.assertEquals(received.size(), 1);
        NonBlockingWarning warning = received.get(0);
        Assert.assertEquals(warning.component(), "ExtentRuntimeAudit");
        Assert.assertEquals(warning.artifact(), "Extent Spark report");
        Assert.assertEquals(warning.exceptionClass(), "LinkageError");
        Assert.assertTrue(warning.message().contains("classpath drift"));
        Assert.assertFalse(warning.repairHint().isBlank(),
                "Runtime audit failures must include a repair hint for artifact honesty diagnostics.");
    }

    @Test
    public void defaultSinkPublishesWarningsIntoActiveReportLogger() {
        RecordingLogger logger = new RecordingLogger();
        List<String> console = new ArrayList<>();
        ConsoleLogger.setSinkForTests(console::add);
        ReportingManager.setLoggerForTests(logger);

        NonBlockingDiagnostic.warn("report-aware diagnostic", new IllegalArgumentException("visible"));

        Assert.assertEquals(logger.details.size(), 1,
                "The default diagnostic sink must publish warning details into the active report logger.");
        Assert.assertTrue(logger.details.get(0).contains("ATF warning: report-aware diagnostic"));
        Assert.assertEquals(console.size(), 1,
                "The default diagnostic sink must also publish one formatted console warning.");
        Assert.assertTrue(console.get(0).startsWith("  WARN: ATF warning: report-aware diagnostic"),
                "Console warning must use the ATF step-output format.");
        Assert.assertTrue(NonBlockingDiagnostic.warnings().get(0).contains("visible"));
    }

    @Test
    public void defaultSinkKeepsWarningsInMemoryAndConsoleWhenNoReportLoggerIsActive() {
        List<String> console = new ArrayList<>();
        ConsoleLogger.setSinkForTests(console::add);

        NonBlockingDiagnostic.warn("headless diagnostic", "visible with console");

        Assert.assertEquals(NonBlockingDiagnostic.warnings().size(), 1,
                "A headless diagnostic must still be recorded in the in-memory warning ledger.");
        Assert.assertTrue(NonBlockingDiagnostic.warnings().get(0).contains("visible with console"),
                "Headless diagnostics must stay auditable in memory.");
        Assert.assertEquals(console.size(), 1,
                "A headless diagnostic must also be published to the formatted ATF console sink.");
        Assert.assertTrue(console.get(0).contains("visible with console"),
                "The console message must preserve the diagnostic detail.");
    }

    private static final class RecordingLogger implements ReportLogger {
        private final List<String> details = new ArrayList<>();

        @Override
        public void startTest(String testName, String description) {
        }

        @Override
        public void endTest(TestStatus status) {
        }

        @Override
        public void startStep(String stepTitle) {
        }

        @Override
        public void logDetail(String detail) {
            details.add(detail);
        }

        @Override
        public void pass(String message) {
        }

        @Override
        public void fail(String message, String stacktraceAsText) {
        }

        @Override
        public void attachScreenshotBase64(String base64, String title) {
        }

        @Override
        public void logCodeBlock(String content) {
        }

        @Override
        public void flush() {
        }
    }
}
