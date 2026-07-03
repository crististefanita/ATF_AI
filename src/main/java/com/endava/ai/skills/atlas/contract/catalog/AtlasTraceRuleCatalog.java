package com.endava.ai.skills.atlas.contract.catalog;

import com.endava.ai.skills.atlas.contract.AtlasRuleIds;
import com.endava.ai.skills.atlas.contract.AtlasRuleSpec;

import java.util.List;

import static com.endava.ai.skills.atlas.contract.AtlasRuleCategory.BUSINESS_COMPREHENSION;
import static com.endava.ai.skills.atlas.contract.AtlasRuleCategory.PROOF_TRACE;
import static com.endava.ai.skills.atlas.contract.AtlasRuleCategory.STRUCTURE;
import static com.endava.ai.skills.atlas.contract.AtlasRuleSeverity.BLOCKER;
import static com.endava.ai.skills.atlas.contract.AtlasRuleSeverity.MAJOR;
import static com.endava.ai.skills.atlas.contract.AtlasRuleSeverity.MINOR;
import static com.endava.ai.skills.atlas.contract.catalog.AtlasRuleCatalogSupport.TRACE_REPAIR;
import static com.endava.ai.skills.atlas.contract.catalog.AtlasRuleCatalogSupport.candidate;

public final class AtlasTraceRuleCatalog {
    private AtlasTraceRuleCatalog() {
    }

    public static List<AtlasRuleSpec> rules() {
        return List.of(
                candidate(AtlasRuleIds.Candidate.MAPPING_DEPTH, PROOF_TRACE, MAJOR,
                        "Mapping must be deeper than class name -> business label.",
                        TRACE_REPAIR),
                candidate(AtlasRuleIds.Candidate.SECTIONS_NUMBERED, STRUCTURE, MINOR,
                        "Numbering helps readers navigate a large atlas.",
                        "Number top-level sections consistently."),
                candidate(AtlasRuleIds.Candidate.SECTIONS_NUMBER_PLACEMENT, STRUCTURE, MINOR,
                        "Numbers belong in section titles, not decorative pills or hints.",
                        "Move numbering to summary/title text only."),
                candidate(AtlasRuleIds.Candidate.SECTIONS_TOP_LEVEL_CONTINUITY, STRUCTURE, MAJOR,
                        "Unnumbered top-level sections between numbered sections break navigation.",
                        "Keep top-level sections continuous and numbered."),
                candidate(AtlasRuleIds.Candidate.FLOW_STORY_COMPLETE, BUSINESS_COMPREHENSION, BLOCKER,
                        "Flows must tell product stories, not just list lanes.",
                        "Express actor -> intent -> visible action -> business object -> observable outcome."),
                candidate(AtlasRuleIds.Candidate.LAYER_TRACE_MAP_COMPLETE, PROOF_TRACE, BLOCKER,
                        "Layer trace is the bridge from test code to business meaning.",
                        TRACE_REPAIR),
                candidate(AtlasRuleIds.Candidate.LAYER_TRACE_CONCRETE_EVIDENCE, PROOF_TRACE, BLOCKER,
                        "Layer trace must name concrete source evidence, not abstract layers only.",
                        TRACE_REPAIR),
                candidate(AtlasRuleIds.Candidate.SOURCE_TRACE_METHOD_LEVEL, PROOF_TRACE, BLOCKER,
                        "Method-level trace prevents invented business claims.",
                        "Name real test methods and the source classes that support them."),
                candidate(AtlasRuleIds.Candidate.SOURCE_TRACE_BACKED_BY_SOURCE, PROOF_TRACE, BLOCKER,
                        "Every source trace claim must exist in analyzed source.",
                        TRACE_REPAIR),
                candidate(AtlasRuleIds.Candidate.SOURCE_TRACE_APPLICATION_BUSINESS, BUSINESS_COMPREHENSION, BLOCKER,
                        "Source trace rows should describe application business, not framework mechanics.",
                        "Keep technical layers as evidence and business objects as the subject.")
        );
    }
}
