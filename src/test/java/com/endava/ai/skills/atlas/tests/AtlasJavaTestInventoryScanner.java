package com.endava.ai.skills.atlas.tests;

import org.testng.Assert;

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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

final class AtlasJavaTestInventoryScanner {
    private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;");
    private static final Pattern CLASS = Pattern.compile("\\bclass\\s+(\\w+)\\b");
    private static final Pattern TEST_METHOD = Pattern.compile(
            "\\b(?:public|protected|private)?\\s*(?:final\\s+)?void\\s+(\\w+)\\s*\\("
    );

    List<AtlasUiCase> scan(AtlasUiScope scope) throws IOException {
        Path testsRoot = scope.testsRoot();
        Assert.assertTrue(Files.isDirectory(testsRoot), "Missing UI tests root: " + testsRoot);

        List<AtlasUiCase> cases = new ArrayList<>();
        try (Stream<Path> files = Files.walk(testsRoot)) {
            for (Path file : files
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList())) {
                cases.addAll(scanFile(scope, file, cases.size()));
            }
        }
        return cases;
    }

    private List<AtlasUiCase> scanFile(AtlasUiScope scope, Path file, int firstSequence) throws IOException {
        JavaSourceIdentity identity = JavaSourceIdentity.from(file, Files.readAllLines(file));
        List<AtlasUiCase> cases = new ArrayList<>();
        boolean nextMethodIsTest = false;
        for (String line : identity.lines) {
            if (line.contains("@Test")) {
                nextMethodIsTest = true;
                continue;
            }
            if (!nextMethodIsTest) {
                continue;
            }
            Matcher methodMatcher = TEST_METHOD.matcher(line);
            if (methodMatcher.find()) {
                cases.add(AtlasUiCase.sourceInventory(
                        scope,
                        identity.fullClassName(),
                        methodMatcher.group(1),
                        firstSequence + cases.size()
                ));
                nextMethodIsTest = false;
            }
        }
        return cases;
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class JavaSourceIdentity {
        private final Path file;
        private final List<String> lines;
        private final String packageName;
        private final String className;

        static JavaSourceIdentity from(Path file, List<String> lines) {
            String packageName = "";
            String className = file.getFileName().toString().replace(".java", "");
            for (String line : lines) {
                Matcher packageMatcher = PACKAGE.matcher(line);
                if (packageMatcher.find()) {
                    packageName = packageMatcher.group(1);
                }
                Matcher classMatcher = CLASS.matcher(line);
                if (classMatcher.find()) {
                    className = classMatcher.group(1);
                }
            }
            return new JavaSourceIdentity(file, lines, packageName, className);
        }

        String fullClassName() {
            return packageName.isBlank() ? className : packageName + "." + className;
        }
    }
}
