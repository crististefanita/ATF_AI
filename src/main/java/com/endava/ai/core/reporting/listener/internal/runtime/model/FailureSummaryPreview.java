package com.endava.ai.core.reporting.listener.internal.runtime.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class FailureSummaryPreview {
    private static final int MAX_VISIBLE_CHARS = 220;
    private static final int MAX_LIST_ITEMS = 4;
    private static final int MIN_LIST_ITEMS = 1;

    private FailureSummaryPreview() {
    }

    public static String from(String rawSummary) {
        String normalized = normalize(rawSummary);
        if (normalized.length() <= MAX_VISIBLE_CHARS) {
            return normalized;
        }
        String bracketPreview = bracketedListPreview(normalized);
        if (bracketPreview != null) {
            return cap(bracketPreview);
        }
        return cap(commaListPreview(normalized));
    }

    private static String bracketedListPreview(String value) {
        int start = value.indexOf('[');
        int end = value.lastIndexOf(']');
        if (start < 0 || end <= start) {
            return null;
        }
        List<String> items = uniqueItems(value.substring(start + 1, end));
        if (items.size() <= MAX_LIST_ITEMS) {
            return null;
        }
        return previewList(value.substring(0, start + 1), compact(items), "]");
    }

    private static String commaListPreview(String value) {
        List<String> items = uniqueItems(value);
        if (items.size() <= MAX_LIST_ITEMS) {
            return value;
        }
        return previewList("", compact(items), "");
    }

    private static String previewList(String prefix, List<String> items, String suffix) {
        int visibleCount = Math.min(MAX_LIST_ITEMS, items.size());
        while (visibleCount > MIN_LIST_ITEMS) {
            String candidate = listPreview(prefix, items, visibleCount, suffix);
            if (candidate.length() <= MAX_VISIBLE_CHARS) {
                return candidate;
            }
            visibleCount--;
        }
        return listPreview(prefix, items, visibleCount, suffix);
    }

    private static String listPreview(String prefix, List<String> items, int visibleCount, String suffix) {
        return prefix
                + String.join(", ", items.subList(0, visibleCount))
                + " ... +" + (items.size() - visibleCount) + " more refs"
                + suffix;
    }

    private static List<String> compact(List<String> items) {
        return items.stream()
                .map(FailureSummaryPreview::compactJavaReference)
                .collect(Collectors.toList());
    }

    private static String compactJavaReference(String value) {
        String normalized = normalize(value);
        int java = normalized.indexOf(".java");
        if (java < 0) {
            return normalized;
        }
        int slash = Math.max(normalized.lastIndexOf('\\', java), normalized.lastIndexOf('/', java));
        return normalized.substring(slash < 0 ? 0 : slash + 1).trim();
    }

    private static List<String> uniqueItems(String value) {
        Set<String> unique = new LinkedHashSet<>();
        for (String item : value.split("\\s*,\\s*")) {
            String normalized = normalize(item);
            if (!normalized.isBlank()) {
                unique.add(normalized);
            }
        }
        return new ArrayList<>(unique);
    }

    private static String cap(String value) {
        String normalized = normalize(value);
        if (normalized.length() <= MAX_VISIBLE_CHARS) {
            return normalized;
        }
        int omittedMarker = normalized.indexOf(" ... +");
        if (omittedMarker > 0) {
            String suffix = normalized.substring(omittedMarker);
            int boundary = normalized.lastIndexOf(", ", omittedMarker);
            if (boundary > 0) {
                return normalized.substring(0, boundary).trim() + suffix;
            }
            int budget = MAX_VISIBLE_CHARS - suffix.length() - 1;
            return normalized.substring(0, Math.max(40, budget)).trim() + suffix;
        }
        int boundary = Math.max(80, normalized.lastIndexOf(' ', MAX_VISIBLE_CHARS - 1));
        return normalized.substring(0, boundary).trim() + " ...";
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }
}
