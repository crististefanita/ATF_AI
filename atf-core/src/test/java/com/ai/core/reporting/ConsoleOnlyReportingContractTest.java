package com.ai.core.reporting;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.spi.ReportingBackendLoader;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Proxy;

public class ConsoleOnlyReportingContractTest {

    @AfterMethod(alwaysRun = true)
    public void reset() {
        TestListener.resetForTests();
    }

    @Test
    public void coreRunsWithoutAnyConcreteReportingImplementation() {
        Assert.assertTrue(ReportingBackendLoader.load().isEmpty(),
                "The Core module test classpath must not include a reporting provider.");
        Assert.assertNull(ReportingManager.tryGetLogger());

        IllegalStateException failure = Assert.expectThrows(
                IllegalStateException.class, ReportingManager::getLogger);

        Assert.assertTrue(failure.getMessage().contains("atf-reporting"));
        TestListener listener = new TestListener();
        listener.onStart((org.testng.ISuite) null);
        listener.beforeInvocation(null, null);
        listener.onTestStart(null);
        listener.afterInvocation(null, null);
        listener.onTestSuccess(null);
        listener.onTestFailure(null);
        listener.onTestSkipped(null);
        listener.onConfigurationFailure(null);
        listener.onFinish((org.testng.ISuite) null);
    }

    @Test
    public void localSessionLifecyclePreservesInjectedLoggerAndGenerationContract() {
        ReportLogger observer = (ReportLogger) Proxy.newProxyInstance(
                ReportLogger.class.getClassLoader(), new Class<?>[]{ReportLogger.class},
                (proxy, method, args) -> null);
        ReportingManager.setLoggerForTests(observer);
        Assert.assertSame(ReportingManager.getLogger(), observer);
        Assert.assertSame(ReportingManager.testLoggerForTests(), observer);

        long before = ReportingManager.suiteGenerationForTests();
        ReportingManager.beginSuite();

        Assert.assertNull(ReportingManager.testLoggerForTests());
        Assert.assertNull(ReportingManager.tryGetLogger());
        Assert.assertTrue(ReportingManager.isSuiteActiveForTests());
        Assert.assertEquals(ReportingManager.suiteGenerationForTests(), before + 1);
        ReportingManager.endSuite();
        Assert.assertFalse(ReportingManager.isSuiteActiveForTests());
        Assert.assertEquals(ReportingManager.suiteGenerationForTests(), before + 1);
    }
}
