package com.endava.ai.atf.core.reporting.integration.skills;

import com.endava.ai.core.reporting.graph.BusinessGraphLayoutAdapter;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tiny consumer smoke proving MCP and Atlas are routed to the shared Reporting graph primitive.
 */
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.REPORTING_PACKAGE_SHAPE)
@AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_CLASS)
@Listeners(TestListener.class)
public final class ReportingBusinessGraphConsumerBridgeContractTest {
    private static final String SKILL_ARCHITECTURE = "atf-core/contracts/skills/skill-architecture.yml";
    private static final String REPORTING_SKILL_SURFACE = "skills/reporting/contracts/reporting-skill-surface.yml";

    @Test
    public void mcpAndAtlasDeclareReportingGraphAsSharedConsumerSurface() {
        Map<String, String> reporting = skill("skill.reporting");
        Map<String, String> mcp = skill("skill.mcp");
        Map<String, String> atlas = skill("skill.atlas");

        Assert.assertTrue(values(mcp, "consumes").contains("skill.reporting"),
                "MCP must consume shared Reporting for report shell and business graph layout.");
        Assert.assertTrue(values(atlas, "consumes").contains("skill.reporting"),
                "Atlas must consume shared Reporting for report shell and business graph layout.");
        Assert.assertTrue(values(mcp, "consumes").contains("skill.business-understanding"),
                "MCP must consume Business Understanding before Reporting renders graph layout.");
        Assert.assertTrue(values(atlas, "consumes").contains("skill.business-understanding"),
                "Atlas must consume Business Understanding before Reporting renders graph layout.");
        Assert.assertTrue(values(reporting, "mustMention").contains(
                        "BusinessGraphLayoutAdapter renders BusinessGraphConstruction without judging business truth"),
                "Reporting skill architecture must keep the neutral graph adapter contract visible.");
    }

    @Test
    public void reportingSurfacePublishesBusinessGraphAdapterAsConsumerContract() {
        Map<String, String> graph = SimpleYamlContractLoader.load(REPORTING_SKILL_SURFACE).stream()
                .filter(entry -> "reporting.skill-surface.business-graph-layout".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing reporting business graph layout contract"));

        Assert.assertEquals(graph.get("adapter"), BusinessGraphLayoutAdapter.class.getSimpleName());
        Assert.assertEquals(graph.get("consumesModel"), "BusinessGraphConstruction");
        Assert.assertTrue(graph.get("expected").contains("Business Understanding, MCP and Atlas keep semantic judgment"));
        Assert.assertTrue(graph.get("stopCondition").contains("preserves supplemental relationship labels"));
    }

    @Test
    public void mcpAndAtlasReadmesRouteGeneratedGraphsToReportingAdapter() throws Exception {
        String mcpReadme = Files.readString(AtfSourcePaths.mainJavaPackage("com.endava.ai.skills.mcp.contracts")
                .resolve(AtfSourcePaths.README_FILE_NAME));
        String atlasReadme = Files.readString(AtfSourcePaths.mainJavaPackage("com.endava.ai.skills.atlas.rules")
                .resolve(AtfSourcePaths.README_FILE_NAME));

        for (String readme : List.of(mcpReadme, atlasReadme)) {
            Assert.assertTrue(readme.contains("BusinessGraphLayoutAdapter"),
                    "Consumer README must point graph rendering to the shared Reporting adapter.");
            Assert.assertTrue(readme.contains("BusinessGraphConstruction"),
                    "Consumer README must name the shared Business Understanding graph input.");
            Assert.assertTrue(readme.contains("must not decide"),
                    "Consumer README must preserve skill ownership boundaries.");
        }
    }

    private static Map<String, String> skill(String id) {
        return SimpleYamlContractLoader.load(SKILL_ARCHITECTURE).stream()
                .filter(entry -> id.equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing skill architecture row: " + id));
    }

    private static List<String> values(Map<String, String> entry, String key) {
        String raw = entry.get(key);
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }
}
