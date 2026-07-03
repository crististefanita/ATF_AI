package com.ai.core.reporting.listener.internal.runtime.audit.model;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
public final class RuntimeAuditWriteRecord {
    private final Path report;
    private final Instant timestamp;
    private final String beforeSha256;
    private final String afterSha256;

    public RuntimeAuditWriteRecord(Path report, Instant timestamp, String beforeSha256, String afterSha256) {
        this.report = Objects.requireNonNull(report, "report").toAbsolutePath().normalize();
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.beforeSha256 = requireHashText(beforeSha256, "beforeSha256");
        this.afterSha256 = requireHashText(afterSha256, "afterSha256");
    }

    public boolean changed() {
        return !beforeSha256.equals(afterSha256);
    }

    private static String requireHashText(String value, String name) {
        String text = Objects.requireNonNull(value, name).trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Missing runtime audit hash: " + name);
        }
        return text;
    }
}
