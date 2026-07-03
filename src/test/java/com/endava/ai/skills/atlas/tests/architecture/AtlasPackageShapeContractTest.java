package com.endava.ai.skills.atlas.tests.architecture;

import com.endava.ai.atf.core.skills.architecture.support.PromptMirrorRootAssertions;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Listeners(TestListener.class)
public class AtlasPackageShapeContractTest {
    private static final String RESOURCE = "skills/atlas/contracts/package-shape.yml";
    private static final Set<String> ALLOWED_KINDS = Set.of(
            "package-default",
            "package-override",
            "recursive-package-cap",
            "required-readme",
            "forbidden-import",
            "console-gateway"
    );
    private static final Pattern DIRECT_CONSOLE = Pattern.compile("\\bSystem\\s*\\.\\s*(out|err)\\b");

    @Test
    public void atlasPackageShapeYamlHasRequiredSchemaAndExistingTargets() {
        List<Map<String, String>> entries = entries();
        Assert.assertFalse(entries.isEmpty(), "skills/atlas/contracts/package-shape.yml must not be empty.");
        Assert.assertEquals(
                entries.stream().map(entry -> required(entry, "id")).collect(Collectors.toSet()).size(),
                entries.size(),
                "Atlas package-shape policy ids must be unique."
        );

        for (Map<String, String> entry : entries) {
            String kind = required(entry, "kind");
            Assert.assertTrue(ALLOWED_KINDS.contains(kind), "Unsupported Atlas package-shape kind: " + kind);
            Assert.assertFalse(required(entry, "reason").isBlank(),
                    "Every Atlas package-shape policy must explain why it exists: " + required(entry, "id"));
            if (entry.containsKey("root")) {
                Assert.assertTrue(Files.isDirectory(path(required(entry, "root"))),
                        "Atlas package-shape root must exist for " + required(entry, "id"));
            }
            if (entry.containsKey("path")) {
                Assert.assertTrue(Files.exists(path(required(entry, "path"))),
                        "Atlas package-shape path must exist for " + required(entry, "id"));
            }
            assertPromptMirrorRootsIfDeclared(entry);
        }
    }

    @Test
    public void atlasPackagesStayBelowYamlCaps() throws IOException {
        Map<Path, Integer> defaults = policies("package-default").stream()
                .collect(Collectors.toMap(
                        entry -> Path.of(required(entry, "root")),
                        entry -> integer(entry, "maxClasses"),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
        Map<Path, Integer> overrides = policies("package-override").stream()
                .collect(Collectors.toMap(
                        entry -> Path.of(required(entry, "path")),
                        entry -> integer(entry, "maxClasses"),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));

        List<String> violations = new ArrayList<>();
        for (Map.Entry<Path, Integer> defaultPolicy : defaults.entrySet()) {
            Path root = defaultPolicy.getKey();
            int defaultCap = defaultPolicy.getValue();
            try (Stream<Path> directories = Files.walk(root)) {
                directories
                        .filter(Files::isDirectory)
                        .map(directory -> packageClassCount(directory, capFor(directory, overrides, defaultCap)))
                        .filter(count -> count.classes() > count.cap())
                        .sorted(Comparator.comparing(PackageClassCount::packagePath))
                        .map(count -> count.packagePath() + " has " + count.classes()
                                + " classes, cap " + count.cap())
                        .forEach(violations::add);
            }
        }

        Assert.assertTrue(violations.isEmpty(),
                "Atlas packages must stay bounded by skill responsibility. Split package families before raising caps: "
                        + violations);
    }

    @Test
    public void broadAtlasSurfacesCannotGrowSilently() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Map<String, String> policy : policies("recursive-package-cap")) {
            Path root = path(required(policy, "path"));
            int cap = integer(policy, "maxClasses");
            long count;
            try (Stream<Path> files = Files.walk(root)) {
                count = files.filter(AtfSourcePaths::isJavaSource).count();
            }
            if (count > cap) {
                violations.add(required(policy, "path") + " has " + count + " Java files, cap " + cap);
            }
        }

        Assert.assertTrue(violations.isEmpty(),
                "Atlas broad surfaces grew past the YAML cap; split ownership or update the cap with rationale: "
                        + violations);
    }

