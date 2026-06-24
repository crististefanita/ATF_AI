package com.endava.ai.atlas.rules.business;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;
import com.endava.ai.atlas.rules.AtlasValidationRule;

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

public final class AtlasSourceTraceBackedBySourceRule implements AtlasValidationRule {
    private static final Pattern METHOD_TOKEN = Pattern.compile("\\b([a-z][A-Za-z0-9_]*)\\(\\)");
    private static final Pattern CODE_TOKEN   = Pattern.compile("<code>([^<]+)</code>");
    private static final Pattern CLASS_TOKEN  = Pattern.compile("^[A-Z][A-Za-z0-9_]+$");

    @Override
    public String id() {
        return "atlas.candidate.source-trace.backed-by-source";
    }

    @Override
    public String expected() {
        return "Source-to-Business Trace names concrete methods/classes that exist in the analyzed test package or traversed UI layers.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String section = sectionAfter(context.atlasText(), "Source-to-Business Trace", 14_000);
        if (section.isBlank()) {
            result.add(id(), "Missing Source-to-Business Trace section", context.atlasHtml(),
                    "Add source-backed trace rows before claiming business comprehension.");
            return;
        }
        SourceCorpus corpus = SourceCorpus.load(context);
        validateMethods(context, result, section, corpus);
        validateClasses(context, result, section, corpus);
    }

    private void validateMethods(AtlasContext context,
                                 AtlasValidationResult result,
                                 String section,
                                 SourceCorpus corpus) {
        Set<String> methods = extractMethods(section);
        if (methods.isEmpty()) {
            result.add(id(), "Source-to-Business Trace does not name concrete method tokens", context.atlasHtml(),
                    "Use representative source methods, not only class families or business prose.");
            return;
        }
        for (String method : methods) {
            if (!corpus.containsMethod(method)) {
                result.add(id(), "Trace method is not found in analyzed source: " + method + "()",
                        context.atlasHtml(),
                        "Keep atlas trace rows source-backed: every methodName() mentioned must exist in test or UI source.");
            }
        }
    }

    private void validateClasses(AtlasContext context,
                                 AtlasValidationResult result,
                                 String section,
                                 SourceCorpus corpus) {
        Set<String> classes = extractConcreteCodeClasses(section);
        if (classes.size() < 6) {
            result.add(id(), "Source-to-Business Trace has too few concrete class/layer tokens: " + classes.size(),
                    context.atlasHtml(),
                    "Name concrete source classes from tests and UI layers, not only wildcard families.");
        }
        for (String className : classes) {
            if (!corpus.containsClass(className)) {
                result.add(id(), "Trace class/layer token is not found in source: " + className,
                        context.atlasHtml(),
                        "Every concrete class in the atlas trace should exist in src/test or src/main UI layers.");
            }
        }
    }

    private static String sectionAfter(String html, String marker, int maxChars) {
        int index = html.indexOf(marker);
        if (index < 0) {
            return "";
        }
        return html.substring(index, Math.min(html.length(), index + maxChars));
    }

    private static Set<String> extractMethods(String section) {
        Matcher matcher = METHOD_TOKEN.matcher(section);
        Set<String> methods = new LinkedHashSet<>();
        while (matcher.find()) {
            methods.add(matcher.group(1));
        }
        return methods;
    }

    private static Set<String> extractConcreteCodeClasses(String section) {
        Matcher matcher = CODE_TOKEN.matcher(section);
        Set<String> classes = new LinkedHashSet<>();
        while (matcher.find()) {
            String token = matcher.group(1).trim();
            if (token.contains(".") || token.contains("*") || token.contains("..") || token.endsWith("()")) {
                continue;
            }
            if (CLASS_TOKEN.matcher(token).matches()) {
                classes.add(token);
            }
        }
        return classes;
    }

    private static final class SourceCorpus {
        private final String text;
        private final Set<String> classNames;

        private SourceCorpus(String text, Set<String> classNames) {
            this.text = text;
            this.classNames = classNames;
        }

        static SourceCorpus load(AtlasContext context) {
            try {
                Set<Path> files = new LinkedHashSet<>();
                files.addAll(javaFiles(context.sourceRoot()));
                files.addAll(javaFiles(context.uiSourceRoot()));
                String allText = files.stream()
                        .map(SourceCorpus::read)
                        .collect(Collectors.joining(System.lineSeparator()));
                Set<String> names = files.stream()
                        .map(path -> path.getFileName().toString().replaceFirst("\\.java$", ""))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                return new SourceCorpus(allText, names);
            } catch (IOException exception) {
                return new SourceCorpus("", Set.of());
            }
        }

        boolean containsMethod(String methodName) {
            return text.contains(methodName + "(");
        }

        boolean containsClass(String className) {
            return classNames.contains(className) || text.contains("class " + className);
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
    }
}
