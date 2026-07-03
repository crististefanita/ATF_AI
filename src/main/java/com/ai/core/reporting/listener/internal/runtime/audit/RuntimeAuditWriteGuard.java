package com.ai.core.reporting.listener.internal.runtime.audit;

import java.io.IOException;
import java.nio.file.Path;

public final class RuntimeAuditWriteGuard {
    private static final String PUBLISHED_FROZEN_ARTIFACT_MARKER = "/published-frozen-artifacts/";

    private RuntimeAuditWriteGuard() {
    }

    public static void requireMutableRuntimeReport(Path report) throws IOException {
        requireMutableRuntimeReport(report, false);
    }

    public static void requireMutableRuntimeReport(Path report, boolean activeRuntimeReport) throws IOException {
        if (activeRuntimeReport) {
            return;
        }
        String normalized = report.toAbsolutePath().normalize().toString().replace('\\', '/');
        if (normalized.contains("/docs/out/")
                || normalized.contains("/docs/atlas/validation/")
                || normalized.contains(PUBLISHED_FROZEN_ARTIFACT_MARKER)) {
            throw new IOException("Refusing to rewrite frozen Extent artifact: " + report);
        }
    }
}
