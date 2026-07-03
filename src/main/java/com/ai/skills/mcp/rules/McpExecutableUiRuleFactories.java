package com.ai.skills.mcp.rules;

import com.ai.skills.mcp.rules.run.RunArtifactSpineRule;
import com.ai.skills.mcp.rules.run.RunCountRule;
import com.ai.skills.mcp.rules.run.RunDiffProgressionRule;
import com.ai.skills.mcp.rules.run.RunExtentProofRule;
import com.ai.skills.mcp.rules.run.RunExtentProvenanceRule;
import com.ai.skills.mcp.rules.run.RunIndependenceRule;
import com.ai.skills.mcp.rules.run.RunStateCompletenessRule;
import com.ai.skills.mcp.rules.ui.BaseTestUiRule;
import com.ai.skills.mcp.rules.ui.ClaimedAbsenceAssertionRule;
import com.ai.skills.mcp.rules.ui.GeneratedUiApiStructureRule;
import com.ai.skills.mcp.rules.ui.NoDirectUiInfrastructureRule;
import com.ai.skills.mcp.rules.ui.NoFrameworkReportingInGeneratedUiTestsRule;
import com.ai.skills.mcp.rules.ui.RealUiInteractionRule;
import com.ai.skills.mcp.rules.ui.UiWrapperHandoffRule;
import com.ai.skills.mcp.rules.ui.ValidationLayerReportingRule;

import java.util.Map;
import java.util.function.Supplier;

import static com.ai.skills.mcp.rules.McpExecutableRuleCatalog.*;

final class McpExecutableUiRuleFactories {
    private McpExecutableUiRuleFactories() {
    }

    static void register(Map<String, Supplier<McpValidationRule>> rules) {
        McpExecutableRuleCatalog.add(rules, CLAIMED_ABSENCE, ClaimedAbsenceAssertionRule::new);
        McpExecutableRuleCatalog.add(rules, WRAPPER_HANDOFF, UiWrapperHandoffRule::new);
        McpExecutableRuleCatalog.add(rules, RUN_COUNT, RunCountRule::new);
        McpExecutableRuleCatalog.add(rules, RUN_SPINE, RunArtifactSpineRule::new);
        McpExecutableRuleCatalog.add(rules, RUN_STATE, RunStateCompletenessRule::new);
        McpExecutableRuleCatalog.add(rules, RUN_EXTENT, RunExtentProofRule::new);
        McpExecutableRuleCatalog.add(rules, RUN_PROVENANCE, RunExtentProvenanceRule::new);
        McpExecutableRuleCatalog.add(rules, RUN_INDEPENDENCE, RunIndependenceRule::new);
        McpExecutableRuleCatalog.add(rules, RUN_DIFF, RunDiffProgressionRule::new);
        McpExecutableRuleCatalog.add(rules, BASE_TEST_UI, BaseTestUiRule::new);
        McpExecutableRuleCatalog.add(rules, UI_API_STRUCTURE, GeneratedUiApiStructureRule::new);
        McpExecutableRuleCatalog.add(rules, NO_FRAMEWORK_REPORTING, NoFrameworkReportingInGeneratedUiTestsRule::new);
        McpExecutableRuleCatalog.add(rules, NO_DIRECT_INFRA, NoDirectUiInfrastructureRule::new);
        McpExecutableRuleCatalog.add(rules, VALIDATION_REPORTING, ValidationLayerReportingRule::new);
        McpExecutableRuleCatalog.add(rules, REAL_UI, RealUiInteractionRule::new);
    }
}
