package com.ai.core.reporting.adapters.extent.synthetic;

import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SyntheticRuntimeInvocation {
    private final String status;
    private final String className;
    private final String methodName;
    private final long startMillis;
    private final String failureDetail;
    private final List<String> annotationTags;

    private SyntheticRuntimeInvocation(
            String status,
            String className,
            String methodName,
            long startMillis,
            String failureDetail,
            List<String> annotationTags
    ) {
        this.status = status == null ? "" : status;
        this.className = className == null ? "" : className;
        this.methodName = methodName == null ? "" : methodName;
        this.startMillis = startMillis;
        this.failureDetail = failureDetail == null ? "" : failureDetail;
        this.annotationTags = annotationTags == null ? List.of() : List.copyOf(annotationTags);
    }

    public static SyntheticRuntimeInvocation passed(String className, String methodName, long startMillis) {
        return of("PASSED", className, methodName, startMillis, "", List.of());
    }

    public static SyntheticRuntimeInvocation skipped(String className, String methodName, long startMillis) {
        return of("SKIPPED", className, methodName, startMillis, "", List.of());
    }

    public static SyntheticRuntimeInvocation failed(
            String className,
            String methodName,
            long startMillis,
            String failureDetail
    ) {
        return of("FAILED", className, methodName, startMillis, assertionFailureDetail(className, methodName, failureDetail), List.of());
    }

    public static SyntheticRuntimeInvocation of(
            String status,
            String className,
            String methodName,
            long startMillis,
            String failureDetail,
            List<String> annotationTags
    ) {
        return new SyntheticRuntimeInvocation(
                status,
                className,
                methodName,
                startMillis,
                failureDetail,
                annotationTagsWithPackageTags(className, annotationTags)
        );
    }

    ExtentRuntimeAuditRegistry.RuntimeAuditInvocation toRuntimeAuditInvocation() {
        return ExtentRuntimeAuditRegistry.RuntimeAuditInvocation.of(
                status,
                className,
                methodName,
                startMillis,
                failureDetail,
                annotationTags
        );
    }

    private static List<String> annotationTagsWithPackageTags(String className, List<String> explicitTags) {
        Set<String> tags = new LinkedHashSet<>();
        String normalizedClassName = className == null ? "" : className;
        if (normalizedClassName.contains(".atf.core.")) {
            tags.add("ATF_CORE");
        }
        if (normalizedClassName.contains(".atf.ui.")) {
            tags.add("ATF_UI");
        }
        if (normalizedClassName.contains(".atf.api.")) {
            tags.add("ATF_API");
        }
        if (normalizedClassName.contains(".atf.reporting.")) {
            tags.add("ATF_REPORTING");
        }
        if (explicitTags != null) {
            tags.addAll(explicitTags);
        }
        return new ArrayList<>(tags);
    }

    private static String assertionFailureDetail(String className, String methodName, String message) {
        String summary = message == null || message.isBlank() ? "synthetic failure" : message;
        if (!summary.contains(":")) {
            summary = "AssertionError: " + summary;
        }
        return "Test reference: " + className + "#" + methodName
                + System.lineSeparator()
                + System.lineSeparator()
                + "Failure chain:"
                + System.lineSeparator()
                + "- " + summary
                + System.lineSeparator()
                + System.lineSeparator()
                + "Root cause detail:"
                + System.lineSeparator()
                + summary;
    }
}
