package com.ai.skills.atlas.rules.business.trace;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasText;
import com.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasLayerTraceConcreteEvidenceRule implements AtlasValidationRule {
    private static final int MIN_CONCRETE_TRACE_ROWS = 3;
    private static final Pattern ROW_PATTERN = Pattern.compile("(?is)<tr>(.*?)</tr>");
    private static final Pattern JAVA_IDENTIFIER_PATTERN = Pattern.compile(
            "\\b[A-Z][A-Za-z0-9]*(?:Actions|Component|Page|Service|Validation|Context|Model|Constants|Tests|Support)\\b");

    @Override
    public String id() {
        return "atlas.candidate.layer-trace.concrete-evidence";
    }

    @Override
    public String expected() {
        return "Layer Trace Map reconstructs major business flows through concrete Java layers, not only generic layer names.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String section = AtlasText.sectionBetween(context.atlasText(), "Layer Trace Map", "4.5");
        if (section.isBlank()) {
            section = AtlasText.sectionBetween(context.atlasText(), "Harta Trace Pe Layere", "4.5");
        }
        if (section.isBlank()) {
            result.add(id(), "Layer Trace Map section is missing", context.atlasHtml(),
                    "Add a Layer Trace Map that follows test -> action/component/page/service/validation/model/constants -> business object.");
            return;
        }

        int dataRows = 0;
        int concreteRows = 0;
        Matcher matcher = ROW_PATTERN.matcher(section);
        while (matcher.find()) {
            String row = matcher.group(1);
            if (row.contains("<th")) {
                continue;
            }
            dataRows++;
            if (hasConcreteLayerEvidence(row)) {
                concreteRows++;
            }
        }
        if (dataRows == 0) {
            result.add(id(), "Layer Trace Map has no data rows", context.atlasHtml(),
                    "Add one row per important business flow with concrete class/method/layer evidence.");
            return;
        }
        if (concreteRows < MIN_CONCRETE_TRACE_ROWS) {
            result.add(id(), "Layer Trace Map has too few concrete source-backed rows: "
                            + concreteRows + " / " + dataRows,
                    context.atlasHtml(),
                    "Name concrete action/component/page/service/validation/model classes in Layer Trace Map, not only generic layer categories.");
        }
    }

    private static boolean hasConcreteLayerEvidence(String row) {
        if (row.contains("<code>")) {
            return true;
        }
        return JAVA_IDENTIFIER_PATTERN.matcher(row).find();
    }

}
