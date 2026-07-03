package com.ai.skills.atlas.validation;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.contract.AtlasRuleIds;
import com.ai.skills.atlas.contract.AtlasRuleCategory;
import com.ai.skills.atlas.contract.AtlasRuleSpec;

import java.nio.file.Path;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Maps a rule to the artifact an agent should inspect or repair first.
 * The rule catalog says what is wrong; this class says where to act.
 */
@Getter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class AtlasRepairTarget {
    private final Path observedArtifact;
    private final Path primaryRepairArtifact;
    private final Path supportingArtifact;
    private final String route;

    static AtlasRepairTarget forRule(AtlasContext context, AtlasRuleSpec spec) {
        if (AtlasRuleIds.Base.SOURCE_READ_ONLY.equals(spec.id())) {
            return new AtlasRepairTarget(
                    context.sourceRoot(),
                    context.atlasHtml(),
                    context.requirementsHtml(),
                    "Restore analyzed source if needed; the atlas skill is read-only and repairs only generated output.");
        }
        if (AtlasRuleIds.Base.USES_SHARED_CSS.equals(spec.id())) {
            return new AtlasRepairTarget(
                    context.atlasHtml(),
                    context.atlasHtml(),
                    context.sharedCss(),
                    "Link the shared stylesheet from the atlas and avoid local replacement of the visual system.");
        }
        if (spec.category() == AtlasRuleCategory.SKILL_READINESS) {
            return new AtlasRepairTarget(
                    context.requirementsHtml(),
                    context.requirementsHtml(),
                    context.atlasHtml(),
                    "Clarify the skill requirement first, then regenerate the atlas output.");
        }
        if (spec.category() == AtlasRuleCategory.GRAPH_STYLE) {
            return new AtlasRepairTarget(
                    context.atlasHtml(),
                    context.atlasHtml(),
                    context.sharedCss(),
                    "Fix report markup first; promote reusable visual primitives to shared CSS only when needed.");
        }
        if (spec.category() == AtlasRuleCategory.PROOF_TRACE
                || spec.category() == AtlasRuleCategory.GRAPH_PROOF
                || spec.category() == AtlasRuleCategory.BUSINESS_COMPREHENSION) {
            return new AtlasRepairTarget(
                    context.atlasHtml(),
                    context.atlasHtml(),
                    context.sourceRoot(),
                    "Repair the atlas claim with source-backed business evidence from the analyzed package.");
        }
        if (spec.category() == AtlasRuleCategory.AUDIT) {
            return new AtlasRepairTarget(
                    context.atlasHtml(),
                    context.atlasHtml(),
                    context.projectRoot().resolve("target"),
                    "Regenerate and link the Atlas validation evidence after report edits.");
        }
        return new AtlasRepairTarget(
                context.atlasHtml(),
                context.atlasHtml(),
                context.requirementsHtml(),
                "Repair the generated atlas output according to the atlas reference contract.");
    }

}
