package com.ai.skills.mcp.provenance;

import com.ai.skills.mcp.fs.HashService;

import java.nio.file.Path;

final class McpExtentDigest {
    private McpExtentDigest() {
    }

    static String firstSha(String content) {
        return HashService.firstSha256(content);
    }

    static String sha256(Path path) {
        return HashService.sha256OrPath(path);
    }
}
