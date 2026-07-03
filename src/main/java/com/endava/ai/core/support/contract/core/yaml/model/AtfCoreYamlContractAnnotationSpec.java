package com.endava.ai.core.support.contract.core.yaml.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.endava.ai.core.support.contract.yaml.YamlScalars.requiredBoolean;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtfCoreYamlContractAnnotationSpec {
    @NonNull
    private final String id;
    @NonNull
    private final String kind;
    @NonNull
    private final String name;
    @NonNull
    private final String recommendedEngine;
    private final boolean pureFramework;
    private final boolean liveBoundary;
    @NonNull
    private final String owner;
    @NonNull
    private final String purpose;

    public static AtfCoreYamlContractAnnotationSpec from(Map<String, String> entry, String resourcePath) {
        String kind = required(entry, "kind", resourcePath);
        return new AtfCoreYamlContractAnnotationSpec(
                required(entry, "id", resourcePath),
                kind,
                required(entry, "name", resourcePath),
                profileValue(entry, kind, "recommendedEngine", resourcePath),
                profileBoolean(entry, kind, "pureFramework", resourcePath),
                profileBoolean(entry, kind, "liveBoundary", resourcePath),
                required(entry, "owner", resourcePath),
                required(entry, "purpose", resourcePath));
    }

    public boolean isProfile() {
        return "profile".equals(kind);
    }

    public boolean isArea() {
        return "area".equals(kind);
    }

    private static String profileValue(Map<String, String> entry,
                                       String kind,
                                       String key,
                                       String resourcePath) {
        if (!"profile".equals(kind)) {
            return "";
        }
        return required(entry, key, resourcePath);
    }

    private static boolean profileBoolean(Map<String, String> entry,
                                          String kind,
                                          String key,
                                          String resourcePath) {
        if (!"profile".equals(kind)) {
            return false;
        }
        return requiredBoolean(entry, key, resourcePath);
    }
}
