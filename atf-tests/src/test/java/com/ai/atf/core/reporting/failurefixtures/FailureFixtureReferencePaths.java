package com.ai.atf.core.reporting.failurefixtures;

import com.ai.core.support.contract.ids.AtfSourcePaths;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

final class FailureFixtureReferencePaths {
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
