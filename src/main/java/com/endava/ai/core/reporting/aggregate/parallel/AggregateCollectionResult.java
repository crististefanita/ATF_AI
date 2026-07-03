package com.endava.ai.core.reporting.aggregate.parallel;

import com.endava.ai.core.reporting.ReportText;
import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.endava.ai.core.reporting.aggregate.AggregateStatus;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

public final class AggregateCollectionResult implements AggregateExtentReportWriter.CaseRecord {
    private static final String SUITE_NAME = "Parallel aggregate collection sweep";
    private static final String CONTEXT_NAME = "isolated collection process";
    private static final String CLASS_NAME = "com.endava.ai.core.reporting.aggregate.parallel.AggregateCollection";

    private final AggregateCollectionSpec spec;
    private final int exitCode;
    private final long startMillis;
    private final long endMillis;
    private final List<String> artifactLinks;
    private final String failureSummary;

    public AggregateCollectionResult(
            AggregateCollectionSpec spec,
            int exitCode,
            long startMillis,
            long endMillis,
            List<String> artifactLinks,
            String failureSummary
    ) {
        this.spec = Objects.requireNonNull(spec, "spec");
        this.exitCode = exitCode;
        this.startMillis = startMillis;
        this.endMillis = Math.max(startMillis, endMillis);
        this.artifactLinks = List.copyOf(artifactLinks == null ? List.of() : artifactLinks);
        this.failureSummary = failureSummary == null ? "" : failureSummary.trim();
    }

    public static AggregateCollectionResult pass(
            AggregateCollectionSpec spec,
            long startMillis,
            long endMillis,
            List<String> artifactLinks
    ) {
        return new AggregateCollectionResult(spec, 0, startMillis, endMillis, artifactLinks, "");
    }

    public static AggregateCollectionResult fail(
            AggregateCollectionSpec spec,
            int exitCode,
            long startMillis,
            long endMillis,
            List<String> artifactLinks,
            String failureSummary
    ) {
        return new AggregateCollectionResult(spec, exitCode, startMillis, endMillis, artifactLinks, failureSummary);
    }

    public AggregateCollectionSpec spec() {
        return spec;
    }

    public int exitCode() {
        return exitCode;
    }

    public List<String> artifactLinks() {
        return artifactLinks;
    }

    @Override
    public String suiteName() {
        return SUITE_NAME;
    }

    @Override
    public String contextName() {
        return CONTEXT_NAME;
    }

    @Override
    public String className() {
        return CLASS_NAME;
    }

    @Override
    public String methodName() {
        return spec.id();
    }

    @Override
    public String parameters() {
        return "outputRoot=" + spec.outputRoot();
    }

    @Override
    public String suiteProfile() {
        return "PARALLEL_COLLECTION_CLOSEOUT";
    }

    @Override
    public String capability() {
        return "parallel aggregate collection orchestration";
    }

    @Override
    public String purpose() {
        return "Run isolated validation collections concurrently and merge them into one final audit surface.";
    }

    @Override
    public String area() {
        return "REPORTING_AGGREGATE";
    }

    @Override
    public String owner() {
        return "ATF core aggregate reporting";
    }

    @Override
    public String expected() {
        return "Collection runs in an isolated output root and publishes auditable evidence.";
    }

    @Override
    public String observed() {
        return "exitCode=" + exitCode + "; artifacts=" + artifactLinks.size();
    }

    @Override
    public String repairHint() {
        return "Keep collection roots unique; merge explicit collection results instead of reading latest target reports.";
    }

    @Override
    public String statusName() {
        return status().name();
    }

    @Override
    public AggregateStatus status() {
        return exitCode == 0 ? AggregateStatus.PASSED : AggregateStatus.FAILED;
    }

    @Override
    public long startMillis() {
        return startMillis;
    }

    @Override
    public long endMillis() {
        return endMillis;
    }

    @Override
    public String stackTrace() {
        return failureSummary;
    }

    @Override
    public String displayName() {
        return spec.displayName();
    }

    @Override
    public String description() {
        return ReportText.printable(spec.command());
    }

    @Override
    public long durationMillis() {
        return Math.max(0L, Duration.ofMillis(endMillis - startMillis).toMillis());
    }
}
