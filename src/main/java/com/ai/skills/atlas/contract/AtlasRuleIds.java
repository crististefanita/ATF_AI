package com.ai.skills.atlas.contract;

public final class AtlasRuleIds {
    private AtlasRuleIds() {
    }

    public static final class Base {
        public static final String OUTPUT_EXISTS          = "atlas.output.exists";
        public static final String DECLARED_OUTPUT        = "atlas.output.declared-artifact";
        public static final String USES_SHARED_CSS        = "atlas.html.uses-shared-css";
        public static final String SECTIONS_COLLAPSIBLE   = "atlas.sections.collapsible";
        public static final String BUSINESS_FLOW_PRESENT  = "atlas.business-flow.present";
        public static final String ROW_TEMPLATE_COMPLETE  = "atlas.row-template.complete";
        public static final String SOURCE_READ_ONLY       = "atlas.source.read-only";
        public static final String DIAGRAM_STYLE_MINIMAL  = "atlas.diagram-style.minimal";

        private Base() {
        }
    }

    public static final class Candidate {
        public static final String SCORE_CARDS_SHAPE                 = "atlas.candidate.score-cards.shape";
        public static final String SHARED_REPORT_SHELL               = "atlas.candidate.report-shell.shared";
        public static final String EXECUTIVE_CONTEXT_COLLAPSED       = "atlas.candidate.executive-context.collapsed";
        public static final String BUSINESS_GRAPH_LEGEND             = "atlas.candidate.business-graph.legend";
        public static final String SUPPORTING_GRAPHS_LEGENDS         = "atlas.candidate.supporting-graphs.legends";
        public static final String GRAPH_REFERENCE_PRIMITIVES        = "atlas.candidate.graph.reference-primitives";
        public static final String GRAPH_CSS_REUSABLE                = "atlas.candidate.graph.css-reusable";
        public static final String GRAPHS_MINIMAL_STYLE_PER_SECTION  = "atlas.candidate.graphs.minimal-style-per-section";
        public static final String GRAPHS_VISUAL_SCALE               = "atlas.candidate.graphs.visual-scale";
        public static final String BUSINESS_SUBGRAPHS_FULL_WIDTH     = "atlas.candidate.business-subgraphs.full-width";
        public static final String LEGENDS_COLLAPSED                 = "atlas.candidate.legends.collapsed";
        public static final String LEGENDS_BULLETED_QUALITY          = "atlas.candidate.legends.bulleted-quality";
        public static final String GRAPH_NODE_BUSINESS_PROOF_STRIP   = "atlas.candidate.graph-node.business-proof-strip";
        public static final String MAIN_GRAPH_NODE_MATURITY_PROOF    = "atlas.candidate.main-graph-node.maturity-proof";
        public static final String GRAPH_PROOF_STRIP_PLACEMENT       = "atlas.candidate.graph-proof-strip.placement";
        public static final String SUBGRAPH_UNDERSTANDING_STRIPS     = "atlas.candidate.subgraph.business-understanding-strips";
        public static final String SUBGRAPH_SEMANTIC_DENSITY         = "atlas.candidate.subgraph.semantic-density";
        public static final String SUBGRAPHS_SOURCE_SPECIFIC_DOMINANCE = "atlas.candidate.subgraphs.source-specific-dominance";
        public static final String LOCAL_PROOF_STRIP_SOURCE_BACKED    = "atlas.candidate.local-proof-strip.source-backed";
        public static final String SEMANTIC_CONTRACT_MARKERS         = "atlas.candidate.semantic-contract-markers";
        public static final String BUSINESS_FLOWS_APPLICATION_ONLY   = "atlas.candidate.business-flows.application-only";
        public static final String BUSINESS_SUBGRAPHS_COLLAPSED      = "atlas.candidate.business-subgraphs.collapsed";
        public static final String SCORE_AXES_CONSISTENT             = "atlas.candidate.score-axes.consistent";
        public static final String SCORE_CARDS_READABLE              = "atlas.candidate.score-cards.readable";
        public static final String SCORE_OPENING_AUDIT_POINTER       = "atlas.candidate.score-opening.audit-pointer";
        public static final String SCORE_RATIONALE_AUDIT             = "atlas.candidate.score-rationale.audit";
        public static final String SCORE_SUPPORT_TRACE               = "atlas.candidate.score-support.trace";
        public static final String SCORE_FORMULA_TRACE               = "atlas.candidate.score-formula.trace";
        public static final String SCORE_GQM_TRACEABILITY            = "atlas.candidate.score-gqm-traceability";
        public static final String SCORE_UNIFORMITY_CAP              = "atlas.candidate.score-uniformity.cap";
        public static final String END_TO_END_LIMIT_SCORE_CAP        = "atlas.candidate.end-to-end-limit.score-cap";
        public static final String LIMIT_DISCIPLINE_95_PLUS          = "atlas.candidate.95-plus.limit-discipline";
        public static final String SUITE_COMPLETENESS_SEPARATION     = "atlas.candidate.suite-completeness.separation";
        public static final String SCORE_OVERALL_CALCULATION         = "atlas.candidate.score-overall.calculation";
        public static final String SCORE_CARDS_DOCUMENTED            = "atlas.candidate.score-cards.documented";
        public static final String SCORE_BUSINESS_EVIDENCE           = "atlas.candidate.score-business-understanding.evidence";
        public static final String MAPPING_DEPTH                     = "atlas.candidate.mapping.depth";
        public static final String SECTIONS_NUMBERED                 = "atlas.candidate.sections.numbered";
        public static final String SECTION_TOOLBAR                   = "atlas.candidate.sections.toolbar";
        public static final String REPORT_LANGUAGE_ROMANIAN          = "atlas.candidate.report-language.romanian";
        public static final String SECTIONS_NUMBER_PLACEMENT         = "atlas.candidate.sections.number-placement";
        public static final String SECTIONS_TOP_LEVEL_CONTINUITY     = "atlas.candidate.sections.top-level-continuity";
        public static final String FLOW_STORY_COMPLETE               = "atlas.candidate.flow-story.complete";
        public static final String LAYER_TRACE_MAP_COMPLETE          = "atlas.candidate.layer-trace-map.complete";
        public static final String LAYER_TRACE_CONCRETE_EVIDENCE     = "atlas.candidate.layer-trace.concrete-evidence";
        public static final String SOURCE_TRACE_METHOD_LEVEL         = "atlas.candidate.source-trace.method-level";
        public static final String SOURCE_TRACE_BACKED_BY_SOURCE     = "atlas.candidate.source-trace.backed-by-source";
        public static final String SOURCE_TRACE_APPLICATION_BUSINESS = "atlas.candidate.source-trace.application-business-only";
        public static final String SUBGRAPHS_BUSINESS_COMPLEXITY     = "atlas.candidate.subgraphs.business-complexity";
        public static final String BUSINESS_OBJECT_CENTERED          = "atlas.candidate.business-object-centered";
        public static final String OBJECT_MATURITY_MATRIX            = "atlas.candidate.object-maturity.matrix";
        public static final String END_TO_END_CLOSURE_LADDER         = "atlas.candidate.end-to-end-closure.ladder";
        public static final String FLOW_MATURITY_DISTRIBUTION        = "atlas.candidate.flow-maturity.distribution";
        public static final String PARTIAL_FLOW_NOT_SOLD_AS_OWNED    = "atlas.candidate.partial-flow-not-sold-as-owned";
        public static final String COMPREHENSION_LIMITS_SECONDARY    = "atlas.candidate.comprehension-limits.secondary";
        public static final String BUSINESS_COMPREHENSION_VOCABULARY = "atlas.candidate.vocabulary.business-comprehension";
        public static final String SKILL_MIGRATION_READINESS         = "atlas.candidate.skill-migration.readiness";
        public static final String SKILL_PARAMETERS_GENERIC          = "atlas.candidate.skill-parameters.generic";
        public static final String TEST_AUDIT_EXTENT_LINKED          = "atlas.candidate.test-audit.extent-linked";
        public static final String TEST_AUDIT_TOTAL_SYNCHRONIZED     = "atlas.candidate.test-audit.total-synchronized";
        public static final String TEST_AUDIT_EXTENT_SUMMARY_COMMAND = "atlas.candidate.test-audit.extent-summary-command";
        public static final String TEST_AUDIT_UI_RUNTIME_SCOPE_PARITY = "atlas.candidate.test-audit.ui-runtime-scope-parity";
        public static final String TEST_AUDIT_MANIFEST_COMMAND_SYNC  = "atlas.candidate.test-audit.manifest-command-sync";

        private Candidate() {
        }
    }
}
