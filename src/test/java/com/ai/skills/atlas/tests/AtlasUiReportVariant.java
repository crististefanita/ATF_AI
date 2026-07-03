package com.ai.skills.atlas.tests;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
class AtlasUiReportVariant {
    String fileSuffix;
    String testTitleSuffix;
    String documentTitleSuffix;
    String reportNameSuffix;
    String descriptionPrefix;
    String fragmentationReason;
    String auditShape;
    String suiteProfile;
    String capability;
    String purpose;
    String expected;
    String manifestScopeNote;

    static AtlasUiReportVariant currentInventory() {
        return new AtlasUiReportVariant(
                "_ui-agentic-current-extent-report",
                " UI Agentic Current Inventory",
                " UI Agentic Current Extent Report",
                " UI Agentic",
                "All @Test methods currently present under ",
                "Atlas needs one stable report containing every UI test method added in the package; "
                        + "browser execution remains evidenced separately by per-run artifacts.",
                "One Extent test case per Java @Test method from the UI tests source inventory.",
                "ATLAS_UI_AGENTIC_CURRENT",
                "UI Agentic current source inventory",
                "Publish a complete Atlas-visible Extent surface for every UI Agentic @Test method.",
                "The UI Agentic Atlas evidence report lists this method exactly once.",
                "this is the Atlas UI Agentic inventory report. It contains every current Java @Test method "
                        + "from the configured UI tests source root. Runtime browser proof remains in the "
                        + "package run artifacts and is not collapsed into this inventory artifact."
        );
    }

    String scopeLabel(AtlasUiScope scope) {
        return scope.name() + " " + capability;
    }
}
