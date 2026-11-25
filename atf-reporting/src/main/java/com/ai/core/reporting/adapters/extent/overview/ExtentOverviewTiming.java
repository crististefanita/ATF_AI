package com.ai.core.reporting.adapters.extent.overview;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.Optional;

final class ExtentOverviewTiming {
    private static final DateTimeFormatter SPARK_OVERVIEW_DATE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMM d, yyyy h:mm:ss a")
            .parseDefaulting(ChronoField.MILLI_OF_SECOND, 0)
            .toFormatter(Locale.ENGLISH);

    private ExtentOverviewTiming() {
    }

    static Optional<String> durationBetween(String startedAt, String endedAt) {
        Optional<LocalDateTime> started = parseSparkDate(startedAt);
        Optional<LocalDateTime> ended = parseSparkDate(endedAt);
        if (started.isEmpty() || ended.isEmpty()) {
            return Optional.empty();
        }
        long millis = Duration.between(
                started.get().atZone(ZoneId.systemDefault()),
                ended.get().atZone(ZoneId.systemDefault())
        ).toMillis();
        return millis < 0L ? Optional.empty() : Optional.of(formatMillis(millis));
    }

    private static Optional<LocalDateTime> parseSparkDate(String value) {
        try {
            return Optional.of(LocalDateTime.parse(value, SPARK_OVERVIEW_DATE));
        } catch (DateTimeParseException parseFailure) {
            return Optional.empty();
        }
    }

    static String formatMillis(long millis) {
        if (millis < 1_000L) {
            return millis + " ms";
        }
        long seconds = millis / 1_000L;
        long remainder = millis % 1_000L;
        return seconds + "." + String.format("%03d", remainder) + " s";
    }
}
