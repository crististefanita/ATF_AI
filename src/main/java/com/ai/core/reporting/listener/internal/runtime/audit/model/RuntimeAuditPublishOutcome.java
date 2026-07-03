package com.ai.core.reporting.listener.internal.runtime.audit.model;

public enum RuntimeAuditPublishOutcome {
    WRITTEN,
    SKIPPED_NO_CANDIDATE,
    SKIPPED_AMBIGUOUS_CANDIDATES,
    BLOCKED_FROZEN_REPORT,
    FAILED_WRITE
}
