package com.ai.ui.core.engine;

import com.ai.core.support.config.UiEngineType;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.List;

public class UiEngineRegistryTest {
    @Test
    public void coreWorksWithoutInstalledBrowserAdapters() {
        UiEngineRegistry registry = UiEngineRegistry.defaults();
        Assert.assertTrue(registry.supportedTypes().isEmpty());
        IllegalStateException failure = Assert.expectThrows(IllegalStateException.class,
                () -> registry.create(UiEngineType.SELENIUM, null));
        Assert.assertTrue(failure.getMessage().contains("com.ai:atf-selenium"));
    }

    @Test
    public void duplicateProvidersFailBeforeSessionCreation() {
        UiEngineProvider provider = new UiEngineProvider() {
            public UiEngineType type() { return UiEngineType.PLAYWRIGHT; }
            public UIEngine create(UiEngineCreationRequest request) {
                throw new AssertionError("Discovery must not start a browser");
            }
        };
        IllegalStateException failure = Assert.expectThrows(IllegalStateException.class,
                () -> new UiEngineRegistry(List.of(provider, provider)));
        Assert.assertTrue(failure.getMessage().contains("Multiple UI engine providers"));
    }
}
