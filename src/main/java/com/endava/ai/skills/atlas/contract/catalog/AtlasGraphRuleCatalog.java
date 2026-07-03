package com.endava.ai.skills.atlas.contract.catalog;

import com.endava.ai.skills.atlas.contract.AtlasRuleIds;
import com.endava.ai.skills.atlas.contract.AtlasRuleSpec;

import java.util.List;

import static com.endava.ai.skills.atlas.contract.AtlasRuleCategory.GRAPH_PROOF;
import static com.endava.ai.skills.atlas.contract.AtlasRuleCategory.GRAPH_STYLE;
import static com.endava.ai.skills.atlas.contract.AtlasRuleCategory.STRUCTURE;
import static com.endava.ai.skills.atlas.contract.AtlasRuleSeverity.BLOCKER;
import static com.endava.ai.skills.atlas.contract.AtlasRuleSeverity.MAJOR;
import static com.endava.ai.skills.atlas.contract.AtlasRuleSeverity.MINOR;
import static com.endava.ai.skills.atlas.contract.catalog.AtlasRuleCatalogSupport.GRAPH_REPAIR;
import static com.endava.ai.skills.atlas.contract.catalog.AtlasRuleCatalogSupport.candidate;

public final class AtlasGraphRuleCatalog {
    private AtlasGraphRuleCatalog() {
    }

