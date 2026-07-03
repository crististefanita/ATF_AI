package com.endava.ai.skills.mcp.tests.contracts;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.skills.mcp.contract.McpContractMatrix;
import com.endava.ai.skills.mcp.contract.McpProfile;
import com.endava.ai.skills.mcp.contract.execution.McpRuleRoute;
import com.endava.ai.skills.mcp.contract.execution.McpRuleRoutingCatalog;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.Collectors;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-rule-routing-catalog-contract-test")
public class McpRuleRoutingCatalogContractTest {
    @Test
    @McpCheckRef("mcp.rule-routing.profiles.visible")
    public void ruleRoutingCatalogPublishesEveryAgentFacingProfile() {
        Assert.assertEquals(Set.copyOf(McpRuleRoutingCatalog.routedProfiles()), Set.of(
                McpProfile.UNIVERSAL,
                McpProfile.UI_PACKAGE,
                McpProfile.HTML_REPORT,
                McpProfile.HTML_EXAMPLE_CANDIDATE,
                McpProfile.SUITE_ATLAS,
                McpProfile.FULL_PACKAGE));

        Assert.assertFalse(McpRuleRoutingCatalog.routesFor(McpProfile.FULL_PACKAGE).isEmpty(),
                "Full-package route must be visible for close-out audits.");
        Assert.assertFalse(McpRuleRoutingCatalog.routesFor(McpProfile.HTML_EXAMPLE_CANDIDATE).isEmpty(),
                "Candidate route must stay separate from package close-out.");
    }

    @Test
    @McpCheckRef("mcp.rule-routing.matrix.unique")
    public void ruleRoutingCatalogExposesUniqueProfileRulePairsAndMatrixRows() {
        Set<String> routeKeys = McpRuleRoutingCatalog.routes().stream()
                .map(route -> route.profile().name() + "::" + route.ruleClass().getName())
                .collect(Collectors.toSet());

        Assert.assertEquals(routeKeys.size(), McpRuleRoutingCatalog.routes().size(),
                "A profile must not route the same executable rule twice.");

        for (McpRuleRoute route : McpRuleRoutingCatalog.routes()) {
            Assert.assertFalse(route.contractSpec().id().isBlank(),
                    "Every routed rule must expose a contract id: " + route.ruleClass());
            Assert.assertFalse(route.contractSpec().repairHint().isBlank(),
                    "Every routed rule must expose repair guidance: " + route.ruleClass());
            Assert.assertEquals(route.descriptor().ruleId(), route.contractSpec().id(),
                    "Descriptor rule id must match routed contract id: " + route.ruleClass());
            Assert.assertFalse(route.descriptor().repairRoute().isBlank(),
                    "Every routed rule must expose an agent repair route: " + route.ruleClass());
        }

        String matrix = McpContractMatrix.allProfilesRuleMatrix();
        Assert.assertTrue(matrix.contains("FULL_PACKAGE | mcp.gate.package-closeout"),
                "All-profile matrix must expose the final package close-out route.");
        Assert.assertTrue(matrix.contains("HTML_EXAMPLE_CANDIDATE"),
                "All-profile matrix must expose the candidate route separately.");
        Assert.assertTrue(matrix.contains("rule_id | family | artifact_family | cost"),
                "All-profile matrix must expose typed family and execution-cost columns.");
        Assert.assertTrue(matrix.contains("artifact_family"),
                "All-profile matrix must reuse the existing McpArtifactFamily vocabulary where artifact routes are known.");
        Assert.assertTrue(matrix.contains("MCP_CLOSEOUT") && matrix.contains("validation/mcp"),
                "All-profile matrix must route close-out repairs to validation/mcp.");
    }
}
