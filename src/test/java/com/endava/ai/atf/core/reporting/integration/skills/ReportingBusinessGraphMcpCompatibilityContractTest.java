package com.endava.ai.atf.core.reporting.integration.skills;

import com.endava.ai.core.reporting.graph.BusinessGraphLayout;
import com.endava.ai.core.reporting.graph.BusinessGraphLayoutAdapter;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.skills.business.model.BusinessGraphConstruction;
import com.endava.ai.skills.business.model.BusinessObject;
import com.endava.ai.skills.mcp.validation.profile.graph.McpGraphDiagramProfile;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Reporting-owned integration guard proving the shared graph adapter stays compatible with MCP graph gates.
 */
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.REPORTING_PACKAGE_SHAPE)
@AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_CLASS)
@Listeners(TestListener.class)
public final class ReportingBusinessGraphMcpCompatibilityContractTest {
    private final BusinessGraphLayoutAdapter adapter = new BusinessGraphLayoutAdapter();

    @Test
    public void reportingBusinessGraphLayoutSatisfiesMcpFinalReportDensityAndSemanticGate() {
        BusinessGraphLayout layout = adapter.render(new BusinessGraphConstruction(
                new BusinessObject("Customer refund", "refund intent is visible"),
                "support staff resolves customer refund",
                new BusinessObject("Support case", "owned resolution record"),
                List.of("anchors", "captures", "proves result", "awaits settlement", "notifies customer"),
                new BusinessObject("Settlement posting", "downstream frontier is capped"),
                "Proof stays in final report audit sections, not in business graph nodes"));

        McpGraphDiagramProfile profile = new McpGraphDiagramProfile(layout.html());

        Assert.assertTrue(profile.densityBlockers().isEmpty(),
                "Reporting graph layout must remain compatible with MCP density gate: "
                        + profile.densityBlockers());
        Assert.assertTrue(profile.semanticNodeBlockers().isEmpty(),
                "Reporting graph layout must expose MCP-readable owned/frontier semantics: "
                        + profile.semanticNodeBlockers());
    }
}
