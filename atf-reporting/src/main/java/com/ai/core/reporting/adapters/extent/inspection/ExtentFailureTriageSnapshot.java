package com.ai.core.reporting.adapters.extent.inspection;

import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExtentFailureTriageSnapshot {
    private final String categoryHtml;

    private ExtentFailureTriageSnapshot(String categoryHtml) {
        this.categoryHtml = String.valueOf(categoryHtml);
    }

    public static ExtentFailureTriageSnapshot fromCategoryHtml(String categoryHtml) {
        return new ExtentFailureTriageSnapshot(categoryHtml);
    }

    public int groupCount() {
        return ExtentSparkReportInspector.countOccurrences(
                categoryHtml,
                attributePrefix(ExtentDataAttributes.RUNTIME_FAILURE_GROUP)
        );
    }

    public boolean hasFailureGroups() {
        return categoryHtml.contains(ExtentMarkers.RUNTIME_FAILURE_GROUPS_LABEL)
                && categoryHtml.contains(attributeEquals(
                        ExtentDataAttributes.RUNTIME_FAILURE_TABLE,
                        ExtentDataAttributes.FAILURE_TABLE_FAILURES
                ));
    }

    public boolean hasRepeatedRootCauseSection() {
        return categoryHtml.contains(attributeEquals(ExtentDataAttributes.RUNTIME_FAILURE_SIGNATURES, "true"))
                && categoryHtml.contains("Repeated Root Cause")
                && categoryHtml.contains("REPEATED ROOT CAUSE");
    }

    public String rootCauseSummary() {
        Matcher matcher = Pattern.compile(
                "(\\d+ root-cause signatures? -> \\d+ failed test cases? -> \\d+ affected Java (?:class|classes))"
        ).matcher(categoryHtml);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Failure triage root-cause summary must be present.");
        }
        return matcher.group(1);
    }

    public boolean hasRootCauseSummary(String expected) {
        return categoryHtml.contains(expected);
    }

    private static String attributeEquals(String attribute, String value) {
        return attribute + "=\"" + value + "\"";
    }

    private static String attributePrefix(String attribute) {
        return attribute + "=\"";
    }
}
