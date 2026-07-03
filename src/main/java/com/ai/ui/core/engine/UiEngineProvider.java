package com.ai.ui.core.engine;

import com.ai.core.support.config.UiEngineType;

public interface UiEngineProvider {
    UiEngineType type();

    UIEngine create(UiEngineCreationRequest request);
}
