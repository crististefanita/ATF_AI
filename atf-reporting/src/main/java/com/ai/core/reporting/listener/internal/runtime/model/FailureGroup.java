package com.ai.core.reporting.listener.internal.runtime.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class FailureGroup {
    private final String className;
    private final String packageName;
    private final List<ExtentRuntimeInvocation> invocations;

    private FailureGroup(String className, String packageName, List<ExtentRuntimeInvocation> invocations) {
        this.className = className;
        this.packageName = packageName;
        this.invocations = List.copyOf(invocations);
    }

    public static List<FailureGroup> from(List<ExtentRuntimeInvocation> failures) {
        return failures.stream()
                .collect(Collectors.groupingBy(
                        invocation -> ExtentRuntimeHtml.simpleClassName(invocation.className),
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> new FailureGroup(
                        entry.getKey(),
                        packageName(entry.getValue()),
                        entry.getValue()
                ))
                .collect(Collectors.toList());
    }

    public int failedCount() {
        return invocations.size();
    }

    private static String packageName(List<ExtentRuntimeInvocation> invocations) {
        return invocations.stream()
                .map(invocation -> ExtentRuntimeHtml.packageName(invocation.className))
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse("");
    }
}
