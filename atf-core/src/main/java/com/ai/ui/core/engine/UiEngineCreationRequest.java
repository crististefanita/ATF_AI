package com.ai.ui.core.engine;

import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.ui.core.engine.window.UIWindowConfiguration;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class UiEngineCreationRequest {
    @NonNull
    private final UIWindowConfiguration windowConfiguration;
    @NonNull
    private final UiRuntimeOptions runtimeOptions;
}
