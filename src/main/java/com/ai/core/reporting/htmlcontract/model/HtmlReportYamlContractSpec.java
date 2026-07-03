package com.ai.core.reporting.htmlcontract.model;

import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class HtmlReportYamlContractSpec {
    private final String id;
    private final HtmlReportShellProfile profile;
    private final String owner;
    private final String expectedLanguage;
    private final List<String> requiredSections;
    private final List<String> requiredShellClasses;
    private final List<String> requiredGraphClasses;
    private final List<String> visibleNodeShapeTags;
    private final List<String> forbiddenGraphWrapperTags;
    private final List<String> collapsedSections;

    private HtmlReportYamlContractSpec(String id,
                                       HtmlReportShellProfile profile,
                                       String owner,
                                       String expectedLanguage,
                                       List<String> requiredSections,
                                       List<String> requiredShellClasses,
                                       List<String> requiredGraphClasses,
                                       List<String> visibleNodeShapeTags,
                                       List<String> forbiddenGraphWrapperTags,
                                       List<String> collapsedSections) {
        this.id = id;
        this.profile = profile;
        this.owner = owner;
        this.expectedLanguage = expectedLanguage;
        this.requiredSections = List.copyOf(requiredSections);
        this.requiredShellClasses = List.copyOf(requiredShellClasses);
        this.requiredGraphClasses = List.copyOf(requiredGraphClasses);
        this.visibleNodeShapeTags = List.copyOf(visibleNodeShapeTags);
        this.forbiddenGraphWrapperTags = List.copyOf(forbiddenGraphWrapperTags);
        this.collapsedSections = List.copyOf(collapsedSections);
    }

    public static HtmlReportYamlContractSpec from(Map<String, String> entry, String resourcePath) {
        return new HtmlReportYamlContractSpec(
                SimpleYamlContractLoader.required(entry, "id", resourcePath),
                HtmlReportShellProfile.valueOf(SimpleYamlContractLoader.required(entry, "profile", resourcePath)),
                SimpleYamlContractLoader.required(entry, "owner", resourcePath),
                entry.getOrDefault("expectedLanguage", "any").trim(),
                list(entry.get("requiredSections")),
                list(entry.get("requiredShellClasses")),
                list(entry.get("requiredGraphClasses")),
                list(entry.get("visibleNodeShapeTags")),
                list(entry.get("forbiddenGraphWrapperTags")),
                list(entry.get("collapsedSections")));
    }

    public String id() {
        return id;
    }

    public HtmlReportShellProfile profile() {
        return profile;
    }

    public String owner() {
        return owner;
    }

    public String expectedLanguage() {
        return expectedLanguage;
    }

    public List<String> requiredSections() {
        return requiredSections;
    }

    public List<String> requiredShellClasses() {
        return requiredShellClasses;
    }

    public List<String> requiredGraphClasses() {
        return requiredGraphClasses;
    }

    public List<String> visibleNodeShapeTags() {
        return visibleNodeShapeTags;
    }

    public List<String> forbiddenGraphWrapperTags() {
        return forbiddenGraphWrapperTags;
    }

    public List<String> collapsedSections() {
        return collapsedSections;
    }

    private static List<String> list(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toUnmodifiableList());
    }
}
