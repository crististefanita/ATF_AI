package com.endava.ai.core.support.contract.capability;

import com.endava.ai.core.support.contract.AtfContractArea;
import com.endava.ai.core.support.contract.AtfContractProfile;

import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Immutable description of one ATF framework capability.
 *
 * <p>Use the builder to keep catalog rows readable while still validating that
 * every capability has an id, profile, area, severity, owner, invariant,
 * suite profile, primary evidence artifact, report expectation, and repair path.</p>
 */
@Getter
@Accessors(fluent = true)
public final class AtfCapabilitySpec {

    private final String id;
    private final AtfContractArea area;
    private final AtfContractProfile profile;
    private final AtfCapabilitySeverity severity;
    private final String owner;
    private final String expected;
    private final String suiteXml;
    private final String primaryEvidence;
    private final String reportExpectation;
    private final String repairHint;

    private AtfCapabilitySpec(Builder builder) {
        this.id = requireText(builder.id, "id");
        this.area = Objects.requireNonNull(builder.area, "area");
        this.profile = Objects.requireNonNull(builder.profile, "profile");
        this.severity = Objects.requireNonNull(builder.severity, "severity");
        this.owner = requireText(builder.owner, "owner");
        this.expected = requireText(builder.expected, "expected");
        this.suiteXml = requireText(builder.suiteXml, "suiteXml");
        this.primaryEvidence = requireText(builder.primaryEvidence, "primaryEvidence");
        this.reportExpectation = requireText(builder.reportExpectation, "reportExpectation");
        this.repairHint = requireText(builder.repairHint, "repairHint");
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing ATF capability " + name);
        }
        return value;
    }

    public static final class Builder {

        private final String id;
        private AtfContractArea area;
        private AtfContractProfile profile;
        private AtfCapabilitySeverity severity = AtfCapabilitySeverity.MAJOR;
        private String owner;
        private String expected;
        private String suiteXml;
        private String primaryEvidence;
        private String reportExpectation;
        private String repairHint;

        private Builder(String id) {
            this.id = id;
        }

        public Builder area(AtfContractArea area) {
            this.area = area;
            return this;
        }

        public Builder profile(AtfContractProfile profile) {
            this.profile = profile;
            return this;
        }

        public Builder severity(AtfCapabilitySeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder owner(String owner) {
            this.owner = owner;
            return this;
        }

        public Builder expected(String expected) {
            this.expected = expected;
            return this;
        }

        public Builder suiteXml(String suiteXml) {
            this.suiteXml = suiteXml;
            return this;
        }

        public Builder primaryEvidence(String primaryEvidence) {
            this.primaryEvidence = primaryEvidence;
            return this;
        }

        public Builder reportExpectation(String reportExpectation) {
            this.reportExpectation = reportExpectation;
            return this;
        }

        public Builder repairHint(String repairHint) {
            this.repairHint = repairHint;
            return this;
        }

        public AtfCapabilitySpec build() {
            return new AtfCapabilitySpec(this);
        }
    }
}
