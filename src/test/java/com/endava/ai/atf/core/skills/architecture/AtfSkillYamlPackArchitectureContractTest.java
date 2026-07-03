package com.endava.ai.atf.core.skills.architecture;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.endava.ai.skills.atlas.skill.AtlasAuditPublicationSkill;
import com.endava.ai.skills.atlas.skill.AtlasBusinessSemanticsSkill;
import com.endava.ai.skills.atlas.skill.AtlasGraphProofSkill;
import com.endava.ai.skills.atlas.skill.AtlasScoreTruthSkill;
import com.endava.ai.skills.mcp.skill.McpCandidatePromotionSkill;
import com.endava.ai.skills.mcp.skill.McpEvidenceProvenanceSkill;
import com.endava.ai.skills.mcp.skill.McpFinalReportSkill;
import com.endava.ai.skills.mcp.skill.McpOrchestrationSkill;
import com.endava.ai.skills.mcp.skill.McpPackageAuditSkill;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.entry;
import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.ids;
import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.path;
import static com.endava.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.values;

@Listeners(TestListener.class)
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.YAML_MANIFEST)
@AtfContractRef(AtfContractNarrativeIds.Contract.YAML_MANIFEST_CATALOG)
public class AtfSkillYamlPackArchitectureContractTest {
    private static final String REPORTING_SKILL_SURFACE = "skills/reporting/contracts/reporting-skill-surface.yml";
    private static final String MCP_CAPABILITY_PACKS = "skills/mcp/contracts/capability-packs.yml";
    private static final String ATLAS_RULE_PACKS = "skills/atlas/contracts/rule-packs.yml";

