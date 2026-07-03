package com.ai.skills.atlas.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AtlasAuditLinksParser {
    private static final Pattern LINK = Pattern.compile("(?is)<a\\b[^>]*href\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>(.*?)</a>");

    List<AtlasReportModel.AuditLink> parse(AtlasHtmlDocument document) {
        String auditSection = document.sectionByAtlasAttribute("audit");
        List<AtlasReportModel.AuditLink> links = new ArrayList<>();
        Matcher matcher = LINK.matcher(auditSection.isBlank() ? document.html() : auditSection);
        while (matcher.find()) {
            links.add(new AtlasReportModel.AuditLink(
                    matcher.group(1),
                    AtlasHtmlDocument.plain(matcher.group(2))));
        }
        return Collections.unmodifiableList(links);
    }
}
