package com.ai.skills.mcp.context;

public final class McpRunArtifacts {
    public static final String MANIFEST               = "manifests/run-manifest.md";
    public static final String INPUT_SNAPSHOT         = "prompt/input-snapshot.md";
    public static final String STATE                  = review("run-state.json");
    public static final String SCORE_SUPPORT          = review("score-support.md");
    public static final String CHECKLIST              = review("artifact-checklist.md");
    public static final String DIFF                   = review("thin-no-index.diff");
    public static final String COMMAND_RESULT         = validation("command-result.md");
    public static final String TESTNG_RESULTS         = validation("testng-results.xml");
    public static final String VALIDATION_ROOT        = McpArtifactPaths.VALIDATION_ROOT;
    public static final String REPORTS_ROOT           = validation("reports");
    public static final String EXTENT_REPORT          = REPORTS_ROOT + "/ExtentReport.html";
    public static final String EXTENT_SHA256          = REPORTS_ROOT + "/ExtentReport.sha256";
    public static final String EXTENT_ORIGIN          = REPORTS_ROOT + "/extent-origin.json";
    public static final String BUSINESS_UNDERSTANDING = review("business-understanding.md");
    public static final String LESSON_LEARNED         = review("lesson-learned.md");
    public static final String FRONTIER_DECISION      = review("frontier-decision.md");
    public static final String SUMMARY                = "SUMMARY.md";

    private McpRunArtifacts() {
    }

    public static String runArtifact(String runId, String artifact) {
        return McpArtifactPaths.RUNS_ROOT + "/" + runId + "/" + artifact;
    }

    public static String linkFromFinalReport(String runId, String artifact) {
        return McpArtifactPaths.linkFromFinalReport(runArtifact(runId, artifact));
    }

    private static String review(String fileName) {
        return McpArtifactPaths.REVIEW_ROOT + "/" + fileName;
    }

    private static String validation(String fileName) {
        return McpArtifactPaths.VALIDATION_ROOT + "/" + fileName;
    }
}
