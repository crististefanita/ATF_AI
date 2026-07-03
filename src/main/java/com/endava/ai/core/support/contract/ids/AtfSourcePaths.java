package com.endava.ai.core.support.contract.ids;

import java.nio.file.Path;

public final class AtfSourcePaths {
    public static final String AI_ROOT_PACKAGE = "com.endava.ai";
    public static final String ATF_ROOT_PACKAGE = AI_ROOT_PACKAGE + ".atf";
    public static final String SKILLS_ROOT_PACKAGE = AI_ROOT_PACKAGE + ".skills";
    public static final String ATLAS_ROOT_PACKAGE = SKILLS_ROOT_PACKAGE + ".atlas";
    public static final String MCP_ROOT_PACKAGE = SKILLS_ROOT_PACKAGE + ".mcp";
    public static final String API_ROOT_PACKAGE = AI_ROOT_PACKAGE + ".api";
    public static final String API_CORE_PACKAGE = API_ROOT_PACKAGE + ".core";
    public static final String UI_ROOT_PACKAGE = AI_ROOT_PACKAGE + ".ui";
    public static final String UI_CORE_PACKAGE = UI_ROOT_PACKAGE + ".core";
    public static final String CORE_ROOT_PACKAGE = AI_ROOT_PACKAGE + ".core";
    public static final String CORE_REPORTING_PACKAGE = CORE_ROOT_PACKAGE + ".reporting";
    public static final String CORE_SUPPORT_PACKAGE = CORE_ROOT_PACKAGE + ".support";
    public static final String CORE_SKILLS_PACKAGE = CORE_ROOT_PACKAGE + ".skills";
    public static final String BUSINESS_SKILL_PACKAGE = SKILLS_ROOT_PACKAGE + ".business";
    public static final String REPORTING_SKILL_PACKAGE = SKILLS_ROOT_PACKAGE + ".reporting";

