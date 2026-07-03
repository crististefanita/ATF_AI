package com.ai.skills.atlas.tests;

import com.ai.core.support.contract.ids.AtfSourcePaths;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class AtlasAtf6UiCurrentReportPublisherTest {
    private static final int EXPECTED_ATF6_UI_TESTS = 60;

    @Test
    public void publishesAtf6CurrentUiAgenticExtentReportForAtlas() throws IOException {
        publish(new AtlasUiScope(
                "framework-under-test",
                Path.of(System.getProperty("user.dir")),
                AtfSourcePaths.unixPath(AtfSourcePaths.TEST_UI_TESTS_ROOT),
                EXPECTED_ATF6_UI_TESTS,
                "framework UI source inventory"
        ));
        publish(new AtlasUiScope(
                "ATF6",
                Path.of(System.getProperty("user.dir")),
                AtfSourcePaths.unixPath(AtfSourcePaths.TEST_UI_TESTS_ROOT),
                EXPECTED_ATF6_UI_TESTS,
                "ATF6 UI source inventory"
        ));
    }

    private static void publish(AtlasUiScope scope) throws IOException {
        AtlasJavaTestInventoryScanner scanner = new AtlasJavaTestInventoryScanner();
        List<AtlasUiCase> cases = scanner.scan(scope);

        Assert.assertEquals(cases.size(), scope.expectedTests(),
                "ATF6 UI Agentic inventory drifted. Update atlas expectations deliberately.");
        for (AtlasUiCase testCase : cases) {
            Assert.assertFalse(testCase.className().endsWith("." + testCase.methodName()),
                    "Atlas UI source inventory must keep Java class and @Test method as separate report dimensions: "
                            + testCase.className() + "#" + testCase.methodName());
            Assert.assertTrue(testCase.durationMillis() >= 100L,
                    "Atlas UI source inventory uses synthetic timing, but it must be visually credible and "
                            + "large enough to avoid misleading sub-second aggregate totals: "
                            + testCase.className() + "#" + testCase.methodName());
        }
        new AtlasUiCurrentReportPublisher().publish(scope, cases, AtlasUiReportVariant.currentInventory());
    }
}
