package com.endava.ai.skills.atlas.tests;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class AtlasAtf4UiCurrentReportPublisherTest {
    private static final int EXPECTED_ATF4_UI_TESTS = 40;
    private static final Path ATF4_PROJECT_ROOT =
            Path.of("C:/work/ex/java/demo/atf4/test-framework");

    @Test
    public void publishesAtf4CurrentUiAgenticExtentReportForAtlas() throws IOException {
        AtlasUiScope scope = new AtlasUiScope(
                "ATF4",
                ATF4_PROJECT_ROOT,
                AtfSourcePaths.unixPath(AtfSourcePaths.TEST_UI_TESTS_ROOT),
                EXPECTED_ATF4_UI_TESTS,
                "ATF4 UI source inventory"
        );

        AtlasJavaTestInventoryScanner scanner = new AtlasJavaTestInventoryScanner();
        List<AtlasUiCase> cases = scanner.scan(scope);

        Assert.assertEquals(cases.size(), scope.expectedTests(),
                "ATF4 UI Agentic inventory drifted. Update atlas expectations deliberately.");
        new AtlasUiCurrentReportPublisher().publish(scope, cases, AtlasUiReportVariant.currentInventory());
    }
}
