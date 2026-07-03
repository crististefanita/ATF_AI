package com.endava.ai.core.reporting.htmlcontract.model;

import com.endava.ai.core.reporting.htmlcontract.yaml.HtmlReportYamlContractCatalog;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class HtmlReportContract {
    private final HtmlReportShellProfile profile;
    private final String id;
    private final String owner;
    private final String expectedLanguage;
    private final List<HtmlReportSectionRequirement> sections;
    private final List<String> requiredShellClasses;
    private final List<String> requiredGraphClasses;
    private final List<String> visibleNodeShapeTags;
    private final List<String> forbiddenGraphWrapperTags;
    private final List<HtmlReportSectionRequirement> collapsedSections;

    private HtmlReportContract(HtmlReportShellProfile profile,
                               String id,
                               String owner,
                               String expectedLanguage,
                               List<HtmlReportSectionRequirement> sections,
                               List<String> requiredShellClasses,
                               List<String> requiredGraphClasses,
                               List<String> visibleNodeShapeTags,
                               List<String> forbiddenGraphWrapperTags,
                               List<HtmlReportSectionRequirement> collapsedSections) {
        this.profile = Objects.requireNonNull(profile, "profile");
        this.id = Objects.requireNonNull(id, "id");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.expectedLanguage = Objects.requireNonNull(expectedLanguage, "expectedLanguage");
        this.sections = List.copyOf(sections);
        this.requiredShellClasses = List.copyOf(requiredShellClasses);
        this.requiredGraphClasses = List.copyOf(requiredGraphClasses);
        this.visibleNodeShapeTags = List.copyOf(visibleNodeShapeTags);
        this.forbiddenGraphWrapperTags = List.copyOf(forbiddenGraphWrapperTags);
        this.collapsedSections = List.copyOf(collapsedSections);
    }

    public static HtmlReportContract forProfile(HtmlReportShellProfile profile) {
        HtmlReportYamlContractSpec spec = HtmlReportYamlContractCatalog.require(profile);
        HtmlReportYamlContractSpec shared = HtmlReportYamlContractCatalog.require(HtmlReportShellProfile.SHARED_FINAL_REPORT);
        return new HtmlReportContract(profile,
                spec.id(),
                spec.owner(),
                spec.expectedLanguage(),
                spec.requiredSections().stream()
                .map(HtmlReportSectionRequirement::named)
                .collect(Collectors.toUnmodifiableList()),
                merged(shared.requiredShellClasses(), spec.requiredShellClasses()),
                merged(shared.requiredGraphClasses(), spec.requiredGraphClasses()),
                merged(shared.visibleNodeShapeTags(), spec.visibleNodeShapeTags()),
                merged(shared.forbiddenGraphWrapperTags(), spec.forbiddenGraphWrapperTags()),
                spec.collapsedSections().stream()
                        .map(HtmlReportSectionRequirement::named)
                        .collect(Collectors.toUnmodifiableList()));
    }

    public HtmlReportShellProfile profile() {
        return profile;
    }

    public String id() {
        return id;
    }

    public String owner() {
        return owner;
    }

    public String expectedLanguage() {
        return expectedLanguage;
    }

    public List<HtmlReportSectionRequirement> sections() {
        return sections;
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

    public List<HtmlReportSectionRequirement> collapsedSections() {
        return collapsedSections;
    }

    private static List<String> merged(List<String> shared, List<String> specific) {
        if (specific.isEmpty()) {
            return shared;
        }
        return List.copyOf(specific);
    }
}
