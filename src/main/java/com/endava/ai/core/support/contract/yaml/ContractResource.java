package com.endava.ai.core.support.contract.yaml;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Typed view over a versioned YAML contract resource.
 */
public final class ContractResource<T> {
    @Getter
    @Accessors(fluent = true)
    private final String resourcePath;
    private final Function<Map<String, String>, T> mapper;

    private ContractResource(String resourcePath, Function<Map<String, String>, T> mapper) {
        this.resourcePath = Objects.requireNonNull(resourcePath, "resourcePath");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    public static <T> ContractResource<T> of(String resourcePath, Function<Map<String, String>, T> mapper) {
        return new ContractResource<>(resourcePath, mapper);
    }

    public List<T> load() {
        return SimpleYamlContractLoader.load(resourcePath).stream()
                .map(mapper)
                .collect(Collectors.toUnmodifiableList());
    }
}
