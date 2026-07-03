package com.ai.ui.core.engine;

import com.ai.core.support.config.UiEngineType;
import com.ai.ui.core.engine.playwright.PlaywrightEngine;
import com.ai.ui.core.engine.selenium.SeleniumEngine;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class UiEngineRegistry {
    private final Map<UiEngineType, UiEngineProvider> providers = new EnumMap<>(UiEngineType.class);

    private UiEngineRegistry(Set<UiEngineProvider> providers) {
        providers.forEach(provider -> this.providers.put(provider.type(), provider));
    }

    public static UiEngineRegistry defaults() {
        return new UiEngineRegistry(Set.of(
                new PlaywrightProvider(),
                new SeleniumProvider()
        ));
    }

    public UIEngine create(UiEngineType type, UiEngineCreationRequest request) {
        UiEngineProvider provider = providers.get(type);
        if (provider == null) {
            throw new IllegalStateException(
                    "Unsupported UI engine type: " + type + ". Supported values: " + supportedValues()
            );
        }
        return provider.create(request);
    }

    public Set<UiEngineType> supportedTypes() {
        return Set.copyOf(providers.keySet());
    }

    public String supportedValues() {
        return providers.keySet().stream()
                .map(UiEngineType::configValue)
                .sorted()
                .collect(Collectors.joining(", "));
    }

    private static final class PlaywrightProvider implements UiEngineProvider {
        @Override
        public UiEngineType type() {
            return UiEngineType.PLAYWRIGHT;
        }

        @Override
        public UIEngine create(UiEngineCreationRequest request) {
            return new PlaywrightEngine(request.windowConfiguration(), request.runtimeOptions());
        }
    }

    private static final class SeleniumProvider implements UiEngineProvider {
        @Override
        public UiEngineType type() {
            return UiEngineType.SELENIUM;
        }

        @Override
        public UIEngine create(UiEngineCreationRequest request) {
            return new SeleniumEngine(request.windowConfiguration(), request.runtimeOptions());
        }
    }
}
