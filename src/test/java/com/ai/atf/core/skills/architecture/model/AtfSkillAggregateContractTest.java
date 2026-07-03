package com.ai.atf.core.skills.architecture.model;

import com.ai.core.skills.AtfSkill;
import com.ai.core.skills.AtfSkillContractCatalog;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AtfSkillAggregateContractTest {

    @Test
    public void atfSkillCatalogLoadsCanonicalSkillsAsTypedAggregates() {
        AtfSkillContractCatalog catalog = AtfSkillContractCatalog.fromDefaultResource();
        List<String> skillIds = catalog.skills().stream()
                .map(AtfSkill::getId)
                .collect(Collectors.toList());

        Assert.assertEquals(skillIds, List.of(
                "skill.core",
                "skill.reporting",
                "skill.business-understanding",
                "skill.code-quality",
                "skill.mcp",
                "skill.atlas"
        ));
        Assert.assertFalse(catalog.flows().isEmpty(), "Skill catalog must expose flow entries.");
        Assert.assertFalse(catalog.useCases().isEmpty(), "Skill catalog must expose use-case entries.");
    }

    @Test
    public void atfSkillAggregatesExposeOwnershipRootsAndRunnableGates() {
        AtfSkillContractCatalog catalog = AtfSkillContractCatalog.fromDefaultResource();

        for (AtfSkill skill : catalog.skills()) {
            Assert.assertTrue(Files.isDirectory(AtfSourcePaths.workspaceRoot().resolve(skill.getSourceRoot())),
                    skill.getId() + " sourceRoot must exist.");
            Assert.assertTrue(Files.isDirectory(AtfSourcePaths.workspaceRoot().resolve(skill.getTestRoot())),
                    skill.getId() + " testRoot must exist.");
            Assert.assertTrue(Files.isRegularFile(AtfSourcePaths.workspaceRoot().resolve(skill.getReadme())),
                    skill.getId() + " readme must exist.");
            Assert.assertTrue(Files.isRegularFile(AtfSourcePaths.workspaceRoot().resolve(skill.getSanitySuite())),
                    skill.getId() + " sanity suite must exist.");
            Assert.assertFalse(skill.getArtifactPolicy().isBlank(),
                    skill.getId() + " must declare artifact policy.");
            Assert.assertTrue(skill.suite().usesSuite(skill.getSanitySuite()),
                    skill.getId() + " suite value object must expose the sanity suite.");
            Assert.assertTrue(skill.suite().hasPromptRoot(),
                    skill.getId() + " suite value object must expose a prompt root.");
            Assert.assertTrue(skill.artifactPolicyModel().isDeclared(),
                    skill.getId() + " artifact policy value object must be declared.");
        }
    }

    @Test
    public void atfSkillAggregatesEncodeSkillBasedDependencyDirection() {
        AtfSkillContractCatalog catalog = AtfSkillContractCatalog.fromDefaultResource();

        AtfSkill core = catalog.requireSkill("skill.core");
        AtfSkill reporting = catalog.requireSkill("skill.reporting");
        AtfSkill business = catalog.requireSkill("skill.business-understanding");
        AtfSkill codeQuality = catalog.requireSkill("skill.code-quality");
        AtfSkill mcp = catalog.requireSkill("skill.mcp");
        AtfSkill atlas = catalog.requireSkill("skill.atlas");

        Assert.assertEquals(core.getConsumes(), List.of("none"));
        Assert.assertTrue(reporting.consumes("skill.core"));
        Assert.assertTrue(reporting.consumes("skill.business-understanding"));
        Assert.assertTrue(business.consumes("skill.core"));
        Assert.assertTrue(codeQuality.consumes("skill.core"));
        Assert.assertTrue(mcp.consumes("skill.business-understanding"));
        Assert.assertTrue(mcp.consumes("skill.reporting"));
        Assert.assertTrue(mcp.consumes("skill.core"));
        Assert.assertTrue(atlas.consumes("skill.business-understanding"));
        Assert.assertTrue(atlas.consumes("skill.reporting"));
        Assert.assertTrue(atlas.consumes("skill.core"));

        Assert.assertFalse(core.consumes("skill.mcp"));
        Assert.assertFalse(core.consumes("skill.atlas"));
        Assert.assertFalse(reporting.consumes("skill.mcp"));
        Assert.assertFalse(reporting.consumes("skill.atlas"));
        Assert.assertFalse(codeQuality.consumes("skill.mcp"));
        Assert.assertFalse(codeQuality.consumes("skill.atlas"));
        Assert.assertFalse(mcp.consumes("skill.atlas"));
        Assert.assertFalse(atlas.consumes("skill.mcp"));
    }

    @Test
    public void atfSkillValueObjectsExposeBoundaryAndArtifactSemantics() {
        AtfSkillContractCatalog catalog = AtfSkillContractCatalog.fromDefaultResource();

        AtfSkill core = catalog.requireSkill("skill.core");
        AtfSkill mcp = catalog.requireSkill("skill.mcp");
        AtfSkill atlas = catalog.requireSkill("skill.atlas");

        Assert.assertFalse(core.boundary().consumes("skill.mcp"));
        Assert.assertTrue(core.boundary().hasForbiddenMainImports());
        Assert.assertTrue(mcp.boundary().consumes("skill.business-understanding"));
        Assert.assertTrue(atlas.boundary().consumes("skill.business-understanding"));
        Assert.assertTrue(mcp.artifactPolicyModel().mentions("generated-package validation artifacts"));
        Assert.assertTrue(atlas.artifactPolicyModel().mentions("must not decide MCP publishability"));
    }

    @Test
    public void skillArchitectureYamlDoesNotDriftFromRunnableSuitesAndPromptDocs() {
        AtfSkillContractCatalog catalog = AtfSkillContractCatalog.fromDefaultResource();
        Set<String> skillIds = catalog.skills().stream()
                .map(AtfSkill::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (Map<String, String> flow : catalog.flows()) {
            Assert.assertTrue(skillIds.contains(flow.get("owner")),
                    flow.get("id") + " must be owned by a declared skill.");
            assertWorkspaceFile(flow, "suiteXml");
        }

        for (Map<String, String> useCase : catalog.useCases()) {
            Assert.assertTrue(skillIds.contains(useCase.get("primarySkill")),
                    useCase.get("id") + " must route to a declared primary skill.");
            AtfSkillContractCatalog.values(useCase, "supportSkills").stream()
                    .filter(skill -> !"none".equalsIgnoreCase(skill))
                    .forEach(skill -> Assert.assertTrue(skillIds.contains(skill),
                            useCase.get("id") + " references unknown support skill " + skill));
            AtfSkillContractCatalog.values(useCase, "suites")
                    .forEach(suite -> assertWorkspaceFile(useCase.get("id"), "suite", suite));
            AtfSkillContractCatalog.values(useCase, "promptDocs")
                    .forEach(prompt -> assertWorkspaceFile(useCase.get("id"), "prompt", prompt));
        }
    }

    @Test
    public void atfSkillAggregatesKeepReportingAsFacadeOverImplementationRoot() {
        AtfSkill reporting = AtfSkillContractCatalog.fromDefaultResource().requireSkill("skill.reporting");

        Assert.assertEquals(reporting.getSourceRoot(), "src/main/java/com/ai/skills/reporting");
        Assert.assertEquals(reporting.getImplementationRoot(), "src/main/java/com/ai/core/reporting");
        Assert.assertTrue(reporting.hasImplementationRoot(),
                "Reporting must stay represented as a skill facade over the larger implementation library.");
    }

    private static void assertWorkspaceFile(Map<String, String> entry, String key) {
        assertWorkspaceFile(entry.get("id"), key, entry.get(key));
    }

    private static void assertWorkspaceFile(String owner, String field, String value) {
        Assert.assertTrue(value != null && !value.isBlank(),
                owner + " must declare " + field + ".");
        Assert.assertTrue(Files.isRegularFile(AtfSourcePaths.workspaceRoot().resolve(value)),
                owner + " references missing " + field + " file " + value);
    }
}
