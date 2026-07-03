package com.endava.ai.core.support.contract.yaml;

import java.util.Locale;
import java.util.Map;

import static com.endava.ai.core.support.contract.yaml.YamlScalars.contractId;

/**
 * Context-rich enum parsing for YAML contract specs.
 */
public final class YamlEnums {
    private YamlEnums() {
    }

    public static <E extends Enum<E>> E enumValue(Class<E> type,
                                                  String value,
                                                  String key,
                                                  String resourcePath,
                                                  Map<String, String> entry) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        try {
            return Enum.valueOf(type, normalized);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid " + key + " in " + resourcePath
                    + " for contract " + contractId(entry) + ": " + value, exception);
        }
    }
}
