package com.ai.core.reporting.adapters.extent.synthetic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class SyntheticExtentReportResult {
    private final Path report;
    private final String marker;

    SyntheticExtentReportResult(Path report, String marker) {
        this.report = report;
        this.marker = marker == null ? "" : marker;
    }

    public String html() throws IOException {
        return Files.readString(report);
    }
}
