package com.ai.skills.business.tests;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Listeners(TestListener.class)
public class BusinessUnderstandingPackageShapeContractTest {
    private static final String PACKAGE_SHAPE_RESOURCE = "skills/business/contracts/package-shape.yml";
    private static final Pattern DIRECT_CONSOLE = Pattern.compile("\\bSystem\\s*\\.\\s*(out|err)\\b");
    private static final Set<String> ALLOWED_PACKAGE_KINDS = Set.of(
            "package-default",
            "forbidden-import",
            "required-readme"
    );

    @Test
    public void packageShapeStaysSmallAndSkillNeutral() throws IOException {
        List<Map<String, String>> entries = SimpleYamlContractLoader.load(PACKAGE_SHAPE_RESOURCE);
        BusinessUnderstandingReportNarrative.verify(
                "business-understanding.package-shape.main-default",
                "Business-understanding package shape must stay small, skill-neutral, free of direct console output and free of MCP/Atlas/Reporting imports.",
                "Package-shape policies=" + entries.size()
                        + ", allowedKinds=" + ALLOWED_PACKAGE_KINDS,
                "Update skills/business/contracts/package-shape.yml or move non-semantic logic back to the owning skill.",
                () -> {
                    Assert.assertFalse(entries.isEmpty(), "Business-understanding package-shape policy must not be empty.");
                    assertCapsStaySemantic(entries);

                    for (Map<String, String> entry : entries) {
                        String kind = required(entry, "kind", PACKAGE_SHAPE_RESOURCE);
                        Assert.assertTrue(ALLOWED_PACKAGE_KINDS.contains(kind),
                                "Unsupported business-understanding package-shape kind: " + kind);
                        Assert.assertFalse(required(entry, "reason", PACKAGE_SHAPE_RESOURCE).isBlank(),
                                "Every package-shape policy must explain why it exists.");
                        if (entry.containsKey("root")) {
                            Assert.assertTrue(Files.isDirectory(path(required(entry, "root", PACKAGE_SHAPE_RESOURCE))),
                                    "Package-shape root must exist: " + required(entry, "root", PACKAGE_SHAPE_RESOURCE));
                        }
                        if (entry.containsKey("path")) {
                            Assert.assertTrue(Files.exists(path(required(entry, "path", PACKAGE_SHAPE_RESOURCE))),
                                    "Package-shape path must exist: " + required(entry, "path", PACKAGE_SHAPE_RESOURCE));
                        }
                    }

                    runIo(() -> assertPackageCaps(entries));
                    runIo(() -> assertForbiddenImports(entries));
                    assertReadmeShape(entries);
                    runIo(() -> assertNoDirectConsole(entries));
                });
    }

