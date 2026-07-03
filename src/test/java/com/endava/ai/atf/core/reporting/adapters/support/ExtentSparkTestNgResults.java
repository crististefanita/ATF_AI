package com.endava.ai.atf.core.reporting.adapters.support;

import com.endava.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.TestResult;

public final class ExtentSparkTestNgResults {
    private ExtentSparkTestNgResults() {
    }

    public static ITestResult successResult(String methodName) {
        return result(methodName, ExtentSparkPresentationContractSupport.ExtentHierarchyFixture.class, ITestResult.SUCCESS);
    }

    public static ITestResult result(String methodName, Class<?> realClass, int status) {
        return result(methodName, realClass, status, null);
    }

    public static ITestResult result(String methodName, Class<?> realClass, int status, Throwable throwable) {
        TestResult result = TestResult.newEmptyTestResult();
        result.setStatus(status);
        result.setMethod(fakeMethod(methodName, realClass));
        result.setThrowable(throwable);
        return result;
    }

    public static ITestNGMethod fakeMethod(String methodName) {
        return fakeMethod(methodName, ExtentSparkPresentationContractSupport.ExtentHierarchyFixture.class);
    }

    public static ITestNGMethod fakeMethod(String methodName, Class<?> realClass) {
        ITestNGMethod method = org.mockito.Mockito.mock(ITestNGMethod.class);
        org.mockito.Mockito.when(method.getMethodName()).thenReturn(methodName);
        org.mockito.Mockito.when(method.getDescription()).thenReturn(methodName);
        org.mockito.Mockito.when(method.getRealClass()).thenReturn(realClass);
        return method;
    }
}
