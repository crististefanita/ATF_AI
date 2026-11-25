package com.ai.ui.core.engine.selenium;

import com.ai.core.support.config.UiEngineType;
import com.ai.ui.core.engine.UIEngine;
import com.ai.ui.core.engine.UiEngineCreationRequest;
import com.ai.ui.core.engine.UiEngineProvider;

public final class SeleniumProvider implements UiEngineProvider {
    @Override
    public UiEngineType type() { return UiEngineType.SELENIUM; }

    @Override
    public UIEngine create(UiEngineCreationRequest request) {
        return new SeleniumEngine(request.windowConfiguration(), request.runtimeOptions());
    }
}