    public static List<AtlasRuleSpec> openingRules() {
        return List.of(
                candidate(AtlasRuleIds.Candidate.SHARED_REPORT_SHELL, STRUCTURE, BLOCKER,
                        "Atlas is a skill-specific report, but its publishable chrome, score opening and collapse behavior are shared with Core reporting.",
                        "Reuse the shared report-shell primitive while keeping Atlas-owned sections: Executive Context, Business Flow Graph, Business Flow Understanding, Business Evidence Tables, Source-to-Business Trace and Atlas Test Audit."),
                candidate(AtlasRuleIds.Candidate.EXECUTIVE_CONTEXT_COLLAPSED, STRUCTURE, MAJOR,
                        "Executive context should inform, not dominate the business map.",
                        "Keep Executive Context collapsed by default with structured subsections."),
                candidate(AtlasRuleIds.Candidate.BUSINESS_GRAPH_LEGEND, GRAPH_STYLE, MAJOR,
                        "The main graph needs a readable legend that explains business meaning.",
                        "Keep the legend collapsed and organize it with concise business bullets."),
                candidate(AtlasRuleIds.Candidate.SUPPORTING_GRAPHS_LEGENDS, GRAPH_STYLE, MAJOR,
                        "Each subgraph must explain its local business semantics.",
                        "Add collapsed bullet legends to every supporting graph."),
                candidate(AtlasRuleIds.Candidate.GRAPH_REFERENCE_PRIMITIVES, GRAPH_STYLE, MAJOR,
                        "Graphs should reuse the proven html-ex visual primitives.",
                        "Use shared node/edge/legend primitives rather than one-off styling."),
                candidate(AtlasRuleIds.Candidate.GRAPH_CSS_REUSABLE, GRAPH_STYLE, MAJOR,
                        "Reusable CSS keeps the atlas skill stable across agents and domains.",
                        "Move reusable graph styling to shared CSS and keep report-local overrides minimal."),
                candidate(AtlasRuleIds.Candidate.GRAPHS_MINIMAL_STYLE_PER_SECTION, GRAPH_STYLE, MAJOR,
                        "Every graph must remain visually calm and comparable.",
                        "Use the same minimal graph style across the main graph and subgraphs."),
                candidate(AtlasRuleIds.Candidate.GRAPHS_VISUAL_SCALE, GRAPH_STYLE, MAJOR,
                        "Inconsistent node/font scale makes the atlas feel generated, not designed.",
                        "Align node dimensions, text weight, and font scale across graph sections."),
                candidate(AtlasRuleIds.Candidate.BUSINESS_SUBGRAPHS_FULL_WIDTH, GRAPH_STYLE, MINOR,
                        "Subgraphs should read as first-class business slices.",
                        "Give Business Subgraphs the same content width rhythm as the main graph."),
                candidate(AtlasRuleIds.Candidate.LEGENDS_COLLAPSED, STRUCTURE, MINOR,
                        "Legends are support material and should not visually drown the graph.",
                        "Keep every legend collapsed by default."),
                candidate(AtlasRuleIds.Candidate.LEGENDS_BULLETED_QUALITY, GRAPH_STYLE, MAJOR,
                        "A legend should teach the business reading of the graph.",
                        "Use concise bullets for actors, object meaning, proof meaning, and limits."),
                candidate(AtlasRuleIds.Candidate.GRAPH_NODE_BUSINESS_PROOF_STRIP, GRAPH_PROOF, BLOCKER,
                        "Main graph nodes must be source-backed business claims, not decorative labels.",
                        GRAPH_REPAIR),
                candidate(AtlasRuleIds.Candidate.MAIN_GRAPH_NODE_MATURITY_PROOF, GRAPH_PROOF, BLOCKER,
                        "Primary graph nodes must reconcile with maturity and limit truth, not only appear in a decorative diagram.",
                        "Add one local proof-strip row per primary graph node, with method/layer evidence, maturity and limit, then mirror that maturity on the rendered SVG node via data-maturity."),
                candidate(AtlasRuleIds.Candidate.GRAPH_PROOF_STRIP_PLACEMENT, GRAPH_PROOF, BLOCKER,
                        "Graph proof belongs locally to each graph section, after the graph legend.",
                        "For every graph panel use this exact collapsed order: SVG graph, Legend, Banda de dovada noduri."),
                candidate(AtlasRuleIds.Candidate.SUBGRAPH_UNDERSTANDING_STRIPS, GRAPH_PROOF, BLOCKER,
                        "Each subgraph needs local proof so readers can reopen why the flow exists.",
                        "Add node -> method -> layer path -> business evidence -> limit strips under every subgraph."),
                candidate(AtlasRuleIds.Candidate.SUBGRAPH_SEMANTIC_DENSITY, GRAPH_PROOF, BLOCKER,
                        "Shared graph grammar is allowed, but subgraphs must not look like generic template clones.",
                        "Keep the common visual style while giving every 3.x subgraph at least two domain-bearing node labels and distinct semantic signatures."),
                candidate(AtlasRuleIds.Candidate.SUBGRAPHS_SOURCE_SPECIFIC_DOMINANCE, GRAPH_PROOF, BLOCKER,
                        "Subgraphs must be selected from dominant source families, not reusable template flow names.",
                        "Rename or rebuild 3.x subgraphs so their dominant classes/methods come from the analyzed source package."),
                candidate(AtlasRuleIds.Candidate.LOCAL_PROOF_STRIP_SOURCE_BACKED, GRAPH_PROOF, BLOCKER,
                        "Local proof strips must name source methods/classes that actually exist.",
                        "Replace generic proof-strip prose with concrete methodName() and class tokens from sourceRoot/UI layers."),
                candidate(AtlasRuleIds.Candidate.SEMANTIC_CONTRACT_MARKERS, STRUCTURE, MAJOR,
                        "Contract markers should be semantic attributes/classes, not hidden keyword hacks.",
                        "Replace hidden marker text with semantic data-* attributes or visible structural markers."),
                candidate(AtlasRuleIds.Candidate.BUSINESS_FLOWS_APPLICATION_ONLY, com.endava.ai.skills.atlas.contract.AtlasRuleCategory.BUSINESS_COMPREHENSION, BLOCKER,
                        "Business flow diagrams must describe the application, not the atlas contract itself.",
                        "Remove test-contract/meta nodes from business flows and keep them in audit sections."),
                candidate(AtlasRuleIds.Candidate.BUSINESS_SUBGRAPHS_COLLAPSED, STRUCTURE, MINOR,
                        "Supporting graphs should be available without overwhelming the first read.",
                        "Keep Business Subgraphs collapsed unless the main graph requires immediate support."),
                candidate(AtlasRuleIds.Candidate.SECTION_TOOLBAR, STRUCTURE, MINOR,
                        "Large atlas reports need explicit open/close controls so readers can inspect or collapse the whole report quickly.",
                        "Add the Deschide sectiunile / Inchide sectiunile toolbar backed by toggleAll(open), matching the canonical atlas report shell."),
                candidate(AtlasRuleIds.Candidate.REPORT_LANGUAGE_ROMANIAN, STRUCTURE, MINOR,
                        "Published Atlas reports must declare their language so the generated artifact is readable and intentional.",
                        "Set html lang=ro and publish a visible Limba raportului: romana marker in the report opening.")
        );
    }
}
