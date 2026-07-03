package com.ai.core.reporting.listener.internal.runtime.audit.model;

import java.nio.file.Path;
import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class RuntimeAuditTarget {

    public enum Kind {
        CURRENT_REPORT,
        MARKER_MATCH,
        NO_CANDIDATE,
        AMBIGUOUS_CANDIDATES
    }

    private final Kind kind;
    private final Path report;
    private final String reason;

    private RuntimeAuditTarget(Kind kind, Path report, String reason) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.report = report == null ? null : report.toAbsolutePath().normalize();
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public static RuntimeAuditTarget current(Path report) {
        return new RuntimeAuditTarget(Kind.CURRENT_REPORT, report, "Active Extent adapter report");
    }

    public static RuntimeAuditTarget markerMatch(Path report) {
        return new RuntimeAuditTarget(Kind.MARKER_MATCH, report, "Extent report contains current runtime invocation marker");
    }

    public static RuntimeAuditTarget noCandidate() {
        return new RuntimeAuditTarget(Kind.NO_CANDIDATE, null, "No Extent report candidate exists");
    }

    public static RuntimeAuditTarget ambiguousCandidates() {
        return new RuntimeAuditTarget(
                Kind.AMBIGUOUS_CANDIDATES,
                null,
                "Extent report candidates exist, but none contains a current runtime invocation marker"
        );
    }

    public boolean writable() {
        return report != null;
    }
}
