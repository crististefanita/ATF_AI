package com.endava.ai.skills.atlas.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class AtlasSourceTraceParser {
    List<AtlasReportModel.SourceTraceRow> parse(AtlasHtmlDocument document) {
        String sourceTrace = document.sectionByAtlasAttribute("source", "trace");
        if (sourceTrace.isBlank()) {
            sourceTrace = document.sectionByAtlasAttribute("source", "business");
        }
        List<AtlasReportModel.SourceTraceRow> rows = new ArrayList<>();
        for (List<String> cells : document.dataRowsFrom(sourceTrace)) {
            if (cells.size() >= 3) {
                rows.add(new AtlasReportModel.SourceTraceRow(cells));
            }
        }
        return Collections.unmodifiableList(rows);
    }
}
