package com.ai.core.support.contract.ids;

import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;

public final class AtfSourcePaths {
    public static final String AI_ROOT_PACKAGE = "com.ai";
    public static final String ATF_ROOT_PACKAGE = AI_ROOT_PACKAGE + ".atf";
    public static final String API_ROOT_PACKAGE = AI_ROOT_PACKAGE + ".api";
    public static final String API_CORE_PACKAGE = API_ROOT_PACKAGE + ".core";
    public static final String UI_ROOT_PACKAGE = AI_ROOT_PACKAGE + ".ui";
    public static final String UI_CORE_PACKAGE = UI_ROOT_PACKAGE + ".core";
    public static final String CORE_ROOT_PACKAGE = AI_ROOT_PACKAGE + ".core";
    public static final String CORE_REPORTING_PACKAGE = CORE_ROOT_PACKAGE + ".reporting";
    public static final String CORE_SUPPORT_PACKAGE = CORE_ROOT_PACKAGE + ".support";

    public static final Path REACTOR_ROOT = findReactorRoot();
    public static final List<Path> PRODUCTION_JAVA_ROOTS = List.of("atf-core", "atf-api", "atf-playwright", "atf-selenium", "atf-reporting")
            .stream().map(module -> REACTOR_ROOT.resolve(module).resolve("src/main/java"))
            .collect(java.util.stream.Collectors.toUnmodifiableList());
    public static final Path MAIN_JAVA_ROOT = REACTOR_ROOT.resolve("atf-core/src/main/java");
    public static final Path MAIN_RESOURCES_ROOT = REACTOR_ROOT.resolve("atf-core/src/main/resources");
    public static final Path API_JAVA_ROOT = REACTOR_ROOT.resolve("atf-api/src/main/java");
    public static final Path SUPPORT_JAVA_ROOT = REACTOR_ROOT.resolve("atf-tests/src/main/java");
    public static final List<Path> VERIFICATION_JAVA_ROOTS = java.util.stream.Stream.of("atf-governance", "atf-tests")
            .flatMap(module -> java.util.stream.Stream.of("main", "test")
                    .map(tree -> REACTOR_ROOT.resolve(module).resolve("src/" + tree + "/java")))
            .collect(java.util.stream.Collectors.toUnmodifiableList());
    public static final Path TEST_JAVA_ROOT = REACTOR_ROOT.resolve("atf-tests/src/test/java");
    public static final Path TEST_RESOURCES_ROOT = REACTOR_ROOT.resolve("atf-tests/src/test/resources");
    public static final Path TARGET_ROOT = Path.of("target");
    public static final Path CORE_TEST_RESOURCES_ROOT = testResource("atf/core");
    public static final Path ATF_CORE_TEST_RESOURCES_ROOT = testResource("atf/core/contracts");
    public static final Path TARGET_REPORTS_ROOT = TARGET_ROOT.resolve("reports");
    public static final Path MAIN_AI_ROOT = mainJavaPackage(AI_ROOT_PACKAGE);
    public static final Path MAIN_ATF_ROOT = mainJavaPackage(ATF_ROOT_PACKAGE);
    public static final Path MAIN_CORE_ROOT = mainJavaPackage(CORE_ROOT_PACKAGE);
    public static final Path MAIN_CORE_REPORTING_ROOT = mainJavaPackage(CORE_REPORTING_PACKAGE);
    public static final Path MAIN_CORE_SUPPORT_ROOT = mainJavaPackage(CORE_SUPPORT_PACKAGE);
    public static final Path MAIN_UI_ROOT = mainJavaPackage(UI_ROOT_PACKAGE);
    public static final Path MAIN_UI_CORE_ROOT = mainJavaPackage(UI_CORE_PACKAGE);
    public static final Path MAIN_API_ROOT = API_JAVA_ROOT.resolve(packagePath(API_ROOT_PACKAGE));
    public static final Path MAIN_API_CORE_ROOT = API_JAVA_ROOT.resolve(packagePath(API_CORE_PACKAGE));
    public static final Path TEST_ATF_ROOT = testJavaPackage(ATF_ROOT_PACKAGE);
    public static final Path TEST_ATF_CORE_ROOT = testJavaPackage(ATF_ROOT_PACKAGE + ".core");
    public static final Path TEST_ATF_CORE_REPORTING_ROOT = testJavaPackage(ATF_ROOT_PACKAGE + ".core.reporting");
    public static final Path TEST_ATF_CORE_SUPPORT_ROOT = testJavaPackage(ATF_ROOT_PACKAGE + ".core.support");
    public static final Path TEST_ATF_API_ROOT = testJavaPackage(ATF_ROOT_PACKAGE + ".api");
    public static final Path TEST_ATF_UI_ROOT = testJavaPackage(ATF_ROOT_PACKAGE + ".ui");
    public static final Path TEST_UI_TESTS_ROOT = testJavaPackage(UI_ROOT_PACKAGE + ".tests");
    public static final Path TEST_API_TESTS_ROOT = testJavaPackage(API_ROOT_PACKAGE + ".tests");
    public static final Path TEST_CLASSES_ROOT = Path.of("target", "test-classes");
    public static final Path MAIN_CLASSES_ROOT = Path.of("target", "classes");

