package com.endava.ai.skills.mcp.contracts.execution;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class McpCloseoutEvidenceStep {
    private final String id;
    private final McpExecutionProfile profile;
    private final String purpose;
    private final String commandTemplate;
    private final List<McpArtifactFamily> artifactFamilies;
    private final String repairTarget;

    private McpCloseoutEvidenceStep(String id,
                                    McpExecutionProfile profile,
                                    String purpose,
                                    String commandTemplate,
                                    List<McpArtifactFamily> artifactFamilies,
                                    String repairTarget) {
        this.id = requireText(id, "id");
        this.profile = Objects.requireNonNull(profile, "profile");
        this.purpose = requireText(purpose, "purpose");
        this.commandTemplate = requireText(commandTemplate, "commandTemplate");
        this.artifactFamilies = List.copyOf(Objects.requireNonNull(artifactFamilies, "artifactFamilies"));
        this.repairTarget = requireText(repairTarget, "repairTarget");
        if (this.artifactFamilies.isEmpty()) {
            throw new IllegalArgumentException("Close-out evidence step must name at least one artifact family: " + id);
        }
    }

    public static McpCloseoutEvidenceStep of(String id,
                                             McpExecutionProfile profile,
                                             String purpose,
                                             String commandTemplate,
                                             List<McpArtifactFamily> artifactFamilies,
                                             String repairTarget) {
        return new McpCloseoutEvidenceStep(id, profile, purpose, commandTemplate, artifactFamilies, repairTarget);
    }

    public String artifactFamilyNames() {
        return artifactFamilies.stream()
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing close-out evidence step " + field);
        }
        return value;
    }
}
