package com.ai.core.reporting.listener.internal.runtime.audit;

import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditPublisher;
import com.ai.core.reporting.listener.internal.runtime.model.ExtentRuntimeInvocation;
import com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditPublishResult;
import com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditTarget;
import com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditWriteRecord;
import com.ai.core.reporting.adapters.ExtentAdapter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class RuntimeAuditPublisherCoordinator {

    private RuntimeAuditPublisherCoordinator() {
    }

    public static RuntimeAuditPublishResult publishCurrentReport(List<ExtentRuntimeInvocation> invocations) {
        RuntimeAuditTarget target = null;
        int invocationCount = invocations == null ? 0 : invocations.size();
        try {
            ExtentAdapter adapter = ExtentAdapter.getInstance();
            if (adapter.hasReportContent()) {
                adapter.flush();
            }
            target = ExtentRuntimeReportLocator.locate(
                    reportMarkers(invocations)
            );
            if (!target.writable()) {
                return RuntimeAuditPublishResult.skipped(target, invocationCount);
            }
            RuntimeAuditWriteRecord writeRecord = ExtentRuntimeAuditPublisher.writeAuditToReport(
                    target.report(),
                    invocations,
                    target.kind() == RuntimeAuditTarget.Kind.CURRENT_REPORT
            );
            return RuntimeAuditPublishResult.written(target, invocationCount, writeRecord);
        } catch (Throwable failure) {
            RuntimeAuditFailureReporter.report("runtime Extent audit update", failure);
            if (isFrozenArtifactBlock(failure)) {
                return RuntimeAuditPublishResult.blockedFrozen(target, invocationCount, failure);
            }
            return RuntimeAuditPublishResult.failed(target, invocationCount, failure);
        }
    }

    public static void publishReport(Path report, List<ExtentRuntimeInvocation> invocations) {
        if (report == null || invocations == null || invocations.isEmpty()) {
            return;
        }
        try {
            ExtentRuntimeAuditPublisher.writeAuditToReport(report, invocations);
        } catch (Throwable failure) {
            RuntimeAuditFailureReporter.report("aggregate Extent audit update", failure);
        }
    }

    public static RuntimeAuditWriteRecord publishReportStrict(
            Path report,
            List<ExtentRuntimeInvocation> invocations
    ) throws IOException {
        if (report == null) {
            throw new IOException("Missing Extent report path for strict runtime audit publication.");
        }
        if (invocations == null || invocations.isEmpty()) {
            throw new IOException("Missing runtime invocations for strict runtime audit publication: " + report);
        }
        return ExtentRuntimeAuditPublisher.writeAuditToReport(report, invocations);
    }

    private static boolean isFrozenArtifactBlock(Throwable failure) {
        return failure != null
                && failure.getMessage() != null
                && failure.getMessage().contains("Refusing to rewrite frozen Extent artifact");
    }

    private static List<String> reportMarkers(List<ExtentRuntimeInvocation> invocations) {
        List<String> markers = new ArrayList<>();
        if (invocations == null) {
            return markers;
        }
        invocations.forEach(invocation -> {
            markers.add(invocation.className());
            markers.add(invocation.methodName());
        });
        return markers;
    }
}
