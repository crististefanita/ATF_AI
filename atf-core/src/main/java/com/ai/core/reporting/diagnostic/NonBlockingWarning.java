package com.ai.core.reporting.diagnostic;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
public final class NonBlockingWarning {
    Severity severity;
    String operation;
    String component;
    String artifact;
    String failureType;
    String failureMessage;
    String repairHint;
    String message;

    private NonBlockingWarning(
            Severity severity,
            String operation,
            String component,
            String artifact,
            String failureType,
            String failureMessage,
            String repairHint,
            String message
    ) {
        this.severity = severity;
        this.operation = safe(operation);
        this.component = safe(component);
        this.artifact = safe(artifact);
        this.failureType = safe(failureType);
        this.failureMessage = safe(failureMessage);
        this.repairHint = safe(repairHint);
        this.message = safe(message);
    }

    static NonBlockingWarning from(String operation, Throwable failure) {
        String normalizedOperation = safe(operation);
        Throwable normalizedFailure = failure == null
                ? new IllegalStateException("No failure detail provided")
                : failure;
        String failureType = normalizedFailure.getClass().getSimpleName();
        String failureMessage = safe(normalizedFailure.getMessage());
        String message = normalizedOperation + " failed non-blocking: "
                + failureType + ": " + failureMessage;
        return new NonBlockingWarning(Severity.WARN, normalizedOperation, "", "", failureType, failureMessage, "", message);
    }

    static NonBlockingWarning from(String operation, String detail) {
        String normalizedOperation = safe(operation);
        String failureMessage = safe(detail);
        String message = normalizedOperation + " failed non-blocking: " + failureMessage;
        return new NonBlockingWarning(Severity.WARN, normalizedOperation, "", "", "Diagnostic", failureMessage, "", message);
    }

    NonBlockingWarning withComponent(String component) {
        return new NonBlockingWarning(
                severity,
                operation,
                component,
                artifact,
                failureType,
                failureMessage,
                repairHint,
                message
        );
    }

    NonBlockingWarning withArtifact(String artifact) {
        return new NonBlockingWarning(
                severity,
                operation,
                component,
                artifact,
                failureType,
                failureMessage,
                repairHint,
                message
        );
    }

    NonBlockingWarning withRepairHint(String repairHint) {
        return new NonBlockingWarning(
                severity,
                operation,
                component,
                artifact,
                failureType,
                failureMessage,
                repairHint,
                message
        );
    }

    public String exceptionClass() {
        return failureType;
    }

    public enum Severity {
        WARN
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
