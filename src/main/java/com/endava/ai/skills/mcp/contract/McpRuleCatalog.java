package com.endava.ai.skills.mcp.contract;

import com.endava.ai.skills.mcp.rules.McpDefaultRules;
import com.endava.ai.skills.mcp.rules.McpValidationRule;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Canonical executable catalog for MCP validation rules.
 * <p>
 * The rule annotation still owns the cold identity of a rule. This catalog
 * makes the active profile surface explicit and adds agent-facing why/repair
 * language without forcing every rule class to repeat the same prose.
 */
public final class McpRuleCatalog {
    private static final List<McpProfile> CATALOG_PROFILES = List.of(
            McpProfile.UNIVERSAL,
            McpProfile.UI_PACKAGE,
            McpProfile.HTML_REPORT,
            McpProfile.HTML_EXAMPLE_CANDIDATE,
            McpProfile.SUITE_ATLAS
    );

    private static final Map<Class<?>, McpContractSpec> SPECS_BY_RULE = buildCatalog();

    private McpRuleCatalog() {
    }

    public static Optional<McpContractSpec> specFor(Class<?> ruleClass) {
        return Optional.ofNullable(SPECS_BY_RULE.get(ruleClass));
    }

    public static McpContractSpec require(Class<?> ruleClass) {
        McpContractSpec spec = SPECS_BY_RULE.get(ruleClass);
        if (spec == null) {
            spec = McpContractSpec.fromAnnotation(ruleClass);
        }
        if (spec == null) {
            throw new IllegalStateException("Missing MCP contract spec for rule: " + ruleClass.getName());
        }
        return spec;
    }

    public static Set<Class<?>> ruleClasses() {
        return SPECS_BY_RULE.keySet();
    }

    public static List<McpContractSpec> specs() {
        return List.copyOf(SPECS_BY_RULE.values());
    }

    private static Map<Class<?>, McpContractSpec> buildCatalog() {
        Map<Class<?>, McpContractSpec> catalog = new LinkedHashMap<>();
        for (McpProfile profile : CATALOG_PROFILES) {
            for (McpValidationRule rule : McpDefaultRules.rulesFor(profile)) {
                Class<?> ruleClass = rule.getClass();
                catalog.putIfAbsent(ruleClass, catalogSpec(ruleClass, profile));
            }
        }
        return Collections.unmodifiableMap(catalog);
    }

    private static McpContractSpec catalogSpec(Class<?> ruleClass, McpProfile profile) {
        McpContractSpec spec = McpContractSpec.fromAnnotation(ruleClass);
        if (spec == null) {
            throw new IllegalStateException("Rule is active in " + profile
                    + " but has no @McpContract metadata: " + ruleClass.getName());
        }
        return spec.withNarrative(whyFor(spec, profile), repairFor(spec));
    }

    private static String whyFor(McpContractSpec spec, McpProfile profile) {
        String profileText = profile.name().toLowerCase().replace('_', '-');
        switch (spec.category()) {
            case RUNTIME:
                return "Keeps runtime thresholds and agent configuration auditable for profile " + profileText + ".";
            case PACKAGE:
                return "Proves the generated package can be reopened as a coherent artifact spine.";
            case RUN:
                return "Proves counted runs have real state, diff, execution, and learning evidence.";
            case REPORT:
                return "Proves the final HTML report publishes the package truth in the canonical shape.";
            case GRAPH:
                return "Proves the Business Flow Graph is application-facing and evidence-backed.";
            case SCORE:
                return "Prevents score inflation by tying Overall, cap, and axes to published evidence.";
            case PRETRAINING:
                return "Keeps intake and pretraining evidence visible before iterative execution.";
            case UI_WRAPPER:
                return "Keeps UI packages aligned with the Living Architecture and wrapper boundaries.";
            case LINKS:
                return "Keeps local report links reopenable inside the frozen package.";
            case SOURCE:
                return "Keeps source diffs focused on generated tests, not hidden framework mutation.";
            case MCP_GATE:
                return "Keeps the saved MCP gate result fresh, scoped, and consistent with current artifacts.";
            case HTML_EXAMPLE_CANDIDATE:
                return "Separates MCP-valid packages from teacher-grade html-ex candidates.";
            default:
                return "Protects " + spec.id() + " for profile " + profileText + ".";
        }
    }

    private static String repairFor(McpContractSpec spec) {
        switch (spec.category()) {
            case RUNTIME:
                return "Restore runtime values from agent-runtime.properties and rerun the relevant MCP profile.";
            case PACKAGE:
                return "Repair package state/accounting artifacts, then regenerate README or package review evidence as needed.";
            case RUN:
                return "Regenerate the affected run state, diff, Extent/XML proof, and learning summary for the counted run.";
            case REPORT:
            case GRAPH:
            case SCORE:
                return "Regenerate analysis/langgraph-business-understanding.html from package evidence and rerun report MCP tests.";
            case PRETRAINING:
                return "Update the pretraining brief from the current package intake before rerunning MCP.";
            case UI_WRAPPER:
                return "Move raw UI mechanics into actions/validation/page layers and keep tests orchestration-only.";
            case LINKS:
                return "Repair the broken local link target inside the package and rerun link validation.";
            case SOURCE:
                return "Move framework edits out of generated package diffs or justify them in the protected source contract.";
            case MCP_GATE:
                return "Rerun the MCP gate and republish the scoped gate artifact after changing package evidence.";
            case HTML_EXAMPLE_CANDIDATE:
                return "Lower candidate claims or strengthen business, score, graph, and proof evidence before promotion.";
            default:
                return "Inspect " + spec.artifact() + ", repair the evidence, regenerate artifacts, and rerun MCP.";
        }
    }
}
