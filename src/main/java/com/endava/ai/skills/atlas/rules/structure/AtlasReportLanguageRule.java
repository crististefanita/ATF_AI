package com.endava.ai.skills.atlas.rules.structure;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.contract.yaml.AtlasYamlGraphReportingCatalog;
import com.endava.ai.skills.atlas.contract.yaml.AtlasYamlGraphReportingContract;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.Locale;

public final class AtlasReportLanguageRule implements AtlasValidationRule {
    private static final AtlasYamlGraphReportingContract CONTRACT =
            AtlasYamlGraphReportingCatalog.require("atlas.candidate.report-language.romanian");

    @Override
    public String id() {
        return CONTRACT.id();
    }

    @Override
    public String expected() {
        return "Published Atlas reports declare html lang=" + CONTRACT.expectedLanguage()
                + " and visibly state the report language.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String lower = context.lowerAtlasText();
        String expectedLanguage = CONTRACT.expectedLanguage().toLowerCase(Locale.ROOT);
        require(context, result,
                lower.matches("(?s).*<html\\b[^>]*\\blang\\s*=\\s*[\"']" + expectedLanguage + "[\"'][^>]*>.*"),
                "Atlas report does not declare html lang=\"" + expectedLanguage + "\"",
                CONTRACT.repair());
        for (String token : CONTRACT.requiredVisibleTokens()) {
            require(context, result,
                    lower.contains(token.toLowerCase(Locale.ROOT)),
                    "Atlas report is missing visible language marker token: " + token,
                    CONTRACT.repair());
        }
        for (String sentence : CONTRACT.requiredRomanianSentences()) {
            require(context, result,
                    lower.contains(sentence.toLowerCase(Locale.ROOT)),
                    "Atlas report is missing required Romanian key sentence: " + sentence,
                    CONTRACT.repair());
        }
        for (String phrase : CONTRACT.forbiddenKeyEnglishPhrases()) {
            require(context, result,
                    !lower.contains(phrase.toLowerCase(Locale.ROOT)),
                    "Atlas report still contains key explanatory English phrase: " + phrase,
                    CONTRACT.repair());
        }
    }

    private static void require(AtlasContext context,
                                AtlasValidationResult result,
                                boolean condition,
                                String message,
                                String repair) {
        if (!condition) {
            result.add(CONTRACT.id(), message, context.atlasHtml(), repair);
        }
    }
}
