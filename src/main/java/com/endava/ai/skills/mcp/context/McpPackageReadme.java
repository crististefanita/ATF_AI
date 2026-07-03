package com.endava.ai.skills.mcp.context;

import java.nio.file.Files;
import java.nio.file.Path;

public final class McpPackageReadme {
    private static final String README = "README.md";

    private McpPackageReadme() {
    }

    public static Path canonicalFor(Path packageRoot) {
        Path packageReadme = packageRoot.resolve(README);
        if (Files.isRegularFile(packageReadme)) {
            return packageReadme;
        }
        return packageRoot.resolveSibling(README);
    }
}
