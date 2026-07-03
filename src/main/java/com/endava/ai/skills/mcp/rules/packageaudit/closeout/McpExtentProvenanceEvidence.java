package com.endava.ai.skills.mcp.rules.packageaudit.closeout;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contracts.execution.McpExecutionProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class McpExtentProvenanceEvidence {
    private McpExtentProvenanceEvidence() {
    }

    public static void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path report = context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_REPORT);
        Path shaFile = context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_SHA256);
        Path origin = context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_ORIGIN);
        if (!Files.isRegularFile(report)) {
            return;
        }
        String actualSha = McpCloseoutText.sha256(report);
        try {
            if (Files.isRegularFile(shaFile)) {
                String expectedSha = McpCloseoutText.firstSha(Files.readString(shaFile));
                if (expectedSha == null) {
                    result.add(McpPackageViolation.blocker("mcp.extent.sha256.invalid",
                            McpArtifactPaths.MCP_EXTENT_SHA256
                                    + " must contain a 64-character SHA-256 digest",
                            shaFile));
                } else if (!expectedSha.equalsIgnoreCase(actualSha)) {
                    result.add(McpPackageViolation.blocker("mcp.extent.hash-mismatch",
                            McpArtifactPaths.MCP_EXTENT_REPORT
                                    + " hash differs from ExtentReport.sha256; final MCP report may not be the last frozen report",
                            report));
                }
            }
            if (Files.isRegularFile(origin)) {
                validateOriginMetadata(Files.readString(origin), origin, report, actualSha, result);
            }
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("mcp.extent.provenance.unreadable",
                    "Cannot read final MCP Extent provenance: " + exception.getMessage(),
                    context.packageRoot().resolve(McpArtifactPaths.MCP_VALIDATION_ROOT)));
        }
    }

    private static void validateOriginMetadata(String json,
                                               Path origin,
                                               Path report,
                                               String actualSha,
                                               McpPackageValidationResult result) throws IOException {
        for (String field : List.of("command", "timestamp", "exit", "sha256", "size")) {
            if (!McpCloseoutText.jsonHasField(json, field) && !(field.equals("exit")
                    && (McpCloseoutText.jsonHasField(json, "exit_code") || McpCloseoutText.jsonHasField(json, "exitCode")))) {
                result.add(McpPackageViolation.blocker("mcp.extent.origin.incomplete",
                        McpArtifactPaths.MCP_EXTENT_ORIGIN + " is missing field: " + field, origin));
            }
        }
        String command = McpCloseoutText.jsonString(json, "command");
        if (command == null || !command.toLowerCase(Locale.ROOT).contains("mvn")
                || !command.toLowerCase(Locale.ROOT).contains("test")
                || !command.toLowerCase(Locale.ROOT).contains("mcp")) {
            result.add(McpPackageViolation.blocker("mcp.extent.origin.not-mcp-maven-test",
                    McpArtifactPaths.MCP_EXTENT_ORIGIN
                            + " command must prove the MCP Maven package close-out execution", origin));
        } else if (!McpCloseoutCommandProfile.hasFinalCloseoutCommand(command)) {
            result.add(McpPackageViolation.blocker("mcp.extent.origin.not-package-closeout",
                    McpArtifactPaths.MCP_EXTENT_ORIGIN
                            + " command must prove the final MCP package close-out subset; expected "
                            + McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName()
                            + ", not full, audit, candidate or ad-hoc MCP tests",
                    origin));
        }
        Integer exit = McpCloseoutText.firstInt(json, "exit", "exit_code", "exitCode");
        if (exit == null || exit != 0) {
            result.add(McpPackageViolation.blocker("mcp.extent.origin-exit.invalid",
                    McpArtifactPaths.MCP_EXTENT_ORIGIN
                            + " must prove the MCP Maven command exited with code 0", origin));
        }
        String originSha = McpCloseoutText.jsonString(json, "sha256");
        if (originSha == null || !originSha.matches("(?i)[a-f0-9]{64}")) {
            result.add(McpPackageViolation.blocker("mcp.extent.origin-sha256.invalid",
                    McpArtifactPaths.MCP_EXTENT_ORIGIN
                            + " must publish a 64-character SHA-256 for ExtentReport.html", origin));
        } else if (!originSha.equalsIgnoreCase(actualSha)) {
            result.add(McpPackageViolation.blocker("mcp.extent.origin-hash-mismatch",
                    McpArtifactPaths.MCP_EXTENT_ORIGIN + " sha256 differs from ExtentReport.html", origin));
        }
        Long originSize = McpCloseoutText.firstLong(json, "size");
        long actualSize = Files.size(report);
        if (originSize == null || originSize < 1) {
            result.add(McpPackageViolation.blocker("mcp.extent.origin-size.invalid",
                    McpArtifactPaths.MCP_EXTENT_ORIGIN + " must publish ExtentReport.html byte size", origin));
        } else if (originSize != actualSize) {
            result.add(McpPackageViolation.blocker("mcp.extent.origin-size-mismatch",
                    McpArtifactPaths.MCP_EXTENT_ORIGIN + " size differs from ExtentReport.html bytes: origin="
                            + originSize + ", actual=" + actualSize,
                    origin));
        }
    }
}

