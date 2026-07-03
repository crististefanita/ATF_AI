package com.ai.skills.mcp.provenance;

import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.context.McpRunArtifacts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

final class McpExtentOriginVerifier {
    private final McpExtentOriginCommandPolicy commandPolicy = new McpExtentOriginCommandPolicy();

    List<McpPackageViolation> verifySnapshot(Path runDirectory, Path extentReport) {
        Path origin = runDirectory.resolve(McpRunArtifacts.EXTENT_ORIGIN);
        if (!Files.isRegularFile(origin)) {
            return List.of(McpPackageViolation.blocker("run.extent.origin.missing",
                    "Missing extent-origin.json with Maven command, timestamp, exit code, size and sha256",
                    origin));
        }
        try {
            String content = Files.readString(origin);
            Optional<McpExtentOriginMetadata> metadata = McpExtentOriginMetadata.parse(content);
            List<McpPackageViolation> violations = new ArrayList<>();
            if (metadata.isEmpty()) {
                violations.add(McpPackageViolation.blocker("run.extent.origin.invalid-json",
                        "extent-origin.json must be valid JSON metadata, not a free-form provenance note", origin));
            } else {
                violations.addAll(validateRequiredFields(metadata.get(), origin));
                violations.addAll(validateOriginHash(metadata.get(), origin, extentReport));
                violations.addAll(validateOriginSize(metadata.get(), origin, extentReport));
                violations.addAll(validateExitCode(metadata.get(), origin));
                violations.addAll(validateTimestamp(metadata.get(), origin));
            }
            violations.addAll(validateNoPostRunEdit(content, origin));
            return violations;
        } catch (IOException exception) {
            return List.of(McpPackageViolation.blocker("run.extent.origin.unreadable",
                    "Cannot read extent-origin.json: " + exception.getMessage(), origin));
        }
    }

    List<McpPackageViolation> verifyNativeMavenTest(Path runDirectory) {
        Path origin = runDirectory.resolve(McpRunArtifacts.EXTENT_ORIGIN);
        if (!Files.isRegularFile(origin)) {
            return List.of();
        }
        try {
            String content = Files.readString(origin);
            Optional<McpExtentOriginMetadata> metadata = McpExtentOriginMetadata.parse(content);
            return commandPolicy.validate(origin, content, metadata);
        } catch (IOException exception) {
            return List.of(McpPackageViolation.blocker("run.extent.origin.unreadable",
                    "Cannot read extent-origin.json: " + exception.getMessage(), origin));
        }
    }

    private List<McpPackageViolation> validateRequiredFields(McpExtentOriginMetadata metadata, Path origin) {
        List<String> missing = ContractCatalog.extentProvenanceTokens("origin-required-field").stream()
                .filter(token -> !metadata.has(token))
                .collect(Collectors.toList());
        if (!metadata.has("exit") && (metadata.has("exit_code") || metadata.has("exitCode"))) {
            missing.remove("exit");
        }
        if (missing.isEmpty()) {
            return List.of();
        }
        return List.of(McpPackageViolation.blocker("run.extent.origin.incomplete",
                "extent-origin.json is missing Maven provenance field(s): " + missing, origin));
    }

    private List<McpPackageViolation> validateNoPostRunEdit(String content, Path origin) {
        String lower = content.toLowerCase(Locale.ROOT);
        List<String> forbiddenEditMarkers = McpExtentOriginEditMarkers.forbiddenTokensIn(lower);
        if (forbiddenEditMarkers.isEmpty() && !McpExtentOriginEditMarkers.declaresAgentEdit(lower)) {
            return List.of();
        }
        return List.of(McpPackageViolation.blocker("run.extent.origin-post-run-edit",
                "extent-origin.json declares or hints that ExtentReport.html was edited after Maven generation: "
                        + forbiddenEditMarkers,
                origin));
    }

    private List<McpPackageViolation> validateOriginHash(McpExtentOriginMetadata metadata, Path origin, Path extentReport) {
        String originSha = metadata.sha256();
        if (originSha == null) {
            return List.of(McpPackageViolation.blocker("run.extent.origin-sha256.invalid",
                    "extent-origin.json must publish a 64-character sha256 for ExtentReport.html", origin));
        }
        if (originSha.equalsIgnoreCase(McpExtentDigest.sha256(extentReport))) {
            return List.of();
        }
        return List.of(McpPackageViolation.blocker("run.extent.origin-hash-mismatch",
                "extent-origin.json sha256 differs from ExtentReport.html", origin));
    }

    private List<McpPackageViolation> validateOriginSize(McpExtentOriginMetadata metadata, Path origin, Path extentReport) {
        Long originSize = metadata.longValue("size");
        if (originSize == null || originSize < 1) {
            return List.of(McpPackageViolation.blocker("run.extent.origin-size.invalid",
                    "extent-origin.json must publish the positive byte size of ExtentReport.html", origin));
        }
        try {
            long actualSize = Files.size(extentReport);
            if (originSize == actualSize) {
                return List.of();
            }
            return List.of(McpPackageViolation.blocker("run.extent.origin-size-mismatch",
                    "extent-origin.json size differs from ExtentReport.html bytes: origin="
                            + originSize + ", actual=" + actualSize,
                    origin));
        } catch (IOException exception) {
            return List.of(McpPackageViolation.blocker("run.extent.origin-size.unreadable",
                    "Cannot read ExtentReport.html size while validating extent-origin.json: " + exception.getMessage(),
                    extentReport));
        }
    }

    private List<McpPackageViolation> validateExitCode(McpExtentOriginMetadata metadata, Path origin) {
        Integer exit = metadata.intValue("exit");
        if (exit == null) {
            exit = metadata.intValue("exit_code");
        }
        if (exit == null) {
            exit = metadata.intValue("exitCode");
        }
        if (exit != null && exit == 0) {
            return List.of();
        }
        return List.of(McpPackageViolation.blocker("run.extent.origin-exit.invalid",
                "extent-origin.json must prove the Maven test command exited with code 0", origin));
    }

    private List<McpPackageViolation> validateTimestamp(McpExtentOriginMetadata metadata, Path origin) {
        Instant timestamp = metadata.timestamp("timestamp");
        if (timestamp == null) {
            return List.of(McpPackageViolation.blocker("run.extent.origin-timestamp.invalid",
                    "extent-origin.json timestamp must be parseable ISO date-time metadata", origin));
        }
        Instant generatedAt = metadata.timestamp("generated_at");
        Instant finishedAt = metadata.timestamp("finished_at");
        if (metadata.has("generated_at") && generatedAt == null) {
            return List.of(McpPackageViolation.blocker("run.extent.origin-generated-at.invalid",
                    "extent-origin.json generated_at must be parseable when it is published", origin));
        }
        if (metadata.has("finished_at") && finishedAt == null) {
            return List.of(McpPackageViolation.blocker("run.extent.origin-finished-at.invalid",
                    "extent-origin.json finished_at must be parseable when it is published", origin));
        }
        if (generatedAt == null || finishedAt == null || !generatedAt.isAfter(finishedAt)) {
            return List.of();
        }
        return List.of(McpPackageViolation.blocker("run.extent.origin-freeze-order.invalid",
                "extent-origin.json generated_at must not be after finished_at", origin));
    }

}
