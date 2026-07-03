package com.endava.ai.atf.core.skills.architecture;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.entry;
import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.path;
import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.required;
import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.skills;

@Listeners(TestListener.class)
public class AtfTestArchitectureContractTest {
    private static final String RESOURCE = "atf-core/contracts/architecture/test-architecture.yml";
    private static final Set<String> ALLOWED_KINDS = Set.of(
            "product-test-lane",
            "framework-test-lane",
            "skill-test-lane"
    );

    @Test
    public void testArchitectureKeepsProductTestsAndSkillTestsInSeparateLanes() {
        List<Map<String, String>> lanes = SimpleYamlContractLoader.load(RESOURCE);
        Assert.assertFalse(lanes.isEmpty(), "test-architecture.yml must declare executable test lanes");

        Set<String> laneIds = lanes.stream()
                .map(lane -> requiredLane(lane, "id"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Assert.assertEquals(laneIds.size(), lanes.size(), "Test architecture lane ids must be unique");
        Assert.assertTrue(laneIds.containsAll(Set.of(
                        "test.product.api",
                        "test.product.ui",
                        "test.framework.core",
                        "test.skill.reporting",
                        "test.skill.reporting-facade",
                        "test.skill.business-understanding",
                        "test.skill.mcp",
                        "test.skill.atlas")),
                "Test architecture must declare product lanes, framework lanes and skill lanes: " + laneIds);

        Set<String> skillIds = skills().stream()
                .map(skill -> required(skill, "id"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        for (Map<String, String> lane : lanes) {
            assertLaneShape(lane, skillIds);
        }

        Assert.assertEquals(lane("test.product.api").get("sourceRoot"), "src/test/java/com/endava/ai/api/tests");
        Assert.assertEquals(lane("test.product.ui").get("sourceRoot"), "src/test/java/com/endava/ai/ui/tests");
        Assert.assertEquals(lane("test.framework.core").get("sourceRoot"), "src/test/java/com/endava/ai/atf");
        Assert.assertEquals(lane("test.skill.reporting").get("sourceRoot"), required(entry("skill.reporting"), "testRoot"));
        Assert.assertEquals(lane("test.skill.reporting-facade").get("sourceRoot"),
                "src/test/java/com/endava/ai/skills/reporting");
        Assert.assertEquals(lane("test.skill.reporting-facade").get("productionRoot"),
                required(entry("skill.reporting"), "sourceRoot"));
        Assert.assertEquals(lane("test.skill.reporting-facade").get("linkedSkill"), "skill.reporting");
        Assert.assertEquals(lane("test.skill.business-understanding").get("sourceRoot"),
                required(entry("skill.business-understanding"), "testRoot"));
        Assert.assertEquals(lane("test.skill.mcp").get("sourceRoot"), required(entry("skill.mcp"), "testRoot"));
        Assert.assertEquals(lane("test.skill.atlas").get("sourceRoot"), required(entry("skill.atlas"), "testRoot"));

        Assert.assertTrue(lane("test.product.api").get("forbidden").contains("MCP assertions"),
                "Product API tests must not become MCP contract tests");
        Assert.assertTrue(lane("test.product.ui").get("suitePolicy").contains("API shortcuts"),
                "Product UI tests may use API setup/polling shortcuts but must keep final UI proof");
        Assert.assertTrue(lane("test.skill.reporting-facade").get("suitePolicy").contains("testng-reporting-sanity.xml"),
                "Reporting facade smoke must be part of the Reporting sanity gate");
        Assert.assertTrue(lane("test.skill.reporting-facade").get("forbidden").contains("MCP package gates"),
                "Reporting facade smoke must not become an MCP gate test");
        Assert.assertTrue(lane("test.skill.business-understanding").get("forbidden").contains("Reporting shell"),
                "Business Understanding tests must stay semantic, not presentation-focused");
    }

    private static void assertLaneShape(Map<String, String> lane, Set<String> skillIds) {
        String id = requiredLane(lane, "id");
        String kind = requiredLane(lane, "kind");
        Assert.assertTrue(ALLOWED_KINDS.contains(kind), "Unsupported test lane kind for " + id + ": " + kind);
        assertExistingLanePath(lane, "sourceRoot");
        assertExistingLanePath(lane, "productionRoot");
        Assert.assertFalse(requiredLane(lane, "purpose").isBlank(), id + " must explain what it validates");
        Assert.assertFalse(requiredLane(lane, "suitePolicy").isBlank(), id + " must explain how it is run");
        Assert.assertFalse(requiredLane(lane, "forbidden").isBlank(), id + " must declare what does not belong");

        String linkedSkill = requiredLane(lane, "linkedSkill");
        if ("product-test-lane".equals(kind)) {
            Assert.assertEquals(linkedSkill, "none", id + " is a product test lane, not a framework skill lane");
        } else if ("skill-test-lane".equals(kind)) {
            Assert.assertTrue(skillIds.contains(linkedSkill), id + " must link to a declared skill: " + linkedSkill);
        }
    }

    private static Map<String, String> lane(String id) {
        return SimpleYamlContractLoader.load(RESOURCE).stream()
                .filter(lane -> id.equals(lane.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing test architecture lane: " + id));
    }

    private static void assertExistingLanePath(Map<String, String> lane, String key) {
        String id = requiredLane(lane, "id");
        String value = requiredLane(lane, key);
        Assert.assertTrue(Files.isDirectory(path(value)), id + " must declare existing " + key + ": " + value);
    }

    private static String requiredLane(Map<String, String> lane, String key) {
        return SimpleYamlContractLoader.required(lane, key, RESOURCE);
    }
}
