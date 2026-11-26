package com.ai.atf.core.reporting.attachment;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import org.testng.annotations.Listeners;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.adapters.ExtentAdapter;
import com.ai.core.reporting.attachment.FailureAttachmentHandler;
import com.ai.core.reporting.attachment.FailureAttachmentRegistry;
import com.ai.core.reporting.diagnostic.NonBlockingDiagnostic;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.TestListenerAdapter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.List;

@SuppressWarnings("ALL")
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.FAILURE_ATTACHMENT_REGISTRY_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.FAILURE_ATTACHMENT_REGISTRY_TEST_CLASS)
@Listeners(TestListener.class)
public class FailureAttachmentRegistryTest {

    private TestFailureHandler handler;

    @BeforeMethod
    void setUp() {
        handler = new TestFailureHandler();
        FailureAttachmentRegistry.clearForTests();
        NonBlockingDiagnostic.clear();
        ReportingManager.reset();
        FailureAttachmentRegistry.register(handler);
    }

    @AfterMethod(alwaysRun = true)
    void tearDown() {
        FailureAttachmentRegistry.clearForTests();
        NonBlockingDiagnostic.clear();
        NonBlockingDiagnostic.resetSinkForTests();
        ReportingManager.reset();
    }

    // ---------- BASIC REGISTRY CONTRACT ----------

    @Test
    public void handler_is_called_on_failure() {
        FailureAttachmentRegistry.onTestFailure(new RuntimeException("boom"));
        Assert.assertEquals(handler.calls, 1);
    }

    @Test
    public void registry_is_safe_without_handler() {
        FailureAttachmentRegistry.clearForTests();
        RuntimeException failure = new RuntimeException("boom");
        FailureAttachmentRegistry.onTestFailure(failure);

        Assert.assertSame(FailureAttachmentRegistry.getFailure(), failure,
                "Failure evidence must remain available even without an attachment handler.");
        Assert.assertEquals(handler.calls, 0, "A cleared handler must not receive callbacks.");
        Assert.assertTrue(NonBlockingDiagnostic.warnings().isEmpty(),
                "An absent optional handler must not create a diagnostic failure.");
        FailureAttachmentRegistry.onTestStart();
        Assert.assertNull(FailureAttachmentRegistry.getFailure(),
                "The next test must not inherit a previous failure.");
    }

    @Test
    public void handler_failures_are_reported_as_non_blocking_diagnostics() {
        FailureAttachmentRegistry.register(new ThrowingFailureHandler());

        FailureAttachmentRegistry.onTestStart();
        FailureAttachmentRegistry.onTestFailure(new RuntimeException("boom"));

        Assert.assertTrue(handler.starts >= 1, "Healthy handlers must still receive test-start callbacks.");
        Assert.assertTrue(handler.calls >= 1, "Healthy handlers must still receive failure callbacks.");
        Assert.assertTrue(
                NonBlockingDiagnostic.warnings().stream()
                        .anyMatch(warning -> warning.contains("failure attachment test-start hook")
                                && warning.contains("attachment start exploded")),
                "Handler start failures must be downgraded to diagnostic warnings."
        );
        Assert.assertTrue(
                NonBlockingDiagnostic.warnings().stream()
                        .anyMatch(warning -> warning.contains("failure attachment capture hook")
                                && warning.contains("attachment failure exploded")),
                "Handler failure-capture failures must be downgraded to diagnostic warnings."
        );
    }

    // Registry dispatch with an installed logger; these cases do not assert backend lifecycle integration.

    @Test
    public void registry_dispatches_failure_with_extent_logger_installed() {
        ReportingManager.setLoggerForTests(ExtentAdapter.getInstance());
        FailureAttachmentRegistry.onTestFailure(new RuntimeException("boom"));
        Assert.assertEquals(handler.calls, 1);
    }


