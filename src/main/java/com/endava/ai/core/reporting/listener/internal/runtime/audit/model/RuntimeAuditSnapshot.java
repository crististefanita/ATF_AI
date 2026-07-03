package com.endava.ai.core.reporting.listener.internal.runtime.audit.model;

import com.endava.ai.core.reporting.listener.internal.runtime.model.ExtentRuntimeInvocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class RuntimeAuditSnapshot {

    private RuntimeAuditSnapshot() {
    }

    public static List<ExtentRuntimeInvocation> sortedCopy(List<ExtentRuntimeInvocation> source) {
        List<ExtentRuntimeInvocation> snapshot;
        synchronized (source) {
            snapshot = new ArrayList<>(source);
        }
        snapshot.sort(Comparator.comparingLong(ExtentRuntimeInvocation::startMillis)
                .thenComparing(ExtentRuntimeInvocation::line));
        return snapshot;
    }

    public static List<String> reportMarkers(List<ExtentRuntimeInvocation> invocations) {
        List<String> markers = new ArrayList<>();
        invocations.forEach(invocation -> {
            markers.add(invocation.className());
            markers.add(invocation.methodName());
        });
        return markers;
    }
}
