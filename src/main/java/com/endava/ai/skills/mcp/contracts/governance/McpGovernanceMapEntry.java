package com.endava.ai.skills.mcp.contracts.governance;

import com.endava.ai.skills.mcp.contracts.execution.McpExecutionProfile;

import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;

import static com.endava.ai.core.support.contract.yaml.YamlScalars.requiredBoolean;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.requiredString;

/**
 * One executable check reference connected to its source location.
 */
@Getter
@Accessors(fluent = true)
public final class McpGovernanceMapEntry implements Comparable<McpGovernanceMapEntry> {
    private final String checkRef;
    private final String testLocation;

    public McpGovernanceMapEntry(String checkRef, String testLocation) {
        this.checkRef = requireText(checkRef, "checkRef");
        this.testLocation = requireText(testLocation, "testLocation");
    }

    @Override
    public int compareTo(McpGovernanceMapEntry other) {
        int byCheck = checkRef.compareTo(other.checkRef);
        return byCheck == 0 ? testLocation.compareTo(other.testLocation) : byCheck;
    }

    private static String requireText(String value, String field) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Missing MCP governance map " + field);
        }
        return normalized;
    }

    @Getter
    @Accessors(fluent = true)
    public static final class AtlasQualityBridgeSpec {
        private final String id;
        private final McpExecutionProfile mcpProfile;
        private final String mcpStage;
        private final String atlasLane;
        private final String finalReportSurface;
        private final String mcpResponsibility;
        private final String atlasResponsibility;
        private final String agentAction;
        private final boolean blocking;

        private AtlasQualityBridgeSpec(Map<String, String> entry, String resourcePath) {
            this.id = required(entry, "id", resourcePath);
            this.mcpProfile = McpExecutionProfile.fromId(required(entry, "mcpProfile", resourcePath));
            this.mcpStage = required(entry, "mcpStage", resourcePath);
            this.atlasLane = required(entry, "atlasLane", resourcePath);
            this.finalReportSurface = required(entry, "finalReportSurface", resourcePath);
            this.mcpResponsibility = required(entry, "mcpResponsibility", resourcePath);
            this.atlasResponsibility = required(entry, "atlasResponsibility", resourcePath);
            this.agentAction = required(entry, "agentAction", resourcePath);
            this.blocking = requiredBoolean(entry, "blocking", resourcePath);
        }

        public static AtlasQualityBridgeSpec from(Map<String, String> entry, String resourcePath) {
            return new AtlasQualityBridgeSpec(entry, resourcePath);
        }

        public String mcpProfileId() {
            return mcpProfile.id();
        }

        private static String required(Map<String, String> entry, String key, String resourcePath) {
            return Objects.requireNonNull(requiredString(entry, key, resourcePath, "MCP Atlas quality bridge"));
        }
    }
}
