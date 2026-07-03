package com.endava.ai.skills.mcp.rules;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.McpRuntimeContract;
import com.endava.ai.skills.mcp.context.McpPackageContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class McpRuleSupport {
    private McpRuleSupport() {
    }

    public static void requireText(String content, Path source, McpPackageValidationResult result,
                            String code, String message, String... expectedAny) {
        for (String expected : expectedAny) {
            if (content.contains(expected)) {
                return;
            }
        }
        result.add(McpPackageViolation.blocker(code, message + ": " + String.join(" or ", expectedAny), source));
    }

    public static void requireNumericValue(String content, Path source, McpPackageValidationResult result,
                                    String code, String message, int expectedValue, String... acceptedKeys) {
        for (String key : acceptedKeys) {
            Integer actual = extractIntValue(content, key);
            if (actual != null && actual == expectedValue) {
                return;
            }
        }
        result.add(McpPackageViolation.blocker(code,
                message + ": key=" + String.join(" or ", acceptedKeys) + ", expected=" + expectedValue,
                source));
    }

    public static void requireRuntimePair(String content, Path source, McpPackageValidationResult result,
                                   String code, String message, String expectedValue, String... acceptedKeys) {
        boolean hasKey = false;
        for (String key : acceptedKeys) {
            if (content.contains(key)) {
                hasKey = true;
                break;
            }
        }
        boolean hasExpectedPair = false;
        for (String key : acceptedKeys) {
            if (hasKeyValuePair(content, key, expectedValue)) {
                hasExpectedPair = true;
                break;
            }
        }
        if (!hasKey || !hasExpectedPair) {
            result.add(McpPackageViolation.blocker(code,
                    message + ": key=" + String.join(" or ", acceptedKeys) + ", value=" + expectedValue,
                    source));
        }
    }

    public static void requireRuntimeTruth(Path packageState, McpRuntimeContract runtime, McpPackageValidationResult result,
                                    McpPackageContext context) {
        if (!Files.isRegularFile(packageState)) {
            return;
        }

        String content = context.files().read(packageState, result);
        requireRuntimePair(content, packageState, result, "runtime.expected-delta",
                "Missing active RUN_EXPECTED_DELTA_QUALITY from " + runtime.source(),
                runtime.expectedDeltaQuality(), "run_expected_delta_quality", "RUN_EXPECTED_DELTA_QUALITY");
        requireRuntimePair(content, packageState, result, "runtime.round-target",
                "Missing active ROUND_TARGET_TEST_COUNT from " + runtime.source(),
                String.valueOf(runtime.roundTargetTestCount()), "round_target_test_count", "ROUND_TARGET_TEST_COUNT", "tests_expected");
        requireRuntimePair(content, packageState, result, "runtime.meta-iteration",
                "Missing active META_ITERATION_COUNT from " + runtime.source(),
                String.valueOf(runtime.metaIterationCount()), "meta_iteration_count", "META_ITERATION_COUNT", "runs_expected");
        requireText(content, packageState, result, "score.overall", "Package state should publish Overall score", "overall");
        requireText(content, packageState, result, "score.cap", "Package state should publish the business score cap", "cap");
    }

    public static Integer firstIntValue(String content, String... acceptedKeys) {
        for (String key : acceptedKeys) {
            Integer value = extractIntValue(content, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    public static Integer extractIntValue(String content, String key) {
        Pattern pattern = Pattern.compile("(?is)[\"']?" + Pattern.quote(key) + "[\"']?\\s*[:=]?\\s*[\"']?(\\d+)[\"']?");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }

    public static String extractStringValue(String content, String key) {
        Pattern pattern = Pattern.compile("(?is)(?:[\"']" + Pattern.quote(key) + "[\"']|\\b"
                + Pattern.quote(key) + "\\b)\\s*[:=]\\s*[\"']([^\"']+)[\"']");
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    public static String firstStringValue(String content, String... acceptedKeys) {
        for (String key : acceptedKeys) {
            String value = extractStringValue(content, key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    public static boolean hasKeyValuePair(String content, String key, String expectedValue) {
        String quotedKey = Pattern.quote(key);
        String quotedValue = Pattern.quote(expectedValue);
        Pattern pattern = Pattern.compile("(?is)[\"']?" + quotedKey + "[\"']?\\s*[:=]\\s*[\"']?" + quotedValue + "[\"']?");
        return pattern.matcher(content).find();
    }

    public static boolean containsAny(String content, String... values) {
        for (String value : values) {
            if (content.contains(value)) {
                return true;
            }
        }
        return false;
    }

    public static String readProjectSource(McpPackageContext context, String relativeRoot) {
        Path sourceRoot = context.projectRoot().resolve(relativeRoot).normalize();
        if (!Files.isDirectory(sourceRoot)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        try (Stream<Path> stream = Files.walk(sourceRoot)) {
            for (Path path : stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".java"))
                    .filter(file -> !file.toString().replace('\\', '/').contains("/com/endava/ai/skills/mcp/"))
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList())) {
                builder.append('\n').append(Files.readString(path));
            }
        } catch (IOException ignored) {
            return builder.toString();
        }
        return builder.toString();
    }

    public static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT);
    }
}
