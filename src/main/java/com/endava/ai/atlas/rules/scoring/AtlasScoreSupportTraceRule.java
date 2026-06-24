package com.endava.ai.atlas.rules.scoring;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;
import com.endava.ai.atlas.rules.AtlasValidationRule;

import java.util.Map;

public final class AtlasScoreSupportTraceRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.score-support.trace";
    }

    @Override
    public String expected() {
        return "Every score card is backed by score-support rows that repeat the same score and cite evidence surfaces.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        Map<String, Integer> cards = AtlasScoreContract.scoreCards(context.atlasText());
        if (cards.isEmpty()) {
            result.add(id(), "No score cards found for support trace validation", context.atlasHtml(),
                    "Publish score cards before adding score support trace.");
            return;
        }

        String html = context.lowerAtlasText();
        String support = AtlasScoreContract.normalized(section(html, "4.5 suport scor", "4.6 formula overall")
                + " " + section(html, "5.1 suport scor", "</details>"));
        if (support.isBlank()) {
            result.add(id(), "Missing score support sections 4.5 / 5.1", context.atlasHtml(),
                    "Add score-support rows under Business Evidence Tables and Source-to-Business Trace.");
            return;
        }

        for (Map.Entry<String, Integer> card : cards.entrySet()) {
            String label = card.getKey();
            if ("overall".equals(label)) {
                continue;
            }
            String row = AtlasScoreContract.rowContext(support, label);
            if (row.isBlank()) {
                result.add(id(), "Score card has no support row: " + label, context.atlasHtml(),
                        "Every score-card axis must be backed by a score-support row in 4.5 or 5.1.");
                continue;
            }
            if (!row.contains(String.valueOf(card.getValue()))) {
                result.add(id(), "Score support row does not repeat card score: " + label,
                        context.atlasHtml(), "Keep score card value and support-table value synchronized.");
            }
            if (!containsAny(row, "tabel", "trace", "sursa", "layer", "obiect", "flux", "dovada")) {
                result.add(id(), "Score support row is not tied to evidence: " + label,
                        context.atlasHtml(), "Explain score support through table, trace, source, layer, object, flow or proof evidence.");
            }
        }
    }

    private static String section(String text, String startToken, String endToken) {
        int start = text.indexOf(startToken);
        if (start < 0) {
            return "";
        }
        int end = text.indexOf(endToken, start + startToken.length());
        if (end < 0) {
            return text.substring(start);
        }
        return text.substring(start, end);
    }

    private static boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
