package com.endava.ai.skills.mcp.validation;

import com.endava.ai.skills.mcp.context.McpProjectLocator;

import java.io.IOException;
import java.nio.file.Path;

final class McpLatestPackage {
    private McpLatestPackage() {
    }

    static Path projectRoot() {
        return McpProjectLocator.projectRoot();
    }

    static Path resolve() throws IOException {
        return McpProjectLocator.packageRoot();
    }
}
