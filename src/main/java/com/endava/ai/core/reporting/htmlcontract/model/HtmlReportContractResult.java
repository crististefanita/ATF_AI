package com.endava.ai.core.reporting.htmlcontract.model;

import java.util.List;

public final class HtmlReportContractResult {
    private final List<HtmlReportShellViolation> violations;

    private HtmlReportContractResult(List<HtmlReportShellViolation> violations) {
        this.violations = List.copyOf(violations);
    }

    public static HtmlReportContractResult of(List<HtmlReportShellViolation> violations) {
        return new HtmlReportContractResult(violations);
    }

    public boolean passed() {
        return violations.isEmpty();
    }

    public List<HtmlReportShellViolation> violations() {
        return violations;
    }
}
