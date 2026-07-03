package com.endava.ai.skills.atlas.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

public final class AtlasUiAgenticCurrentReportPublisherTest {

    @Test
    public void publishesCurrentUiAgenticExtentReportsForAtlas() throws IOException {
        AtlasJavaTestInventoryScanner scanner = new AtlasJavaTestInventoryScanner();
        AtlasUiCurrentReportPublisher publisher = new AtlasUiCurrentReportPublisher();
        AtlasUiReportVariant variant = AtlasUiReportVariant.currentInventory();

        for (AtlasUiScope scope : AtlasUiScopeCatalog.currentScopes()) {
            List<AtlasUiCase> cases = scanner.scan(scope);
            Assert.assertEquals(cases.size(), scope.expectedTests(),
                    scope.name() + " UI Agentic inventory drifted. Update atlas expectations deliberately.");
            publisher.publish(scope, cases, variant);
        }
    }
}
