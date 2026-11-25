package com.ai.ui.core.engine.playwright;

import com.ai.core.support.config.UiEngineType;
import com.ai.ui.core.engine.UIEngine;
import com.ai.ui.core.engine.UiEngineCreationRequest;
import com.ai.ui.core.engine.UiEngineProvider;

public final class PlaywrightProvider implements UiEngineProvider {
    @Override
    public UiEngineType type() { return UiEngineType.PLAYWRIGHT; }

    @Override
    public UIEngine create(UiEngineCreationRequest request) {
        return new PlaywrightEngine(request.windowConfiguration(), request.runtimeOptions());
    }
}