    public static final String JAVA_EXTENSION = ".java";
    public static final String XML_EXTENSION = ".xml";
    public static final String README_FILE_NAME = "README.md";

    private AtfSourcePaths() {
    }

    public static Path mainJava(Class<?> type) {
        return mainJava(type.getName());
    }

    public static Path mainJava(String fullyQualifiedName) {
                return java.util.stream.Stream.concat(PRODUCTION_JAVA_ROOTS.stream(),
                        VERIFICATION_JAVA_ROOTS.stream().filter(root -> root.toString().replace('\\', '/').endsWith("/src/main/java")))
                .map(root -> javaSource(root, fullyQualifiedName))
                .filter(Files::isRegularFile).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No production source for " + fullyQualifiedName));
    }

    public static Path testJava(Class<?> type) {
        return testJava(type.getName());
    }

    public static Path testJava(String fullyQualifiedName) {
        Path owner = fullyQualifiedName.startsWith("com.ai.atf.governance.")
                ? REACTOR_ROOT.resolve("atf-governance/src/test/java") : TEST_JAVA_ROOT;
        return javaSource(owner, fullyQualifiedName);
    }

    public static Path mainJavaPackage(String packageName) {
        Path owner = packageName.equals(API_ROOT_PACKAGE) || packageName.startsWith(API_ROOT_PACKAGE + ".")
                ? API_JAVA_ROOT : MAIN_JAVA_ROOT;
        return owner.resolve(packagePath(packageName));
    }

    public static Path supportJavaPackage(String packageName) {
        return SUPPORT_JAVA_ROOT.resolve(packagePath(packageName));
    }

    public static Path testJavaPackage(String packageName) {
        return TEST_JAVA_ROOT.resolve(packagePath(packageName));
    }

    public static Path testResource(String relativePath) {
        return TEST_RESOURCES_ROOT.resolve(relativePath);
    }

    public static Path workspaceRoot() {
        return REACTOR_ROOT;
    }

    public static Path projectPath(Path relativePath) {
        return workspaceRoot().resolve(relativePath);
    }

    public static Path projectPath(String relativePath) {
        return workspaceRoot().resolve(relativePath);
    }

    public static String projectRelative(Path source) {
        return unixPath(workspaceRoot().relativize(source));
    }

    public static String unixPath(Path source) {
        return unixPath(source.toString());
    }

    public static String unixPath(String source) {
        return source.replace('\\', '/').replace(java.io.File.separatorChar, '/');
    }

    public static boolean isJavaSource(Path source) {
        return source.getFileName().toString().endsWith(JAVA_EXTENSION);
    }

    public static boolean isTestJavaSource(Path source) {
        return source.getFileName().toString().endsWith("Test" + JAVA_EXTENSION);
    }

    public static boolean isXmlFile(Path source) {
        return source.getFileName().toString().endsWith(XML_EXTENSION);
    }

    public static String className(Path root, Path source) {
        String relative = root.relativize(source).toString().replace(java.io.File.separatorChar, '.');
        return relative.substring(0, relative.length() - JAVA_EXTENSION.length());
    }

    public static boolean isUnderPackage(Path source, String packageName) {
        return unixPath(source).contains("/" + unixPath(packagePath(packageName)) + "/");
    }

    private static Path findReactorRoot() {
        Path candidate = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (candidate != null) {
            if (Files.isRegularFile(candidate.resolve("pom.xml"))
                    && Files.isDirectory(candidate.resolve("atf-core"))
                    && Files.isDirectory(candidate.resolve("atf-tests"))) return candidate;
            candidate = candidate.getParent();
        }
        throw new IllegalStateException("Cannot locate the ATF reactor root from user.dir");
    }

    private static Path javaSource(Path root, String fullyQualifiedName) {
        return root.resolve(packagePath(fullyQualifiedName) + JAVA_EXTENSION);
    }

    private static Path packagePath(String fullyQualifiedName) {
        return Path.of(fullyQualifiedName.replace('.', java.io.File.separatorChar));
    }

}
