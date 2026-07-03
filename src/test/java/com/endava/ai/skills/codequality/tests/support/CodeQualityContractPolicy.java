package com.endava.ai.skills.codequality.tests.support;

import com.endava.ai.skills.codequality.tests.support.compatibility.CodeQualityJavaCompatibilityRule;
import com.endava.ai.skills.codequality.tests.support.assertion.CodeQualityAssertionDensityPolicy;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.endava.ai.skills.codequality.tests.support.dependency.CodeQualityDependencyBoundary;
import com.endava.ai.skills.codequality.tests.support.method.CodeQualityMethodSizePolicy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CodeQualityContractPolicy {
    public static final String CONTRACT_RESOURCE = "skills/codequality/contracts/code-quality.yml";
    private static final String DEFAULT_LIMIT_ID = "code-quality.package-class-default";
    private static final String CLASS_SIZE_DEFAULT_ID = "code-quality.class-size-default";
    private static final String METHOD_SIZE_DEFAULT_ID = "code-quality.method-size-default";
    private static final String ASSERTION_DENSITY_DEFAULT_ID = "code-quality.assertion-density-default";
    private static final String YAML_SIZE_DEFAULT_ID = "code-quality.yaml-size-default";
    private static final String SUITE_COUNT_DEFAULT_ID = "code-quality.suite-count-default";
    private static final String STALE_SLACK_ID = "code-quality.legacy-ratchet-stale-slack";
    private static final String YAML_STALE_SLACK_ID = "code-quality.legacy-yaml-ratchet-stale-slack";

    private final List<Map<String, String>> entries;

    private CodeQualityContractPolicy(List<Map<String, String>> entries) {
        this.entries = entries;
    }

    public static CodeQualityContractPolicy load() {
        List<Map<String, String>> entries = SimpleYamlContractLoader.load(CONTRACT_RESOURCE);
        if (entries.isEmpty()) {
            throw new AssertionError("code-quality.yml must not be empty.");
        }
        return new CodeQualityContractPolicy(entries);
    }

    public void assertUniqueIds() {
        List<String> ids = entries.stream()
                .map(entry -> required(entry, "id"))
                .collect(Collectors.toList());
        if (ids.stream().distinct().count() != ids.size()) {
            throw new AssertionError("code-quality.yml ids must be unique: " + ids);
        }
    }

    public int defaultPackageClassLimit() {
        return entries.stream()
                .filter(entry -> DEFAULT_LIMIT_ID.equals(required(entry, "id")))
                .findFirst()
                .map(entry -> integer(entry, "max"))
                .orElseThrow(() -> new AssertionError("Missing default package class limit: " + DEFAULT_LIMIT_ID));
    }

    public int staleRatchetSlack() {
        return entries.stream()
                .filter(entry -> STALE_SLACK_ID.equals(required(entry, "id")))
                .findFirst()
                .map(entry -> integer(entry, "slack"))
                .orElse(2);
    }

    public int staleYamlRatchetSlack() {
        return entries.stream()
                .filter(entry -> YAML_STALE_SLACK_ID.equals(required(entry, "id")))
                .findFirst()
                .map(entry -> integer(entry, "slackBytes"))
                .orElse(1024);
    }

    public List<Path> sourceRoots() {
        return roots("source-root", "Code Quality source root must exist: ");
    }

    public List<Path> resourceRoots() {
        return roots("resource-root", "Code Quality resource root must exist: ");
    }

    private List<Path> roots(String kind, String missingMessage) {
        List<Path> roots = entries.stream()
                .filter(entry -> kind.equals(required(entry, "kind")))
                .map(entry -> AtfSourcePaths.projectPath(required(entry, "path")))
                .collect(Collectors.toUnmodifiableList());
        if (roots.isEmpty()) {
            throw new AssertionError("Code Quality contract must declare " + kind + " entries.");
        }
        roots.forEach(root -> {
            if (!Files.isDirectory(root)) {
                throw new AssertionError(missingMessage + root);
            }
        });
        return roots;
    }

    public Map<String, Integer> legacyPackageCaps() {
        return caps("legacy-package-cap");
    }

    public Map<String, Integer> legacyClassSizeCaps() {
        return caps("legacy-class-size-cap");
    }

    public Map<String, Integer> legacyYamlSizeCaps() {
        return caps("legacy-yaml-size-cap");
    }

    public CodeQualityClassSizePolicy classSizePolicy() {
        Map<String, String> entry = policyEntry(CLASS_SIZE_DEFAULT_ID, "class-size");
        return new CodeQualityClassSizePolicy(
                integer(entry, "warningLines"),
                integer(entry, "maxLines"),
                integer(entry, "maxWarningFiles"),
                integer(entry, "maxOverMaxFiles"));
    }

    public CodeQualityMethodSizePolicy methodSizePolicy() {
        Map<String, String> entry = policyEntry(METHOD_SIZE_DEFAULT_ID, "method-size");
        return new CodeQualityMethodSizePolicy(
                integer(entry, "warningLines"),
                integer(entry, "maxLines"),
                integer(entry, "maxWarningMethods"),
                integer(entry, "maxOverMaxMethods"));
    }

    public CodeQualityAssertionDensityPolicy assertionDensityPolicy() {
        Map<String, String> entry = policyEntry(ASSERTION_DENSITY_DEFAULT_ID, "assertion-density");
        return new CodeQualityAssertionDensityPolicy(integer(entry, "minimumCuePercent"),
                integer(entry, "maxLowDensityClasses"), integer(entry, "maxZeroCueClasses"));
    }

    public CodeQualityYamlSizePolicy yamlSizePolicy() {
        Map<String, String> entry = policyEntry(YAML_SIZE_DEFAULT_ID, "YAML-size");
        return new CodeQualityYamlSizePolicy(
                integer(entry, "warningBytes"),
                integer(entry, "maxBytes"),
                integer(entry, "maxWarningFiles"),
                integer(entry, "maxOverMaxFiles"));
    }

    public int maxSuiteCount() {
        return entries.stream()
                .filter(entry -> SUITE_COUNT_DEFAULT_ID.equals(required(entry, "id")))
                .findFirst()
                .map(entry -> integer(entry, "maxSuites"))
                .orElseThrow(() -> new AssertionError("Missing suite-count policy: " + SUITE_COUNT_DEFAULT_ID));
    }

    public List<CodeQualityDependencyBoundary> dependencyBoundaries() {
        List<CodeQualityDependencyBoundary> boundaries = entries.stream()
                .filter(entry -> "dependency-boundary".equals(required(entry, "kind")))
                .map(entry -> new CodeQualityDependencyBoundary(
                        required(entry, "id"),
                        required(entry, "root"),
                        required(entry, "forbiddenPackage"),
                        required(entry, "repair")))
                .collect(Collectors.toUnmodifiableList());
        if (boundaries.isEmpty()) {
            throw new AssertionError("Code Quality contract must declare dependency-boundary entries.");
        }
        boundaries.forEach(boundary -> {
            if (boundary.getRepair().isBlank()) {
                throw new AssertionError("dependency-boundary must explain repair: " + boundary.getId());
            }
            Path root = AtfSourcePaths.projectPath(boundary.getRoot());
            if (!Files.isDirectory(root)) {
                throw new AssertionError("Code Quality dependency boundary root must exist: " + root);
            }
        });
        return boundaries;
    }

    public List<CodeQualityJavaCompatibilityRule> javaCompatibilityRules() {
        List<CodeQualityJavaCompatibilityRule> rules = entries.stream()
                .filter(entry -> "java-compatibility-ban".equals(required(entry, "kind")))
                .map(entry -> new CodeQualityJavaCompatibilityRule(
                        required(entry, "id"),
                        required(entry, "forbiddenApi"),
                        required(entry, "repair")))
                .collect(Collectors.toUnmodifiableList());
        if (rules.isEmpty()) {
            throw new AssertionError("Code Quality contract must declare java-compatibility-ban entries.");
        }
        return rules;
    }

    private Map<String, String> policyEntry(String id, String label) {
        return entries.stream()
                .filter(candidate -> id.equals(required(candidate, "id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing " + label + " policy: " + id));
    }

    private Map<String, Integer> caps(String kind) {
        Map<String, Integer> caps = new LinkedHashMap<>();
        for (Map<String, String> entry : entries) {
            if (kind.equals(required(entry, "kind"))) {
                String path = required(entry, "path");
                int max = integer(entry, "max");
                if (required(entry, "repair").isBlank()) {
                    throw new AssertionError(kind + " must explain repair: " + path);
                }
                caps.put(path, max);
            }
        }
        return caps;
    }

    private static int integer(Map<String, String> entry, String key) {
        return Integer.parseInt(required(entry, key));
    }

    private static String required(Map<String, String> entry, String key) {
        return SimpleYamlContractLoader.required(entry, key, CONTRACT_RESOURCE);
    }
}
