package com.endava.ai.skills.mcp.contracts.catalog;

import java.util.Objects;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ContractLazy<T> {
    @NonNull
    private final String resourcePath;
    @NonNull
    private final Supplier<T> loader;
    private T value;
    private RuntimeException failure;

    static <T> ContractLazy<T> of(String resourcePath, Supplier<T> loader) {
        return new ContractLazy<>(resourcePath, loader);
    }

    synchronized T get() {
        if (value != null) {
            return value;
        }
        if (failure != null) {
            throw failure;
        }
        try {
            value = Objects.requireNonNull(loader.get(), "Loaded MCP contract catalog is null: " + resourcePath);
            return value;
        } catch (RuntimeException exception) {
            failure = new IllegalStateException("Cannot load MCP contract catalog [" + resourcePath + "]: "
                    + exception.getMessage(), exception);
            throw failure;
        }
    }
}
