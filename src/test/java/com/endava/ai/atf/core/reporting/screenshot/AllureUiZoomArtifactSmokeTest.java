package com.endava.ai.atf.core.reporting.screenshot;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.atf.core.reporting.support.AllureZoomEvidenceFixture;
import com.endava.ai.core.support.config.UiEngineType;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.endava.ai.atf.core.reporting.support.UiReportingTestSupport.resetReportingState;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ALLURE_UI_ZOOM_ARTIFACT_SMOKE_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ALLURE_UI_ZOOM_ARTIFACT_SMOKE_TEST_CLASS)
@Listeners(TestListener.class)
@Test(groups = AtfTestGroups.ALLURE_ONLY)
public class AllureUiZoomArtifactSmokeTest {

    private AtfRuntimeConfigSandbox config;

    @AfterMethod
    public void tearDown() {
        if (config != null) {
            config.close();
            config = null;
        }
        resetReportingState();
    }

    @DataProvider
    public Object[][] uiEngines() {
        return new Object[][]{
                {UiEngineType.SELENIUM.configValue()},
                {UiEngineType.PLAYWRIGHT.configValue()}
        };
    }

    @Test(dataProvider = "uiEngines")
    public void allure_final_screenshots_reflect_real_zoom_difference(String uiEngine) throws Exception {
        AllureZoomEvidenceFixture fixture = new AllureZoomEvidenceFixture(
                ensureSandbox(),
                AllureUiZoomArtifactSmokeTest.class
        );

        Path baselineScreenshot = fixture.captureZoomEvidence(uiEngine, "100", "allureZoom100");
        Path zoomedScreenshot = fixture.captureZoomEvidence(uiEngine, "60", "allureZoom60");

        Assert.assertNotEquals(Files.size(baselineScreenshot), Files.size(zoomedScreenshot));
        Assert.assertTrue(
                imageDifferenceRatio(baselineScreenshot, zoomedScreenshot) > 0.01d,
                "Zoomed Allure screenshot should differ visibly for " + uiEngine
        );
    }

    private static double imageDifferenceRatio(Path first, Path second) throws IOException {
        BufferedImage left = ImageIO.read(first.toFile());
        BufferedImage right = ImageIO.read(second.toFile());
        if (left == null || right == null) {
            throw new IllegalStateException("Unable to decode screenshot artifacts");
        }

        int width = Math.min(left.getWidth(), right.getWidth());
        int height = Math.min(left.getHeight(), right.getHeight());
        long totalSamples = 0;
        long changedSamples = 0;

        for (int y = 0; y < height; y += 4) {
            for (int x = 0; x < width; x += 4) {
                totalSamples++;
                if (left.getRGB(x, y) != right.getRGB(x, y)) {
                    changedSamples++;
                }
            }
        }

        return totalSamples == 0 ? 0.0d : (double) changedSamples / totalSamples;
    }

    private AtfRuntimeConfigSandbox ensureSandbox() {
        if (config == null) {
            config = AtfRuntimeConfigSandbox.open();
        }
        return config;
    }
}
