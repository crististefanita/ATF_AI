package com.ai.skills.atlas.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class AtlasSectionsParser {
    List<AtlasReportModel.AtlasSection> parse(AtlasHtmlDocument document) {
        List<AtlasReportModel.AtlasSection> sections = new ArrayList<>();
        for (AtlasHtmlDocument.AtlasSectionTag tag : document.sectionTags()) {
            sections.add(new AtlasReportModel.AtlasSection(tag.attribute(), tag.summary(), tag.index()));
        }
        return Collections.unmodifiableList(sections);
    }
}
