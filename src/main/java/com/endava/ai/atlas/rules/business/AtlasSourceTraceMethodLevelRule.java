package com.endava.ai.atlas.rules.business;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;
import com.endava.ai.atlas.rules.AtlasValidationRule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasSourceTraceMethodLevelRule implements AtlasValidationRule {
    private static final Pattern METHOD_TOKEN = Pattern.compile("\\b[a-z][A-Za-z0-9_]*\\(\\)");

    @Override
    public String id() {
        return "atlas.candidate.source-trace.method-level";
    }

    @Override
    public String expected() {
        return "Source-to-Business Trace includes representative method-level claims and explicit source-layer chains, not only class families.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String section = sectionAfter(context.lowerAtlasText(), "source-to-business trace", 10_000);
        if (section.isBlank()) {
            result.add(id(), "Missing Source-to-Business Trace section", context.atlasHtml(),
                    "Add a central trace table that maps representative test methods to layer path and business outcome.");
            return;
        }
        int methodCount = countMethods(section);
        if (methodCount < 3) {
            result.add(id(), "Source-to-Business Trace has too few representative method claims: " + methodCount,
                    context.atlasHtml(),
                    "Include at least three concrete test/validation method claims, for example paymentSuccessDoesNotExposeReceiptOrderOrInvoiceIdentity().");
        }
        int chainCount = count(section, "->");
        if (chainCount < 8) {
            result.add(id(), "Source-to-Business Trace has too few layer-chain arrows: " + chainCount,
                    context.atlasHtml(),
                    "Show cold chains such as test -> action/component/page/service/validation/model/constants -> business object.");
        }
        for (String layerToken : List.of("actions", "component", "page", "validation", "context")) {
            if (!section.contains(layerToken)) {
                result.add(id(), "Source-to-Business Trace misses layer token: " + layerToken,
                        context.atlasHtml(),
                        "Keep the trace source-backed: test method -> action/component/page/service/validation/model/constants -> business object.");
            }
        }
    }

    private static String sectionAfter(String html, String marker, int maxChars) {
        int index = html.indexOf(marker);
        if (index < 0) {
            return "";
        }
        return html.substring(index, Math.min(html.length(), index + maxChars));
    }

    private static int countMethods(String text) {
        Matcher matcher = METHOD_TOKEN.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static int count(String text, String token) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }
}
