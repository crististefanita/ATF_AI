package com.endava.ai.skills.mcp.context;

public final class McpArtifactPaths {
    public static final String FINAL_REPORT_LINK_PREFIX     = "../";

    public static final String README                       = "README.md";
    public static final String CALIBRATION                  = "CALIBRATION.md";
    public static final String RUNTIME_PROPERTIES           = "docs/prompts/html_ro/mcp/agent-runtime.properties";

    public static final String ANALYSIS_ROOT                = "analysis";
    public static final String FINAL_REPORT                 = ANALYSIS_ROOT + "/langgraph-business-understanding.html";
    public static final String REPORT_CSS                   = ANALYSIS_ROOT + "/shared-report-reference.css";
    public static final String RENDER_PROOF                 = ANALYSIS_ROOT + "/canonical-render-check.png";

    public static final String MANIFESTS_ROOT               = "manifests";
    public static final String PACKAGE_MANIFEST             = MANIFESTS_ROOT + "/package-manifest.md";

    public static final String PROMPT_ROOT                  = "prompt";
    public static final String INPUT_SNAPSHOT               = PROMPT_ROOT + "/input-snapshot.md";

    public static final String REVIEW_ROOT                  = "review";
    public static final String PACKAGE_STATE                = REVIEW_ROOT + "/package-state.json";
    public static final String EXECUTION_GATE_CARD          = REVIEW_ROOT + "/execution-gate-card.md";
    public static final String PACKAGE_CLOSE_OUT            = REVIEW_ROOT + "/package-close-out.md";
    public static final String ROOT_DIFF                    = REVIEW_ROOT + "/root-source-thin-no-index.diff";
    public static final String SOURCE_EVOLUTION_LEDGER      = REVIEW_ROOT + "/source-evolution-ledger.md";
    public static final String LOCAL_LINK_CHECK             = REVIEW_ROOT + "/local-link-check.txt";
    public static final String QUALITY_ACCOUNTING_VERDICT   = REVIEW_ROOT + "/quality-accounting-verdict.md";
    public static final String FRAMEWORK_PROPERTIES_SNAPSHOT = REVIEW_ROOT + "/framework.properties";
    public static final String FRONTIER_RANKING_LEDGER      = REVIEW_ROOT + "/frontier-ranking-ledger.md";
    public static final String NEXT_RUN_ELIGIBILITY_CARD    = REVIEW_ROOT + "/next-run-eligibility-card.md";
    public static final String ROUND_PRETRAINING_BRIEF      = REVIEW_ROOT + "/round-pretraining-brief.md";
    public static final String FROZEN_PACKAGE_INVENTORY     = REVIEW_ROOT + "/frozen-package-inventory.md";

    public static final String VALIDATION_ROOT              = "validation";
    public static final String MCP_VALIDATION_ROOT          = VALIDATION_ROOT + "/mcp";
    public static final String MCP_GATE_RESULT              = MCP_VALIDATION_ROOT + "/mcp-gate-result.md";
    public static final String MCP_EXTENT_REPORT            = MCP_VALIDATION_ROOT + "/ExtentReport.html";
    public static final String MCP_EXTENT_SHA256            = MCP_VALIDATION_ROOT + "/ExtentReport.sha256";
    public static final String MCP_EXTENT_ORIGIN            = MCP_VALIDATION_ROOT + "/extent-origin.json";
    public static final String MCP_TESTNG_XML               = MCP_VALIDATION_ROOT + "/TEST-TestSuite.xml";
    public static final String MCP_ALTERNATE_TESTNG_XML     = MCP_VALIDATION_ROOT + "/testng-results.xml";
    public static final String MCP_METHOD_LEDGER            = MCP_VALIDATION_ROOT + "/full-mcp-method-ledger.md";
    public static final String MCP_KPI_MATRIX               = MCP_VALIDATION_ROOT + "/mcp-kpi-matrix.md";
    public static final String MCP_KPI_MATRIX_HTML          = MCP_VALIDATION_ROOT + "/mcp-kpi-matrix.html";
    public static final String MCP_KPI_FORMULA_TRACE        = MCP_VALIDATION_ROOT + "/mcp-kpi-formula-trace.md";
    public static final String MCP_KPI_REPAIR_BOARD         = MCP_VALIDATION_ROOT + "/mcp-kpi-repair-board.md";
    public static final String MCP_KPI_REPAIR_BOARD_HTML    = MCP_VALIDATION_ROOT + "/mcp-kpi-repair-board.html";
    public static final String MCP_SCORE_CAP_RATIONALE      = MCP_VALIDATION_ROOT + "/mcp-score-cap-rationale.md";

    public static final String PROJECT_SHARED_REPORT_CSS    = "docs/shared-report-reference.css";

    public static final String RUNS_ROOT                    = "runs";

    private McpArtifactPaths() {
    }

    public static String linkFromFinalReport(String artifact) {
        return FINAL_REPORT_LINK_PREFIX + artifact;
    }

    public static String fileName(String artifact) {
        int slash = artifact.lastIndexOf('/');
        return slash < 0 ? artifact : artifact.substring(slash + 1);
    }
}
