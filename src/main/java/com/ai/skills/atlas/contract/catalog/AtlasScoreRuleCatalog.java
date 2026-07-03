package com.ai.skills.atlas.contract.catalog;

import com.ai.skills.atlas.contract.AtlasRuleIds;
import com.ai.skills.atlas.contract.AtlasRuleSpec;

import java.util.List;

import static com.ai.skills.atlas.contract.AtlasRuleCategory.SCORING;
import static com.ai.skills.atlas.contract.AtlasRuleSeverity.BLOCKER;
import static com.ai.skills.atlas.contract.AtlasRuleSeverity.MAJOR;
import static com.ai.skills.atlas.contract.AtlasRuleSeverity.MINOR;
import static com.ai.skills.atlas.contract.catalog.AtlasRuleCatalogSupport.SCORE_REPAIR;
import static com.ai.skills.atlas.contract.catalog.AtlasRuleCatalogSupport.candidate;

public final class AtlasScoreRuleCatalog {
    private AtlasScoreRuleCatalog() {
    }

    public static List<AtlasRuleSpec> openingRules() {
        return List.of(
                candidate(AtlasRuleIds.Candidate.SCORE_CARDS_SHAPE, SCORING, MAJOR,
                        "Score cards are the first audit surface for atlas quality.",
                        "Use the canonical atlas score card family and keep Overall last.")
        );
    }

    public static List<AtlasRuleSpec> auditRules() {
        return List.of(
                candidate(AtlasRuleIds.Candidate.SCORE_AXES_CONSISTENT, SCORING, MAJOR,
                        "Score axes must be stable across cards, formulas, and audit tables.",
                        SCORE_REPAIR),
                candidate(AtlasRuleIds.Candidate.SCORE_CARDS_READABLE, SCORING, MAJOR,
                        "Unreadable score-card text breaks the first evaluation surface.",
                        "Shorten score explanations and avoid overflow inside score cards."),
                candidate(AtlasRuleIds.Candidate.SCORE_OPENING_AUDIT_POINTER, SCORING, MINOR,
                        "Opening scores should point readers to the audit trail.",
                        "Add concise links or references from score cards to score support sections."),
                candidate(AtlasRuleIds.Candidate.SCORE_RATIONALE_AUDIT, SCORING, MAJOR,
                        "Scores must be explained, not merely displayed.",
                        "Add a score rationale table that states goal, evidence, cap, and verdict per axis."),
                candidate(AtlasRuleIds.Candidate.SCORE_SUPPORT_TRACE, SCORING, MAJOR,
                        "Score cards must reopen to source/layer evidence.",
                        "Connect every score axis to rows in source trace, layer trace, and graph proof."),
                candidate(AtlasRuleIds.Candidate.SCORE_FORMULA_TRACE, SCORING, MAJOR,
                        "Overall must be calculable, not decorative.",
                        "Publish the formula, inputs, rounding, and cap logic."),
                candidate(AtlasRuleIds.Candidate.SCORE_GQM_TRACEABILITY, SCORING, MAJOR,
                        "GQM keeps scores tied to business goals, questions, and measures.",
                        "Use business goal -> flow question -> source/layer evidence -> score."),
                candidate(AtlasRuleIds.Candidate.SCORE_UNIFORMITY_CAP, SCORING, MAJOR,
                        "Flat scores can hide real limits unless capped or justified.",
                        "Differentiate axes or explain the shared cap explicitly."),
                candidate(AtlasRuleIds.Candidate.END_TO_END_LIMIT_SCORE_CAP, SCORING, BLOCKER,
                        "Major end-to-end limits must cap Overall instead of being buried.",
                        "Lower Overall or explain why the atlas quality remains high despite source-suite limits."),
                candidate(AtlasRuleIds.Candidate.LIMIT_DISCIPLINE_95_PLUS, SCORING, BLOCKER,
                        "A 95+ atlas must make severe limits unmistakable and non-decorative.",
                        "Do not publish 95+ unless limits are narrow, explained, and reflected in the score."),
                candidate(AtlasRuleIds.Candidate.SUITE_COMPLETENESS_SEPARATION, SCORING, MAJOR,
                        "Atlas quality and suite completeness are different concepts.",
                        "Explain that the score measures comprehension quality, not full product coverage."),
                candidate(AtlasRuleIds.Candidate.SCORE_OVERALL_CALCULATION, SCORING, MAJOR,
                        "Readers must be able to recompute Overall.",
                        "Publish axis values, formula, rounding, and cap."),
                candidate(AtlasRuleIds.Candidate.SCORE_CARDS_DOCUMENTED, SCORING, MAJOR,
                        "The generated score cards must match the skill requirements.",
                        "Keep score-card names and meanings aligned with business-test-atlas-reference.html."),
                candidate(AtlasRuleIds.Candidate.SCORE_BUSINESS_EVIDENCE, SCORING, BLOCKER,
                        "Business understanding scores must be backed by business evidence.",
                        "Link score rationale to graph proof, source trace, and layer trace.")
        );
    }
}
