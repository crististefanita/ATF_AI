package com.endava.ai.core.reporting.aggregate;

import com.endava.ai.core.reporting.aggregate.model.AggregateTimingSummary;

import java.util.Locale;

final class AggregateExtentSummaryNodeTimingReconciler {
    private static final String TEST_LIST_NAME_PREFIX = "<p class=\"name\">";
    private static final String TEST_LIST_NAME_SUFFIX = "</p>";
    private static final String DETAIL_BODY_PREFIX = "<div class=\"detail-body";
    private static final int FALLBACK_SECTION_LENGTH = 2_000;
    private static final long MINIMUM_VISIBLE_DURATION_MILLIS = 1L;
    private static final String ZERO_DURATION_WITH_HOURS = "00:00:00:000";
    private static final String ZERO_DURATION_WITHOUT_HOURS = "00:00:000";
    private static final long MILLIS_PER_HOUR = 3_600_000L;
    private static final long MILLIS_PER_MINUTE = 60_000L;
    private static final long MILLIS_PER_SECOND = 1_000L;

    private AggregateExtentSummaryNodeTimingReconciler() {
    }

    static String reconcile(String html, AggregateReportDescriptor descriptor, AggregateTimingSummary timing) {
        if (html == null || descriptor == null || timing == null || !timing.observed()) {
            return html;
        }

        int titleIndex = html.indexOf(summaryTitleToken(descriptor));
        if (titleIndex < 0) {
            return html;
        }

        int sectionEnd = summaryHeaderEnd(html, titleIndex);
        String section = html.substring(titleIndex, sectionEnd);
        String duration = sparkDuration(Math.max(MINIMUM_VISIBLE_DURATION_MILLIS, timing.overviewDurationMillis()));
        String updated = section
                .replace(ZERO_DURATION_WITH_HOURS, duration)
                .replace(ZERO_DURATION_WITHOUT_HOURS, duration);

        return updated.equals(section)
                ? html
                : html.substring(0, titleIndex) + updated + html.substring(sectionEnd);
    }

    private static String summaryTitleToken(AggregateReportDescriptor descriptor) {
        return TEST_LIST_NAME_PREFIX + descriptor.testTitle() + TEST_LIST_NAME_SUFFIX;
    }

    private static int summaryHeaderEnd(String html, int titleIndex) {
        int detailBodyIndex = html.indexOf(DETAIL_BODY_PREFIX, titleIndex);
        return detailBodyIndex > titleIndex
                ? detailBodyIndex
                : Math.min(html.length(), titleIndex + FALLBACK_SECTION_LENGTH);
    }

    private static String sparkDuration(long millis) {
        long hours = millis / MILLIS_PER_HOUR;
        long remaining = millis % MILLIS_PER_HOUR;
        long minutes = remaining / MILLIS_PER_MINUTE;
        remaining %= MILLIS_PER_MINUTE;
        long seconds = remaining / MILLIS_PER_SECOND;
        long milliseconds = remaining % MILLIS_PER_SECOND;
        return String.format(Locale.ROOT, "%02d:%02d:%02d:%03d", hours, minutes, seconds, milliseconds);
    }
}
