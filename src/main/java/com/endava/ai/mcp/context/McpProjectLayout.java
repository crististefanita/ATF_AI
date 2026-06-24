package com.endava.ai.mcp.context;

public final class McpProjectLayout {
    public static final String BASE_PACKAGE = "com.endava.ai";
    public static final String BASE_PACKAGE_PATH = BASE_PACKAGE.replace('.', '/');

    public static final String MAIN_JAVA_ROOT = "src/main/java";
    public static final String TEST_JAVA_ROOT = "src/test/java";

    public static final String MAIN_BASE_PATH = MAIN_JAVA_ROOT + "/" + BASE_PACKAGE_PATH;
    public static final String TEST_BASE_PATH = TEST_JAVA_ROOT + "/" + BASE_PACKAGE_PATH;

    public static final String MCP_MAIN_PATH = MAIN_BASE_PATH + "/mcp/";
    public static final String MCP_TEST_PATH = TEST_BASE_PATH + "/mcp/";
    public static final String CORE_MAIN_PATH = MAIN_BASE_PATH + "/core/";
    public static final String UI_MAIN_PATH = MAIN_BASE_PATH + "/ui/";
    public static final String API_MAIN_PATH = MAIN_BASE_PATH + "/api/";

    public static final String UI_BASE_TEST_CLASS = BASE_PACKAGE + ".ui.core.BaseTestUI";
    public static final String UI_BASE_TEST_IMPORT = "import " + UI_BASE_TEST_CLASS + ";";
    public static final String UI_IMPORT_PREFIX = "import " + BASE_PACKAGE + ".ui.";

    private McpProjectLayout() {
    }
}
