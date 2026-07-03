package com.endava.ai.skills.atlas.rules.business.trace;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasText;

public final class AtlasMappingDepthRule implements AtlasValidationRule {
    private static final int MIN_MAPPING_ROWS = 8;

    @Override
    public String id() {
        return "atlas.candidate.mapping.depth";
    }

    @Override
    public String expected() {
        return "Source-to-Business Trace is granular enough to explain comprehension: multiple source rows, business objects, evidence layers, maturity, and comprehension limits.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        int index = html.indexOf("source-to-business trace");
        if (index < 0) {
            result.add(id(), "Missing Source-to-Business Trace section", context.atlasHtml(),
                    "Add a mapping section from test/class to business object, layer evidence, maturity, and comprehension limit.");
            return;
        }
        String section = html.substring(index, Math.min(html.length(), index + 8_000));
        int rows = AtlasText.count(section, "<tr>") - 1;
        if (rows < MIN_MAPPING_ROWS) {
            result.add(id(), "Source-to-Business Trace has too few data rows: " + rows, context.atlasHtml(),
                    "Add 5-8 more rows for saturated and important families so the atlas explains the tested business, not just top-level lanes.");
        }
        for (String concept : new String[]{"business object", "evidence layer", "maturity", "comprehension limit"}) {
            if (!section.contains(concept)) {
                result.add(id(), "Mapping section missing column/concept: " + concept, context.atlasHtml(),
                        "Keep the row template complete: test/class, business object, evidence layer, maturity, comprehension limit.");
            }
        }
        if (!section.contains("validation") || !section.contains("actions")) {
            result.add(id(), "Mapping does not expose enough source-layer evidence", context.atlasHtml(),
                    "Reference action/component/page/validation layers so the atlas is based on source structure, not class names only.");
        }
    }

}
