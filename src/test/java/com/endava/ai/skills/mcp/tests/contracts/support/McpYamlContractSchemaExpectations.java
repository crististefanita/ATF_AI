package com.endava.ai.skills.mcp.tests.contracts.support;

import com.endava.ai.skills.mcp.contracts.execution.McpExecutionProfile;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class McpYamlContractSchemaExpectations {
    public static final String PROFILE_FULL = McpExecutionProfile.FULL.id();
    public static final String PROFILE_PACKAGE_AUDIT = McpExecutionProfile.PACKAGE_AUDIT.id();
    public static final String PROFILE_PACKAGE_CLOSEOUT = McpExecutionProfile.PACKAGE_CLOSEOUT.id();
    public static final String PROFILE_POST_SYNC_SMOKE = McpExecutionProfile.POST_SYNC_SMOKE.id();
    public static final String PROFILE_FINAL_REPORT_CLOSEOUT = McpExecutionProfile.FINAL_REPORT_CLOSEOUT.id();
    public static final String PROFILE_LANGGRAPH_FINAL_REPORT = McpExecutionProfile.LANGGRAPH_FINAL_REPORT.id();
    public static final Set<String> REQUIRED_ROOT_KPIS = Set.of(
            "root_diff_files_count",
            "src_main_files",
            "src_test_files",
            "files_added",
            "files_modified",
            "files_deleted",
            "test_methods_added",
            "protected_root_dirty_count");
    public static final Set<String> PRIMARY_PROFILE_IDS = Set.of(
            McpExecutionProfile.SELF.id(),
            PROFILE_FULL,
            PROFILE_PACKAGE_AUDIT,
            PROFILE_PACKAGE_CLOSEOUT,
            McpExecutionProfile.CANDIDATE.id(),
            McpExecutionProfile.HTML_EX_CORPUS.id());
    public static final List<String> EXPECTED_AGENTIC_FLOW = List.of(
            McpExecutionProfile.SELF.id(),
            McpExecutionProfile.RUN_PREFLIGHT.id(),
            PROFILE_PACKAGE_AUDIT,
            PROFILE_PACKAGE_CLOSEOUT,
            PROFILE_POST_SYNC_SMOKE,
            PROFILE_FINAL_REPORT_CLOSEOUT,
            PROFILE_LANGGRAPH_FINAL_REPORT,
            McpExecutionProfile.CANDIDATE.id());
    public static final Set<String> REQUIRED_ATLAS_QUALITY_LANES = Set.of(
            "atlas.quality.business-map",
            "atlas.quality.source-trace",
            "atlas.quality.score-truth",
            "atlas.quality.graph-proof",
            "atlas.quality.validation-audit");
    public static final Path HTML_EN_PROMPT_ROOT = Path.of("docs", "prompts", "html_EN");
    public static final Path HTML_RO_PROMPT_ROOT = Path.of("docs", "prompts", "html_ro");
    public static final Path PROMPT_OWNERSHIP_ROOT = Path.of("docs", "prompts");
    public static final Path HTML_CONTRACT_OWNERSHIP =
            Path.of("docs", "prompts", "html-contract-ownership.yml");
    public static final Path PROJECT_SHARED_REPORT_CSS = Path.of("docs", "shared-report-reference.css");
    public static final Path PROMPT_SHARED_REPORT_CSS =
            Path.of("docs", "prompts", "assets", "shared-report-reference.css");
    public static final List<Path> MCP_AGENTIC_PROFILE_PROMPTS = List.of(
            HTML_EN_PROMPT_ROOT.resolve("mcp").resolve("orchestrate-iterative-runs.html"),
            HTML_RO_PROMPT_ROOT.resolve("mcp").resolve("orchestrate-iterative-runs.html"),
            HTML_EN_PROMPT_ROOT.resolve("mcp").resolve("execute-and-understand-run.html"),
            HTML_RO_PROMPT_ROOT.resolve("mcp").resolve("execute-and-understand-run.html"),
            HTML_EN_PROMPT_ROOT.resolve("mcp").resolve("reporting.html"),
            HTML_RO_PROMPT_ROOT.resolve("mcp").resolve("reporting.html"));
    public static final Set<String> HTML_CONTRACT_OWNERS = Set.of("MCP", "Atlas", "Shared");
    public static final Set<String> HTML_CONTRACT_APPLIES_TO = Set.of("MCP", "Atlas", "Core");
    public static final Set<String> HTML_CONTRACT_USAGES = Set.of("referenced", "informational");
    public static final Pattern PROMPT_FILE_REFERENCE =
            Pattern.compile("([A-Za-z0-9_.-]+\\.(?:html|properties))");
    public static final Path CONTRACT_RESOURCE_ROOT = Path.of("src/main/resources");

    private McpYamlContractSchemaExpectations() {
    }
}
