package com.endava.ai.skills.atlas.rules.business;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.endava.ai.skills.atlas.rules.AtlasText;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasPartialFlowOwnershipConsistencyRule implements AtlasValidationRule {
    private static final Pattern TABLE_ROW = Pattern.compile("(?is)<tr>(.*?)</tr>");
    private static final List<String> WEAK_MATURITY = List.of(
            "partial",
            "diagnostic",
            "surface-only",
            "suprafata",
            "incomplet",
            "neexplicat");
    private static final List<String> STRONG_OWNERSHIP = List.of(
            "owned puternic",
            "strong owned",
            "fully owned",
            "ownership complet",
            "complete ownership",
            "end-to-end owned");

    @Override
    public String id() {
        return "atlas.candidate.partial-flow-not-sold-as-owned";
    }

    @Override
    public String expected() {
        return "A partial, diagnostic or surface-only flow is never sold as strong ownership without an explicit cap.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        Matcher matcher = TABLE_ROW.matcher(context.lowerAtlasText());
        int checkedRows = 0;
        while (matcher.find()) {
            String row = AtlasText.plainText(matcher.group(1));
            if (row.isBlank()) {
                continue;
            }
            checkedRows++;
            boolean weak = AtlasSemanticVocabulary.containsAny(row, WEAK_MATURITY);
            boolean strong = AtlasSemanticVocabulary.containsAny(row, STRONG_OWNERSHIP);
            boolean capped = AtlasSemanticVocabulary.containsAny(row, AtlasSemanticVocabulary.CAP_MARKERS)
                    || AtlasSemanticVocabulary.containsAny(row, AtlasSemanticVocabulary.LIMIT_MARKERS);
            if (weak && strong && !capped) {
                result.add(id(), "A weak-maturity flow is sold as strong ownership without cap language: "
                                + AtlasText.abbreviate(row, 220),
                        context.atlasHtml(),
                        "If a flow is partial, diagnostic or surface-only, keep ownership wording capped and explain the limit in the same row.");
            }
        }
        if (checkedRows < 8) {
            result.add(id(), "Atlas has too few table rows to audit ownership consistency: " + checkedRows,
                    context.atlasHtml(),
                    "Publish enough source-backed rows for the atlas to compare maturity and ownership claims.");
        }
    }

}
