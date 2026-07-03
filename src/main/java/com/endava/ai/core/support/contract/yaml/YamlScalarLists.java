package com.endava.ai.core.support.contract.yaml;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Shared parsers for scalar list fields in flattened YAML contract entries.
 */
public final class YamlScalarLists {
    private YamlScalarLists() {
    }

    public static List<String> strictCommaSeparated(String value, String fieldLabel) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing YAML scalar list: " + fieldLabel);
        }
        List<String> parts = Arrays.stream(value.split(",", -1))
                .map(String::trim)
                .collect(Collectors.toUnmodifiableList());
        rejectBlankParts(parts, fieldLabel, ",");
        return parts;
    }

    public static List<List<String>> strictCommaSeparatedAlternatives(String value, String fieldLabel) {
        return strictCommaSeparated(value, fieldLabel).stream()
                .map(group -> strictPipeSeparated(group, fieldLabel))
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<String> commaSeparated(String value) {
        if (value == null || value.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<List<String>> commaSeparatedAlternatives(String value) {
        return commaSeparated(value).stream()
                .map(group -> Arrays.stream(group.split("\\|"))
                        .map(String::trim)
                        .filter(part -> !part.isEmpty())
                        .collect(Collectors.toUnmodifiableList()))
                .filter(group -> !group.isEmpty())
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<String> pipeSeparated(String value) {
        if (value == null || value.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(value.split("\\|"))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<String> strictPipeSeparated(String value, String fieldLabel) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing YAML scalar list: " + fieldLabel);
        }
        List<String> parts = Arrays.stream(value.split("\\|", -1))
                .map(String::trim)
                .collect(Collectors.toUnmodifiableList());
        rejectBlankParts(parts, fieldLabel, "|");
        return parts;
    }

    private static void rejectBlankParts(List<String> parts, String fieldLabel, String separator) {
        for (int index = 0; index < parts.size(); index++) {
            if (parts.get(index).isEmpty()) {
                throw new IllegalArgumentException("Blank YAML scalar list item in " + fieldLabel
                        + " at position " + (index + 1) + " around separator '" + separator + "'.");
            }
        }
    }
}
