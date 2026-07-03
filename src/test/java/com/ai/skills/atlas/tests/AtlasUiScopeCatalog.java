package com.ai.skills.atlas.tests;

import com.ai.skills.atlas.AtlasContext;
import com.ai.core.support.contract.ids.AtfSourcePaths;

import java.nio.file.Path;
import java.util.List;

final class AtlasUiScopeCatalog {

    private AtlasUiScopeCatalog() {
    }

    static List<AtlasUiScope> currentScopes() {
        return List.of(
                scope("html-ex-ui-boundary", "atlas.scope.html-ex-ui-boundary.root",
                        AtfSourcePaths.TEST_ATF_UI_ROOT,
                        37, "html-ex UI boundary source inventory"),
                scope("html-ex-ui-agentic-primary", "atlas.scope.html-ex-ui-agentic-primary.root",
                        AtfSourcePaths.TEST_UI_TESTS_ROOT,
                        80, "html-ex UI MCP Agentic source inventory"),
                scope("html-ex-ui-agentic-secondary", "atlas.scope.html-ex-ui-agentic-secondary.root",
                        AtfSourcePaths.TEST_UI_TESTS_ROOT,
                        32, "html-ex UI MCP Agentic source inventory"),
                scope("framework-under-test", "atlas.scope.framework-under-test.root",
                        AtfSourcePaths.TEST_UI_TESTS_ROOT,
                        60, "framework UI source inventory"),
                scope("ATF6", "atlas.scope.ATF6.root",
                        AtfSourcePaths.TEST_UI_TESTS_ROOT,
                        60, "ATF6 UI source inventory")
        );
    }

    private static AtlasUiScope scope(
            String name,
            String rootProperty,
            Path sourceRoot,
            int expectedTests,
            String description
    ) {
        String fallback = AtlasContext.ATLAS_DIRECTORY + "/source-corpus/" + name;
        return new AtlasUiScope(
                name,
                Path.of(System.getProperty(rootProperty, fallback)),
                AtfSourcePaths.unixPath(sourceRoot),
                expectedTests,
                description
        );
    }
}
