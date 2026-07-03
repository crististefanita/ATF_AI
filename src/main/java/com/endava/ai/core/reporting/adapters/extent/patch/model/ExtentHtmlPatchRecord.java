package com.endava.ai.core.reporting.adapters.extent.patch.model;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

@Getter
@Accessors(fluent = true)
public final class ExtentHtmlPatchRecord {
    private final String id;
    private final String marker;
    private final PatchOutcome outcome;
    private final String reason;

    public ExtentHtmlPatchRecord(String id, String marker, PatchOutcome outcome, String reason) {
        this.id = Objects.requireNonNull(id, "id");
        this.marker = marker == null ? "" : marker;
        this.outcome = Objects.requireNonNull(outcome, "outcome");
        this.reason = reason == null ? "" : reason;
    }
}
