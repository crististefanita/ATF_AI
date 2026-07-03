package com.endava.ai.core.reporting.aggregate;

import com.endava.ai.core.reporting.adapters.extent.config.ExtentReportSettings;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
public final class AggregateReportDescriptor {
    private static final String DEFAULT_TITLE = "Aggregate Report Summary";
    private static final String DEFAULT_DESCRIPTION =
            "One audit surface for all test cases in the configured aggregate suite.";
    private static final String DEFAULT_SCOPE = "configured aggregate suite";
    private static final String DEFAULT_AUDIT_SHAPE =
            "Every test node publishes execution result plus available metadata.";

    private final String testTitle;
    private final String description;
    private final String documentTitle;
    private final String reportName;
    private final String scopeLabel;
    private final String fragmentationReason;
    private final String auditShape;
    private final boolean includeOverviewTest;
    private final ExtentReportKind reportKind;
    private final List<String> runCommands;

    private AggregateReportDescriptor(Builder builder) {
        this.testTitle = requireText(builder.testTitle, "testTitle");
        this.description = requireText(builder.description, "description");
        this.documentTitle = requireText(builder.documentTitle, "documentTitle");
        this.reportName = requireText(builder.reportName, "reportName");
        this.scopeLabel = requireText(builder.scopeLabel, "scopeLabel");
        this.fragmentationReason = requireText(builder.fragmentationReason, "fragmentationReason");
        this.auditShape = requireText(builder.auditShape, "auditShape");
        this.includeOverviewTest = builder.includeOverviewTest;
        this.reportKind = Objects.requireNonNull(builder.reportKind, "reportKind");
        this.runCommands = List.copyOf(builder.runCommands);
    }

    public static AggregateReportDescriptor defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private static String requireText(String value, String name) {
        String text = Objects.requireNonNull(value, name).trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Missing aggregate report descriptor " + name);
        }
        return text;
    }

    public static final class Builder {
        private String testTitle = DEFAULT_TITLE;
        private String description = DEFAULT_DESCRIPTION;
        private String documentTitle = ExtentReportSettings.DEFAULT_DOCUMENT_TITLE;
        private String reportName = ExtentReportSettings.DEFAULT_REPORT_NAME;
        private String scopeLabel = DEFAULT_SCOPE;
        private String fragmentationReason =
                "Runtime reports can be fragmented by adapter-isolation contract tests.";
        private String auditShape = DEFAULT_AUDIT_SHAPE;
        private boolean includeOverviewTest = true;
        private ExtentReportKind reportKind = ExtentReportKind.AGGREGATE_VALIDATION;
        private List<String> runCommands = new ArrayList<>();

        private Builder() {
        }

        public Builder testTitle(String testTitle) {
            this.testTitle = testTitle;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder documentTitle(String documentTitle) {
            this.documentTitle = documentTitle;
            return this;
        }

        public Builder reportName(String reportName) {
            this.reportName = reportName;
            return this;
        }

        public Builder scopeLabel(String scopeLabel) {
            this.scopeLabel = scopeLabel;
            return this;
        }

        public Builder fragmentationReason(String fragmentationReason) {
            this.fragmentationReason = fragmentationReason;
            return this;
        }

        public Builder auditShape(String auditShape) {
            this.auditShape = auditShape;
            return this;
        }

        public Builder includeOverviewTest(boolean includeOverviewTest) {
            this.includeOverviewTest = includeOverviewTest;
            return this;
        }

        public Builder reportKind(ExtentReportKind reportKind) {
            this.reportKind = Objects.requireNonNull(reportKind, "reportKind");
            return this;
        }

        public Builder runCommands(List<String> runCommands) {
            this.runCommands = new ArrayList<>(Objects.requireNonNull(runCommands, "runCommands"));
            return this;
        }

        public Builder addRunCommand(String runCommand) {
            this.runCommands.add(requireText(runCommand, "runCommand"));
            return this;
        }

        public AggregateReportDescriptor build() {
            return new AggregateReportDescriptor(this);
        }
    }
}
