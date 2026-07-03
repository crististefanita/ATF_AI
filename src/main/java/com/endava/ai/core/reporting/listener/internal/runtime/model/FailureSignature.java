package com.endava.ai.core.reporting.listener.internal.runtime.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class FailureSignature {
    private final String summary;
    private final List<ExtentRuntimeInvocation> invocations;

    private FailureSignature(String summary, List<ExtentRuntimeInvocation> invocations) {
        this.summary = summary;
        this.invocations = List.copyOf(invocations);
    }

    public static List<FailureSignature> from(List<ExtentRuntimeInvocation> failures) {
        return failures.stream()
                .collect(Collectors.groupingBy(
                        ExtentRuntimeInvocation::failureSummary,
                        LinkedHashMap::new,
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> new FailureSignature(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public String displaySummary() {
        return FailureSummaryPreview.from(summary);
    }

    public int invocationCount() {
        return invocations.size();
    }

    public String severityLabel() {
        return invocationCount() == 1 ? "UNIQUE FAILURE" : "REPEATED ROOT CAUSE";
    }

    public String invocationLabel() {
        return invocationCount() == 1 ? "1 test case" : invocationCount() + " test cases";
    }

    public String scopeLabel() {
        long classCount = invocations.stream()
                .map(invocation -> ExtentRuntimeHtml.simpleClassName(invocation.className))
                .distinct()
                .count();
        return classCount == 1
                ? "Affects 1 Java test class"
                : "Affects " + classCount + " Java test classes";
    }

    public String methodCountLabel() {
        long methodCount = invocations.stream()
                .map(invocation -> invocation.methodName)
                .distinct()
                .count();
        return methodCount == 1 ? "1 affected method" : methodCount + " affected methods";
    }

    public Map<String, Set<String>> methodsByClass() {
        return invocations.stream()
                .collect(Collectors.groupingBy(
                        invocation -> ExtentRuntimeHtml.simpleClassName(invocation.className),
                        LinkedHashMap::new,
                        Collectors.mapping(invocation -> invocation.methodName, Collectors.toCollection(LinkedHashSet::new))
                ));
    }
}
