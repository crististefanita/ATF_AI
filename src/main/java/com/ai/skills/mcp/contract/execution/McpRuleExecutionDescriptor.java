package com.ai.skills.mcp.contract.execution;

import com.ai.skills.mcp.contract.McpContractSpec;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.contracts.execution.McpArtifactFamily;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Typed execution metadata derived from the executable rule contract.
 */
@Getter
@Accessors(fluent = true)
public final class McpRuleExecutionDescriptor {
    private final String ruleId;
    private final String ruleClassName;
    private final McpRuleFamily family;
    private final Optional<McpArtifactFamily> artifactFamily;
    private final McpRuleCost cost;
    private final String repairRoute;

    private McpRuleExecutionDescriptor(String ruleId,
                                       String ruleClassName,
                                       McpRuleFamily family,
                                       Optional<McpArtifactFamily> artifactFamily,
                                       McpRuleCost cost,
                                       String repairRoute) {
        this.ruleId = requireText(ruleId, "ruleId");
        this.ruleClassName = requireText(ruleClassName, "ruleClassName");
        this.family = Objects.requireNonNull(family, "family");
        this.artifactFamily = Objects.requireNonNull(artifactFamily, "artifactFamily");
        this.cost = Objects.requireNonNull(cost, "cost");
        this.repairRoute = requireText(repairRoute, "repairRoute");
    }

    public static McpRuleExecutionDescriptor from(McpValidationRule rule) {
        Objects.requireNonNull(rule, "rule");
        McpContractSpec contract = Objects.requireNonNull(rule.contractSpec(), "contractSpec");
        McpRuleFamily family = familyFor(contract);
        return new McpRuleExecutionDescriptor(
                contract.id(),
                rule.getClass().getName(),
                family,
                artifactFamilyFor(contract),
                costFor(contract, family),
                repairRouteFor(contract, family));
    }

    public String artifactFamilyName() {
        return artifactFamily.map(Enum::name).orElse("UNMAPPED");
    }

    private static McpRuleFamily familyFor(McpContractSpec contract) {
        String id = normalizedId(contract);
        if (id.startsWith("candidate.")) {
            return McpRuleFamily.CANDIDATE_HTML_EX;
        }
        if (id.startsWith("mcp.gate.")) {
            return McpRuleFamily.MCP_CLOSEOUT;
        }
        if (id.startsWith("run.")) {
            return McpRuleFamily.RUN_EVIDENCE;
        }
        if (id.startsWith("ui.") || id.startsWith("ui-api-")) {
            return McpRuleFamily.UI_ARCHITECTURE;
        }
        if (id.startsWith("source.")) {
            return McpRuleFamily.SOURCE_EVOLUTION;
        }
        if (id.startsWith("score.")) {
            return McpRuleFamily.SCORE_GUARD;
        }
        if (id.startsWith("report.")) {
            return McpRuleFamily.REPORT_SHAPE;
        }
        if (id.startsWith("business.") || id.startsWith("claim-action.")) {
            return McpRuleFamily.BUSINESS_PROOF;
        }
        if (id.startsWith("pretraining.")) {
            return McpRuleFamily.PRETRAINING;
        }

        switch (contract.category()) {
            case RUNTIME:
                return McpRuleFamily.RUNTIME;
            case PACKAGE:
                return McpRuleFamily.PACKAGE_ARTIFACT;
            case RUN:
                return McpRuleFamily.RUN_EVIDENCE;
            case REPORT:
            case GRAPH:
                return McpRuleFamily.REPORT_SHAPE;
            case SCORE:
                return McpRuleFamily.SCORE_GUARD;
            case PRETRAINING:
                return McpRuleFamily.PRETRAINING;
            case UI_WRAPPER:
                return McpRuleFamily.UI_ARCHITECTURE;
            case LINKS:
                return McpRuleFamily.LINK_AUDIT;
            case SOURCE:
                return McpRuleFamily.SOURCE_EVOLUTION;
            case MCP_GATE:
                return McpRuleFamily.MCP_CLOSEOUT;
            case HTML_EXAMPLE_CANDIDATE:
                return McpRuleFamily.CANDIDATE_HTML_EX;
            default:
                throw new IllegalArgumentException("Unsupported MCP rule category: " + contract.category());
        }
    }

    private static McpRuleCost costFor(McpContractSpec contract, McpRuleFamily family) {
        String id = normalizedId(contract);
        if (family == McpRuleFamily.MCP_CLOSEOUT || id.contains("closeout") || id.contains("package-closeout")) {
            return McpRuleCost.FINAL_ONLY;
        }
        if (family == McpRuleFamily.RUN_EVIDENCE
                || family == McpRuleFamily.SOURCE_EVOLUTION
                || family == McpRuleFamily.UI_ARCHITECTURE) {
            return McpRuleCost.MEDIUM;
        }
        if (family == McpRuleFamily.CANDIDATE_HTML_EX || family == McpRuleFamily.BUSINESS_PROOF) {
            return McpRuleCost.HEAVY;
        }
        return McpRuleCost.FAST;
    }

    private static Optional<McpArtifactFamily> artifactFamilyFor(McpContractSpec contract) {
        String artifact = contract.artifact();
        if (artifact == null || artifact.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(McpArtifactFamily.values())
                .filter(family -> family.covers(artifact))
                .findFirst();
    }

    private static String repairRouteFor(McpContractSpec contract, McpRuleFamily family) {
        String id = normalizedId(contract);
        if (family == McpRuleFamily.MCP_CLOSEOUT) {
            return McpArtifactPaths.MCP_VALIDATION_ROOT;
        }
        if (family == McpRuleFamily.SOURCE_EVOLUTION) {
            return id.contains("kpi") ? McpArtifactPaths.SOURCE_EVOLUTION_LEDGER + " + "
                    + McpArtifactPaths.MCP_VALIDATION_ROOT + "/mcp-kpi-*" :
                    McpArtifactPaths.ROOT_DIFF + " + " + McpArtifactPaths.SOURCE_EVOLUTION_LEDGER;
        }
        if (family == McpRuleFamily.RUN_EVIDENCE) {
            return "runs/run_##/review + runs/run_##/validation";
        }
        if (family == McpRuleFamily.REPORT_SHAPE || family == McpRuleFamily.LINK_AUDIT) {
            return McpArtifactPaths.FINAL_REPORT;
        }
        if (family == McpRuleFamily.UI_ARCHITECTURE) {
            return "generated src/main + src/test UI/API support only";
        }
        if (family == McpRuleFamily.CANDIDATE_HTML_EX) {
            return "docs/html-ex candidate package";
        }
        String artifact = contract.artifact();
        return artifact == null || artifact.isBlank() ? "package artifact named by rule" : artifact;
    }

    private static String normalizedId(McpContractSpec contract) {
        return contract.id().toLowerCase(Locale.ROOT);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing MCP rule descriptor " + field);
        }
        return value;
    }
}
