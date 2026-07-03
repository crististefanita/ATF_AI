package com.ai.skills.mcp.rules;

import com.ai.skills.mcp.contract.McpContract;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Executable MCP rule catalog.
 * <p>
 * Keep rule identity and construction in one place; profile composition can
 * then read as ordered rule ids instead of direct constructor inventories.
 */
final class McpExecutableRuleCatalog {
    static final String REPORT_CHROME = "report.chrome.shared-css";
    static final String BUSINESS_GRAPH = "report.business-graph.canonical";
    static final String SCORE_OVERVIEW = "report.score-overview.canonical";
    static final String PROOF_MAPPING = "report.proof-mapping.claim-evidence";
    static final String CLOSE_OUT_REPORT = "report.close-out.state-decision";
    static final String RUNTIME_CONTRACT = "runtime.active-values.published";
    static final String PACKAGE_SPINE = "package.artifact-spine.required";
    static final String PACKAGE_ACCOUNTING = "package.accounting.runtime-window";
    static final String README_SYNC = "package.readme.state-sync";
    static final String ROOT_DIFF = "source.root-diff.files-exist";
    static final String SOURCE_LEDGER = "source.evolution.ledger";
    static final String SOURCE_KPI = "source.evolution.kpi-consistency";
    static final String PROTECTED_FRAMEWORK = "source.protected-framework.not-mutated";
    static final String PROTECTED_GIT = "source.protected-framework.git-clean";
    static final String BUSINESS_NOVELTY = "business.novelty.against-baseline";
    static final String DURABLE_OBJECT = "business.object.durable-owned";
    static final String BUSINESS_TRACE = "business.evidence.trace";
    static final String CLAIM_ACTION = "claim-action.source-proof-alignment";
    static final String NEXT_FRONTIER = "business.next-frontier.single-object";
    static final String SCORE_CAP = "score.cap.evidence";
    static final String RUN_LEARNING = "run.learning.progression";
    static final String REPORT_STATE_SYNC = "report.state.vocabulary-sync";
    static final String PRETRAINING = "pretraining.intake.quality";
    static final String LEARNING_TRACE = "run.learning.trace-quality";
    static final String GATE_FRESHNESS = "mcp.gate.evidence-freshness";
    static final String PACKAGE_CLOSEOUT = "mcp.gate.package-closeout";
    static final String KPI_CLOSEOUT = "mcp.gate.kpi-closeout-artifacts";
    static final String NO_TEMPLATE_NOISE = "package.artifacts.no-template-noise";
    static final String TEST_COUNTABILITY = "run.countability.xml-to-java-test";
    static final String LOCAL_LINKS = "report.local-links.resolve";
    static final String CLAIMED_ABSENCE = "ui.claimed-absence.requires-assertion";
    static final String WRAPPER_HANDOFF = "ui.wrapper.handoff.quality";
    static final String RUN_COUNT = "run.window.count";
    static final String RUN_SPINE = "run.window.artifact-spine";
    static final String RUN_STATE = "run.window.state-completeness";
    static final String RUN_EXTENT = "run.window.extent-proof";
    static final String RUN_PROVENANCE = "run.extent.maven-provenance";
    static final String RUN_INDEPENDENCE = "run.window.independence";
    static final String RUN_DIFF = "run.diff.progression";
    static final String BASE_TEST_UI = "ui.test-architecture.base-test";
    static final String UI_API_STRUCTURE = "ui-api-generated.structure.modern-layout";
    static final String NO_FRAMEWORK_REPORTING = "ui.test-architecture.framework-reporting-enabled";
    static final String NO_DIRECT_INFRA = "ui.test-architecture.no-direct-infra";
    static final String VALIDATION_REPORTING = "ui.test-architecture.validation-layer-reporting";
    static final String REAL_UI = "ui.test-architecture.real-ui-interaction";
    static final String CANDIDATE_SCORE = "candidate.score.teacher-threshold";
    static final String CANDIDATE_BUSINESS_CLOSURE = "candidate.business-closure.not-weakest";
    static final String CANDIDATE_OBJECT_DEPTH = "candidate.business-object.depth";
    static final String CANDIDATE_POSITIVE_OWNERSHIP = "candidate.positive-ownership";
    static final String CANDIDATE_REOPENABILITY = "candidate.reopenability.proof";
    static final String CANDIDATE_PROOF_TOKEN = "candidate.proof-token.completeness";
    static final String CANDIDATE_RUN_PROGRESSION = "candidate.run-progression";
    static final String CANDIDATE_DIFF_PEDAGOGY = "candidate.diff.pedagogy";
    static final String CANDIDATE_SINGLE_FAMILY = "candidate.single-family.cap";
    static final String CANDIDATE_GRAPH_OBJECT = "candidate.business-graph.object-specificity";
    static final String CANDIDATE_SCORE_JUSTIFICATION = "candidate.score.justification";
    static final String CANDIDATE_PROMOTION = "candidate.html-example.promotion-bundle";
    static final String CANDIDATE_LIFECYCLE = "candidate.business-lifecycle.depth";
    static final String CANDIDATE_EVIDENCE_TRACE = "candidate.business-evidence.trace";

    private static final Map<String, Supplier<McpValidationRule>> RULES = rules();

    private McpExecutableRuleCatalog() {
    }

    static McpValidationRule create(String id) {
        Supplier<McpValidationRule> factory = RULES.get(id);
        if (factory == null) {
            throw new IllegalArgumentException("Missing executable MCP rule factory: " + id);
        }
        return factory.get();
    }

    static McpValidationRule createVerified(String id) {
        McpValidationRule rule = create(id);
        McpContract contract = rule.getClass().getAnnotation(McpContract.class);
        String actual = contract == null ? "" : contract.id();
        if (!id.equals(actual)) {
            throw new IllegalStateException("Executable MCP rule id mismatch: requested=" + id + ", actual=" + actual);
        }
        return rule;
    }

    static List<String> ruleIds() {
        return List.copyOf(RULES.keySet());
    }

    static List<McpValidationRule> createAll(List<String> ids) {
        return ids.stream()
                .map(McpExecutableRuleCatalog::create)
                .collect(Collectors.toUnmodifiableList());
    }

    private static Map<String, Supplier<McpValidationRule>> rules() {
        Map<String, Supplier<McpValidationRule>> rules = new LinkedHashMap<>();
        McpExecutableUniversalRuleFactories.register(rules);
        McpExecutableUiRuleFactories.register(rules);
        McpExecutableCandidateRuleFactories.register(rules);
        return Collections.unmodifiableMap(new LinkedHashMap<>(rules));
    }

    static void add(Map<String, Supplier<McpValidationRule>> rules,
                    String id,
                    Supplier<McpValidationRule> factory) {
        Supplier<McpValidationRule> previous = rules.put(id, factory);
        if (previous != null) {
            throw new IllegalStateException("Duplicate executable MCP rule id: " + id);
        }
    }
}
