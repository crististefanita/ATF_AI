package com.ai.skills.atlas.contract.catalog;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.contract.AtlasRuleIds;
import com.ai.skills.atlas.contract.AtlasRuleSpec;

import java.util.List;

import static com.ai.skills.atlas.contract.AtlasRuleCategory.BASE_OUTPUT;
import static com.ai.skills.atlas.contract.AtlasRuleCategory.BUSINESS_COMPREHENSION;
import static com.ai.skills.atlas.contract.AtlasRuleCategory.GRAPH_STYLE;
import static com.ai.skills.atlas.contract.AtlasRuleCategory.PROOF_TRACE;
import static com.ai.skills.atlas.contract.AtlasRuleCategory.STRUCTURE;
import static com.ai.skills.atlas.contract.AtlasRuleSeverity.BLOCKER;
import static com.ai.skills.atlas.contract.AtlasRuleSeverity.MAJOR;
import static com.ai.skills.atlas.contract.catalog.AtlasRuleCatalogSupport.OUTPUT_REPAIR;
import static com.ai.skills.atlas.contract.catalog.AtlasRuleCatalogSupport.base;

public final class AtlasBaseRuleCatalog {
    private AtlasBaseRuleCatalog() {
    }

    public static List<AtlasRuleSpec> rules() {
        return List.of(
                base(AtlasRuleIds.Base.OUTPUT_EXISTS, BASE_OUTPUT, BLOCKER,
                        "The atlas file is the generated output under contract.",
                        OUTPUT_REPAIR),
                base(AtlasRuleIds.Base.DECLARED_OUTPUT, BASE_OUTPUT, BLOCKER,
                        "The skill needs a declared atlas artifact, either generic or scoped to the analyzed ATF/package.",
                        "Produce " + AtlasContext.BUSINESS_ATLAS_FILE
                                + " for the generic template or <ATF_NAME>"
                                + AtlasContext.BUSINESS_ATLAS_SUFFIX + " for scoped analyses."),
                base(AtlasRuleIds.Base.USES_SHARED_CSS, BASE_OUTPUT, BLOCKER,
                        "The atlas must reuse the shared report visual system instead of inventing local styling.",
                        "Link the shared stylesheet and remove local visual-system overrides."),
                base(AtlasRuleIds.Base.SECTIONS_COLLAPSIBLE, STRUCTURE, MAJOR,
                        "Atlas readers need navigable sections, not one long wall of HTML.",
                        "Use details/summary sections with only the minimum business overview open by default."),
                base(AtlasRuleIds.Base.BUSINESS_FLOW_PRESENT, BUSINESS_COMPREHENSION, BLOCKER,
                        "A business atlas without business flow diagrams is only a test inventory.",
                        "Add an application business flow graph built from source-level test understanding."),
                base(AtlasRuleIds.Base.ROW_TEMPLATE_COMPLETE, PROOF_TRACE, MAJOR,
                        "Rows must expose a repeatable mapping shape so the atlas is auditable.",
                        "Add rows that connect source/test/layer evidence to business object and outcome."),
                base(AtlasRuleIds.Base.SOURCE_READ_ONLY, BASE_OUTPUT, BLOCKER,
                        "The atlas skill is analysis-only; changing analyzed sources violates the contract.",
                        "Restore source changes and regenerate only the atlas output."),
                base(AtlasRuleIds.Base.DIAGRAM_STYLE_MINIMAL, GRAPH_STYLE, MAJOR,
                        "Diagrams must stay readable and aligned with shared-report-reference.css.",
                        "Reduce local colors, oversized nodes, and custom visual noise.")
        );
    }
}
