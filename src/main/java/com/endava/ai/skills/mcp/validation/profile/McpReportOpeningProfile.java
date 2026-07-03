package com.endava.ai.skills.mcp.validation.profile;

import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;

import java.util.List;

public final class McpReportOpeningProfile {
    public static final String SCORE_WALL_CLASS = "class=\"score-wall\"";
    public static final String SCORE_CARD_CLASS = "class=\"score-card\"";
    public static final String EXECUTIVE_CONTEXT_CLASS = "executive-context";

    private McpReportOpeningProfile() {
    }

    public static List<String> canonicalScoreCards() {
        return ContractCatalog.reportTemplateTokens("score-card-label");
    }

    public static List<String> contractArtifactLabels() {
        return ContractCatalog.reportTemplateTokens("contract-artifact-label");
    }

    public static List<String> executiveContextEvidence() {
        return ContractCatalog.reportTemplateTokens("executive-context-evidence");
    }

    public static List<String> runtimeConstantsAndCounters() {
        return ContractCatalog.reportTemplateTokens("runtime-token");
    }

    public static List<String> forbiddenCalibrationTokens() {
        return ContractCatalog.reportTemplateTokens("forbidden-calibration-token");
    }
}
