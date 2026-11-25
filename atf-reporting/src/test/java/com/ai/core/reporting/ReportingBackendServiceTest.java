package com.ai.core.reporting;

import com.ai.core.reporting.listener.ReportingTestListener;
import com.ai.core.reporting.spi.ReportingBackend;
import com.ai.core.reporting.spi.ReportingBackendLoader;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Proxy;

public class ReportingBackendServiceTest {

    @AfterMethod(alwaysRun = true)
    public void reset() {
        ReportingManager.reset();
    }

    @Test
    public void serviceDescriptorConnectsCoreToConcreteReportingLifecycle() {
        ReportingBackend backend = ReportingBackendLoader.load().orElseThrow();

        Assert.assertEquals(backend.getClass(), DefaultReportingBackend.class);
        Assert.assertTrue(backend.createListener() instanceof ReportingTestListener);
        Assert.assertTrue(ReportingManager.createTestListener() instanceof ReportingTestListener,
                "The compatibility facade must use the discovered reporting lifecycle.");
    }

    @Test
    public void discoveredBackendPreservesSessionStateThroughTheCoreFacade() {
        ReportLogger observer = (ReportLogger) Proxy.newProxyInstance(
                ReportLogger.class.getClassLoader(), new Class<?>[]{ReportLogger.class},
                (proxy, method, args) -> null);
        ReportingManager.setLoggerForTests(observer);
        Assert.assertSame(ReportingManager.testLoggerForTests(), observer);
        ReportLogger active = ReportingManager.getLogger();
        Assert.assertSame(active instanceof CompositeReportLogger
                ? ((CompositeReportLogger) active).primary() : active, observer);

        long before = ReportingManager.suiteGenerationForTests();
        ReportingManager.beginSuite();

        Assert.assertTrue(ReportingManager.isSuiteActiveForTests());
        Assert.assertEquals(ReportingManager.suiteGenerationForTests(), before + 1);
        Assert.assertNull(ReportingManager.testLoggerForTests());
        ReportingManager.endSuite();
        Assert.assertFalse(ReportingManager.isSuiteActiveForTests());
    }
}
