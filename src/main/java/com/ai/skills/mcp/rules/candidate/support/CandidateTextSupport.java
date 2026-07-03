package com.ai.skills.mcp.rules.candidate.support;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.context.McpRunArtifacts;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class CandidateTextSupport {
    private static final Set<String> STOP_WORDS = Set.of(
            "state", "object", "business", "frontier", "memory", "owned", "real",
            "after", "before", "with", "without", "current", "package", "candidate");

    private CandidateTextSupport() {
    }

    public static Set<String> meaningfulTokens(String value) {
        if (value == null) {
            return Set.of();
        }
        return Arrays.stream(value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(token -> token.length() >= 4)
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    public static String runArtifactText(McpPackageContext context,
                                  McpPackageValidationResult result,
                                  String... relativeArtifacts) {
        StringBuilder builder = new StringBuilder();
        for (Path runDirectory : context.files().runDirectories(context.paths().runsRoot(), result)) {
            for (String artifact : relativeArtifacts) {
                builder.append('\n').append(context.files().readIfRegular(runDirectory.resolve(artifact), result));
            }
        }
        return builder.toString();
    }

    public static String allRunLearningText(McpPackageContext context, McpPackageValidationResult result) {
        return runArtifactText(context, result,
                McpRunArtifacts.STATE,
                McpRunArtifacts.SUMMARY,
                McpRunArtifacts.BUSINESS_UNDERSTANDING,
                McpRunArtifacts.LESSON_LEARNED,
                McpRunArtifacts.FRONTIER_DECISION);
    }

    public static boolean containsAny(String text, String... tokens) {
        String lower = normalize(text);
        for (String token : tokens) {
            if (lower.contains(token)) {
                return true;
            }
        }
        return false;
    }

    public static long countRunArtifactsContainingAnyToken(McpPackageContext context,
                                                    McpPackageValidationResult result,
                                                    Set<String> tokens,
                                                    String relativeArtifact) {
        return context.files().runDirectories(context.paths().runsRoot(), result).stream()
                .map(runDirectory -> normalize(context.files().readIfRegular(runDirectory.resolve(relativeArtifact), result)))
                .filter(text -> tokens.stream().anyMatch(text::contains))
                .count();
    }
}
