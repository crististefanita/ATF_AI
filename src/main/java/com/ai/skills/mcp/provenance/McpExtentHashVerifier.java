package com.ai.skills.mcp.provenance;

import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpRunArtifacts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class McpExtentHashVerifier {
    List<McpPackageViolation> verify(Path runDirectory, Path extentReport) {
        Path shaFile = runDirectory.resolve(McpRunArtifacts.EXTENT_SHA256);
        if (!Files.isRegularFile(shaFile)) {
            return List.of(McpPackageViolation.blocker("run.extent.sha256.missing",
                    "Missing ExtentReport.sha256; frozen Extent cannot prove it is unchanged after Maven",
                    shaFile));
        }
        try {
            String expected = McpExtentDigest.firstSha(Files.readString(shaFile));
            if (expected == null) {
                return List.of(McpPackageViolation.blocker("run.extent.sha256.invalid",
                        "ExtentReport.sha256 must contain a 64-character SHA-256 digest", shaFile));
            }
            String actual = McpExtentDigest.sha256(extentReport);
            if (!expected.equalsIgnoreCase(actual)) {
                return List.of(McpPackageViolation.blocker("run.extent.hash-mismatch",
                        "ExtentReport.html hash differs from ExtentReport.sha256; report may have been altered post-run",
                        extentReport));
            }
            return List.of();
        } catch (IOException exception) {
            return List.of(McpPackageViolation.blocker("run.extent.sha256.unreadable",
                    "Cannot read ExtentReport.sha256: " + exception.getMessage(), shaFile));
        }
    }
}
