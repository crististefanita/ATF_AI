package com.endava.ai.core.reporting.listener.internal.runtime.audit.model;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
public final class RuntimeAuditPublishResult {
    private final RuntimeAuditPublishOutcome outcome;
    private final RuntimeAuditTarget.Kind targetKind;
    private final Path report;
    private final String reason;
    private final int invocationCount;
    private final RuntimeAuditWriteRecord writeRecord;
    private final String failureClass;
    private final String failureMessage;

    private RuntimeAuditPublishResult(
            RuntimeAuditPublishOutcome outcome,
            RuntimeAuditTarget.Kind targetKind,
            Path report,
            String reason,
            int invocationCount,
            RuntimeAuditWriteRecord writeRecord,
            Throwable failure
    ) {
        this.outcome = Objects.requireNonNull(outcome, "outcome");
        this.targetKind = targetKind;
        this.report = report == null ? null : report.toAbsolutePath().normalize();
        this.reason = reason == null ? "" : reason;
        this.invocationCount = invocationCount;
        this.writeRecord = writeRecord;
        this.failureClass = failure == null ? "" : failure.getClass().getName();
        this.failureMessage = failure == null || failure.getMessage() == null ? "" : failure.getMessage();
    }

    public static RuntimeAuditPublishResult written(
            RuntimeAuditTarget target,
            int invocationCount,
            RuntimeAuditWriteRecord writeRecord
    ) {
        return new RuntimeAuditPublishResult(
                RuntimeAuditPublishOutcome.WRITTEN,
                target.kind(),
                target.report(),
                target.reason(),
                invocationCount,
                writeRecord,
                null
        );
    }

    public static RuntimeAuditPublishResult skipped(RuntimeAuditTarget target, int invocationCount) {
        RuntimeAuditPublishOutcome outcome = target.kind() == RuntimeAuditTarget.Kind.AMBIGUOUS_CANDIDATES
                ? RuntimeAuditPublishOutcome.SKIPPED_AMBIGUOUS_CANDIDATES
                : RuntimeAuditPublishOutcome.SKIPPED_NO_CANDIDATE;
        return new RuntimeAuditPublishResult(
                outcome,
                target.kind(),
                target.report(),
                target.reason(),
                invocationCount,
                null,
                null
        );
    }

    public static RuntimeAuditPublishResult blockedFrozen(
            RuntimeAuditTarget target,
            int invocationCount,
            Throwable failure
    ) {
        return new RuntimeAuditPublishResult(
                RuntimeAuditPublishOutcome.BLOCKED_FROZEN_REPORT,
                target == null ? null : target.kind(),
                target == null ? null : target.report(),
                target == null ? "Frozen Extent report blocked runtime audit publication" : target.reason(),
                invocationCount,
                null,
                failure
        );
    }

    public static RuntimeAuditPublishResult failed(
            RuntimeAuditTarget target,
            int invocationCount,
            Throwable failure
    ) {
        return new RuntimeAuditPublishResult(
                RuntimeAuditPublishOutcome.FAILED_WRITE,
                target == null ? null : target.kind(),
                target == null ? null : target.report(),
                target == null ? "Runtime audit publish failed before target resolution" : target.reason(),
                invocationCount,
                null,
                failure
        );
    }

    public boolean armsPublishGuard() {
        return outcome == RuntimeAuditPublishOutcome.WRITTEN
                || outcome == RuntimeAuditPublishOutcome.BLOCKED_FROZEN_REPORT
                || outcome == RuntimeAuditPublishOutcome.FAILED_WRITE;
    }

    public boolean written() {
        return outcome == RuntimeAuditPublishOutcome.WRITTEN;
    }
}