    public static final Path MAIN_JAVA_ROOT = Path.of("src", "main", "java");
    public static final Path MAIN_RESOURCES_ROOT = Path.of("src", "main", "resources");
    public static final Path TEST_JAVA_ROOT = Path.of("src", "test", "java");
    public static final Path TEST_RESOURCES_ROOT = Path.of("src", "test", "resources");
    public static final Path TARGET_ROOT = Path.of("target");
    public static final Path CORE_TEST_RESOURCES_ROOT = testResource("atf/core");
    public static final Path ATF_CORE_TEST_RESOURCES_ROOT = testResource("atf/core/contracts");
    public static final Path TARGET_REPORTS_ROOT = TARGET_ROOT.resolve("reports");
    public static final Path MAIN_AI_ROOT = mainJavaPackage(AI_ROOT_PACKAGE);
    public static final Path MAIN_ATF_ROOT = mainJavaPackage(ATF_ROOT_PACKAGE);
    public static final Path MAIN_ATLAS_ROOT = mainJavaPackage(ATLAS_ROOT_PACKAGE);
    public static final Path MAIN_MCP_ROOT = mainJavaPackage(MCP_ROOT_PACKAGE);
    public static final Path MAIN_CORE_ROOT = mainJavaPackage(CORE_ROOT_PACKAGE);
    public static final Path MAIN_CORE_REPORTING_ROOT = mainJavaPackage(CORE_REPORTING_PACKAGE);
    public static final Path MAIN_CORE_SUPPORT_ROOT = mainJavaPackage(CORE_SUPPORT_PACKAGE);
    public static final Path MAIN_CORE_SKILLS_ROOT = mainJavaPackage(CORE_SKILLS_PACKAGE);
    public static final Path MAIN_BUSINESS_SKILL_ROOT = mainJavaPackage(BUSINESS_SKILL_PACKAGE);
    public static final Path MAIN_REPORTING_SKILL_ROOT = mainJavaPackage(REPORTING_SKILL_PACKAGE);
    public static final Path MAIN_UI_ROOT = mainJavaPackage(UI_ROOT_PACKAGE);
    public static final Path MAIN_UI_CORE_ROOT = mainJavaPackage(UI_CORE_PACKAGE);
    public static final Path MAIN_API_ROOT = mainJavaPackage(API_ROOT_PACKAGE);
    public static final Path MAIN_API_CORE_ROOT = mainJavaPackage(API_CORE_PACKAGE);
    public static final Path TEST_ATF_ROOT = testJavaPackage(ATF_ROOT_PACKAGE);
    public static final Path TEST_ATF_CORE_ROOT = testJavaPackage(ATF_ROOT_PACKAGE + ".core");
    public static final Path TEST_ATF_CORE_REPORTING_ROOT = testJavaPackage(ATF_ROOT_PACKAGE + ".core.reporting");
    public static final Path TEST_ATF_CORE_SUPPORT_ROOT = testJavaPackage(ATF_ROOT_PACKAGE + ".core.support");
    public static final Path TEST_ATF_CORE_SKILLS_ROOT = testJavaPackage(ATF_ROOT_PACKAGE + ".core.skills");
    public static final Path TEST_BUSINESS_SKILL_ROOT =
            testJavaPackage(BUSINESS_SKILL_PACKAGE + ".tests");
    public static final Path TEST_ATF_API_ROOT = testJavaPackage(ATF_ROOT_PACKAGE + ".api");
    public static final Path TEST_ATF_UI_ROOT = testJavaPackage(ATF_ROOT_PACKAGE + ".ui");
    public static final Path TEST_ATLAS_ROOT = testJavaPackage(ATLAS_ROOT_PACKAGE);
    public static final Path TEST_ATLAS_TESTS_ROOT = testJavaPackage(ATLAS_ROOT_PACKAGE + ".tests");
    public static final Path TEST_MCP_ROOT = testJavaPackage(MCP_ROOT_PACKAGE);
    public static final Path TEST_MCP_TESTS_ROOT = testJavaPackage(MCP_ROOT_PACKAGE + ".tests");
    public static final Path TEST_UI_TESTS_ROOT = testJavaPackage(UI_ROOT_PACKAGE + ".tests");
    public static final Path TEST_API_TESTS_ROOT = testJavaPackage(API_ROOT_PACKAGE + ".tests");
    public static final Path TEST_SKILLS_RESOURCES_ROOT = testResource("skills");
    public static final Path TEST_MCP_RESOURCES_ROOT = testResource("skills/mcp");
    public static final Path TEST_ATLAS_RESOURCES_ROOT = testResource("skills/atlas");
    public static final Path TEST_CLASSES_ROOT = Path.of("target", "test-classes");
    public static final Path MAIN_CLASSES_ROOT = Path.of("target", "classes");

    public static final String JAVA_EXTENSION = ".java";
    public static final String XML_EXTENSION = ".xml";
    public static final String README_FILE_NAME = "README.md";

    private AtfSourcePaths() {
    }

    public static Path mainJava(Class<?> type) {
        return javaSource(MAIN_JAVA_ROOT, type.getName());
    }

    public static Path mainJava(String fullyQualifiedName) {
        return javaSource(MAIN_JAVA_ROOT, fullyQualifiedName);
    }

    public static Path testJava(Class<?> type) {
        return javaSource(TEST_JAVA_ROOT, type.getName());
    }

    public static Path testJava(String fullyQualifiedName) {
        return javaSource(TEST_JAVA_ROOT, fullyQualifiedName);
    }

    public static Path mainJavaPackage(String packageName) {
        return MAIN_JAVA_ROOT.resolve(packagePath(packageName));
    }

    public static Path testJavaPackage(String packageName) {
        return TEST_JAVA_ROOT.resolve(packagePath(packageName));
    }

    public static Path testResource(String relativePath) {
        return TEST_RESOURCES_ROOT.resolve(relativePath);
    }

    public static Path workspaceRoot() {
        return Path.of(System.getProperty("user.dir"));
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

    private static Path javaSource(Path root, String fullyQualifiedName) {
        return root.resolve(packagePath(fullyQualifiedName) + JAVA_EXTENSION);
    }

    private static Path packagePath(String fullyQualifiedName) {
        return Path.of(fullyQualifiedName.replace('.', java.io.File.separatorChar));
    }

}
