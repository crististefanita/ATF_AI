package com.endava.ai.skills.mcp.rules.source.inventory;

import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Heuristic support/test design signals extracted from source diff additions.
 */
@Getter
@Accessors(fluent = true)
public final class SupportQualityInventory {
    private static final Pattern ADDED_ASSERTION = Pattern.compile("(?im)^\\+.*\\b(?:assert|verify|validate|expect)\\w*\\b");
    private static final Pattern SUPPORT_USAGE = Pattern.compile("(?im)^\\+.*\\bsupport\\b");
    private static final Pattern TEST_LAYER_ENGINE_USAGE =
            Pattern.compile("(?im)^\\+.*src/test/.*\\b(?:playwright|webdriver|selenium|apiresponse|requestcontext)\\b");
    private static final Pattern SUPPORT_ASSERTION =
            Pattern.compile("(?im)^\\+.*(?:/support/|/validation/).*\\b(?:assert|verify|validate|expect)\\w*\\b");

    private final int testMethodsUsingSupport;
    private final int supportCohesionScore;
    private final int duplicateActionLineRatio;
    private final int duplicateAssertionLineRatio;
    private final int layeringViolationCount;
    private final int assertionLocalityScore;
    private final int refactorBurnDown;

    private SupportQualityInventory(int testMethodsUsingSupport,
                                    int supportCohesionScore,
                                    int duplicateActionLineRatio,
                                    int duplicateAssertionLineRatio,
                                    int layeringViolationCount,
                                    int assertionLocalityScore,
                                    int refactorBurnDown) {
        this.testMethodsUsingSupport = testMethodsUsingSupport;
        this.supportCohesionScore = supportCohesionScore;
        this.duplicateActionLineRatio = duplicateActionLineRatio;
        this.duplicateAssertionLineRatio = duplicateAssertionLineRatio;
        this.layeringViolationCount = layeringViolationCount;
        this.assertionLocalityScore = assertionLocalityScore;
        this.refactorBurnDown = refactorBurnDown;
    }

    public static SupportQualityInventory from(String diff,
                                               int testMethodsAdded,
                                               int supportClasses,
                                               int testClasses) {
        int testMethodsUsingSupport = Math.min(testMethodsAdded, countMatches(diff, SUPPORT_USAGE));
        int addedAssertions = countMatches(diff, ADDED_ASSERTION);
        int assertionsInSupportOrValidation = countMatches(diff, SUPPORT_ASSERTION);
        DuplicateStats duplicates = duplicateStats(diff);
        return new SupportQualityInventory(
                testMethodsUsingSupport,
                supportClasses == 0 ? 0 : Math.min(100, percentage(testMethodsUsingSupport, supportClasses)),
                duplicates.actionRatio(),
                duplicates.assertionRatio(),
                countMatches(diff, TEST_LAYER_ENGINE_USAGE),
                addedAssertions == 0 ? 100 : Math.max(0,
                        100 - percentage(addedAssertions - assertionsInSupportOrValidation, addedAssertions)),
                percentage(supportClasses, supportClasses + testClasses)
        );
    }

    private static int countMatches(String content, Pattern pattern) {
        int count = 0;
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static int percentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return numerator == 0 ? 0 : 100;
        }
        return Math.round((numerator * 100.0f) / denominator);
    }

    private static DuplicateStats duplicateStats(String diff) {
        Map<String, Integer> actions = new LinkedHashMap<>();
        Map<String, Integer> assertions = new LinkedHashMap<>();
        int actionLines = 0;
        int assertionLines = 0;
        for (String line : diff.split("\\R")) {
            if (!line.startsWith("+") || line.startsWith("+++") || line.length() < 24) {
                continue;
            }
            String normalized = normalizeAddedLine(line);
            if (normalized.isBlank()) {
                continue;
            }
            actionLines++;
            actions.merge(normalized, 1, Integer::sum);
            if (ADDED_ASSERTION.matcher(line).find()) {
                assertionLines++;
                assertions.merge(normalized, 1, Integer::sum);
            }
        }
        return new DuplicateStats(
                percentage(repeatedCount(actions), actionLines),
                percentage(repeatedCount(assertions), assertionLines)
        );
    }

    private static String normalizeAddedLine(String line) {
        String normalized = line.substring(1).trim()
                .replaceAll("\"[^\"]*\"", "\"\"")
                .replaceAll("\\b\\d+\\b", "0")
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
        return normalized.startsWith("//") || normalized.startsWith("*") ? "" : normalized;
    }

    private static int repeatedCount(Map<String, Integer> counts) {
        return counts.values().stream().filter(count -> count > 1).mapToInt(count -> count - 1).sum();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    @Accessors(fluent = true)
    private static final class DuplicateStats {
        private final int actionRatio;
        private final int assertionRatio;
    }
}
