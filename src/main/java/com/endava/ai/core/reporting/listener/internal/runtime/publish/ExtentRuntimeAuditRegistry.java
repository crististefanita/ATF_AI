package com.endava.ai.core.reporting.listener.internal.runtime.publish;

import com.endava.ai.core.reporting.listener.internal.runtime.render.*;

import com.endava.ai.core.reporting.listener.internal.runtime.model.*;

import com.endava.ai.core.support.config.ConfigManager;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditPublishResult;
import com.endava.ai.core.reporting.listener.internal.runtime.audit.RuntimeAuditPublisherCoordinator;
import com.endava.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditWriteRecord;
import com.endava.ai.core.reporting.listener.internal.runtime.audit.RuntimeInvocationFilter;
import com.endava.ai.core.reporting.utils.StacktraceFormatter;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class ExtentRuntimeAuditRegistry {

    private static final List<ExtentRuntimeInvocation> INVOCATIONS =
            Collections.synchronizedList(new ArrayList<>());
    private static boolean currentReportPublished;

    private ExtentRuntimeAuditRegistry() {
    }

    public static synchronized void clear() {
        INVOCATIONS.clear();
        currentReportPublished = false;
    }

    public static void record(ITestResult result, String status) {
        if (result == null || result.getMethod() == null) {
            return;
        }
        Class<?> realClass = result.getMethod().getRealClass();
        String className = realClass == null ? "" : realClass.getName();
        String methodName = result.getMethod().getMethodName();
        if (!RuntimeInvocationFilter.isReportable(result, className, methodName)) {
            return;
        }
        INVOCATIONS.add(new ExtentRuntimeInvocation(
                status,
                className,
                methodName,
                result.getStartMillis(),
                failureDetail(result),
                ExtentRuntimeTags.from(result, realClass, className)
        ));
        currentReportPublished = false;
    }

    private static String failureDetail(ITestResult result) {
        if (result == null || result.getThrowable() == null) {
            return "";
        }
        String className = result.getMethod() == null || result.getMethod().getRealClass() == null
                ? "<unknown class>"
                : result.getMethod().getRealClass().getName();
        String methodName = result.getMethod() == null
                ? "<unknown method>"
                : result.getMethod().getMethodName();
        return "Test reference: " + className + "#" + methodName
                + System.lineSeparator()
                + System.lineSeparator()
                + StacktraceFormatter.format(result.getThrowable());
    }

    public static synchronized void publishOrUpdateIfExtent() {
        if (currentReportPublished) {
            return;
        }
        if (INVOCATIONS.isEmpty()) {
            return;
        }
        if (!ReportingConfigKeys.ENGINE_EXTENT.equalsIgnoreCase(
                ConfigManager.get(ReportingConfigKeys.REPORTING_ENGINE, ""))) {
            return;
        }

        RuntimeAuditPublishResult result = RuntimeAuditPublisherCoordinator.publishCurrentReport(
                sortedInvocationSnapshot()
        );
        currentReportPublished = result.armsPublishGuard();
    }

    private static List<ExtentRuntimeInvocation> sortedInvocationSnapshot() {
        List<ExtentRuntimeInvocation> snapshot;
        synchronized (INVOCATIONS) {
            snapshot = new ArrayList<>(INVOCATIONS);
        }
        snapshot.sort(Comparator.comparingLong(ExtentRuntimeInvocation::startMillis)
                .thenComparing(ExtentRuntimeInvocation::line));
        return snapshot;
    }

    public static synchronized void publishOrUpdateReport(
            Path report,
            List<RuntimeAuditInvocation> runtimeInvocations
    ) {
        if (runtimeInvocations == null || runtimeInvocations.isEmpty()) {
            return;
        }

        List<ExtentRuntimeInvocation> invocations = runtimeInvocations.stream()
                .map(RuntimeAuditInvocation::toInvocation)
                .collect(Collectors.toList());
        RuntimeAuditPublisherCoordinator.publishReport(report, invocations);
    }

    public static synchronized RuntimeAuditWriteRecord publishOrUpdateReportStrict(
            Path report,
            List<RuntimeAuditInvocation> runtimeInvocations
    ) throws IOException {
        if (runtimeInvocations == null || runtimeInvocations.isEmpty()) {
            throw new IOException("Missing runtime invocations for strict Extent audit publication: " + report);
        }

        List<ExtentRuntimeInvocation> invocations = runtimeInvocations.stream()
                .map(RuntimeAuditInvocation::toInvocation)
                .collect(Collectors.toList());
        return RuntimeAuditPublisherCoordinator.publishReportStrict(report, invocations);
    }

    public static void publishIfExtent() {
        publishOrUpdateIfExtent();
    }

    public static final class RuntimeAuditInvocation {
        private final String status;
        private final String className;
        private final String methodName;
        private final long startMillis;
        private final String failureDetail;
        private final List<String> annotationTags;

        private RuntimeAuditInvocation(
                String status,
                String className,
                String methodName,
                long startMillis,
                String failureDetail,
                List<String> annotationTags
        ) {
            this.status = status == null ? "" : status;
            this.className = className == null ? "" : className;
            this.methodName = methodName == null ? "" : methodName;
            this.startMillis = startMillis;
            this.failureDetail = failureDetail == null ? "" : failureDetail;
            this.annotationTags = annotationTags == null ? List.of() : List.copyOf(annotationTags);
        }

        public static RuntimeAuditInvocation of(
                String status,
                String className,
                String methodName,
                long startMillis,
                String failureDetail,
                List<String> annotationTags
        ) {
            return new RuntimeAuditInvocation(status, className, methodName, startMillis, failureDetail, annotationTags);
        }

        private ExtentRuntimeInvocation toInvocation() {
            return new ExtentRuntimeInvocation(status, className, methodName, startMillis, failureDetail, annotationTags);
        }
    }

}
