package com.endava.ai.atf.core.reporting.support;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.ReportingManager;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.core.reporting.attachment.FailureAttachmentRegistry;
import com.endava.ai.ui.core.runtime.DriverManager;
import com.endava.ai.ui.core.engine.UIEngine;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.TestResult;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class UiReportingTestSupport {

    private UiReportingTestSupport() {
    }

    public static void resetReportingState() {
        setEngineForCurrentThread(null);
        FailureAttachmentRegistry.clearForTests();
        ReportingManager.reset();
        StepLogger.clear();
        TestListener.resetForTests();
    }

    public static void setEngineForCurrentThread(UIEngine engine) {
        try {
            Field field = DriverManager.class.getDeclaredField("ENGINE");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            ThreadLocal<UIEngine> engines = (ThreadLocal<UIEngine>) field.get(null);

            if (engine == null) {
                engines.remove();
            } else {
                engines.set(engine);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set UI engine for tests", e);
        }
    }

    public static ITestResult successResult(Class<?> realClass, String methodName) {
        TestResult result = TestResult.newEmptyTestResult();
        result.setStatus(ITestResult.SUCCESS);
        result.setMethod(fakeMethod(realClass, methodName));
        return result;
    }

    public static ITestResult failureResult(Class<?> realClass, String methodName, Throwable throwable) {
        TestResult result = TestResult.newEmptyTestResult();
        result.setStatus(ITestResult.FAILURE);
        result.setThrowable(throwable);
        result.setMethod(fakeMethod(realClass, methodName));
        return result;
    }

    private static ITestNGMethod fakeMethod(Class<?> realClass, String methodName) {
        ITestNGMethod method = mock(ITestNGMethod.class);
        when(method.getMethodName()).thenReturn(methodName);
        when(method.getDescription()).thenReturn(methodName);
        when(method.getRealClass()).thenReturn(realClass);
        return method;
    }
}
