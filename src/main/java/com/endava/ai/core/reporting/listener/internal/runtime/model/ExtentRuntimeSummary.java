package com.endava.ai.core.reporting.listener.internal.runtime.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExtentRuntimeSummary {
    public final int total;
    public final long passed;
    public final long failed;
    public final long skipped;
    public final int testClasses;
    public final Map<String, Integer> classCounts;
    public final Map<String, Integer> packageCounts;
    public final Map<String, Integer> annotationCounts;
    public final List<ExtentRuntimeInvocation> failures;

    public static ExtentRuntimeSummary from(List<ExtentRuntimeInvocation> invocations) {
        Set<String> classes = invocations.stream()
                .map(invocation -> invocation.className)
                .filter(className -> className != null && !className.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, Integer> classCounts = countBy(
                invocations,
                invocation -> ExtentRuntimeHtml.simpleClassName(invocation.className)
        );
        Map<String, Integer> packageCounts = countBy(
                invocations,
                invocation -> ExtentRuntimeHtml.packageName(invocation.className)
        );
        Map<String, Integer> annotationCounts = new LinkedHashMap<>();
        invocations.stream()
                .flatMap(invocation -> invocation.annotationTags.stream())
                .forEach(tag -> annotationCounts.merge(tag, 1, Integer::sum));
        List<ExtentRuntimeInvocation> failures = invocations.stream()
                .filter(invocation -> "FAILED".equals(invocation.status))
                .collect(Collectors.toList());
        return new ExtentRuntimeSummary(
                invocations.size(),
                invocations.stream().filter(invocation -> "PASSED".equals(invocation.status)).count(),
                invocations.stream().filter(invocation -> "FAILED".equals(invocation.status)).count(),
                invocations.stream().filter(invocation -> "SKIPPED".equals(invocation.status)).count(),
                classes.size(),
                classCounts,
                packageCounts,
                annotationCounts,
                failures
        );
    }

    public String verdictLabel() {
        return failed == 0 && skipped == 0 ? "PASS" : "CHECK";
    }

    public String verdictText() {
        return failed == 0 && skipped == 0
                ? "PASS - all observed ATF-core invocations are green."
                : "CHECK - failed or skipped ATF-core invocations exist.";
    }

    private static <T> Map<String, Integer> countBy(List<T> values, Function<T, String> classifier) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        values.stream()
                .map(classifier)
                .filter(value -> value != null && !value.isBlank())
                .forEach(value -> counts.merge(value, 1, Integer::sum));
        return counts;
    }
}
