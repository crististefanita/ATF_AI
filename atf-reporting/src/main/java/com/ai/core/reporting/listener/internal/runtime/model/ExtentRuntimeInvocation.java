package com.ai.core.reporting.listener.internal.runtime.model;

import java.util.List;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class ExtentRuntimeInvocation {
    public final String status;
    public final String className;
    public final String methodName;
    public final long startMillis;
    public final String failureDetail;
    public final List<String> annotationTags;

    public ExtentRuntimeInvocation(
            String status,
            String className,
            String methodName,
            long startMillis,
            String failureDetail,
            List<String> annotationTags
    ) {
        this.status = normalize(status);
        this.className = normalize(className);
        this.methodName = normalize(methodName);
        this.startMillis = startMillis;
        this.failureDetail = normalize(failureDetail);
        this.annotationTags = annotationTags == null ? List.of() : List.copyOf(annotationTags);
    }

    public String line() {
        return status + " | " + className + "#" + methodName;
    }

    public boolean hasFailureDetail() {
        return !failureDetail.isBlank();
    }

    public String failureSummary() {
        if (!hasFailureDetail()) {
            return "Failure detail unavailable";
        }
        return failureDetail.lines()
                .filter(line -> !line.startsWith("Test reference:"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> line.startsWith("- ") || line.startsWith("caused by: "))
                .findFirst()
                .map(ExtentRuntimeInvocation::stripSummaryBullet)
                .orElse("Full failure details");
    }

    public String failureDisplaySummary() {
        return FailureSummaryPreview.from(failureSummary());
    }

    private static String stripSummaryBullet(String line) {
        return line.startsWith("- ") ? line.substring(2) : line;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
