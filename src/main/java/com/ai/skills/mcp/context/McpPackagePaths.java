package com.ai.skills.mcp.context;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class McpPackagePaths {
    private final Path packageRoot;

    public Path root() {
        return packageRoot;
    }

    public Path readme() {
        return packageRoot.resolve(McpArtifactPaths.README);
    }

    public Path report() {
        return packageRoot.resolve(McpArtifactPaths.FINAL_REPORT);
    }

    public Path reportCss() {
        return packageRoot.resolve(McpArtifactPaths.REPORT_CSS);
    }

    public Path renderProof() {
        return packageRoot.resolve(McpArtifactPaths.RENDER_PROOF);
    }

    public Path runsRoot() {
        return packageRoot.resolve(McpArtifactPaths.RUNS_ROOT);
    }

    public Path review(String name) {
        return packageRoot.resolve(McpArtifactPaths.REVIEW_ROOT).resolve(name);
    }

    public Path packageState() {
        return packageRoot.resolve(McpArtifactPaths.PACKAGE_STATE);
    }

    public Path executionGateCard() {
        return packageRoot.resolve(McpArtifactPaths.EXECUTION_GATE_CARD);
    }

    public Path packageCloseOut() {
        return packageRoot.resolve(McpArtifactPaths.PACKAGE_CLOSE_OUT);
    }

    public Path rootDiff() {
        return packageRoot.resolve(McpArtifactPaths.ROOT_DIFF);
    }
}
