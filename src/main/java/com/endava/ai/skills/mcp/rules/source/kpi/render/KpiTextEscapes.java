package com.endava.ai.skills.mcp.rules.source.kpi.render;

import com.endava.ai.core.support.html.HtmlText;

final class KpiTextEscapes {
    private KpiTextEscapes() {
    }

    static String markdown(String value) {
        return HtmlText.markdownCell(value);
    }

    static String html(String value) {
        return HtmlText.html(value);
    }
}
