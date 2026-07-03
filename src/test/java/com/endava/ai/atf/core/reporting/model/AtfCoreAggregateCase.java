package com.endava.ai.atf.core.reporting.model;

import com.endava.ai.core.support.contract.AtfContract;
import com.endava.ai.core.support.contract.AtfContractSuite;
import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.endava.ai.core.reporting.aggregate.AggregateStatus;
import io.qameta.allure.model.Status;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class AtfCoreAggregateCase implements AggregateExtentReportWriter.CaseRecord {
    private final String suiteName;
    private final String contextName;
    private final String className;
    private final String methodName;
    private final String parameters;
    private final String suiteProfile;
    private final String capability;
    private final String purpose;
    private final String area;
    private final String owner;
    private final String expected;
    private final String observed;
    private final String repairHint;
    private final AggregateStatus status;
    private final long startMillis;
    private final long endMillis;
    private final String throwableMessage;
    private final String stackTrace;

    public AtfCoreAggregateCase(
            String suiteName,
            String contextName,
            String className,
            String methodName,
            String parameters,
            String suiteProfile,
            String capability,
            String purpose,
            String area,
            String owner,
            String expected,
            String observed,
            String repairHint,
            Status status,
            long startMillis,
            long endMillis,
            String throwableMessage,
            String stackTrace
    ) {
        this(suiteName, contextName, className, methodName, parameters, suiteProfile, capability, purpose,
                area, owner, expected, observed, repairHint, aggregateStatus(status), startMillis, endMillis,
                throwableMessage, stackTrace);
    }

    public static AtfCoreAggregateCase from(ISuite suite, ITestContext context, ITestResult result, Status status) {
        return fromResult(
                suite.getName(),
                context.getName(),
                result,
                status
        );
    }

    public static AtfCoreAggregateCase from(ITestResult result, Status status) {
        ITestContext context = result.getTestContext();
        ISuite suite = context == null ? null : context.getSuite();
        String suiteName = suite == null ? "runtime-listener" : suite.getName();
        String contextName = context == null ? "runtime-listener" : context.getName();
        return fromResult(suiteName, contextName, result, status);
    }

    private static AtfCoreAggregateCase fromResult(
            String suiteName,
            String contextName,
            ITestResult result,
            Status status
    ) {
        String className = result.getMethod().getRealClass().getName();
        String methodName = result.getMethod().getMethodName();
        AtfContractSuite suiteContract = suiteContract(result);
        AtfContract atfContract = atfContract(result);
        Throwable throwable = result.getThrowable();
        return new AtfCoreAggregateCase(
                suiteName,
                contextName,
                className,
                methodName,
                parameters(result.getParameters()),
                suiteContract == null ? "" : suiteContract.profile().name(),
                suiteContract == null ? "" : suiteContract.capability(),
                suiteContract == null ? "" : suiteContract.purpose(),
                atfContract == null ? "" : atfContract.area().name(),
                atfContract == null ? "" : atfContract.owner(),
                atfContract == null ? "" : atfContract.expected(),
                atfContract == null ? "" : atfContract.observed(),
                atfContract == null ? "" : atfContract.repairHint(),
                aggregateStatus(status),
                result.getStartMillis(),
                result.getEndMillis(),
                throwable == null ? "" : String.valueOf(throwable.getMessage()),
                stackTrace(throwable)
        );
    }

    private static AtfContractSuite suiteContract(ITestResult result) {
        Class<?> realClass = result.getMethod().getRealClass();
        return realClass == null ? null : realClass.getAnnotation(AtfContractSuite.class);
    }

    private static AtfContract atfContract(ITestResult result) {
        AtfContract methodContract = null;
        if (result.getMethod().getConstructorOrMethod() != null
                && result.getMethod().getConstructorOrMethod().getMethod() != null) {
            Method javaMethod = result.getMethod()
                    .getConstructorOrMethod()
                    .getMethod();
            methodContract = javaMethod.getAnnotation(AtfContract.class);
        }
        if (methodContract != null) {
            return methodContract;
        }
        Class<?> realClass = result.getMethod().getRealClass();
        return realClass == null ? null : realClass.getAnnotation(AtfContract.class);
    }

    public String fullName() {
        return className + "#" + methodName + parameters;
    }

    public String statusName() { return status.name(); }

    public String displayName() {
        return methodName + (parameters.isBlank() ? "" : " " + parameters);
    }

    public String description() {
        return className + "#" + methodName;
    }

    public long durationMillis() {
        return Math.max(0, endMillis - startMillis);
    }

    private static String parameters(Object[] values) {
        if (values == null || values.length == 0) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (Object value : values) {
            parts.add(String.valueOf(value));
        }
        return parts.toString();
    }

    private static AggregateStatus aggregateStatus(Status status) {
        if (status == Status.PASSED) {
            return AggregateStatus.PASSED;
        }
        if (status == Status.SKIPPED) {
            return AggregateStatus.SKIPPED;
        }
        return AggregateStatus.FAILED;
    }

    private static String stackTrace(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
