package com.endava.ai.skills.mcp.rules.score;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

final class ScoreEvidencePublicationMatcher {
    private static final Set<String> STOP_WORDS = Set.of(
            "axis", "score", "proof", "package", "state", "overall", "while", "with", "without",
            "because", "below", "above", "cap", "capped", "keeps", "keep", "remain", "remains",
            "reason", "publishable", "current", "final", "this", "that", "from", "into", "and",
            "the", "but", "not", "are", "is", "was", "were", "has", "have", "had"
    );

    private ScoreEvidencePublicationMatcher() {
    }

    static boolean isWeakestAxisPublished(String document, String weakestAxis) {
        String normalizedDocument = normalize(document);
        String normalizedAxis = normalize(weakestAxis);
        if (normalizedAxis.isBlank()) {
            return false;
        }
        if (normalizedDocument.contains(normalizedAxis)) {
            return true;
        }

        List<String> terms = significantTerms(normalizedAxis);
        if (terms.isEmpty()) {
            return false;
        }
        long matchedTerms = terms.stream()
                .filter(term -> containsWord(normalizedDocument, term))
                .count();
        int requiredTerms = Math.min(4, Math.max(2, terms.size() / 3));
        return matchedTerms >= requiredTerms && hasCapContext(normalizedDocument);
    }

    private static List<String> significantTerms(String normalizedText) {
        return Arrays.stream(normalizedText.split(" "))
                .filter(term -> term.length() >= 4)
                .filter(term -> !STOP_WORDS.contains(term))
                .distinct()
                .collect(Collectors.toList());
    }

    private static boolean containsWord(String text, String word) {
        return (" " + text + " ").contains(" " + word + " ");
    }

    private static boolean hasCapContext(String text) {
        return text.contains(" cap ")
                || text.contains(" capped ")
                || text.contains(" score cap ")
                || text.contains(" weakest ")
                || text.contains(" publishability ");
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replaceAll("(?is)<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replace("&amp;", " and ")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
