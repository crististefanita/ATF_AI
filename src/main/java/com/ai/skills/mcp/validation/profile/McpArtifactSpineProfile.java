package com.ai.skills.mcp.validation.profile;

import com.ai.skills.mcp.contracts.catalog.ContractCatalog;

import java.util.List;

public final class McpArtifactSpineProfile {
    private McpArtifactSpineProfile() {
    }

    public static List<String> packageArtifacts() {
        return ContractCatalog.reportTemplateTokens("package-artifact");
    }

    public static List<String> runArtifacts() {
        return ContractCatalog.reportTemplateTokens("run-artifact");
    }

    public static List<String> proofFamilies() {
        return ContractCatalog.reportTemplateTokens("proof-family");
    }

    public static List<String> proofMappingTokens() {
        return ContractCatalog.reportTemplateTokens("proof-mapping-token");
    }

    public static List<String> artifactIndexTokens() {
        return ContractCatalog.reportTemplateTokens("artifact-index-token");
    }
}
