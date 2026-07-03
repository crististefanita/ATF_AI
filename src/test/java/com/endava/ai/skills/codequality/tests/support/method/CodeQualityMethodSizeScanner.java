package com.endava.ai.skills.codequality.tests.support.method;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CodeQualityMethodSizeScanner {
    private static final Pattern METHOD_DECLARATION = Pattern.compile(
            "\\b(public|protected|private)\\s+(?:static\\s+)?(?:final\\s+)?[\\w<>\\[\\], ?]+\\s+(\\w+)\\s*\\([^;]*\\)\\s*\\{");
    private static final Pattern CONSTRUCTOR_DECLARATION = Pattern.compile(
            "\\b(public|protected|private)\\s+(\\w+)\\s*\\([^;]*\\)\\s*\\{");

    private CodeQualityMethodSizeScanner() {
    }

    public static List<CodeQualityMethodSize> methodSizes(List<Path> sourceRoots) throws IOException {
        List<CodeQualityMethodSize> methods = new ArrayList<>();
        for (Path sourceRoot : sourceRoots) {
            try (Stream<Path> files = Files.walk(sourceRoot)) {
                for (Path source : javaSources(files)) {
                    methods.addAll(sourceMethods(source));
                }
            }
        }
        return methods.stream()
                .sorted(Comparator.comparingInt(CodeQualityMethodSize::getLines).reversed()
                        .thenComparing(CodeQualityMethodSize::getSource)
                        .thenComparing(CodeQualityMethodSize::getStartLine))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<Path> javaSources(Stream<Path> files) {
        return files.filter(Files::isRegularFile)
                .filter(AtfSourcePaths::isJavaSource)
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<CodeQualityMethodSize> sourceMethods(Path source) throws IOException {
        List<String> lines = Files.readAllLines(source);
        List<CodeQualityMethodSize> methods = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            String method = methodName(lines.get(index));
            if (!method.isEmpty()) {
                int methodLines = countBlockLines(lines, index);
                methods.add(new CodeQualityMethodSize(
                        AtfSourcePaths.projectRelative(source),
                        method,
                        index + 1,
                        methodLines));
                index += Math.max(methodLines - 1, 0);
            }
        }
        return methods;
    }

    private static String methodName(String line) {
        String normalized = line.strip();
        Matcher method = METHOD_DECLARATION.matcher(normalized);
        if (method.find() && !"if".equals(method.group(2))) {
            return method.group(2);
        }
        Matcher constructor = CONSTRUCTOR_DECLARATION.matcher(normalized);
        return constructor.find() ? constructor.group(2) : "";
    }

    private static int countBlockLines(List<String> lines, int startIndex) {
        int depth = 0;
        for (int index = startIndex; index < lines.size(); index++) {
            depth += braceDelta(lines.get(index));
            if (depth == 0 && index > startIndex) {
                return index - startIndex + 1;
            }
        }
        return 1;
    }

    private static int braceDelta(String line) {
        int delta = 0;
        for (char character : line.toCharArray()) {
            if (character == '{') {
                delta++;
            } else if (character == '}') {
                delta--;
            }
        }
        return delta;
    }
}
