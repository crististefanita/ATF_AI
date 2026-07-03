package com.endava.ai.atf.core.reporting.attachment;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import org.testng.annotations.Listeners;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.ReportingManager;
import com.endava.ai.core.reporting.adapters.AllureAdapter;
import com.endava.ai.core.reporting.adapters.ExtentAdapter;
import com.endava.ai.core.reporting.attachment.FailureAttachmentHandler;
import com.endava.ai.core.reporting.attachment.FailureAttachmentRegistry;
import com.endava.ai.core.reporting.diagnostic.NonBlockingDiagnostic;
import org.testng.Assert;
import org.testng.TestNG;
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

    @AfterMethod
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
        FailureAttachmentRegistry.onTestFailure(new RuntimeException("boom"));
        Assert.assertTrue(true); // no exception
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

    // ---------- ENGINE-SPECIFIC ----------

    @Test
    public void extent_triggers_failure_attachment() {
        ReportingManager.setLoggerForTests(ExtentAdapter.getInstance());
        FailureAttachmentRegistry.onTestFailure(new RuntimeException("boom"));
        Assert.assertEquals(handler.calls, 1);
    }

    @Test
    public void allure_triggers_failure_attachment() {
        ReportingManager.setLoggerForTests(AllureAdapter.getInstance());
        FailureAttachmentRegistry.onTestFailure(new RuntimeException("boom"));
        Assert.assertEquals(handler.calls, 1);
    }

    @Test
    public void multiple_failures_trigger_multiple_attachments() {
        ReportingManager.setLoggerForTests(AllureAdapter.getInstance());

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

        ReportingManager.setLoggerForTests(AllureAdapter.getInstance());

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
        testng.setVerbose(0);

        testng.run();

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
