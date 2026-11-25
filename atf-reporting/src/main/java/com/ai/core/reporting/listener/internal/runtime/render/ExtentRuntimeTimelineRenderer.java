package com.ai.core.reporting.listener.internal.runtime.render;

import com.ai.core.reporting.listener.internal.runtime.model.ExtentRuntimeHtml;
import com.ai.core.reporting.listener.internal.runtime.model.ExtentRuntimeInvocation;

import com.ai.core.reporting.listener.internal.runtime.support.ExtentRuntimePercentages;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExtentRuntimeTimelineRenderer {

    private ExtentRuntimeTimelineRenderer() {
    }

    public static String section(List<ExtentRuntimeInvocation> invocations) {
        StringBuilder html = new StringBuilder()
                .append("<div class=\"row atf-runtime-timeline-row\"><div class=\"col-md-12\">")
                .append("<div class=\"card atf-runtime-timeline-card\"><div class=\"card-header\">")
                .append("<p>Timeline</p>")
                .append("</div><div class=\"card-body\">")
                .append("<div class=\"atf-runtime-timeline-track\">");

        List<TimelineClass> ordered = timelineClasses(invocations);
        Map<String, Integer> widths = ExtentRuntimePercentages.exactIntegerShares(
                timelineClassCounts(ordered),
                ordered.stream().mapToInt(timelineClass -> timelineClass.total).sum()
        );
        int index = 0;
        for (TimelineClass timelineClass : ordered) {
            int timelineWidth = widths.getOrDefault(String.valueOf(index), 0);
            String tooltip = timelineClass.tooltip();
            html.append("<span class=\"atf-runtime-timeline-segment atf-runtime-timeline-segment-")
                    .append(index % 8)
                    .append("\" style=\"width:")
                    .append(timelineWidth)
                    .append("%\" tabindex=\"0\" title=\"")
                    .append(ExtentRuntimeHtml.escape(tooltip))
                    .append("\" data-atf-runtime-tooltip=\"")
                    .append(ExtentRuntimeHtml.escape(tooltip))
                    .append("\"></span>");
            index++;
        }

        return html.append("</div><div class=\"atf-runtime-timeline-axis\">")
                .append("<span>0</span><span>")
                .append(ordered.size())
                .append("</span></div></div></div></div></div>").toString();
    }

    private static List<TimelineClass> timelineClasses(List<ExtentRuntimeInvocation> invocations) {
        Map<String, TimelineClass> byClass = new LinkedHashMap<>();
        invocations.stream()
                .sorted(Comparator.comparingLong(ExtentRuntimeInvocation::startMillis))
                .forEach(invocation -> byClass
                        .computeIfAbsent(invocation.className, TimelineClass::new)
                        .record(invocation));
        return new ArrayList<>(byClass.values());
    }

    private static Map<String, Integer> timelineClassCounts(List<TimelineClass> classes) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (int index = 0; index < classes.size(); index++) {
            counts.put(String.valueOf(index), classes.get(index).total);
        }
        return counts;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TimelineClass {
        private final String className;
        private int total;
        private int passed;
        private int failed;
        private int skipped;

        private void record(ExtentRuntimeInvocation invocation) {
            total++;
            if ("PASSED".equalsIgnoreCase(invocation.status)) {
                passed++;
            } else if ("FAILED".equalsIgnoreCase(invocation.status)) {
                failed++;
            } else if ("SKIPPED".equalsIgnoreCase(invocation.status)) {
                skipped++;
            }
        }

        private String tooltip() {
            return ExtentRuntimeHtml.simpleClassName(className) + ": " + total + " test(s), "
                    + passed + " passed, " + failed + " failed, " + skipped + " skipped";
        }
    }
}