    private static void assertCapsStaySemantic(List<Map<String, String>> entries) {
        Map<String, String> main = entries.stream()
                .filter(entry -> "business-understanding.package-shape.main-default".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing main business-understanding package cap"));
        Map<String, String> tests = entries.stream()
                .filter(entry -> "business-understanding.package-shape.test-default".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing test business-understanding package cap"));

        Assert.assertTrue(integer(main, "maxClasses", PACKAGE_SHAPE_RESOURCE) <= 17,
                "Business-understanding main must stay a small shared semantic kernel; the accepted extra model is BusinessGraphConstruction.");
        Assert.assertTrue(integer(tests, "maxClasses", PACKAGE_SHAPE_RESOURCE) <= 6,
                "Business-understanding tests must stay focused on shared semantics and consumer bridge checks.");
    }

    private static void runIo(IoAssertion assertion) {
        try {
            assertion.run();
        } catch (IOException e) {
            throw new AssertionError("Business-understanding IO assertion failed.", e);
        }
    }

    @FunctionalInterface
    private interface IoAssertion {
        void run() throws IOException;
    }

    private static void assertPackageCaps(List<Map<String, String>> entries) throws IOException {
        List<String> violations = new ArrayList<>();
        for (Map<String, String> policy : policies(entries, "package-default")) {
            Path root = path(required(policy, "root", PACKAGE_SHAPE_RESOURCE));
            int cap = integer(policy, "maxClasses", PACKAGE_SHAPE_RESOURCE);
            try (Stream<Path> files = Files.walk(root)) {
                long count = files.filter(AtfSourcePaths::isJavaSource).count();
                if (count > cap) {
                    violations.add(required(policy, "root", PACKAGE_SHAPE_RESOURCE)
                            + " has " + count + " Java files, cap " + cap);
                }
            }
        }
        Assert.assertTrue(violations.isEmpty(),
                "Business-understanding kernel grew past its semantic package caps: " + violations);
    }

    private static void assertForbiddenImports(List<Map<String, String>> entries) throws IOException {
        List<String> violations = new ArrayList<>();
        for (Map<String, String> policy : policies(entries, "forbidden-import")) {
            Path root = path(required(policy, "root", PACKAGE_SHAPE_RESOURCE));
            String forbiddenImport = "import " + required(policy, "forbiddenPackage", PACKAGE_SHAPE_RESOURCE) + ".";
            try (Stream<Path> files = Files.walk(root)) {
                List<Path> javaFiles = files.filter(AtfSourcePaths::isJavaSource).collect(Collectors.toList());
                for (Path source : javaFiles) {
                    if (Files.readString(source).contains(forbiddenImport)) {
                        violations.add(AtfSourcePaths.unixPath(source.normalize()) + " imports " + forbiddenImport);
                    }
                }
            }
        }
        Assert.assertTrue(violations.isEmpty(),
                "Business-understanding kernel must not import MCP, Atlas or Reporting: " + violations);
    }

    private static void assertReadmeShape(List<Map<String, String>> entries) {
        List<String> violations = policies(entries, "required-readme").stream()
                .map(policy -> path(required(policy, "path", PACKAGE_SHAPE_RESOURCE)))
                .filter(path -> !read(path).contains("Business Understanding Kernel")
                        || !read(path).contains("Do not decide MCP package publishability")
                        || !read(path).contains("Do not own Atlas generated artifacts")
                        || !read(path).contains("semantic-lexicon.yml")
                        || !read(path).contains("testng-business-understanding-sanity.xml"))
                .map(path -> AtfSourcePaths.unixPath(path.normalize()))
                .collect(Collectors.toList());
        Assert.assertTrue(violations.isEmpty(),
                "Business-understanding README must explain ownership, boundaries and sanity suite: " + violations);
    }

    private static void assertNoDirectConsole(List<Map<String, String>> entries) throws IOException {
        List<String> violations = new ArrayList<>();
        for (Map<String, String> policy : policies(entries, "package-default")) {
            try (Stream<Path> files = Files.walk(path(required(policy, "root", PACKAGE_SHAPE_RESOURCE)))) {
                files.filter(AtfSourcePaths::isJavaSource)
                        .filter(source -> DIRECT_CONSOLE.matcher(read(source)).find())
                        .map(source -> AtfSourcePaths.unixPath(source.normalize()))
                        .forEach(violations::add);
            }
        }
        Assert.assertTrue(violations.isEmpty(),
                "Business-understanding kernel must not write direct console output: " + violations);
    }

    private static List<Map<String, String>> policies(List<Map<String, String>> entries, String kind) {
        return entries.stream()
                .filter(entry -> kind.equals(entry.get("kind")))
                .collect(Collectors.toList());
    }

    private static int integer(Map<String, String> entry, String key, String resource) {
        return Integer.parseInt(required(entry, key, resource));
    }

    private static String required(Map<String, String> entry, String key, String resource) {
        return SimpleYamlContractLoader.required(entry, key, resource);
    }

    private static Path path(String relativePath) {
        return AtfSourcePaths.workspaceRoot().resolve(relativePath);
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new AssertionError("Could not read " + path, e);
        }
    }
}
