package com.endava.ai.core.reporting.listener.internal.runtime.model;

import org.testng.ITestResult;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ExtentRuntimeTags {
    private static final String ATF_CONTRACT_SUITE = "com.endava.ai.core.support.contract.AtfContractSuite";
    private static final String ATF_CONTRACT = "com.endava.ai.core.support.contract.AtfContract";

    private ExtentRuntimeTags() {
    }

    public static List<String> from(ITestResult result, Class<?> realClass, String className) {
        Set<String> tags = new LinkedHashSet<>();
        addPackageTags(tags, className);
        addTestGroups(tags, result);
        addContractTags(tags, realClass);
        return List.copyOf(tags);
    }

    private static void addPackageTags(Set<String> tags, String className) {
        if (className.contains(".atf.core.")) {
            tags.add("ATF_CORE");
        }
        if (className.contains(".atf.ui.")) {
            tags.add("ATF_UI");
        }
        if (className.contains(".atf.api.")) {
            tags.add("ATF_API");
        }
        if (className.contains(".atf.reporting.")) {
            tags.add("ATF_REPORTING");
        }
    }

    private static void addTestGroups(Set<String> tags, ITestResult result) {
        if (result.getMethod() == null || result.getMethod().getGroups() == null) {
            return;
        }
        Arrays.stream(result.getMethod().getGroups())
                .filter(group -> group != null && !group.isBlank())
                .map(group -> "GROUP:" + group)
                .forEach(tags::add);
    }

    private static void addContractTags(Set<String> tags, Class<?> realClass) {
        if (realClass == null) {
            return;
        }
        for (Annotation annotation : realClass.getAnnotations()) {
            String annotationName = annotation.annotationType().getName();
            if (ATF_CONTRACT_SUITE.equals(annotationName)) {
                tags.add("ATF_CONTRACT_SUITE");
                readAnnotationMember(annotation, "profile").ifPresent(value -> tags.add("PROFILE:" + value));
                readAnnotationMember(annotation, "area").ifPresent(value -> tags.add("AREA:" + value));
            }
            if (ATF_CONTRACT.equals(annotationName)) {
                tags.add("ATF_CONTRACT");
            }
        }
    }

    private static java.util.Optional<String> readAnnotationMember(Annotation annotation, String member) {
        try {
            Object value = annotation.annotationType().getMethod(member).invoke(annotation);
            return value == null ? java.util.Optional.empty() : java.util.Optional.of(value.toString());
        } catch (ReflectiveOperationException unavailable) {
            return java.util.Optional.empty();
        }
    }
}
