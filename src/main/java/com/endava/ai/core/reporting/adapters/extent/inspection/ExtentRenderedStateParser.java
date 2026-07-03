package com.endava.ai.core.reporting.adapters.extent.inspection;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ExtentRenderedStateParser {
    private final Map<String, String> values;

    private ExtentRenderedStateParser(Map<String, String> values) {
        this.values = Map.copyOf(values);
    }

    public static ExtentRenderedStateParser parse(String renderedState) {
        Map<String, String> parsed = new LinkedHashMap<>();
        for (String entry : String.valueOf(renderedState).split(";")) {
            int split = entry.indexOf('=');
            if (split > 0) {
                parsed.put(entry.substring(0, split), entry.substring(split + 1));
            }
        }
        return new ExtentRenderedStateParser(parsed);
    }

    public String value(String key) {
        String value = values.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Rendered state must include value for " + key + ". State values: " + values);
        }
        return value;
    }

    public double numericValue(String key) {
        String value = value(key);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Rendered state value for " + key + " must be numeric: " + value,
                    e
            );
        }
    }

    public int[] pipeSeparatedIntegers(String key, int expectedParts) {
        String[] parts = value(key).split("\\|");
        if (parts.length != expectedParts) {
            throw new IllegalArgumentException(
                    "Rendered state key " + key + " must expose " + expectedParts + " pipe-separated values."
            );
        }
        int[] numbers = new int[parts.length];
        for (int index = 0; index < parts.length; index++) {
            numbers[index] = Integer.parseInt(parts[index]);
        }
        return numbers;
    }
}
