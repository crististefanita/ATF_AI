package com.ai.skills.mcp.contracts.advisor;

import com.ai.skills.mcp.contracts.catalog.ContractCatalog;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Selects non-blocking advisor entries that apply to the current run shape.
 */
public final class McpAiTaskAdvisorEvaluator {
    private static final int BROAD_SCOPE_WORD_LIMIT = 45;
    private static final Set<String> BROAD_SCOPE_MARKERS = Set.of(
            "all modules", "everything", "whole system", "totul", "toate modulele", "orice zona");

    private McpAiTaskAdvisorEvaluator() {
    }

    public static List<McpAiTaskAdvisorSpec> evaluate(McpAiTaskAdvisorInput input) {
        return evaluate(input, ContractCatalog.aiTaskAdvisorContracts());
    }

    public static List<McpAiTaskAdvisorSpec> evaluate(McpAiTaskAdvisorInput input,
                                                      Collection<McpAiTaskAdvisorSpec> specs) {
        return specs.stream()
                .filter(spec -> applies(spec, input))
                .collect(Collectors.toUnmodifiableList());
    }

    private static boolean applies(McpAiTaskAdvisorSpec spec, McpAiTaskAdvisorInput input) {
        if ("advisor.prompt.scope.too-broad".equals(spec.id())) {
            return scopeLooksTooBroad(input.taskScope());
        }
        if ("advisor.model-tier.too-high".equals(spec.id())) {
            return modelTierLooksTooExpensive(input.modelTier(), input.taskScope());
        }
        if ("advisor.acceptance-criteria.missing".equals(spec.id())) {
            return input.acceptanceCriteria().isBlank();
        }
        if ("advisor.stop-condition.missing".equals(spec.id())) {
            return input.stopCondition().isBlank();
        }
        if ("advisor.value-rationale.missing".equals(spec.id())) {
            return input.valueRationale().isBlank();
        }
        return false;
    }

    private static boolean scopeLooksTooBroad(String scope) {
        String normalized = lower(scope);
        if (normalized.isBlank()) {
            return true;
        }
        if (wordCount(normalized) > BROAD_SCOPE_WORD_LIMIT) {
            return true;
        }
        return BROAD_SCOPE_MARKERS.stream().anyMatch(normalized::contains);
    }

    private static boolean modelTierLooksTooExpensive(String modelTier, String scope) {
        String normalized = lower(modelTier);
        if (!(normalized.contains("high") || normalized.contains("extra"))) {
            return false;
        }
        return !scopeLooksTooBroad(scope) && wordCount(scope) <= 12;
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private static int wordCount(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return value.trim().split("\\s+").length;
    }
}
