package com.endava.ai.ui.core.engine;

import com.endava.ai.core.support.config.UiEngineType;

public interface UiEngineProvider {
    UiEngineType type();

    UIEngine create(UiEngineCreationRequest request);
}
