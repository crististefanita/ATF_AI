package com.ai.core.reporting.listener.internal;

import com.ai.core.reporting.ReportText;
import org.testng.ITestResult;

import java.util.Arrays;

final class TestDisplayName {
    private TestDisplayName() {
    }

    static String from(ITestResult result) {
        if (result == null || result.getMethod() == null) {
            return "Test";
        }
        String methodName = result.getMethod().getMethodName();
        StringBuilder name = new StringBuilder(methodName == null || methodName.isBlank() ? "Test" : methodName);
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            name.append(" ").append(Arrays.toString(parameters));
        }
        Class<?> realClass = result.getMethod().getRealClass();
        if (realClass != null && realClass.getName().contains("$")) {
            name.append(" [").append(ReportText.simpleClassName(realClass.getName())).append("]");
        }
        return name.toString();
    }
}
