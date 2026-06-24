package com.endava.ai.atlas.rules.scoring;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;
import com.endava.ai.atlas.rules.AtlasValidationRule;

import java.util.List;

public final class AtlasScoreEvidenceTraceRule implements AtlasValidationRule {
    private static final List<AxisEvidence> EVIDENCE = List.of(
            new AxisEvidence("intelegere business", List.of("business", "testele", "sursa")),
            new AxisEvidence("reconstructie flow", List.of("actor", "intentie", "actiune vizibila", "obiect business", "rezultat observabil")),
            new AxisEvidence("trasabilitate layere", List.of("actions", "components", "validation")),
            new AxisEvidence("claritate obiect business", List.of("obiect", "business", "page", "action", "selector")),
            new AxisEvidence("fidelitate dovada", List.of("concluzie", "test", "layere")));

    @Override
    public String id() {
        return "atlas.candidate.score-evidence.trace";
    }

    @Override
    public String expected() {
        return "Each score rationale row names concrete evidence dimensions, so scores are explainable and repairable.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String normalized = AtlasScoreContract.normalized(AtlasScoreContract.scoreSupportAuditText(context.lowerAtlasText()));
        if (normalized.isBlank()) {
            result.add(id(), "Cannot audit score evidence because score rationale is missing", context.atlasHtml(),
                    "Add score rationale under Executive Context before claiming atlas score-card quality.");
            return;
        }

        for (AxisEvidence axis : EVIDENCE) {
            String row = rowContext(normalized, axis.name());
            if (row.isBlank()) {
                result.add(id(), "Missing score evidence row for: " + axis.name(), context.atlasHtml(),
                        "Every canonical axis needs a rationale row with source/business/layer evidence.");
                continue;
            }
            for (String token : axis.requiredTokens()) {
                if (!row.contains(token)) {
                    result.add(id(), "Score evidence for " + axis.name() + " misses token: " + token,
                            context.atlasHtml(),
                            "Explain the score using concrete business/story/layer evidence, not just a number.");
                }
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

    private static final class AxisEvidence {
        private final String name;
        private final List<String> requiredTokens;

        private AxisEvidence(String name, List<String> requiredTokens) {
            this.name = name;
            this.requiredTokens = requiredTokens;
        }

        private String name() {
            return name;
        }

        private List<String> requiredTokens() {
            return requiredTokens;
        }
    }
}
