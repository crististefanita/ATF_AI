package com.ai.skills.atlas.rules.business;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.ai.skills.atlas.rules.AtlasText;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasObjectMaturityMatrixRule implements AtlasValidationRule {
    private static final Pattern ROW_PATTERN = Pattern.compile("(?is)<tr>(.*?)</tr>");

    @Override
    public String id() {
        return "atlas.candidate.object-maturity.matrix";
    }

    @Override
    public String expected() {
        return "Major business objects are classified by maturity, evidence and comprehension limit, not just named.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        if (!AtlasText.containsAll(html, "maturitate", "limite")) {
            result.add(id(), "Atlas does not expose an object maturity and limits section",
                    context.atlasHtml(),
                    "Add an object maturity matrix that separates owned, partial, diagnostic and surface-only business objects.");
        }

        if (!AtlasSemanticVocabulary.containsAny(html, "obiect business", "business object")) {
            result.add(id(), "Atlas does not anchor maturity around business objects",
                    context.atlasHtml(),
                    "Use business objects as the unit of maturity; technical layers are evidence only.");
        }

        int maturityRows = 0;
        Matcher matcher = ROW_PATTERN.matcher(html);
        while (matcher.find()) {
            String row = AtlasText.plainText(matcher.group(1));
            boolean hasMaturity = AtlasSemanticVocabulary.containsAny(row, AtlasSemanticVocabulary.MATURITY_MARKERS);
            boolean hasLimit = AtlasSemanticVocabulary.containsAny(row, AtlasSemanticVocabulary.LIMIT_MARKERS);
            if (hasMaturity && hasLimit) {
                maturityRows++;
            }
        }
        if (maturityRows < 5) {
            result.add(id(), "Object maturity matrix is too shallow: maturity rows=" + maturityRows,
                    context.atlasHtml(),
                    "Publish enough rows where each major object has maturity, evidence and a comprehension limit.");
        }
    }

}
