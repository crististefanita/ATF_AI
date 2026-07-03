package com.endava.ai.skills.atlas.rules.business.trace;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasSourceInventory;
import com.endava.ai.skills.atlas.rules.AtlasText;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasSourceTraceBackedBySourceRule implements AtlasValidationRule {
    private static final Pattern METHOD_TOKEN      = Pattern.compile("\\b([a-z][A-Za-z0-9_]*)\\(\\)");
    private static final Pattern CODE_TOKEN        = Pattern.compile("<code>([^<]+)</code>");
    private static final Pattern CLASS_TOKEN       = Pattern.compile("^[A-Z][A-Za-z0-9_]+$");
    private static final Pattern JAVA_FILE_COUNT   = Pattern.compile("(?i)\\b(\\d{1,4})\\s+(?:fisiere|files)\\s+java\\b");
    private static final Pattern TEST_METHOD_COUNT = Pattern.compile("(?i)\\b(\\d{1,4})\\s+(?:metode\\s+@test|@test\\s+methods|metode\\s+de\\s+test|test\\s+methods)\\b");

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
        String section = AtlasText.sectionAfter(context.atlasText(), "Source-to-Business Trace", 14_000);
        if (section.isBlank()) {
            result.add(id(), "Missing Source-to-Business Trace section", context.atlasHtml(),
                    "Add source-backed trace rows before claiming business comprehension.");
            return;
        }
        AtlasSourceInventory source = AtlasSourceInventory.load(context);
        validateDeclaredSourceCounts(context, result, source);
        validateMethods(context, result, section, source);
        validateClasses(context, result, section, source);
    }

    private void validateDeclaredSourceCounts(AtlasContext context,
                                              AtlasValidationResult result,
                                              AtlasSourceInventory source) {
        validateDeclaredCount(context, result, JAVA_FILE_COUNT, source.sourceJavaFileCount(),
                "Atlas declared Java file count does not match analyzed test package",
                "Keep source-count pills and scope rows mechanically tied to the input test package, not manually guessed.");
        validateDeclaredCount(context, result, TEST_METHOD_COUNT, source.sourceTestAnnotationCount(),
                "Atlas declared @Test method count does not match analyzed test package",
                "Keep test method count mechanically tied to the input test package so atlas scope stays auditable.");
    }

    private void validateMethods(AtlasContext context,
                                 AtlasValidationResult result,
                                 String section,
                                 AtlasSourceInventory source) {
        Set<String> methods = extractMethods(section);
        if (methods.isEmpty()) {
            result.add(id(), "Source-to-Business Trace does not name concrete method tokens", context.atlasHtml(),
                    "Use representative source methods, not only class families or business prose.");
            return;
        }
        for (String method : methods) {
            if (!source.containsMethod(method)) {
                result.add(id(), "Trace method is not found in analyzed source: " + method + "()",
                        context.atlasHtml(),
                        "Keep atlas trace rows source-backed: every methodName() mentioned must exist in test or UI source.");
            }
        }
    }

    private void validateClasses(AtlasContext context,
                                 AtlasValidationResult result,
                                 String section,
                                 AtlasSourceInventory source) {
        Set<String> classes = extractConcreteCodeClasses(section);
        if (classes.size() < 6) {
            result.add(id(), "Source-to-Business Trace has too few concrete class/layer tokens: " + classes.size(),
                    context.atlasHtml(),
                    "Name concrete source classes from tests and UI layers, not only wildcard families.");
        }
        for (String className : classes) {
            if (!source.containsClass(className)) {
                result.add(id(), "Trace class/layer token is not found in source: " + className,
                        context.atlasHtml(),
                        "Every concrete class in the atlas trace should exist in src/test or src/main UI layers.");
            }
        }
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

    private void validateDeclaredCount(AtlasContext context,
                                       AtlasValidationResult result,
                                       Pattern pattern,
                                       int expected,
                                       String message,
                                       String repair) {
        Set<Integer> declared = new LinkedHashSet<>();
        Matcher matcher = pattern.matcher(context.atlasText());
        while (matcher.find()) {
            declared.add(Integer.parseInt(matcher.group(1)));
        }
        if (declared.isEmpty()) {
            result.add(id(),
                    message + ": no declared value",
                    context.atlasHtml(),
                    repair);
            return;
        }
        if (!declared.contains(expected)) {
            result.add(id(),
                    message + ": declared=" + declared + ", actual=" + expected,
                    context.atlasHtml(),
                    repair);
        }
    }
}
