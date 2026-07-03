package com.endava.ai.core.reporting.adapters.extent.patch.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class ExtentHtmlPatchResult {
    private final String html;
    private final List<ExtentHtmlPatchRecord> records;

    public ExtentHtmlPatchResult(String html, List<ExtentHtmlPatchRecord> records) {
        this.html = html == null ? "" : html;
        this.records = Collections.unmodifiableList(new ArrayList<>(records == null ? List.of() : records));
    }

    public boolean changed() {
        return records.stream().anyMatch(record -> record.outcome() == PatchOutcome.APPLIED);
    }
}
