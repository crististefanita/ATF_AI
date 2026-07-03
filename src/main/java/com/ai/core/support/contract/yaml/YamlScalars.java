package com.ai.core.support.contract.yaml;

import java.util.Map;

/**
 * Shared parsers for scalar fields in flattened YAML contract entries.
 */
public final class YamlScalars {
    private YamlScalars() {
    }

    public static String optional(Map<String, String> entry, String key) {
        String value = entry.get(key);
        return value == null ? "" : value.trim();
    }

    public static String optionalOrDefault(Map<String, String> entry, String key, String fallback) {
        String value = optional(entry, key);
        return value.isEmpty() ? fallback : value;
    }

    public static String requiredString(Map<String, String> entry,
                                        String key,
                                        String resourcePath,
                                        String contractLabel) {
        return requiredValue(optional(entry, key), key, resourcePath, contractLabel, contractId(entry));
    }

    public static String requiredValue(String value,
                                       String key,
                                       String resourcePath,
                                       String contractLabel,
                                       String contractId) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Missing " + key + " in " + resourcePath
                    + " for " + contractLabel + " " + contractId);
        }
        return normalized;
    }

    public static boolean optionalBoolean(Map<String, String> entry,
                                          String key,
                                          boolean fallback,
                                          String resourcePath) {
        String value = optional(entry, key);
        if (value.isEmpty()) {
            return fallback;
        }
        return parseBoolean(value, key, resourcePath, entry);
    }

    public static boolean requiredBoolean(Map<String, String> entry, String key, String resourcePath) {
        String value = SimpleYamlContractLoader.required(entry, key, resourcePath);
        return parseBoolean(value, key, resourcePath, entry);
    }

    public static int requiredInt(Map<String, String> entry, String key, String resourcePath) {
        return parseInt(SimpleYamlContractLoader.required(entry, key, resourcePath), key, resourcePath, entry);
    }

    public static int optionalInt(Map<String, String> entry,
                                  String key,
                                  int fallback,
                                  String resourcePath) {
        String value = optional(entry, key);
        return value.isEmpty() ? fallback : parseInt(value, key, resourcePath, entry);
    }

    public static Integer optionalInteger(Map<String, String> entry, String key, String resourcePath) {
        String value = optional(entry, key);
        return value.isEmpty() ? null : parseInt(value, key, resourcePath, entry);
    }

    public static Integer optionalNonNegativeInteger(Map<String, String> entry,
                                                     String key,
                                                     String resourcePath) {
        Integer value = optionalInteger(entry, key, resourcePath);
        if (value != null && value < 0) {
            throw new IllegalArgumentException("Negative integer " + key + " in " + resourcePath
                    + " for contract " + contractId(entry) + ": " + value);
        }
        return value;
    }

    private static int parseInt(String value, String key, String resourcePath, Map<String, String> entry) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer " + key + " in " + resourcePath
                    + " for contract " + contractId(entry) + ": " + value, e);
        }
    }

    private static boolean parseBoolean(String value, String key, String resourcePath, Map<String, String> entry) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new IllegalArgumentException("Invalid boolean " + key + " in " + resourcePath
                + " for contract " + contractId(entry) + ": " + value);
    }

    public static String contractId(Map<String, String> entry) {
        return entry.getOrDefault("id", "<unknown>");
    }
}
