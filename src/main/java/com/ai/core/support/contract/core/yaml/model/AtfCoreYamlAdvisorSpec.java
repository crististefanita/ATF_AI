package com.ai.core.support.contract.core.yaml.model;

import lombok.Getter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.Map;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.ai.core.support.contract.yaml.YamlScalars.requiredBoolean;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AtfCoreYamlAdvisorSpec {
    private final String id;
    private final String severity;
    private final String signal;
    private final String recommendation;
    private final String repairTarget;
    private final String recommendedProfile;
    private final String reportAnchor;
    private final boolean blocking;

    public static AtfCoreYamlAdvisorSpec from(Map<String, String> entry, String resourcePath) {
        return new AtfCoreYamlAdvisorSpec(
                required(entry, "id", resourcePath),
                required(entry, "severity", resourcePath),
                required(entry, "signal", resourcePath),
                required(entry, "recommendation", resourcePath),
                required(entry, "repairTarget", resourcePath),
                required(entry, "recommendedProfile", resourcePath),
                required(entry, "reportAnchor", resourcePath),
                requiredBoolean(entry, "blocking", resourcePath));
    }

    public Path repairTargetPath() {
        return Path.of(repairTarget);
    }
}
