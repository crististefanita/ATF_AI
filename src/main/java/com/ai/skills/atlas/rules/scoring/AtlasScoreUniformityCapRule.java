package com.ai.skills.atlas.rules.scoring;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.ai.skills.atlas.rules.AtlasText;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasScoreUniformityCapRule implements AtlasValidationRule {
    private static final int EXCELLENT_SCORE = 95;
    private static final Pattern CAP_VALUE_PATTERN = Pattern.compile(
            "(?i)(?:cap|capped|plafon|plafonat|capat)[^0-9]{0,60}(\\d{1,3})");
    private static final Pattern SCORE_NUMBER_PATTERN = Pattern.compile("\\d{1,3}");

    @Override
    public String id() {
        return "atlas.candidate.score-uniformity.cap";
    }

    @Override
    public String expected() {
        return "Perfectly uniform atlas score axes are allowed only when the atlas explains the cap rationale and why the axes are not differentiated.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        Map<String, Integer> businessScores = businessAxisScores(context.atlasText());
        if (businessScores.size() != AtlasScoreContract.BUSINESS_AXES.size() || !allEqual(businessScores)) {
            return;
        }

        String scoreAudit = AtlasScoreContract.normalized(AtlasScoreContract.scoreSupportAuditText(context.lowerAtlasText()));
        if (!AtlasSemanticVocabulary.containsAny(scoreAudit, AtlasSemanticVocabulary.UNIFORM_SCORE_MARKERS)) {
            result.add(id(), "All business score axes use the same score without an explicit uniform-score rationale",
                    context.atlasHtml(),
                    "If every axis has the same score, add a short rationale explaining why the scores are uniform and what cap rationale applies.");
        }
        if (!AtlasSemanticVocabulary.containsAny(scoreAudit, AtlasSemanticVocabulary.CAP_MARKERS)) {
            result.add(id(), "Uniform business scores do not expose a cap rationale",
                    context.atlasHtml(),
                    "Explain the cap behind the uniform score, or differentiate the axis scores according to the evidence.");
        }
        validateDistinctAxisEvidence(context, result, scoreAudit, businessScores);
        validateNoScoreExceedsDeclaredCap(context, result, scoreAudit);
    }

    private static Map<String, Integer> businessAxisScores(String html) {
        Map<String, Integer> cards = AtlasScoreContract.scoreCards(html);
        Map<String, Integer> businessScores = new LinkedHashMap<>();
        for (String axis : AtlasScoreContract.BUSINESS_AXES) {
            Integer score = cards.get(axis);
            if (score != null) {
                businessScores.put(axis, score);
            }
        }
        return businessScores;
    }

    private static boolean allEqual(Map<String, Integer> scores) {
        return scores.values().stream().distinct().count() == 1;
    }

    private void validateDistinctAxisEvidence(AtlasContext context,
                                              AtlasValidationResult result,
                                              String scoreAudit,
                                              Map<String, Integer> businessScores) {
        int uniformScore = businessScores.values().stream().findFirst().orElse(-1);
        if (uniformScore < EXCELLENT_SCORE) {
            return;
        }
        Set<String> supportRows = new LinkedHashSet<>();
        for (String axis : businessScores.keySet()) {
            String row = AtlasScoreContract.rowContext(scoreAudit, axis);
            if (!row.isBlank()) {
                supportRows.add(evidenceSignature(row, axis));
            }
        }
        if (supportRows.size() < Math.max(3, businessScores.size() - 1)) {
            result.add(id(), "Uniform excellent score axes reuse too little distinct support evidence: "
                            + supportRows.size() + " distinct rows for " + businessScores.size() + " axes",
                    context.atlasHtml(),
                    "Uniform scores are acceptable only when each axis has its own auditable GQM/source/layer rationale, not a repeated generic explanation.");
        }
    }

    private void validateNoScoreExceedsDeclaredCap(AtlasContext context,
                                                   AtlasValidationResult result,
                                                   String scoreAudit) {
        int cap = declaredCap(scoreAudit);
        if (cap < 0) {
            return;
        }
        for (Map.Entry<String, Integer> card : AtlasScoreContract.scoreCards(context.atlasText()).entrySet()) {
            if (card.getValue() > cap) {
                result.add(id(), "Score card exceeds declared cap: " + card.getKey()
                                + "=" + card.getValue() + ", cap=" + cap,
                        context.atlasHtml(),
                        "A declared atlas cap is binding: lower the score or explain a colder cap calculation.");
            }
        }
    }

    private static int declaredCap(String scoreAudit) {
        Matcher matcher = CAP_VALUE_PATTERN.matcher(scoreAudit);
        int cap = -1;
        while (matcher.find()) {
            int candidate = Integer.parseInt(matcher.group(1));
            if (candidate >= 80 && candidate <= 100) {
                cap = cap < 0 ? candidate : Math.min(cap, candidate);
            }
        }
        return cap;
    }

    private static String evidenceSignature(String row, String axis) {
        String withoutAxis = row.replace(axis, "");
        return AtlasText.normalizeWhitespace(SCORE_NUMBER_PATTERN.matcher(withoutAxis).replaceAll("#"));
    }
}
