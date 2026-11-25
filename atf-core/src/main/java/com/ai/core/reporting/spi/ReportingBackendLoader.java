package com.ai.core.reporting.spi;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/** Resolves one optional backend and rejects an ambiguous runtime classpath. */
public final class ReportingBackendLoader {

    private ReportingBackendLoader() {
    }

    public static Optional<ReportingBackend> load() {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        return load(contextLoader != null ? contextLoader : ReportingBackend.class.getClassLoader());
    }

    public static Optional<ReportingBackend> load(ClassLoader classLoader) {
        List<ServiceLoader.Provider<ReportingBackend>> providers =
                ServiceLoader.load(ReportingBackend.class, classLoader).stream().collect(Collectors.toList());
        if (providers.size() > 1) {
            String implementations = providers.stream()
                    .map(provider -> provider.type().getName())
                    .sorted()
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
            throw new IllegalStateException(
                    "Multiple reporting backends are available: " + implementations
                            + ". Keep exactly one ReportingBackend provider on the runtime classpath.");
        }
        return providers.isEmpty() ? Optional.empty() : Optional.of(providers.get(0).get());
    }
}
