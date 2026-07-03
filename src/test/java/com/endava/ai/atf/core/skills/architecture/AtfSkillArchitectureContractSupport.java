package com.endava.ai.atf.core.skills.architecture;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class AtfSkillArchitectureContractSupport {
    static final String RESOURCE = "atf-core/contracts/skills/skill-architecture.yml";

    private AtfSkillArchitectureContractSupport() {
    }

    static void scanForbiddenImports(Map<String, String> skill,
                                     String rootKey,
                                     String forbiddenKey,
                                     List<String> violations) throws IOException {
        List<String> forbiddenImports = values(skill, forbiddenKey);
        if (forbiddenImports.isEmpty() || forbiddenImports.contains("none")) {
            return;
        }
        Path root = path(required(skill, rootKey));
        if (!Files.isDirectory(root)) {
            violations.add(required(skill, "id") + " missing scan root " + root);
            return;
        }
        try (Stream<Path> files = Files.walk(root)) {
            List<Path> javaSources = files
                    .filter(Files::isRegularFile)
                    .filter(AtfSourcePaths::isJavaSource)
                    .collect(Collectors.toList());
            for (Path source : javaSources) {
                inspectForbiddenImports(source, forbiddenImports, required(skill, "id"), violations);
            }
        }
    }

    static void assertReadmeMentionsDeclaredTokens(Map<String, String> entry) throws IOException {
        String source = Files.readString(path(required(entry, "readme")));
        for (String token : values(entry, "mustMention")) {
            Assert.assertTrue(source.contains(token),
                    required(entry, "id") + " README must mention `" + token + "`");
        }
    }

    static void assertKnownSkill(Map<String, String> entry, String key, Set<String> skillIds) {
        String skillId = required(entry, key);
        Assert.assertTrue(skillIds.contains(skillId), required(entry, "id") + " references unknown " + key
                + " " + skillId);
    }

    static void assertCoreSkillsPackageContainsOnlyRegistryModel() throws IOException {
        Path coreSkillsRoot = path("src/main/java/com/endava/ai/core/skills");
        Set<String> expectedRegistryFiles = Set.of(
                "AtfSkill.java",
                "AtfSkillArtifactPolicy.java",
                "AtfSkillBoundary.java",
                "AtfSkillContractCatalog.java",
                "AtfSkillSuite.java",
                "README.md");

        try (Stream<Path> children = Files.list(coreSkillsRoot)) {
            List<Path> entries = children.collect(Collectors.toList());
            List<String> subpackages = entries.stream()
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
            List<String> unexpectedFiles = entries.stream()
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(fileName -> !expectedRegistryFiles.contains(fileName))
                    .sorted()
                    .collect(Collectors.toList());

            Assert.assertTrue(subpackages.isEmpty(),
                    "Core skills must not host public skill subpackages; add concrete skills under "
                            + "com.endava.ai.skills.* and register them in skill-architecture.yml: " + subpackages);
            Assert.assertTrue(unexpectedFiles.isEmpty(),
                    "Core skills must stay a small AtfSkill registry model. Unexpected files: " + unexpectedFiles);
        }
    }

    static void assertBefore(List<String> ordered, String first, String second) {
        Assert.assertTrue(ordered.contains(first), "Missing ordered item: " + first);
        Assert.assertTrue(ordered.contains(second), "Missing ordered item: " + second);
        Assert.assertTrue(ordered.indexOf(first) < ordered.indexOf(second),
                first + " must appear before " + second + ": " + ordered);
    }

    static void assertUniqueIds(List<Map<String, String>> entries) {
        Set<String> ids = ids(entries);
        Assert.assertEquals(ids.size(), entries.size(), "Architecture contract ids must be unique");
    }

    static Set<String> ids(List<Map<String, String>> entries) {
        return entries.stream()
                .map(entry -> required(entry, "id"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    static Map<String, String> skillSanitySuites(List<Map<String, String>> entries) {
        return entries.stream()
                .filter(entry -> isSkillEntry(required(entry, "kind")))
                .collect(Collectors.toMap(entry -> required(entry, "id"), entry -> required(entry, "sanitySuite")));
    }

    static Map<String, String> entry(String id) {
        return entries().stream()
                .filter(entry -> id.equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing architecture entry: " + id));
    }

    static void assertPackageShapeVisible(String skillId, String contractPath, String ownerReadme)
            throws IOException {
        Assert.assertTrue(Files.isRegularFile(path(contractPath)),
                skillId + " must expose package-shape governance: " + contractPath);
        String readme = Files.readString(path(ownerReadme));
        Assert.assertTrue(readme.contains("package-shape.yml"),
                skillId + " owner README must mention package-shape.yml: " + ownerReadme);
    }

    static void assertDirectory(Map<String, String> entry, String key) {
        Assert.assertTrue(Files.isDirectory(path(required(entry, key))),
                required(entry, "id") + " must declare existing " + key + ": " + required(entry, key));
    }

    static void assertRegularFile(Map<String, String> entry, String key) {
        Assert.assertTrue(Files.isRegularFile(path(required(entry, key))),
                required(entry, "id") + " must declare existing " + key + ": " + required(entry, key));
    }

    static List<Map<String, String>> skills() {
        return entries().stream()
                .filter(entry -> isSkillEntry(required(entry, "kind")))
                .collect(Collectors.toList());
    }

    static List<Map<String, String>> entriesByKind(String kind) {
        return entries().stream()
                .filter(entry -> kind.equals(required(entry, "kind")))
                .collect(Collectors.toList());
    }

    static boolean isSkillEntry(String kind) {
        return "skill".equals(kind);
    }

    static String required(Map<String, String> entry, String key) {
        return SimpleYamlContractLoader.required(entry, key, RESOURCE);
    }

    static List<String> values(Map<String, String> entry, String key) {
        String raw = entry.getOrDefault(key, "").trim();
        if (raw.isBlank()) {
            return List.of();
        }
        return Stream.of(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }

    static List<Map<String, String>> entries() {
        return SimpleYamlContractLoader.load(RESOURCE);
    }

    static Path path(String relativePath) {
        return AtfSourcePaths.workspaceRoot().resolve(relativePath);
    }

    private static void inspectForbiddenImports(Path source,
                                                List<String> forbiddenImports,
                                                String skillId,
                                                List<String> violations) throws IOException {
        for (String line : Files.readAllLines(source)) {
            String trimmed = line.trim();
            if (!trimmed.startsWith("import ")) {
                continue;
            }
            for (String forbidden : forbiddenImports) {
                if (trimmed.startsWith("import " + forbidden + ".")) {
                    violations.add(skillId + " imports " + forbidden
                            + " from " + AtfSourcePaths.unixPath(source));
                }
            }
        }
    }
}
