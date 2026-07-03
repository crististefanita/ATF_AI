package com.endava.ai.skills.atlas.tests;

import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
class AtlasUiSummary implements AggregateExtentReportWriter.SummaryRecord {
    int total;
    long passed;
    long failed;
    long skipped;

    static AtlasUiSummary allPassed(int total) {
        return new AtlasUiSummary(total, total, 0, 0);
    }
}