    @Test
    public void atlasReadmesExposeSkillOwnershipAndVerification() {
        List<String> violations = policies("required-readme").stream()
                .map(entry -> path(required(entry, "path")))
                .filter(path -> !hasSkillReadmeShape(path))
                .map(path -> AtfSourcePaths.unixPath(path.normalize()))
                .collect(Collectors.toList());

        Assert.assertTrue(violations.isEmpty(),
                "Atlas skill packages need local README maps with purpose, entry points, do-not rules and verification: "
                        + violations);
    }

    @Test
    public void atlasJavaDoesNotImportForbiddenSkillPackages() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Map<String, String> policy : policies("forbidden-import")) {
            collectForbiddenImports(path(required(policy, "root")),
                    required(policy, "forbiddenPackage"),
                    violations);
        }

        Assert.assertTrue(violations.isEmpty(),
                "Atlas must stay skill-isolated; use YAML/artifacts for MCP bridges instead of Java imports: "
                        + violations);
    }

    @Test
    public void atlasMainCodeRoutesConsoleOutputThroughFrameworkGateways() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Map<String, String> policy : policies("console-gateway")) {
            try (Stream<Path> files = Files.walk(path(required(policy, "root")))) {
                files
                        .filter(AtfSourcePaths::isJavaSource)
                        .filter(source -> DIRECT_CONSOLE.matcher(read(source)).find())
                        .map(source -> AtfSourcePaths.unixPath(source.normalize()))
                        .forEach(violations::add);
            }
        }

        Assert.assertTrue(violations.isEmpty(),
                "Atlas main code must not use direct System.out/System.err: " + violations);
    }

    private static void collectForbiddenImports(Path root, String forbiddenPackage, List<String> violations)
            throws IOException {
        String forbiddenImport = "import " + forbiddenPackage + ".";
        try (Stream<Path> files = Files.walk(root)) {
            files
                    .filter(AtfSourcePaths::isJavaSource)
                    .filter(path -> read(path).contains(forbiddenImport))
                    .map(path -> AtfSourcePaths.unixPath(path.normalize()))
                    .forEach(violations::add);
        }
    }

    private static boolean hasSkillReadmeShape(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        String source = read(path).toLowerCase();
        return source.contains("## purpose")
                && source.contains("## entry point")
                && source.contains("## do not")
                && source.contains("## verification");
    }

    private static PackageClassCount packageClassCount(Path directory, int cap) {
        try (Stream<Path> files = Files.list(directory)) {
            long classes = files
                    .filter(AtfSourcePaths::isJavaSource)
                    .count();
            return new PackageClassCount(AtfSourcePaths.unixPath(directory.normalize()), classes, cap);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot inspect Atlas package " + directory, exception);
        }
    }

    private static int capFor(Path directory, Map<Path, Integer> overrides, int defaultCap) {
        String normalized = normalize(directory);
        return overrides.entrySet().stream()
                .filter(entry -> normalize(entry.getKey()).equals(normalized))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(defaultCap);
    }

    private static List<Map<String, String>> policies(String kind) {
        return entries().stream()
                .filter(entry -> kind.equals(required(entry, "kind")))
                .collect(Collectors.toList());
    }

    private static int integer(Map<String, String> entry, String key) {
        return Integer.parseInt(required(entry, key));
    }

    private static String required(Map<String, String> entry, String key) {
        return SimpleYamlContractLoader.required(entry, key, RESOURCE);
    }

    private static List<Map<String, String>> entries() {
        return SimpleYamlContractLoader.load(RESOURCE);
    }

    private static Path path(String relativePath) {
        return AtfSourcePaths.workspaceRoot().resolve(relativePath);
    }

    private static String normalize(Path path) {
        return AtfSourcePaths.unixPath(path.normalize());
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read Atlas source " + path, exception);
        }
    }

    private static void assertPromptMirrorRootsIfDeclared(Map<String, String> entry) {
        if (!entry.containsKey("sourceRoot") && !entry.containsKey("mirrorRoot")) {
            return;
        }
        PromptMirrorRootAssertions.assertDeclaredRootsAreIndexed(
                entry, AtfSourcePaths.workspaceRoot(), required(entry, "path"), "Atlas");
    }

    private static final class PackageClassCount {
        private final String packagePath;
        private final long classes;
        private final int cap;

        private PackageClassCount(String packagePath, long classes, int cap) {
            this.packagePath = packagePath;
            this.classes = classes;
            this.cap = cap;
        }

        private String packagePath() {
            return packagePath;
        }

        private long classes() {
            return classes;
        }

        private int cap() {
            return cap;
        }
    }
}
