package com.endava.ai.skills.mcp.context;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;

public final class McpProjectLayout {
    public static final String BASE_PACKAGE = AtfSourcePaths.AI_ROOT_PACKAGE;
    public static final String BASE_PACKAGE_PATH = BASE_PACKAGE.replace('.', '/');

    public static final String MAIN_JAVA_ROOT = path(AtfSourcePaths.MAIN_JAVA_ROOT);
    public static final String TEST_JAVA_ROOT = path(AtfSourcePaths.TEST_JAVA_ROOT);

    public static final String MAIN_BASE_PATH = path(AtfSourcePaths.MAIN_AI_ROOT);
    public static final String TEST_BASE_PATH = path(AtfSourcePaths.TEST_JAVA_ROOT.resolve(BASE_PACKAGE_PATH));

    public static final String SKILLS_MAIN_PATH = MAIN_BASE_PATH + "/skills/";
    public static final String SKILLS_TEST_PATH = TEST_BASE_PATH + "/skills/";

    public static final String MCP_MAIN_PATH = pathWithSlash(AtfSourcePaths.MAIN_MCP_ROOT);
    public static final String MCP_TEST_PATH = pathWithSlash(AtfSourcePaths.TEST_MCP_ROOT);
    public static final String CORE_MAIN_PATH = pathWithSlash(AtfSourcePaths.MAIN_CORE_ROOT);
    public static final String ATF_TEST_PATH = pathWithSlash(AtfSourcePaths.TEST_ATF_ROOT);
    public static final String UI_MAIN_PATH = pathWithSlash(AtfSourcePaths.MAIN_UI_ROOT);
    public static final String UI_SUPPORT_MCP_MAIN_PATH = UI_MAIN_PATH + "support/mcp/";
    public static final String UI_TEST_PATH = pathWithSlash(AtfSourcePaths.TEST_UI_TESTS_ROOT.getParent());
    public static final String UI_TESTS_PATH = pathWithSlash(AtfSourcePaths.TEST_UI_TESTS_ROOT);
    public static final String UI_CORE_MAIN_PATH = pathWithSlash(AtfSourcePaths.MAIN_UI_CORE_ROOT);
    public static final String API_MAIN_PATH = pathWithSlash(AtfSourcePaths.MAIN_API_ROOT);
    public static final String API_CORE_MAIN_PATH = pathWithSlash(AtfSourcePaths.MAIN_API_CORE_ROOT);
    public static final String ATLAS_MAIN_PATH = pathWithSlash(AtfSourcePaths.MAIN_ATLAS_ROOT);
    public static final String ATLAS_TEST_PATH = pathWithSlash(AtfSourcePaths.TEST_ATLAS_ROOT);

    public static final String UI_BASE_TEST_CLASS = BASE_PACKAGE + ".ui.core.runtime.BaseTestUI";
    public static final String UI_BASE_TEST_IMPORT = "import " + UI_BASE_TEST_CLASS + ";";
    public static final String UI_IMPORT_PREFIX = "import " + BASE_PACKAGE + ".ui.";

    private McpProjectLayout() {
    }

    private static String path(java.nio.file.Path source) {
        return AtfSourcePaths.unixPath(source);
    }

    private static String pathWithSlash(java.nio.file.Path source) {
        return path(source) + "/";
    }
}