    @Test
    public void reportingSkillSurfaceContractKeepsFacadeNeutralAndRoutable() {
        List<Map<String, String>> entries = SimpleYamlContractLoader.load(REPORTING_SKILL_SURFACE);
        Assert.assertEquals(entries.size(), 3, "Reporting skill surface must stay small and readable.");

        Map<String, String> facade = entries.stream()
                .filter(contract -> "reporting.skill-surface.facade".equals(contract.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing reporting facade contract"));
        Assert.assertEquals(required(facade, "owner", REPORTING_SKILL_SURFACE), "skill.reporting");
        Assert.assertEquals(required(facade, "facadeRoot", REPORTING_SKILL_SURFACE),
                required(entry("skill.reporting"), "sourceRoot"));
        Assert.assertEquals(required(facade, "implementationRoot", REPORTING_SKILL_SURFACE),
                required(entry("skill.reporting"), "implementationRoot"));
        Assert.assertEquals(required(facade, "sanitySuite", REPORTING_SKILL_SURFACE),
                required(entry("skill.reporting"), "sanitySuite"));
        Assert.assertTrue(Files.isRegularFile(path(required(facade, "integrationSuite", REPORTING_SKILL_SURFACE))));
        Assert.assertTrue(Files.isRegularFile(path(required(facade, "consumerSmokeSuite", REPORTING_SKILL_SURFACE))));

        Map<String, String> boundary = entries.stream()
                .filter(contract -> "reporting.skill-surface.neutral-boundary".equals(contract.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing reporting neutral boundary contract"));
        Assert.assertTrue(required(boundary, "owns", REPORTING_SKILL_SURFACE).contains("html shell"));
        Assert.assertTrue(required(boundary, "forbiddenOwnership", REPORTING_SKILL_SURFACE)
                        .contains("MCP package publishability"),
                "Reporting surface must explicitly reject MCP ownership.");
        Assert.assertTrue(required(boundary, "forbiddenOwnership", REPORTING_SKILL_SURFACE)
                        .contains("Atlas business meaning"),
                "Reporting surface must explicitly reject Atlas business meaning.");

        Map<String, String> graphLayout = entries.stream()
                .filter(contract -> "reporting.skill-surface.business-graph-layout".equals(contract.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing reporting business graph layout contract"));
        Assert.assertEquals(required(graphLayout, "adapter", REPORTING_SKILL_SURFACE),
                "BusinessGraphLayoutAdapter");
        Assert.assertEquals(required(graphLayout, "consumesModel", REPORTING_SKILL_SURFACE),
                "BusinessGraphConstruction");
        Assert.assertTrue(Files.isDirectory(path(required(graphLayout, "sourceRoot", REPORTING_SKILL_SURFACE))));
        Assert.assertTrue(Files.isRegularFile(path("src/test/java/"
                        + required(graphLayout, "contractTest", REPORTING_SKILL_SURFACE).replace('.', '/') + ".java")),
                "Reporting graph layout contract test must exist.");
        Assert.assertTrue(required(graphLayout, "forbiddenOwnership", REPORTING_SKILL_SURFACE)
                        .contains("business truth scoring"),
                "Reporting graph layout must reject business truth scoring.");
        Assert.assertTrue(required(graphLayout, "stopCondition", REPORTING_SKILL_SURFACE)
                        .contains("keeps technical proof tokens outside SVG business nodes"),
                "Reporting graph layout stop condition must protect proof boundary separation.");
    }

    @Test
    public void mcpCapabilityPacksRouteComplexityToProfilesContractsAndRepairSurfaces() {
        List<Map<String, String>> packs = SimpleYamlContractLoader.load(MCP_CAPABILITY_PACKS);
        Set<String> ids = ids(packs);
        Assert.assertTrue(ids.containsAll(Set.of(
                        "mcp.capability-pack.orchestration",
                        "mcp.capability-pack.package-governance",
                        "mcp.capability-pack.report-closeout",
                        "mcp.capability-pack.extent-provenance",
                        "mcp.capability-pack.candidate-promotion")),
                "MCP capability packs must cover the major agentic repair lanes: " + ids);

        for (Map<String, String> pack : packs) {
            Assert.assertEquals(required(pack, "kind", MCP_CAPABILITY_PACKS), "mcp-capability-pack");
            Assert.assertEquals(required(pack, "owner", MCP_CAPABILITY_PACKS), "skill.mcp");
            for (String root : values(pack, "packageRoots")) {
                Assert.assertTrue(Files.isDirectory(path(root)),
                        required(pack, "id", MCP_CAPABILITY_PACKS) + " references missing package root " + root);
            }
            for (String contract : values(pack, "contracts")) {
                Assert.assertTrue(Files.isRegularFile(path("src/main/resources/skills/mcp/contracts/" + contract)),
                        required(pack, "id", MCP_CAPABILITY_PACKS) + " references missing MCP contract " + contract);
            }
            Assert.assertTrue(Files.isRegularFile(path(required(pack, "primarySuite", MCP_CAPABILITY_PACKS))),
                    required(pack, "id", MCP_CAPABILITY_PACKS) + " references missing primary suite");
            if (pack.containsKey("followUpSuite")) {
                Assert.assertTrue(Files.isRegularFile(path(required(pack, "followUpSuite", MCP_CAPABILITY_PACKS))),
                        required(pack, "id", MCP_CAPABILITY_PACKS) + " references missing follow-up suite");
            }
            Assert.assertFalse(required(pack, "role", MCP_CAPABILITY_PACKS).isBlank());
            Assert.assertFalse(required(pack, "whenToUse", MCP_CAPABILITY_PACKS).isBlank());
            Assert.assertFalse(required(pack, "publishedEvidence", MCP_CAPABILITY_PACKS).isBlank());
            Assert.assertFalse(required(pack, "stopCondition", MCP_CAPABILITY_PACKS).isBlank());
            Assert.assertFalse(required(pack, "repair", MCP_CAPABILITY_PACKS).isBlank());
        }

        Map<String, String> reportCloseout = packs.stream()
                .filter(pack -> "mcp.capability-pack.report-closeout".equals(pack.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing MCP report closeout capability pack"));
        Assert.assertTrue(required(reportCloseout, "publishedEvidence", MCP_CAPABILITY_PACKS).contains("LangGraph"),
                "MCP report closeout must make LangGraph final report validation visible.");
        Assert.assertTrue(required(reportCloseout, "stopCondition", MCP_CAPABILITY_PACKS).contains("no source changes"),
                "MCP report closeout must freeze source changes after package close-out.");

        assertMcpFacadeMatchesPack(McpOrchestrationSkill.CAPABILITY_ID,
                McpOrchestrationSkill.PRIMARY_SUITE,
                McpOrchestrationSkill.REPAIR_TARGET,
                packs);
        assertMcpFacadeMatchesPack(McpPackageAuditSkill.CAPABILITY_ID,
                McpPackageAuditSkill.PRIMARY_SUITE,
                McpPackageAuditSkill.REPAIR_TARGET,
                packs);
        assertMcpFacadeMatchesPack(McpFinalReportSkill.CAPABILITY_ID,
                McpFinalReportSkill.PRIMARY_SUITE,
                McpFinalReportSkill.REPAIR_TARGET,
                packs);
        assertMcpFacadeMatchesPack(McpEvidenceProvenanceSkill.CAPABILITY_ID,
                McpEvidenceProvenanceSkill.PRIMARY_SUITE,
                McpEvidenceProvenanceSkill.REPAIR_TARGET,
                packs);
        assertMcpFacadeMatchesPack(McpCandidatePromotionSkill.CAPABILITY_ID,
                McpCandidatePromotionSkill.PRIMARY_SUITE,
                McpCandidatePromotionSkill.REPAIR_TARGET,
                packs);
        Assert.assertEquals(required(reportCloseout, "followUpSuite", MCP_CAPABILITY_PACKS),
                McpFinalReportSkill.FOLLOW_UP_SUITE,
                "MCP final-report facade must expose the LangGraph follow-up suite.");
    }

    @Test
    public void atlasRulePacksRouteBusinessGraphScoreAndAuditOwnership() {
        List<Map<String, String>> packs = SimpleYamlContractLoader.load(ATLAS_RULE_PACKS);
        Set<String> ids = ids(packs);
        Assert.assertTrue(ids.containsAll(Set.of(
                        "atlas.rule-pack.business-semantics",
                        "atlas.rule-pack.graph-proof",
                        "atlas.rule-pack.score-truth",
                        "atlas.rule-pack.audit-publication")),
                "Atlas rule packs must cover semantic, graph, score and audit lanes: " + ids);

        for (Map<String, String> pack : packs) {
            Assert.assertEquals(required(pack, "kind", ATLAS_RULE_PACKS), "atlas-rule-pack");
            Assert.assertEquals(required(pack, "owner", ATLAS_RULE_PACKS), "skill.atlas");
            for (String root : values(pack, "packageRoots")) {
                Assert.assertTrue(Files.isDirectory(path(root)),
                        required(pack, "id", ATLAS_RULE_PACKS) + " references missing package root " + root);
            }
            for (String contract : values(pack, "contracts")) {
                Assert.assertTrue(Files.isRegularFile(path("src/main/resources/skills/atlas/contracts/" + contract)),
                        required(pack, "id", ATLAS_RULE_PACKS) + " references missing Atlas contract " + contract);
            }
            Assert.assertTrue(Files.isRegularFile(path(required(pack, "primarySuite", ATLAS_RULE_PACKS))),
                    required(pack, "id", ATLAS_RULE_PACKS) + " references missing primary suite");
            Assert.assertFalse(required(pack, "role", ATLAS_RULE_PACKS).isBlank());
            Assert.assertFalse(required(pack, "whenToUse", ATLAS_RULE_PACKS).isBlank());
            Assert.assertFalse(required(pack, "publishedEvidence", ATLAS_RULE_PACKS).isBlank());
            Assert.assertFalse(required(pack, "stopCondition", ATLAS_RULE_PACKS).isBlank());
            Assert.assertFalse(required(pack, "repair", ATLAS_RULE_PACKS).isBlank());
            Assert.assertFalse(required(pack, "repair", ATLAS_RULE_PACKS).contains("MCP package close-out"),
                    "Atlas rule packs must not route repair into MCP close-out.");
            Assert.assertFalse(required(pack, "stopCondition", ATLAS_RULE_PACKS).contains("MCP package close-out"),
                    "Atlas rule pack stop conditions must not become MCP close-out gates.");
        }

        assertAtlasFacadeMatchesPack(AtlasBusinessSemanticsSkill.RULE_PACK_ID,
                AtlasBusinessSemanticsSkill.PRIMARY_SUITE,
                AtlasBusinessSemanticsSkill.REPAIR_TARGET,
                packs);
        assertAtlasFacadeMatchesPack(AtlasGraphProofSkill.RULE_PACK_ID,
                AtlasGraphProofSkill.PRIMARY_SUITE,
                AtlasGraphProofSkill.REPAIR_TARGET,
                packs);
        assertAtlasFacadeMatchesPack(AtlasScoreTruthSkill.RULE_PACK_ID,
                AtlasScoreTruthSkill.PRIMARY_SUITE,
                AtlasScoreTruthSkill.REPAIR_TARGET,
                packs);
        assertAtlasFacadeMatchesPack(AtlasAuditPublicationSkill.RULE_PACK_ID,
                AtlasAuditPublicationSkill.PRIMARY_SUITE,
                AtlasAuditPublicationSkill.REPAIR_TARGET,
                packs);
    }

    @Test
    public void businessUnderstandingKernelDeclaresSharedInputsAndSemanticPipeline() {
        Map<String, String> business = entry("skill.business-understanding");

        Assert.assertEquals(required(business, "sourceRoot"), "src/main/java/com/endava/ai/skills/business",
                "Business Understanding kernel must stay in the shared Core skill namespace.");
        Assert.assertTrue(values(business, "sharedConsumers").containsAll(List.of("skill.mcp", "skill.atlas")),
                "Business Understanding must explicitly serve both MCP and Atlas.");
        Assert.assertTrue(values(entry("skill.mcp"), "consumes").contains("skill.business-understanding"),
                "MCP must consume the shared semantic kernel.");
        Assert.assertTrue(values(entry("skill.atlas"), "consumes").contains("skill.business-understanding"),
                "Atlas must consume the shared semantic kernel.");
        Assert.assertTrue(values(business, "acceptedInputs").containsAll(List.of(
                        "git-diff-extent",
                        "source-structure",
                        "final-report",
                        "atlas-output")),
                "Business Understanding must support both MCP-style evidence and Atlas-style source/report evidence.");
        Assert.assertEquals(values(business, "semanticPipeline"), List.of(
                        "normalize input",
                        "map support layers",
                        "identify business objects",
                        "trace claims",
                        "map proof chains",
                        "score understanding",
                        "publish limits"),
                "Different inputs may enter the kernel, but the semantic pipeline must stay identical.");
        Assert.assertEquals(required(business, "artifactPolicy"),
                "Business Understanding defines shared semantics only; it must not decide MCP package publishability, own Atlas generated artifacts, or own Reporting shell behavior.",
                "Business Understanding must stay a small semantic kernel, not a hidden MCP/Atlas/Reporting owner.");
    }

    private static String required(Map<String, String> entry, String key) {
        return SimpleYamlContractLoader.required(entry, key, AtfSkillArchitectureContractSupport.RESOURCE);
    }

    private static String required(Map<String, String> entry, String key, String resource) {
        return SimpleYamlContractLoader.required(entry, key, resource);
    }

    private static void assertMcpFacadeMatchesPack(String id,
                                                   String primarySuite,
                                                   String repairTarget,
                                                   List<Map<String, String>> packs) {
        Map<String, String> pack = byId(packs, id);
        Assert.assertEquals(required(pack, "primarySuite", MCP_CAPABILITY_PACKS), primarySuite,
                "MCP facade primary suite drifted from capability-packs.yml: " + id);
        Assert.assertTrue(required(pack, "repair", MCP_CAPABILITY_PACKS).contains(repairTarget),
                "MCP facade repair target must stay visible in capability-packs.yml: " + id);
    }

    private static void assertAtlasFacadeMatchesPack(String id,
                                                     String primarySuite,
                                                     String repairTarget,
                                                     List<Map<String, String>> packs) {
        Map<String, String> pack = byId(packs, id);
        Assert.assertEquals(required(pack, "primarySuite", ATLAS_RULE_PACKS), primarySuite,
                "Atlas facade primary suite drifted from rule-packs.yml: " + id);
        Assert.assertTrue(required(pack, "repair", ATLAS_RULE_PACKS).contains(repairTarget),
                "Atlas facade repair target must stay visible in rule-packs.yml: " + id);
    }

    private static Map<String, String> byId(List<Map<String, String>> entries, String id) {
        return entries.stream()
                .filter(entry -> id.equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing pack for facade: " + id));
    }
}
