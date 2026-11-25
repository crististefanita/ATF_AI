package com.ai.core.support.contract.core.yaml.model;

import java.util.Map;
import java.util.Objects;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.ai.core.support.contract.yaml.YamlScalars.optional;
import static com.ai.core.support.contract.yaml.YamlScalars.requiredValue;

public final class AtfCoreYamlPolicySpec {
    private static final String KIND = "kind";
    private static final String SURFACE = "surface";
    private static final String OWNER = "owner";
    private static final String SOURCE = "source";
    private static final String TOKEN = "token";
    private static final String FRAGMENT = "fragment";
    private static final String KEY = "key";
    private static final String CONSTANT = "constant";
    private static final String DEFAULT_VALUE = "defaultValue";
    private static final String VALIDATOR = "validator";
    private static final String REPAIR = "repair";

    private final String resourcePath;
    private final String id;
    private final String kind;
    private final String surface;
    private final String owner;
    private final String source;
    private final String token;
    private final String fragment;
    private final String key;
    private final String constant;
    private final String defaultValue;
    private final String validator;
    private final String repair;

    private AtfCoreYamlPolicySpec(String resourcePath, Map<String, String> entry) {
        this.resourcePath = Objects.requireNonNull(resourcePath, "resourcePath");
        this.id = required(entry, "id", resourcePath);
        this.kind = required(entry, KIND, resourcePath);
        this.surface = optional(entry, SURFACE);
        this.owner = optional(entry, OWNER);
        this.source = optional(entry, SOURCE);
        this.token = optional(entry, TOKEN);
        this.fragment = optional(entry, FRAGMENT);
        this.key = optional(entry, KEY);
        this.constant = optional(entry, CONSTANT);
        this.defaultValue = optional(entry, DEFAULT_VALUE);
        this.validator = optional(entry, VALIDATOR);
        this.repair = optional(entry, REPAIR);
    }

    public static AtfCoreYamlPolicySpec from(Map<String, String> entry, String resourcePath) {
        return new AtfCoreYamlPolicySpec(resourcePath, entry);
    }

    public String id() {
        return id;
    }

    public String kind() {
        return kind;
    }

    public String surface() {
        return requiredField(surface, SURFACE);
    }

    public String owner() {
        return requiredField(owner, OWNER);
    }

    public String source() {
        return requiredField(source, SOURCE);
    }

    public String token() {
        return requiredField(token, TOKEN);
    }

    public String fragment() {
        return requiredField(fragment, FRAGMENT);
    }

    public String key() {
        return requiredField(key, KEY);
    }

    public String constant() {
        return requiredField(constant, CONSTANT);
    }

    public String defaultValue() {
        return requiredField(defaultValue, DEFAULT_VALUE);
    }

    public String validator() {
        return requiredField(validator, VALIDATOR);
    }

    public String repair() {
        return requiredField(repair, REPAIR);
    }

    private String requiredField(String value, String key) {
        return requiredValue(value, key, resourcePath, "contract", id);
    }
}
