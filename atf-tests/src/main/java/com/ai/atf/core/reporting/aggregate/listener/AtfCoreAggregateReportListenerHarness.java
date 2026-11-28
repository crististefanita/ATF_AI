package com.ai.atf.core.reporting.aggregate.listener;

import com.ai.atf.core.reporting.aggregate.AtfCoreAggregateReportListener;
import com.ai.core.reporting.aggregate.model.AggregateSummary;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.TestResult;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;

import static com.ai.atf.core.reporting.aggregate.AtfCoreAggregateExtentFixtures.aggregateCase;
import static com.ai.atf.core.reporting.aggregate.AtfCoreAggregateExtentFixtures.aggregateFixture;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AtfCoreAggregateReportListenerHarness {
    private AtfCoreAggregateReportListenerHarness() {
    }

    public static void invokeAggregateAppend() throws Exception {
        Object summary = new AggregateSummary(110, 110L, 4L, 0L);

        Method append = AtfCoreAggregateReportListener.class
                .getDeclaredMethod("appendRuntimeAuditToExtentHtml", AggregateSummary.class, java.util.List.class);
        append.setAccessible(true);
        append.invoke(null, summary, java.util.List.of());
    }

    public static void invokeAggregateExtentWriter(Path report) throws Exception {
        Object summary = new AggregateSummary(4, 4L, 0L, 0L);

        Method write = AtfCoreAggregateReportListener.class
                .getDeclaredMethod("writeExtentReport", Path.class, AggregateSummary.class, java.util.List.class);
        write.setAccessible(true);
        write.invoke(null, report, summary, java.util.List.of(
                aggregateCase("first_standard_shell_contract", 10L),
                aggregateCase("second_standard_shell_contract", 20L),
                aggregateCase(
                        aggregateFixture("FailureAttachmentRegistryTest$ExpectedFailingTestA"),
                        "fails_as_expected",
                        30L,
                        ""
                ),
                aggregateCase(
                        aggregateFixture("FailureAttachmentRegistryTest$ExpectedFailingTestB"),
                        "fails_as_expected",
                        40L,
                        ""
                )
        ));
    }

    public static Path invokeLatestExtentReport() throws Exception {
        Method method = AtfCoreAggregateReportListener.class.getDeclaredMethod("latestExtentReport");
        method.setAccessible(true);
        return ((Path) method.invoke(null)).toAbsolutePath().normalize();
    }

    public static void resetHtmlAuditFlag() throws Exception {
        Field field = AtfCoreAggregateReportListener.class.getDeclaredField("htmlExtentAuditWritten");
        field.setAccessible(true);
        field.setBoolean(null, false);
    }

    public static void clearRuntimeCases() throws Exception {
        Field field = AtfCoreAggregateReportListener.class.getDeclaredField("RUNTIME_CASES");
        field.setAccessible(true);
        ((java.util.List<?>) field.get(null)).clear();
    }

    public static void recordRuntimeCase(ITestResult result, com.ai.core.reporting.aggregate.AggregateStatus status) throws Exception {
        Method method = AtfCoreAggregateReportListener.class
                .getDeclaredMethod("recordRuntimeCase", ITestResult.class, com.ai.core.reporting.aggregate.AggregateStatus.class);
        method.setAccessible(true);
        method.invoke(null, result, status);
    }

    public static ITestResult result(String methodName, Class<?> testClass, int status) {
        TestResult result = TestResult.newEmptyTestResult();
        ITestNGMethod method = mock(ITestNGMethod.class);
        when(method.getMethodName()).thenReturn(methodName);
        when(method.getDescription()).thenReturn(methodName);
        when(method.getRealClass()).thenReturn((Class) testClass);
        result.setMethod(method);
        result.setStatus(status);
        return result;
    }
}
