package com.ai.skills.atlas.rules.structure;

import com.ai.skills.atlas.rules.AtlasValidationRule;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;

import java.util.List;
public final class AtlasExecutiveContextShapeRule implements AtlasValidationRule {
    private static final List<String> REQUIRED_SUBSECTIONS = List.of(
            "verdict",
            "scope",
            "score rationale",
            "comprehension limits");

    private static final List<String> SCORE_RATIONALE_AXES = List.of(
            "business comprehension",
            "flow reconstruction",
            "layer traceability",
            "business object clarity",
            "evidence fidelity");

    @Override
    public String id() {
        return "atlas.candidate.executive-context.collapsed";
    }

    @Override
    public String expected() {
        return "Executive Context is collapsed by default and contains collapsed subdetails for Verdict, Scope, Score rationale, and Comprehension limits.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        int sectionStart = html.indexOf("executive context");
        if (sectionStart < 0) {
            result.add(id(), "Missing Executive Context <details> section", context.atlasHtml(),
                    "Add Executive Context as a top-level collapsed details section after score cards.");
            return;
        }
        int sectionEnd = html.indexOf("business flow graph", sectionStart + "executive context".length());
        String section = html.substring(sectionStart, sectionEnd > sectionStart ? sectionEnd : Math.min(html.length(), sectionStart + 5_000));
        String openingSlice = html.substring(Math.max(0, sectionStart - 250), sectionStart);
        if (openingSlice.contains("<details") && openingSlice.contains("open")) {
            result.add(id(), "Executive Context is open by default", context.atlasHtml(),
                    "Keep Executive Context collapsed by default; only the primary business graph should usually open.");
        }
        if (!section.contains("<details")) {
            result.add(id(), "Executive Context has no collapsed subdetails", context.atlasHtml(),
                    "Split Executive Context into subdetails: Verdict, Scope, Score rationale, Comprehension limits.");
        }
        for (String subsection : REQUIRED_SUBSECTIONS) {
            if (!section.contains(subsection)) {
                result.add(id(), "Executive Context missing subdetail: " + subsection, context.atlasHtml(),
                        "Add this as a collapsed subdetail inside Executive Context.");
            }
        }
        boolean hasScoreRationale = section.contains("score rationale");
        for (String axis : SCORE_RATIONALE_AXES) {
            if (section.contains(axis) && !hasScoreRationale) {
                result.add(id(), "Score axis table appears without Score rationale subdetail: " + axis, context.atlasHtml(),
                        "Move score-axis explanation into Executive Context -> Score rationale.");
            }
        }
    }
}
