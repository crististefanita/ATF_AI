package com.ai.skills.atlas.contract.catalog;

import com.ai.skills.atlas.contract.AtlasRuleIds;
import com.ai.skills.atlas.contract.AtlasRuleSpec;

import java.util.List;

import static com.ai.skills.atlas.contract.AtlasRuleCategory.BUSINESS_COMPREHENSION;
import static com.ai.skills.atlas.contract.AtlasRuleCategory.STRUCTURE;
import static com.ai.skills.atlas.contract.AtlasRuleSeverity.BLOCKER;
import static com.ai.skills.atlas.contract.AtlasRuleSeverity.MAJOR;
import static com.ai.skills.atlas.contract.catalog.AtlasRuleCatalogSupport.candidate;

public final class AtlasBusinessRuleCatalog {
    private AtlasBusinessRuleCatalog() {
    }

    public static List<AtlasRuleSpec> rules() {
        return List.of(
                candidate(AtlasRuleIds.Candidate.SUBGRAPHS_BUSINESS_COMPLEXITY, BUSINESS_COMPREHENSION, MAJOR,
                        "Subgraphs must have enough flow complexity to teach the business slice.",
                        "Use multiple nodes/transitions and avoid one-node summaries."),
                candidate(AtlasRuleIds.Candidate.BUSINESS_OBJECT_CENTERED, BUSINESS_COMPREHENSION, BLOCKER,
                        "Business object is the central unit of the atlas.",
                        "Organize nodes/sections around visible business objects, not pages/selectors/helpers."),
                candidate(AtlasRuleIds.Candidate.OBJECT_MATURITY_MATRIX, BUSINESS_COMPREHENSION, MAJOR,
                        "Object maturity helps separate owned, partial, diagnostic, and surface-only understanding.",
                        "Publish a maturity matrix for major objects."),
                candidate(AtlasRuleIds.Candidate.END_TO_END_CLOSURE_LADDER, BUSINESS_COMPREHENSION, MAJOR,
                        "Closure ladder shows how far each flow really goes.",
                        "Map start -> visible action -> durable object -> reopen/relogin -> unresolved lifecycle."),
                candidate(AtlasRuleIds.Candidate.FLOW_MATURITY_DISTRIBUTION, BUSINESS_COMPREHENSION, MAJOR,
                        "Atlas readers need distribution, not only best examples.",
                        "Summarize owned/partial/diagnostic/surface-only flow counts or proportions."),
                candidate(AtlasRuleIds.Candidate.PARTIAL_FLOW_NOT_SOLD_AS_OWNED, BUSINESS_COMPREHENSION, BLOCKER,
                        "Partial or diagnostic flows must not be sold as strong ownership elsewhere.",
                        "Align graph, trace, score, and maturity wording for partial flows."),
                candidate(AtlasRuleIds.Candidate.COMPREHENSION_LIMITS_SECONDARY, STRUCTURE, MAJOR,
                        "Comprehension limits matter, but must not replace the business map.",
                        "Keep limits visible but secondary to business understanding."),
                candidate(AtlasRuleIds.Candidate.BUSINESS_COMPREHENSION_VOCABULARY, BUSINESS_COMPREHENSION, MAJOR,
                        "The atlas vocabulary must stay about understanding business from source.",
                        "Prefer comprehension, flow, object, evidence, and limit language over planning/run language.")
        );
    }
}
