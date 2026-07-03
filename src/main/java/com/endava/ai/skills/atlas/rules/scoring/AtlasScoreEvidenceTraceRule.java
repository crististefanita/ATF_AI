package com.endava.ai.skills.atlas.rules.scoring;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

public final class AtlasScoreEvidenceTraceRule implements AtlasValidationRule {
    private static final int MIN_STORY_FAMILIES_PER_RECONSTRUCTION_ROW = 4;

    @Override
    public String id() {
        return "atlas.candidate.score-business-understanding.evidence";
    }

    @Override
    public String expected() {
        return "Each score rationale row explains the business understanding evidence behind the score, not only searchable words.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String normalized = AtlasScoreContract.normalized(AtlasScoreContract.scoreSupportAuditText(context.lowerAtlasText()));
        if (normalized.isBlank()) {
            result.add(id(), "Cannot audit score evidence because score rationale is missing", context.atlasHtml(),
                    "Add score rationale under Executive Context before claiming atlas score-card quality.");
            return;
        }

        for (String axis : AtlasScoreContract.BUSINESS_AXES) {
            String row = rowContext(normalized, axis);
            if (row.isBlank()) {
                result.add(id(), "Missing business-understanding evidence row for: " + axis, context.atlasHtml(),
                        "Every canonical axis needs a rationale row that explains what business understanding was proven from source and layers.");
                continue;
            }
            if (!AtlasSemanticVocabulary.containsAny(row, AtlasSemanticVocabulary.EVIDENCE_SURFACE_MARKERS)) {
                result.add(id(), "Score rationale is not tied to an evidence surface: " + axis,
                        context.atlasHtml(),
                        "Explain the score through source, layer, trace, table, object, flow or proof evidence.");
            }
            if ("reconstructie flow".equals(axis)
                    && AtlasSemanticVocabulary.familyCoverage(row, AtlasSemanticVocabulary.FLOW_STORY_FAMILIES)
                    < MIN_STORY_FAMILIES_PER_RECONSTRUCTION_ROW) {
                result.add(id(), "Flow reconstruction score does not expose enough product-story concepts",
                        context.atlasHtml(),
                        "For flow reconstruction, show most of: actor, intent, visible action, business object and observable result.");
            }
        }
    }

    private static String rowContext(String text, String axis) {
        int start = text.indexOf(axis);
        if (start < 0) {
            return "";
        }
        int end = text.length();
        for (String nextAxis : AtlasScoreContract.ALL_CARDS) {
            int next = text.indexOf(nextAxis, start + axis.length());
            if (next > start && next < end) {
                end = next;
            }
        }
        return text.substring(start, Math.min(end, start + 500));
    }

}
