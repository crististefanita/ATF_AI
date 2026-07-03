package com.ai.skills.atlas.rules.business;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasSemanticVocabulary;
import com.ai.skills.atlas.rules.AtlasText;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasFlowMaturityDistributionRule implements AtlasValidationRule {
    private static final Pattern TABLE_ROW = Pattern.compile("(?is)<tr>(.*?)</tr>");
    private static final int MIN_MATURITY_FAMILY_MARKERS = 3;

    @Override
    public String id() {
        return "atlas.candidate.flow-maturity.distribution";
    }

    @Override
    public String expected() {
        return "Atlas explains the maturity distribution of flows, not just isolated business rows.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        if (!html.contains("maturitate") && !html.contains("maturity")) {
            result.add(id(), "Atlas does not expose a maturity/distribution vocabulary",
                    context.atlasHtml(),
                    "Add a maturity distribution that separates owned, partial, diagnostic and surface-only flows.");
            return;
        }

        int representedFamilies = AtlasSemanticVocabulary.maturityFamilyCoverage(html);
        if (representedFamilies < MIN_MATURITY_FAMILY_MARKERS) {
            result.add(id(), "Atlas maturity distribution is too flat: represented families=" + representedFamilies,
                    context.atlasHtml(),
                    "Show several maturity families discovered from the analyzed source, using the atlas semantic vocabulary.");
        }

        int maturityRows = 0;
        Matcher matcher = TABLE_ROW.matcher(html);
        while (matcher.find()) {
            String row = AtlasText.plainText(matcher.group(1));
            if (AtlasSemanticVocabulary.containsAny(row, AtlasSemanticVocabulary.MATURITY_MARKERS)) {
                maturityRows++;
            }
        }
        if (maturityRows < 5) {
            result.add(id(), "Atlas has too few maturity-classified business rows: " + maturityRows,
                    context.atlasHtml(),
                    "Classify enough source-backed flows by maturity so the atlas can explain distribution, not just mention limits.");
        }
    }

}