    @Test
    public void multiple_failures_trigger_multiple_attachments() {
        ReportingManager.setLoggerForTests(ExtentAdapter.getInstance());

        FailureAttachmentRegistry.onTestFailure(new RuntimeException("boom1"));
        FailureAttachmentRegistry.onTestFailure(new RuntimeException("boom2"));

        Assert.assertEquals(handler.calls, 2);
    }

    @Test
    public void multiple_handlers_are_called_once_each() {
        SecondTestFailureHandler second = new SecondTestFailureHandler();
        FailureAttachmentRegistry.register(second);
        FailureAttachmentRegistry.register(second);
        int handlerStartsBefore = handler.starts;
        int secondStartsBefore = second.starts;

        FailureAttachmentRegistry.onTestStart();
        FailureAttachmentRegistry.onTestFailure(new RuntimeException("boom"));

        Assert.assertEquals(handler.starts, handlerStartsBefore + 1);
        Assert.assertEquals(second.starts, secondStartsBefore + 1);
        Assert.assertEquals(handler.calls, 1);
        Assert.assertEquals(second.calls, 1);
    }

    // ---------- REAL TestNG FLOW ----------

    @Test
    public void attachments_are_not_triggered_for_expected_exceptions() {

        ReportingManager.setLoggerForTests(ExtentAdapter.getInstance());

        XmlSuite suite = new XmlSuite();
        suite.setName("suite");

        XmlTest test = new XmlTest(suite);
        test.setName("ui-tests");
        test.setXmlClasses(List.of(
                new XmlClass(ExpectedFailingTestA.class),
                new XmlClass(ExpectedFailingTestB.class)
        ));

        TestNG testng = new TestNG();
        testng.setXmlSuites(List.of(suite));
        testng.setUseDefaultListeners(false);
        testng.addListener(new TestListener());
        TestListenerAdapter outcomes = new TestListenerAdapter();
        testng.addListener(outcomes);
        testng.setVerbose(0);

        testng.run();

        Assert.assertEquals(outcomes.getPassedTests().size(), 2,
                "Both expected-exception fixtures must execute and pass before attachment absence is evidence.");
        Assert.assertTrue(outcomes.getFailedTests().isEmpty(), "Nested fixtures must not fail unexpectedly.");
        Assert.assertTrue(outcomes.getSkippedTests().isEmpty(), "Skipped fixtures do not prove attachment behavior.");
        Assert.assertTrue(outcomes.getConfigurationFailures().isEmpty(), "Fixture configuration must succeed.");
        Assert.assertTrue(outcomes.getConfigurationSkips().isEmpty(), "Fixture configuration must not be skipped.");
        Assert.assertEquals(
                handler.calls,
                0,
                "Attachments must NOT be triggered for expectedExceptions"
        );
    }

    // ---------- SUPPORT ----------

    private static final class TestFailureHandler implements FailureAttachmentHandler {
        private int calls = 0;
        private int starts = 0;

        @Override
        public void onTestFailure() {
            calls++;
        }

        @Override
        public void onTestStart() {
            starts++;
        }
    }

    private static final class SecondTestFailureHandler implements FailureAttachmentHandler {
        private int calls = 0;
        private int starts = 0;

        @Override
        public void onTestFailure() {
            calls++;
        }

        @Override
        public void onTestStart() {
            starts++;
        }
    }

    private static final class ThrowingFailureHandler implements FailureAttachmentHandler {
        @Override
        public void onTestStart() {
            throw new NoClassDefFoundError("attachment start exploded");
        }

        @Override
        public void onTestFailure() {
            throw new NoClassDefFoundError("attachment failure exploded");
        }
    }

    public static class ExpectedFailingTestA {
        @Test(expectedExceptions = AssertionError.class)
        public void fails_as_expected() {
            Assert.fail("expected");
        }
    }

    public static class ExpectedFailingTestB {
        @Test(expectedExceptions = AssertionError.class)
        public void fails_as_expected() {
            Assert.fail("expected");
        }
    }
}
