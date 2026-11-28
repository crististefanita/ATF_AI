package com.ai.atf.core.reporting.failurefixtures;

import com.ai.core.support.contract.ids.AtfSourcePaths;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

public final class FailureFixtureReferencePaths {
    public static final String ACTIONABLE_REASON = "Move bulky @AtfContract/@AtfContractSuite narratives ";
    public static final String FIRST_REFERENCE_TITLE = "ConfigManagerFileContractTest.java @AtfContractRef";
    public static final String REPEATED_SOURCE_PREFIX = "src\\test\\java\\com\\atf\\ai";
    public static final String DIAGNOSTIC_SHELL_REFERENCE = AtfSourcePaths.testJava(
            "com.ai.atf.core.reporting.regression.overview.ExtentPublishedOverviewInspectorContractTest")
            .toString().replace('/', '\\') + " @AtfContractRef";
    private static final String REFERENCE_SEPARATOR = ", ";

    private FailureFixtureReferencePaths() {
    }

    static String refs(SourceReference... references) {
        return Arrays.stream(references)
                .map(SourceReference::format)
                .collect(Collectors.joining(REFERENCE_SEPARATOR));
    }

    static SourceReference ref(Class<?> testClass, Class<?> annotationType) {
        return new SourceReference(sourcePath(testClass), "@" + annotationType.getSimpleName());
    }

    private static String sourcePath(Class<?> testClass) {
        return AtfSourcePaths.testJava(testClass).toString().replace('/', '\\');
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    static final class SourceReference {
        private final String sourcePath;
        private final String marker;

        String format() {
            return sourcePath + " " + marker;
        }
    }
}
