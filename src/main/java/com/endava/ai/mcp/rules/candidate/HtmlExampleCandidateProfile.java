package com.endava.ai.mcp.rules.candidate;

import com.endava.ai.mcp.context.McpPackageContext;

public final class HtmlExampleCandidateProfile {
    private final int teacherThreshold;
    private final int minimumTraceRunStates;
    private final int minimumTraceRunDiffs;

    private HtmlExampleCandidateProfile(int teacherThreshold, int minimumTraceRunStates, int minimumTraceRunDiffs) {
        this.teacherThreshold = teacherThreshold;
        this.minimumTraceRunStates = minimumTraceRunStates;
        this.minimumTraceRunDiffs = minimumTraceRunDiffs;
    }

    public static HtmlExampleCandidateProfile from(McpPackageContext context) {
        return new HtmlExampleCandidateProfile(
                context.runtime().expectedDeltaQualityAsOverallCap(),
                McpCandidateThresholds.MIN_TRACE_RUN_STATES,
                McpCandidateThresholds.MIN_TRACE_RUN_DIFFS);
    }

    public int teacherThreshold() {
        return teacherThreshold;
    }

    public int minimumTraceRunStates() {
        return minimumTraceRunStates;
    }

    public int minimumTraceRunDiffs() {
        return minimumTraceRunDiffs;
    }
}
