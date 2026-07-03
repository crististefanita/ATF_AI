package com.ai.skills.atlas.rules;

import com.ai.skills.atlas.AtlasContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class AtlasSourceInventory {
    private static final Pattern TEST_ANNOTATION = Pattern.compile("@Test\\b");
    private static final Pattern METHOD_NAME = Pattern.compile("\\b(?:public|protected|private)?\\s*(?:static\\s+)?[A-Za-z0-9_<>, ?\\[\\]]+\\s+([a-z][A-Za-z0-9_]*)\\s*\\(");

    private final String text;
    @Getter
    private final Set<String> classNames;
    @Getter
    private final Set<String> testClassNames;
    @Getter
    private final Set<String> methodNames;
    @Getter
    private final int sourceJavaFileCount;
    @Getter
    private final int sourceTestAnnotationCount;

    private AtlasSourceInventory(String text,
                                 Set<String> classNames,
                                 Set<String> testClassNames,
                                 Set<String> methodNames,
                                 int sourceJavaFileCount,
                                 int sourceTestAnnotationCount) {
        this.text = text;
        this.classNames = Set.copyOf(classNames);
        this.testClassNames = Set.copyOf(testClassNames);
        this.methodNames = Set.copyOf(methodNames);
        this.sourceJavaFileCount = sourceJavaFileCount;
        this.sourceTestAnnotationCount = sourceTestAnnotationCount;
    }

    public static AtlasSourceInventory load(AtlasContext context) {
        try {
            Set<Path> testFiles = javaFiles(context.sourceRoot());
            Set<Path> uiFiles = javaFiles(context.uiSourceRoot());
            Set<Path> allFiles = new LinkedHashSet<>();
            allFiles.addAll(testFiles);
            allFiles.addAll(uiFiles);

            String allText = allFiles.stream()
                    .map(AtlasSourceInventory::read)
                    .collect(Collectors.joining(System.lineSeparator()));
            String testText = testFiles.stream()
                    .map(AtlasSourceInventory::read)
                    .collect(Collectors.joining(System.lineSeparator()));
            Set<String> names = allFiles.stream()
                    .map(path -> path.getFileName().toString().replaceFirst("\\.java$", ""))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            Set<String> testNames = testFiles.stream()
                    .map(path -> path.getFileName().toString().replaceFirst("\\.java$", ""))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            Set<String> methodNames = extractMethodNames(allText);
            int testAnnotationCount = testFiles.stream()
                    .map(AtlasSourceInventory::read)
                    .mapToInt(text -> AtlasText.count(TEST_ANNOTATION, text))
                    .sum();

            return new AtlasSourceInventory(allText, names, testNames, methodNames, testFiles.size(), testAnnotationCount);
        } catch (IOException exception) {
            return new AtlasSourceInventory("", Set.of(), Set.of(), Set.of(), 0, 0);
        }
    }

    public boolean containsMethod(String methodName) {
        return text.contains(methodName + "(");
    }

    public boolean containsClass(String className) {
        return classNames.contains(className) || text.contains("class " + className);
    }

    public boolean containsSourceToken(String token) {
        return containsMethod(token) || containsClass(token);
    }

    private static Set<Path> javaFiles(Path root) throws IOException {
        if (!Files.exists(root)) {
            return Set.of();
        }
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            return "";
        }
    }

    private static Set<String> extractMethodNames(String javaText) {
        Matcher matcher = METHOD_NAME.matcher(javaText);
        Set<String> methods = new LinkedHashSet<>();
        while (matcher.find()) {
            methods.add(matcher.group(1));
        }
        return methods;
    }
}
