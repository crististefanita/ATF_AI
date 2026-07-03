package com.ai.core.support.contract.core.yaml.model;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Objects;

import static com.ai.core.support.contract.yaml.SimpleYamlContractLoader.required;
import static com.ai.core.support.contract.yaml.YamlScalars.optional;
import static com.ai.core.support.contract.yaml.YamlScalars.requiredValue;

@Getter
@Accessors(fluent = true)
public final class AtfCoreYamlContractNarrativeSpec {
    private static final String CONTRACT = "contract";
    private static final String SUITE = "suite";
    private static final String EXPECTED_FAILURE = "expectedFailure";

    private final String resourcePath;
    private final String id;
    private final String kind;
    private final String profile;
    private final String area;
    private final String capability;
    private final String purpose;
    private final String reportHint;
    private final String owner;
    private final String expected;
    private final String observed;
    private final String repairHint;
    private final String reason;
    private final String expectedSignal;

    private AtfCoreYamlContractNarrativeSpec(String resourcePath, Map<String, String> entry) {
        this.resourcePath = Objects.requireNonNull(resourcePath, "resourcePath");
        this.id = required(entry, "id", resourcePath);
        this.kind = required(entry, "kind", resourcePath);
        this.profile = optional(entry, "profile");
        this.area = optional(entry, "area");
        this.capability = optional(entry, "capability");
        this.purpose = optional(entry, "purpose");
        this.reportHint = optional(entry, "reportHint");
        this.owner = optional(entry, "owner");
        this.expected = optional(entry, "expected");
        this.observed = optional(entry, "observed");
        this.repairHint = optional(entry, "repairHint");
        this.reason = optional(entry, "reason");
        this.expectedSignal = optional(entry, "expectedSignal");
        validateKind();
    }

    public static AtfCoreYamlContractNarrativeSpec from(Map<String, String> entry, String resourcePath) {
        return new AtfCoreYamlContractNarrativeSpec(resourcePath, entry);
    }

    public boolean isContract() {
        return CONTRACT.equals(kind);
    }

    public boolean isSuite() {
        return SUITE.equals(kind);
    }

    public boolean isExpectedFailure() {
        return EXPECTED_FAILURE.equals(kind);
    }

    public String contractArea() {
        requireContract();
        return requiredField(area, "area");
    }

    public String contractOwner() {
        requireContract();
        return requiredField(owner, "owner");
    }

    public String contractExpected() {
        requireContract();
        return requiredField(expected, "expected");
    }

    public String contractObserved() {
        requireContract();
        return requiredField(observed, "observed");
    }

    public String contractRepairHint() {
        requireContract();
        return requiredField(repairHint, "repairHint");
    }

    public String suiteProfile() {
        requireSuite();
        return requiredField(profile, "profile");
    }

    public String suiteArea() {
        requireSuite();
        return requiredField(area, "area");
    }

    public String suiteCapability() {
        requireSuite();
        return requiredField(capability, "capability");
    }

    public String suitePurpose() {
        requireSuite();
        return requiredField(purpose, "purpose");
    }

    public String suiteReportHint() {
        requireSuite();
        return reportHint;
    }

    public String expectedFailureReason() {
        requireExpectedFailure();
        return requiredField(reason, "reason");
    }

    public String expectedFailureSignal() {
        requireExpectedFailure();
        return requiredField(expectedSignal, "expectedSignal");
    }

    private void validateKind() {
        if (!isContract() && !isSuite() && !isExpectedFailure()) {
            throw new IllegalArgumentException("Invalid kind in " + resourcePath
                    + " for contract narrative " + id + ": " + kind);
        }
    }

    private void requireContract() {
        if (!isContract()) {
            throw new IllegalArgumentException("Contract narrative " + id + " in "
                    + resourcePath + " is not kind=contract");
        }
    }

    private void requireSuite() {
        if (!isSuite()) {
            throw new IllegalArgumentException("Contract narrative " + id + " in "
                    + resourcePath + " is not kind=suite");
        }
    }

    private void requireExpectedFailure() {
        if (!isExpectedFailure()) {
            throw new IllegalArgumentException("Contract narrative " + id + " in "
                    + resourcePath + " is not kind=expectedFailure");
        }
    }

    private String requiredField(String value, String key) {
        return requiredValue(value, key, resourcePath, "contract narrative", id);
    }
}
