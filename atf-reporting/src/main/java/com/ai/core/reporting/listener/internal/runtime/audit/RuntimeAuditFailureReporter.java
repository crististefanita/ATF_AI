package com.ai.core.reporting.listener.internal.runtime.audit;

import com.ai.core.reporting.diagnostic.NonBlockingDiagnostic;

public final class RuntimeAuditFailureReporter {

    private static final String COMPONENT = "ExtentRuntimeAudit";
    private static final String ARTIFACT = "Extent Spark report";
    private static final String REPAIR_HINT = "Keep runtime audit publication idempotent and provenance-preserving; "
            + "inspect report path, permissions, and post-processing boundaries.";

    private RuntimeAuditFailureReporter() {
    }

    public static void report(String operation, Throwable failure) {
        NonBlockingDiagnostic.warn(operation, failure, COMPONENT, ARTIFACT, REPAIR_HINT);
    }
}
