package com.endava.ai.skills.atlas.rules.business.trace;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasText;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasSourceTraceApplicationBusinessRule implements AtlasValidationRule {
    private static final Pattern ROW_PATTERN = Pattern.compile("(?is)<tr>(.*?)</tr>");
    private static final List<String> META_ROW_TOKENS = List.of(
            "agentul",
            "seed",
            "baseline",
            "contract test",
            "atlas test",
            "mcp",
            "html-ex",
            "readiness");

    @Override
    public String id() {
        return "atlas.candidate.source-trace.application-business-only";
    }

    @Override
    public String expected() {
        return "Source-to-Business Trace rows describe application business flows only; seed/meta/test-contract rows stay outside the business map.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String section = AtlasText.sectionBetween(context.lowerAtlasText(), "source-to-business trace", "atlas test audit");
        if (section.isBlank()) {
            section = AtlasText.sectionBetween(context.lowerAtlasText(), "trace sursa-catre-business", "audit teste atlas");
        }
        if (section.isBlank()) {
            result.add(id(), "Missing Source-to-Business Trace section", context.atlasHtml(),
                    "Add Source-to-Business Trace with application business flows discovered from the analyzed test package.");
            return;
        }
        Matcher matcher = ROW_PATTERN.matcher(section);
        int dataRows = 0;
        while (matcher.find()) {
            String row = matcher.group(1);
            if (row.contains("<th")) {
                continue;
            }
            dataRows++;
            for (String token : META_ROW_TOKENS) {
                if (row.contains(token)) {
                    result.add(id(), "Source-to-Business Trace contains non-business/meta row token: " + token,
                            context.atlasHtml(),
                            "Keep seed/baseline/agent/MCP/atlas-test facts in audit or limits sections; Source-to-Business Trace should map only application business flows.");
                    break;
                }
            }
        }
        if (dataRows == 0) {
            result.add(id(), "Source-to-Business Trace has no data rows", context.atlasHtml(),
                    "Add rows that start from test sources but explain real application business objects and observable results.");
        }
    }

}
