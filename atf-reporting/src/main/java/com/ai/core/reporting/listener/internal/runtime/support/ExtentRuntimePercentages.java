package com.ai.core.reporting.listener.internal.runtime.support;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ExtentRuntimePercentages {
    private ExtentRuntimePercentages() {
    }

    public static int simple(long value, int total) {
        if (total <= 0) {
            return 0;
        }
        return (int) ((value * 100) / total);
    }

    public static Map<String, Integer> exactIntegerShares(Map<String, Integer> counts, int total) {
        if (total <= 0 || counts.isEmpty()) {
            return Map.of();
        }

        Map<String, Integer> percentages = new LinkedHashMap<>();
        counts.forEach((name, count) -> percentages.put(name, (count * 100) / total));

        int assigned = percentages.values().stream().mapToInt(Integer::intValue).sum();
        int missing = 100 - assigned;
        counts.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(entry -> (entry.getValue() * 100) % total)
                        .reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(Math.max(0, missing))
                .forEach(entry -> percentages.computeIfPresent(entry.getKey(), (key, value) -> value + 1));

        return percentages;
    }
}
