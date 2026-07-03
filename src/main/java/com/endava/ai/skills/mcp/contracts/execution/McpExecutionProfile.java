package com.endava.ai.skills.mcp.contracts.execution;

import com.endava.ai.core.support.contract.execution.AtfExecutionProfile;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public enum McpExecutionProfile implements AtfExecutionProfile {
    SANITY("mcp-sanity", "src/test/resources/skills/mcp/testng-mcp-sanity.xml"),
    RUN("mcp-run", "src/test/resources/skills/mcp/testng-mcp-run.xml"),
    RUN_PREFLIGHT("mcp-run-preflight", "src/test/resources/skills/mcp/testng-mcp-run-preflight.xml"),
    CANDIDATE("mcp-candidate", "src/test/resources/skills/mcp/testng-mcp-candidate.xml"),
    HTML_EX_CORPUS("mcp-html-ex-corpus", "src/test/resources/skills/mcp/testng-mcp-html-ex-corpus.xml"),
    SELF("mcp-self", "src/test/resources/skills/mcp/testng-mcp-self.xml"),
    PACKAGE_AUDIT("mcp-package-audit", "src/test/resources/skills/mcp/testng-mcp-package-audit.xml"),
    POST_SYNC_SMOKE("mcp-post-sync-smoke", "src/test/resources/skills/mcp/testng-mcp-post-sync-smoke.xml"),
    FINAL_REPORT_CLOSEOUT("mcp-final-report-closeout", "src/test/resources/skills/mcp/testng-mcp-final-report-closeout.xml"),
    LANGGRAPH_FINAL_REPORT("mcp-langgraph-final-report", "src/test/resources/skills/mcp/testng-mcp-langgraph-final-report.xml"),
    PACKAGE_CLOSEOUT("mcp-package-closeout", "src/test/resources/skills/mcp/testng-mcp-package-closeout.xml"),
    FULL("mcp-full", "src/test/resources/skills/mcp/testng-mcp-full.xml");

    private final String id;
    private final String suiteXml;

    McpExecutionProfile(String id, String suiteXml) {
        this.id = id;
        this.suiteXml = suiteXml;
    }

    public static McpExecutionProfile fromId(String id) {
        String normalized = id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(profile -> profile.id.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown MCP execution profile: " + id));
    }

    public static List<String> ids() {
        return Arrays.stream(values())
                .map(McpExecutionProfile::id)
                .collect(Collectors.toUnmodifiableList());
    }
}
