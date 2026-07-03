package com.endava.ai.skills.atlas.tests;

import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.endava.ai.core.reporting.aggregate.AggregateStatus;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
class AtlasUiCase implements AggregateExtentReportWriter.CaseRecord {
    private static final long SYNTHETIC_INVENTORY_BASE_MILLIS = 1_735_689_600_000L;
    private static final long SYNTHETIC_INVENTORY_CASE_DURATION_MILLIS = 100L;

    String suiteName;
    String contextName;
    String className;
    String methodName;
    String scopeDescription;
    AtlasUiReportVariant variant;
    long startMillis;
    long endMillis;

    AtlasUiCase(
            String suiteName,
            String contextName,
            String className,
            String methodName,
            String scopeDescription,
            AtlasUiReportVariant variant,
            long startMillis,
            long endMillis
    ) {
        this.suiteName = suiteName;
        this.contextName = contextName;
        this.className = className;
        this.methodName = methodName;
        this.scopeDescription = scopeDescription;
        this.variant = variant;
        this.startMillis = startMillis;
        this.endMillis = endMillis;
    }

    static AtlasUiCase sourceInventory(
            AtlasUiScope scope,
            String className,
            String methodName,
            int sequence
    ) {
        long start = SYNTHETIC_INVENTORY_BASE_MILLIS
                + Math.max(0, sequence) * SYNTHETIC_INVENTORY_CASE_DURATION_MILLIS;
        return new AtlasUiCase(
                scope.name() + " UI Agentic Atlas inventory",
                "source-inventory",
                className,
                methodName,
                scope.description(),
                AtlasUiReportVariant.currentInventory(),
                start,
                start + SYNTHETIC_INVENTORY_CASE_DURATION_MILLIS
        );
    }

    AtlasUiCase forVariant(AtlasUiReportVariant reportVariant) {
        return new AtlasUiCase(
                suiteName,
                contextName,
                className,
                methodName,
                scopeDescription,
                reportVariant,
                startMillis,
                endMillis
        );
    }

    public String parameters() {
        return "";
    }

    public String suiteProfile() {
        return variant.suiteProfile();
    }

    public String capability() {
        return variant.capability();
    }

    public String purpose() {
        return variant.purpose();
    }

    public String area() {
        return "ATLAS";
    }

    public String owner() {
        return "Framework Atlas";
    }

    public String expected() {
        return variant.expected();
    }

    public String observed() {
        return "Method present in source inventory: " + className + "#" + methodName;
    }

    public String repairHint() {
        return "If the source count changes, regenerate this report and update the matching business atlas row.";
    }

    public String statusName() {
        return AggregateStatus.PASSED.name();
    }

    public AggregateStatus status() {
        return AggregateStatus.PASSED;
    }

    public long startMillis() {
        return startMillis;
    }

    public long endMillis() {
        return endMillis;
    }

    public String stackTrace() {
        return "";
    }

    public String displayName() {
        return methodName;
    }

    public String description() {
        return scopeDescription;
    }

    public long durationMillis() {
        return Math.max(1L, endMillis - startMillis);
    }
}
