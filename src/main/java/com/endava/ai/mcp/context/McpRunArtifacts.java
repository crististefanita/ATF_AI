package com.endava.ai.mcp.context;

public final class McpRunArtifacts {
    public static final String MANIFEST               = "manifests/run-manifest.md";
    public static final String INPUT_SNAPSHOT         = "prompt/input-snapshot.md";
    public static final String STATE                  = "review/run-state.json";
    public static final String SCORE_SUPPORT          = "review/score-support.md";
    public static final String CHECKLIST              = "review/artifact-checklist.md";
    public static final String DIFF                   = "review/thin-no-index.diff";
    public static final String COMMAND_RESULT         = "validation/command-result.md";
    public static final String TESTNG_RESULTS         = "validation/testng-results.xml";
    public static final String VALIDATION_ROOT        = "validation";
    public static final String REPORTS_ROOT           = "validation/reports";
    public static final String BUSINESS_UNDERSTANDING = "review/business-understanding.md";
    public static final String LESSON_LEARNED         = "review/lesson-learned.md";
    public static final String FRONTIER_DECISION      = "review/frontier-decision.md";
    public static final String SUMMARY                = "SUMMARY.md";

    private McpRunArtifacts() {
    }
}
