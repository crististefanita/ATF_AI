package com.endava.ai.skills.mcp.provenance;

import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

final class McpExtentOriginCommandPolicy {

    List<McpPackageViolation> validate(Path origin, String content, Optional<McpExtentOriginMetadata> metadata) {
        List<McpPackageViolation> violations = new ArrayList<>();
        violations.addAll(validateNoForbiddenPostRunWriter(origin, content));
        metadata.ifPresent(value -> violations.addAll(validateMavenTestCommand(origin, value)));
        return violations;
    }

    private List<McpPackageViolation> validateNoForbiddenPostRunWriter(Path origin, String content) {
        String lower = content.toLowerCase(Locale.ROOT);
        List<String> forbiddenWriters = ContractCatalog.extentProvenanceTokens("origin-forbidden-writer-token")
                .stream()
                .filter(lower::contains)
                .collect(Collectors.toList());
        if (forbiddenWriters.isEmpty()) {
            return List.of();
        }
        return List.of(McpPackageViolation.blocker("run.extent.origin.post-run-writer",
                "extent-origin.json points to a post-run writer/support command instead of the test execution command: "
                        + forbiddenWriters,
                origin));
    }

    private List<McpPackageViolation> validateMavenTestCommand(Path origin, McpExtentOriginMetadata metadata) {
        String command = metadata.stringValue("command");
        if (command == null || command.isBlank()) {
            return List.of();
        }
        if (!McpExtentOriginCommand.looksLikeMavenTestCommand(command)) {
            return List.of(McpPackageViolation.blocker("run.extent.origin.not-maven-test",
                    "extent-origin.json command must be an auditable Maven test command, not a compile/copy/proof step: "
                            + command,
                    origin));
        }
        if (McpExtentOriginCommand.hasConcreteTestSelector(command)) {
            return List.of();
        }
        return List.of(McpPackageViolation.warning("run.extent.origin.command-broad",
                "extent-origin.json command is a Maven test command but has no explicit -Dtest or suite selector: "
                        + command,
                origin));
    }
}
