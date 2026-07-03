package com.endava.ai.core.reporting.aggregate.model;

import com.endava.ai.core.reporting.ReportText;
import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class AggregateTimingSummary {
    private static final DateTimeFormatter SPARK_OVERVIEW_DATE =
            DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm:ss a", Locale.ENGLISH);
    private static final AggregateTimingSummary EMPTY = new AggregateTimingSummary(false, 0, 0, 0, 0, "n/a", "n/a");

    @Getter
    private final boolean observed;
    private final long startedAtMillis;
    private final long endedAtMillis;
    @Getter
    private final long wallClockMillis;
    @Getter
    private final long cumulativeDurationMillis;
    @Getter
    private final String slowestTest;
    @Getter
    private final String slowestClass;

    private AggregateTimingSummary(
            boolean observed,
            long startedAtMillis,
            long endedAtMillis,
            long wallClockMillis,
            long cumulativeDurationMillis,
            String slowestTest,
            String slowestClass
    ) {
        this.observed = observed;
        this.startedAtMillis = nonNegative(startedAtMillis, "startedAtMillis");
        this.endedAtMillis = nonNegative(endedAtMillis, "endedAtMillis");
        this.wallClockMillis = nonNegative(wallClockMillis, "wallClockMillis");
        this.cumulativeDurationMillis = nonNegative(cumulativeDurationMillis, "cumulativeDurationMillis");
        this.slowestTest = Objects.requireNonNull(slowestTest, "slowestTest");
        this.slowestClass = Objects.requireNonNull(slowestClass, "slowestClass");
    }

    public static AggregateTimingSummary empty() {
        return EMPTY;
    }

    public static AggregateTimingSummary from(List<? extends AggregateExtentReportWriter.CaseRecord> cases) {
        if (cases == null || cases.isEmpty()) {
            return empty();
        }

        long firstStart = Long.MAX_VALUE;
        long lastEnd = Long.MIN_VALUE;
        long cumulative = 0L;
        AggregateExtentReportWriter.CaseRecord slowest = null;
        long slowestDuration = -1L;
        Map<String, ClassTiming> classTimings = new LinkedHashMap<>();

        for (AggregateExtentReportWriter.CaseRecord testCase : cases) {
            AggregateCaseTiming timing = AggregateCaseTiming.from(testCase);
            long duration = timing.durationMillis();
            firstStart = Math.min(firstStart, timing.startMillis());
            lastEnd = Math.max(lastEnd, timing.endMillis());
            cumulative += duration;
            if (duration > slowestDuration) {
                slowest = testCase;
                slowestDuration = duration;
            }
            classTimings.computeIfAbsent(testCase.className(), ignored -> new ClassTiming())
                    .add(duration);
        }

        ClassTiming slowestClass = null;
        String slowestClassName = "";
        for (Map.Entry<String, ClassTiming> entry : classTimings.entrySet()) {
            if (slowestClass == null || entry.getValue().durationMillis > slowestClass.durationMillis) {
                slowestClassName = entry.getKey();
                slowestClass = entry.getValue();
            }
        }

        return new AggregateTimingSummary(
                true,
                firstStart,
                lastEnd,
                Math.max(0L, lastEnd - firstStart),
                cumulative,
                slowest == null ? "n/a" : testLabel(slowest) + " - " + formatMillis(slowestDuration),
                slowestClass == null ? "n/a" : ReportText.simpleClassName(slowestClassName)
                        + " - " + formatMillis(slowestClass.durationMillis)
                        + " (" + slowestClass.caseCount + " cases)"
        );
    }

    public String startedAt() {
        return observed ? formatSparkDate(startedAtMillis) : "n/a";
    }

    public String endedAt() {
        return observed ? formatSparkDate(endedAtMillis) : "n/a";
    }

    public String wallClockDuration() {
        return formatMillis(wallClockMillis);
    }

    public String overviewDuration() {
        return formatMillis(overviewDurationMillis());
    }

    public String cumulativeDuration() {
        return formatMillis(cumulativeDurationMillis);
    }

    public long overviewDurationMillis() {
        return wallClockMillis > 0L ? wallClockMillis : cumulativeDurationMillis;
    }

    private static String testLabel(AggregateExtentReportWriter.CaseRecord testCase) {
        String label = ReportText.simpleClassName(testCase.className()) + "#" + ReportText.printable(testCase.methodName());
        String parameters = ReportText.printable(testCase.parameters()).trim();
        if ("n/a".equalsIgnoreCase(parameters)) {
            parameters = "";
        }
        return parameters.isBlank() ? label : label + parameters;
    }

    private static String formatMillis(long millis) {
        if (millis < 1_000L) {
            return millis + " ms";
        }
        long seconds = millis / 1_000L;
        long remainder = millis % 1_000L;
        return seconds + "." + String.format("%03d", remainder) + " s";
    }

    private static String formatSparkDate(long millis) {
        return SPARK_OVERVIEW_DATE.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()));
    }

    private static long nonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("Aggregate timing " + name + " must be non-negative: " + value);
        }
        return value;
    }

    private static final class ClassTiming {
        private long durationMillis;
        private int caseCount;

        void add(long duration) {
            durationMillis += duration;
            caseCount++;
        }
    }
}
