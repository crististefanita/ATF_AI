package com.ai.skills.atlas.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AtlasBusinessGraphParser {
    private static final Pattern SUMMARY = Pattern.compile("(?is)<summary\\b[^>]*>(.*?)</summary>");
    private static final Pattern BUSINESS_FLOW_OPENING_TAG = Pattern.compile(
            "(?is)<details\\b(?<attributes>[^>]*data-business-flow\\s*=\\s*['\"](?<flow>[^'\"]+)['\"][^>]*)>");

    List<AtlasReportModel.BusinessGraph> parse(AtlasHtmlDocument document,
                                               List<AtlasReportModel.AtlasSection> sections) {
        List<AtlasReportModel.BusinessGraph> graphs = new ArrayList<>();
        for (AtlasReportModel.AtlasSection section : sections) {
            if (section.normalizedAttribute().contains("business flow graph")) {
                graphs.add(new AtlasReportModel.BusinessGraph(section.attribute(), section.summary(), section.index()));
            }
        }
        Matcher matcher = BUSINESS_FLOW_OPENING_TAG.matcher(document.html());
        while (matcher.find()) {
            if (matcher.group("attributes").toLowerCase(Locale.ROOT).contains("data-atlas-section")) {
                continue;
            }
            graphs.add(new AtlasReportModel.BusinessGraph(
                    matcher.group("flow"),
                    AtlasHtmlDocument.firstPlain(SUMMARY, nearbyBody(document.html(), matcher.end())),
                    matcher.start()));
        }
        return Collections.unmodifiableList(graphs);
    }

    private static String nearbyBody(String html, int start) {
        return html.substring(start, Math.min(html.length(), start + 800));
    }
}
