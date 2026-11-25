package com.ai.ui.core.engine;

import com.ai.core.support.config.UiEngineType;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

/** Discovers installed adapters without linking Core to a browser SDK. */
public final class UiEngineRegistry {
    private final Map<UiEngineType, UiEngineProvider> providers = new EnumMap<>(UiEngineType.class);

    UiEngineRegistry(Iterable<UiEngineProvider> candidates) {
        for (UiEngineProvider provider : candidates) {
            UiEngineType type = Objects.requireNonNull(provider.type(), "Provider engine type");
            UiEngineProvider previous = providers.putIfAbsent(type, provider);
            if (previous != null) {
                throw new IllegalStateException("Multiple UI engine providers for " + type.configValue()
                        + ": " + previous.getClass().getName() + ", " + provider.getClass().getName());
            }
        }
    }

    public static UiEngineRegistry defaults() {
        return load(Thread.currentThread().getContextClassLoader());
    }

    public static UiEngineRegistry load(ClassLoader classLoader) {
        return new UiEngineRegistry(ServiceLoader.load(UiEngineProvider.class, classLoader));
    }

    public UIEngine create(UiEngineType type, UiEngineCreationRequest request) {
        UiEngineProvider provider = providers.get(Objects.requireNonNull(type, "UI engine type"));
        if (provider == null) {
            throw new IllegalStateException("UI engine '" + type.configValue()
                    + "' is not installed. Add com.ai:atf-" + type.configValue()
                    + " to the runtime classpath. Installed engines: " + supportedValues());
        }
        return provider.create(request);
    }

    public Set<UiEngineType> supportedTypes() { return Set.copyOf(providers.keySet()); }

    public String supportedValues() {
        return providers.keySet().stream().map(UiEngineType::configValue).sorted()
                .collect(Collectors.joining(", "));
    }
}