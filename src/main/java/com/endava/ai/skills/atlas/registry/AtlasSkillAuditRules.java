package com.endava.ai.skills.atlas.registry;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import com.endava.ai.skills.atlas.rules.audit.AtlasTestAuditExtentRule;
import com.endava.ai.skills.atlas.rules.audit.AtlasTestAuditExtentSummaryCommandRule;
import com.endava.ai.skills.atlas.rules.audit.AtlasTestAuditManifestCommandSyncRule;
import com.endava.ai.skills.atlas.rules.audit.AtlasTestAuditTotalsRule;
import com.endava.ai.skills.atlas.rules.audit.AtlasTestAuditUiRuntimeScopeParityRule;
import com.endava.ai.skills.atlas.rules.skill.AtlasSkillMigrationReadinessRule;
import com.endava.ai.skills.atlas.rules.skill.AtlasSkillParameterizationRule;

import java.util.List;

public final class AtlasSkillAuditRules {
    private AtlasSkillAuditRules() {
    }

    public static List<AtlasValidationRule> rules() {
        return List.of(
                new AtlasSkillMigrationReadinessRule(),
                new AtlasSkillParameterizationRule(),
                new AtlasTestAuditExtentRule(),
                new AtlasTestAuditTotalsRule(),
                new AtlasTestAuditExtentSummaryCommandRule(),
                new AtlasTestAuditUiRuntimeScopeParityRule(),
                new AtlasTestAuditManifestCommandSyncRule()
        );
    }
}
