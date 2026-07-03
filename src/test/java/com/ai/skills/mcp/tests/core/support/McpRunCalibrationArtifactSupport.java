package com.ai.skills.mcp.tests.core.support;

import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import com.ai.skills.mcp.tests.support.McpTestDigests;

import java.nio.file.Path;

final class McpRunCalibrationArtifactSupport {
    private McpRunCalibrationArtifactSupport() {
    }

    static Path mcpRoot(Path packageRoot) {
        return packageRoot.resolve(McpArtifactPaths.MCP_VALIDATION_ROOT);
    }

    static Path analysisRoot(Path packageRoot) {
        return packageRoot.resolve(McpArtifactPaths.ANALYSIS_ROOT);
    }

    static String artifactHref(String artifact, String label) {
        return "<a href=\"../" + artifact + "\">" + label + "</a>";
    }

    static String packageCloseoutCommand(Path packageRoot) {
        return McpExecutionProfile.PACKAGE_CLOSEOUT.mavenCommand()
                .replace(" test", " -Dmcp.package.root=" + packageRoot + " test");
    }

    static String fileName(String artifactPath) {
        return McpArtifactPaths.fileName(artifactPath);
    }

    static String hashExtentReport(Path path) {
        return McpTestDigests.sha256(path);
    }

    static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
