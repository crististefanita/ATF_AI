package com.ai.core.support.contract.core.yaml.model;

import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;

@Getter
@Accessors(fluent = true)
public final class AtfCoreYamlUiEngineSpec {
    private final String id;
    private final String kind;
    private final String key;
    private final String value;
    private final String owner;
    private final String repair;

    private AtfCoreYamlUiEngineSpec(String id,
                                    String kind,
                                    String key,
                                    String value,
                                    String owner,
                                    String repair) {
        this.id = Objects.requireNonNull(id, "id");
        this.kind = Objects.requireNonNull(kind, "kind");
        this.key = Objects.requireNonNull(key, "key");
        this.value = Objects.requireNonNull(value, "value");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.repair = Objects.requireNonNull(repair, "repair");
    }

    public static AtfCoreYamlUiEngineSpec from(Map<String, String> entry, String resourcePath) {
        return new AtfCoreYamlUiEngineSpec(
                required(entry, "id", resourcePath),
                required(entry, "kind", resourcePath),
                required(entry, "key", resourcePath),
                required(entry, "value", resourcePath),
                required(entry, "owner", resourcePath),
                required(entry, "repair", resourcePath));
    }

}
