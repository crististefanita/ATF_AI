package com.ai.ui.core.engine;

import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.ui.core.engine.window.UIWindowConfiguration;
import com.ai.ui.core.engine.window.UIWindowSizeResolver;

public final class UIEngineFactory {
    private static final UiEngineRegistry REGISTRY = UiEngineRegistry.defaults();

    private UIEngineFactory() {
    }

    /**
     * Supported ui.engine values: selenium, playwright, default.
     * The legacy default alias is resolved by UiEngineType; framework.properties currently selects playwright explicitly.
     */
    public static UIEngine create() {
        UiRuntimeOptions runtimeOptions = UiRuntimeOptions.load();
        UIWindowConfiguration windowConfiguration = UIWindowSizeResolver.resolve();
        return REGISTRY.create(
                runtimeOptions.engineType(),
                new UiEngineCreationRequest(windowConfiguration, runtimeOptions)
        );
    }

}
