package com.ai.skills.atlas.registry;

import com.ai.skills.atlas.rules.AtlasValidationRule;
import com.ai.skills.atlas.rules.base.AtlasBusinessFlowRule;
import com.ai.skills.atlas.rules.base.AtlasCollapsibleSectionsRule;
import com.ai.skills.atlas.rules.base.AtlasDeclaredOutputRule;
import com.ai.skills.atlas.rules.base.AtlasDiagramStyleRule;
import com.ai.skills.atlas.rules.base.AtlasOutputExistsRule;
import com.ai.skills.atlas.rules.base.AtlasRowTemplateRule;
import com.ai.skills.atlas.rules.base.AtlasSharedCssRule;
import com.ai.skills.atlas.rules.base.AtlasSourceReadOnlyRule;

import java.util.List;

public final class AtlasBaseRules {
    private AtlasBaseRules() {
    }

    public static List<AtlasValidationRule> rules() {
        return List.of(
                new AtlasOutputExistsRule(),
                new AtlasDeclaredOutputRule(),
                new AtlasSharedCssRule(),
                new AtlasCollapsibleSectionsRule(),
                new AtlasBusinessFlowRule(),
                new AtlasRowTemplateRule(),
                new AtlasSourceReadOnlyRule(),
                new AtlasDiagramStyleRule()
        );
    }
}
