package com.ai.atf.core.skills.architecture;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.RESOURCE;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.assertCoreSkillsPackageContainsOnlyRegistryModel;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.assertDirectory;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.assertKnownSkill;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.assertReadmeMentionsDeclaredTokens;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.assertRegularFile;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.assertUniqueIds;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.entries;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.entriesByKind;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.entry;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.ids;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.isSkillEntry;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.path;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.required;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.scanForbiddenImports;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.skillSanitySuites;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.skills;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.values;

@Listeners(TestListener.class)
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.YAML_MANIFEST)
@AtfContractRef(AtfContractNarrativeIds.Contract.YAML_MANIFEST_CATALOG)
public class AtfSkillArchitectureContractTest {
    private static final Set<String> ALLOWED_KINDS = Set.of("skill", "prompt-assets", "flow", "use-case");

    @Test
    public void skillArchitectureManifestDeclaresExistingSkillSurfaces() throws IOException {
        List<Map<String, String>> entries = entries();
        Assert.assertFalse(entries.isEmpty(), "skill-architecture.yml must not be empty");
        assertUniqueIds(entries);

        for (Map<String, String> entry : entries) {
            String id = required(entry, "id");
            String kind = required(entry, "kind");
            Assert.assertTrue(ALLOWED_KINDS.contains(kind), "Unsupported architecture kind for " + id + ": " + kind);

            if (isSkillEntry(kind)) {
                assertDirectory(entry, "sourceRoot");
                assertDirectory(entry, "testRoot");
                assertRegularFile(entry, "readme");
                assertRegularFile(entry, "sanitySuite");
                assertRegularFile(entry, "deepSuite");
                assertDirectory(entry, "promptRoot");
                assertReadmeMentionsDeclaredTokens(entry);
                Assert.assertFalse(required(entry, "artifactPolicy").isBlank(),
                        required(entry, "id") + " must declare artifact ownership policy");
            }
            if ("prompt-assets".equals(kind)) {
                assertDirectory(entry, "sourceRoot");
                assertDirectory(entry, "testRoot");
                assertRegularFile(entry, "readme");
                assertRegularFile(entry, "sanitySuite");
                assertRegularFile(entry, "deepSuite");
                assertDirectory(entry, "promptRoot");
                assertReadmeMentionsDeclaredTokens(entry);
                Assert.assertFalse(required(entry, "artifactPolicy").isBlank(),
                        required(entry, "id") + " must declare prompt artifact ownership policy");
            }
        }

        Set<String> ids = ids(entries);
        Assert.assertTrue(ids.containsAll(Set.of(
                        "skill.core",
                        "skill.reporting",
                        "skill.business-understanding",
                        "skill.mcp",
                        "skill.atlas")),
                "Architecture manifest must declare Core, Reporting, Business Understanding, MCP and Atlas skills: " + ids);
        Assert.assertFalse(ids.contains("skill.prompts"),
                "Prompt contracts must not be modeled as an independent skill.");
        Assert.assertTrue(ids.contains("prompt.assets"),
                "Architecture manifest must declare prompt assets as versioned contract assets.");
    }

    @Test
    public void skillArchitectureKeepsRuntimeAndValidatorDependenciesDirectional() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Map<String, String> skill : skills()) {
            scanForbiddenImports(skill, "sourceRoot", "forbiddenMainImports", violations);
            scanForbiddenImports(skill, "testRoot", "forbiddenTestImports", violations);
        }

        Assert.assertTrue(violations.isEmpty(),
                "Skill boundaries must stay directional; collaborate through Core, YAML and artifacts, not cross-imports: "
                        + violations);
    }

    @Test
    public void coreSkillsPackageContainsOnlyRegistryModelNotPublicSkillSubpackages() throws IOException {
        assertCoreSkillsPackageContainsOnlyRegistryModel();
    }

    @Test
    public void skillArchitectureUseCasesReferenceKnownSkillsAndRunnableSuites() {
        List<Map<String, String>> entries = entries();
        Map<String, String> skillSanitySuites = skillSanitySuites(entries);
        Set<String> skillIds = entries.stream()
                .filter(entry -> isSkillEntry(required(entry, "kind")))
                .map(entry -> required(entry, "id"))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (Map<String, String> useCase : entriesByKind("use-case")) {
            assertKnownSkill(useCase, "owner", skillIds);
            assertKnownSkill(useCase, "primarySkill", skillIds);
            Assert.assertEquals(required(useCase, "sanityFirst"), "true",
                    required(useCase, "id") + " must explicitly require sanity-first execution");
            for (String supportSkill : values(useCase, "supportSkills")) {
                if (!"none".equals(supportSkill)) {
                    Assert.assertTrue(skillIds.contains(supportSkill),
                            required(useCase, "id") + " references unknown support skill " + supportSkill);
                }
            }
            for (String suite : values(useCase, "suites")) {
                Assert.assertTrue(Files.isRegularFile(path(suite)),
                        required(useCase, "id") + " references missing suite " + suite);
            }
            String expectedFirstSuite = skillSanitySuites.get(required(useCase, "primarySkill"));
            Assert.assertFalse(values(useCase, "suites").isEmpty(),
                    required(useCase, "id") + " must declare at least one executable suite");
            Assert.assertEquals(values(useCase, "suites").get(0), expectedFirstSuite,
                    required(useCase, "id") + " must start with the primary skill sanity suite");
            Assert.assertFalse(required(useCase, "artifactHandoff").isBlank(),
                    "Use-case must declare artifact handoff: " + required(useCase, "id"));
            Assert.assertFalse(required(useCase, "artifactPolicy").isBlank(),
                    "Use-case must declare Git/artifact policy: " + required(useCase, "id"));
            Assert.assertFalse(required(useCase, "stopCondition").isBlank(),
                    "Use-case must declare stop condition: " + required(useCase, "id"));
        }
    }

    private static String required(Map<String, String> entry, String key) {
        return SimpleYamlContractLoader.required(entry, key, RESOURCE);
    }

    private static String required(Map<String, String> entry, String key, String resource) {
        return SimpleYamlContractLoader.required(entry, key, resource);
    }

}
