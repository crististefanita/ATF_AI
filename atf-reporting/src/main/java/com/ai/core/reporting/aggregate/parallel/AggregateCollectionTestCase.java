package com.ai.core.reporting.aggregate.parallel;

import com.ai.core.reporting.ReportText;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.AggregateStatus;

import java.util.Objects;

public final class AggregateCollectionTestCase implements AggregateExtentReportWriter.CaseRecord {
    private final AggregateCollectionSpec collection;
    private final String className;
    private final String methodName;
    private final AggregateStatus status;
    private final long startMillis;
    private final long endMillis;
    private final String stackTrace;

    public AggregateCollectionTestCase(
            AggregateCollectionSpec collection,
            String className,
            String methodName,
            AggregateStatus status,
            long startMillis,
            long endMillis,
            String stackTrace
    ) {
        this.collection = Objects.requireNonNull(collection, "collection");
        this.className = requireText(className, "className");
        this.methodName = requireText(methodName, "methodName");
        this.status = Objects.requireNonNull(status, "status");
        this.startMillis = startMillis;
        this.endMillis = Math.max(startMillis, endMillis);
        this.stackTrace = stackTrace == null ? "" : stackTrace.trim();
    }

    @Override
    public String suiteName() {
        return "Parallel aggregate method sweep";
    }

    @Override
    public String contextName() {
        return collection.id();
    }

    @Override
    public String className() {
        return className;
    }

    @Override
    public String methodName() {
        return methodName;
    }

    @Override
    public String parameters() {
        return "collection=" + collection.id();
    }

    @Override
    public String suiteProfile() {
        return "PARALLEL_METHOD_CLOSEOUT";
    }

    @Override
    public String capability() {
        return "parallel aggregate method-level reporting";
    }

    @Override
    public String purpose() {
        return "Merge TestNG method evidence from isolated validation collections into one final Extent report.";
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
        return "Every TestNG method from the collection is visible with its real status.";
    }

    @Override
    public String observed() {
        return status.name() + " in " + collection.id();
    }

    @Override
    public String repairHint() {
        return "Repair the owning collection command, then regenerate the method-level aggregate report.";
    }

    @Override
    public String statusName() {
        return status.name();
    }

    @Override
    public AggregateStatus status() {
        return status;
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
        return stackTrace;
    }

    @Override
    public String displayName() {
        return collection.id() + " | " + ReportText.printable(methodName);
    }

    @Override
    public String description() {
        return collection.command();
    }

    @Override
    public long durationMillis() {
        return Math.max(0L, endMillis - startMillis);
    }

    private static String requireText(String value, String name) {
        String text = Objects.requireNonNull(value, name).trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Missing aggregate collection test-case " + name);
        }
        return text;
    }
}
