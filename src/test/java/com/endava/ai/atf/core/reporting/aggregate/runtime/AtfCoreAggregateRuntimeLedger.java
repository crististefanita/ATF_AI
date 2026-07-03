package com.endava.ai.atf.core.reporting.aggregate.runtime;

import com.endava.ai.atf.core.reporting.model.AtfCoreAggregateCase;
import com.endava.ai.core.reporting.aggregate.AggregateStatus;
import com.endava.ai.core.reporting.aggregate.model.AggregateRuntimeAuditSnapshot;
import com.endava.ai.core.reporting.aggregate.model.AggregateRuntimeCase;
import com.endava.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry.RuntimeAuditInvocation;
import io.qameta.allure.model.Status;
import org.testng.ITestResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class AtfCoreAggregateRuntimeLedger {
    private static final String CURRENT_AUDIT_SUFFIX = " (current audit node)";
    private static final String FAKE_METHOD_PREFIX = "fake";
    private static final String INNER_CLASS_SEPARATOR = "$";
    private static final String RUNTIME_SOURCE_TAG = "SOURCE:ATF_CORE_RUNTIME_LEDGER";
    private static final String STATUS_TAG_PREFIX = "STATUS:";

    private AtfCoreAggregateRuntimeLedger() {
    }

    public static void record(
            List<AggregateRuntimeCase> runtimeCases,
            List<AtfCoreAggregateCase> aggregateCases,
            ITestResult result,
            Status status
    ) {
        if (result == null || result.getMethod() == null) {
            return;
        }
        Class<?> realClass = result.getMethod().getRealClass();
        String className = realClass == null ? "" : realClass.getName();
        String methodName = result.getMethod().getMethodName();
        if (!isReportableRuntimeInvocation(className, methodName)) {
            return;
        }
        runtimeCases.add(AggregateRuntimeCase.of(className, methodName, aggregateStatus(status), result.getStartMillis()));
        aggregateCases.add(AtfCoreAggregateCase.from(result, status));
    }

    public static AggregateRuntimeAuditSnapshot snapshot(List<AggregateRuntimeCase> runtimeCases) {
        List<AggregateRuntimeCase> cases = snapshotCases(runtimeCases);

        long passed = cases.stream().filter(testCase -> testCase.status().passed()).count();
        long failed = cases.stream().filter(testCase -> testCase.status().failed()).count();
        long skipped = cases.stream().filter(testCase -> testCase.status().skipped()).count();
        List<String> lines = cases.stream()
                .map(AggregateRuntimeCase::line)
                .collect(Collectors.toCollection(ArrayList::new));
        return new AggregateRuntimeAuditSnapshot(lines.size(), passed, failed, skipped, lines);
    }

    public static AggregateRuntimeAuditSnapshot snapshotIncludingCurrentAudit(
            List<AggregateRuntimeCase> runtimeCases,
            String currentClassName,
            String currentMethodName
    ) {
        AggregateRuntimeAuditSnapshot snapshot = snapshot(runtimeCases);
        List<String> lines = new ArrayList<>(snapshot.lineItems());
        lines.add("PASSED | " + currentClassName + "#" + currentMethodName + CURRENT_AUDIT_SUFFIX);
        return new AggregateRuntimeAuditSnapshot(
                lines.size(),
                snapshot.passed() + 1,
                snapshot.failed(),
                snapshot.skipped(),
                lines
        );
    }

    public static List<AggregateRuntimeCase> snapshotCases(List<AggregateRuntimeCase> runtimeCases) {
        List<AggregateRuntimeCase> cases;
        synchronized (runtimeCases) {
            cases = new ArrayList<>(runtimeCases);
        }
        cases.sort(Comparator.comparingLong(AggregateRuntimeCase::startMillis)
                .thenComparing(AggregateRuntimeCase::fullName));
        return cases;
    }

    public static List<RuntimeAuditInvocation> runtimeInvocations(List<AggregateRuntimeCase> cases) {
        return cases.stream()
                .map(testCase -> RuntimeAuditInvocation.of(
                        testCase.status().name(),
                        testCase.className(),
                        testCase.methodName(),
                        testCase.startMillis(),
                        "",
                        List.of(STATUS_TAG_PREFIX + testCase.status().name(), RUNTIME_SOURCE_TAG)
                ))
                .collect(Collectors.toList());
    }

    private static boolean isReportableRuntimeInvocation(String className, String methodName) {
        if (className == null || className.isBlank()) {
            return false;
        }
        if (methodName == null || methodName.isBlank()) {
            return false;
        }
        if (className.contains(INNER_CLASS_SEPARATOR)) {
            return false;
        }
        return !methodName.startsWith(FAKE_METHOD_PREFIX);
    }

    private static AggregateStatus aggregateStatus(Status status) {
        if (status == Status.FAILED || status == Status.BROKEN) {
            return AggregateStatus.FAILED;
        }
        if (status == Status.SKIPPED) {
            return AggregateStatus.SKIPPED;
        }
        return AggregateStatus.PASSED;
    }
}
