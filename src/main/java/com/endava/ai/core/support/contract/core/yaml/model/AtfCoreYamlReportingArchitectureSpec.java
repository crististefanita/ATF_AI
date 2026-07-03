package com.endava.ai.core.support.contract.core.yaml.model;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import static com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;

@Getter
@Accessors(fluent = true)
public final class AtfCoreYamlReportingArchitectureSpec {
    private final String id;
    private final String owner;
    private final String sourceRoot;
    private final String publicApi;
    private final String internalBoundary;
    private final String auditSurface;
    private final String contractTest;
    private final String suiteXml;
    private final String repairTarget;
    private final String responsibility;

    private AtfCoreYamlReportingArchitectureSpec(String id,
                                                 String owner,
                                                 String sourceRoot,
                                                 String publicApi,
                                                 String internalBoundary,
                                                 String auditSurface,
                                                 String contractTest,
                                                 String suiteXml,
                                                 String repairTarget,
                                                 String responsibility) {
        this.id = Objects.requireNonNull(id, "id");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.sourceRoot = Objects.requireNonNull(sourceRoot, "sourceRoot");
        this.publicApi = Objects.requireNonNull(publicApi, "publicApi");
        this.internalBoundary = Objects.requireNonNull(internalBoundary, "internalBoundary");
        this.auditSurface = Objects.requireNonNull(auditSurface, "auditSurface");
        this.contractTest = Objects.requireNonNull(contractTest, "contractTest");
        this.suiteXml = Objects.requireNonNull(suiteXml, "suiteXml");
        this.repairTarget = Objects.requireNonNull(repairTarget, "repairTarget");
        this.responsibility = Objects.requireNonNull(responsibility, "responsibility");
    }

    public static AtfCoreYamlReportingArchitectureSpec from(Map<String, String> entry, String resourcePath) {
        return new AtfCoreYamlReportingArchitectureSpec(
                required(entry, "id", resourcePath),
                required(entry, "owner", resourcePath),
                required(entry, "sourceRoot", resourcePath),
                required(entry, "publicApi", resourcePath),
                required(entry, "internalBoundary", resourcePath),
                required(entry, "auditSurface", resourcePath),
                required(entry, "contractTest", resourcePath),
                required(entry, "suiteXml", resourcePath),
                required(entry, "repairTarget", resourcePath),
                required(entry, "responsibility", resourcePath));
    }

    public Path sourceRootPath() {
        return Path.of(sourceRoot);
    }

    public Path repairTargetPath() {
        return Path.of(repairTarget);
    }
}
