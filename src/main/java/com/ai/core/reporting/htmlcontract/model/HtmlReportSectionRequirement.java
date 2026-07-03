package com.ai.core.reporting.htmlcontract.model;

import java.util.Objects;

public final class HtmlReportSectionRequirement {
    private final String label;

    private HtmlReportSectionRequirement(String label) {
        this.label = Objects.requireNonNull(label, "label");
    }

    public static HtmlReportSectionRequirement named(String label) {
        return new HtmlReportSectionRequirement(label);
    }

    public String label() {
        return label;
    }
}
