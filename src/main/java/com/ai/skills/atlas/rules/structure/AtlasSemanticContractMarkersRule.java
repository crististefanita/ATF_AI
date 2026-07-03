package com.ai.skills.atlas.rules.structure;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasText;
import com.ai.skills.atlas.rules.AtlasValidationRule;

public final class AtlasSemanticContractMarkersRule implements AtlasValidationRule {
    private static final int MAX_ALLOWED_HIDDEN_MARKERS = 6;
    private static final int MIN_DATA_ATLAS_MARKERS = 6;

    @Override
    public String id() {
        return "atlas.candidate.semantic-contract-markers";
    }

    @Override
    public String expected() {
        return "Candidate atlas exposes contract meaning with semantic data-atlas/data-contract attributes, not many hidden text markers.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.lowerAtlasText();
        int hiddenMarkers = AtlasText.count(html, " hidden");
        int dataMarkers = AtlasText.count(html, "data-atlas")
                + AtlasText.count(html, "data-contract")
                + AtlasText.count(html, "data-section")
                + AtlasText.count(html, "data-role");
        if (hiddenMarkers > MAX_ALLOWED_HIDDEN_MARKERS) {
            result.add(id(), "Atlas relies on too many hidden markers: " + hiddenMarkers,
                    context.atlasHtml(),
                    "Replace hidden keyword scaffolding with semantic data-atlas/data-contract attributes. Hidden text may stay only for rare accessibility/backward-compatibility cases.");
        }
        if (dataMarkers < MIN_DATA_ATLAS_MARKERS) {
            result.add(id(), "Atlas has too few semantic contract attributes: " + dataMarkers,
                    context.atlasHtml(),
                    "Add semantic attributes such as data-atlas-section, data-atlas-role, data-contract or data-business-flow so tests read structure, not invisible keyword text.");
        }
    }

}
