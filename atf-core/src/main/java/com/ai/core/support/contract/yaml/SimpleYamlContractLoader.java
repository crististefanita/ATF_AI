package com.ai.core.support.contract.yaml;

import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Contract YAML loader for versioned list-of-contract maps.
 */
public final class SimpleYamlContractLoader {
    public static final String SCHEMA_VERSION_KEY = "schemaVersion";
    public static final String CONTRACTS_KEY = "contracts";
    public static final String SUPPORTED_SCHEMA_VERSION = "1";

    private SimpleYamlContractLoader() {
    }

    public static List<Map<String, String>> load(String resourcePath) {
        try (InputStream stream = SimpleYamlContractLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream != null) {
                return parse(stream, resourcePath);
            }
            return parse(fallbackResource(resourcePath), resourcePath);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load YAML contract resource: " + resourcePath, e);
        }
    }

    public static List<Map<String, String>> loadText(String yaml, String resourcePath) {
        return parse(yaml, resourcePath);
    }

    public static String required(Map<String, String> entry, String key, String resourcePath) {
        String value = entry.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing " + key + " in " + resourcePath
                    + " for contract " + entry.getOrDefault("id", "<unknown>"));
        }
        return value.trim();
    }

    static List<Map<String, String>> parse(InputStream stream, String resourcePath) throws IOException {
        byte[] bytes = stream.readAllBytes();
        return parse(new String(bytes, StandardCharsets.UTF_8), resourcePath);
    }

    private static String fallbackResource(String resourcePath) throws IOException {
        for (Path root : List.of(Path.of("src", "main", "resources"), Path.of("src", "test", "resources"))) {
            Path candidate = root.resolve(resourcePath);
            if (Files.isRegularFile(candidate)) {
                return Files.readString(candidate, StandardCharsets.UTF_8);
            }
        }
        throw new IllegalStateException("Missing YAML contract resource: " + resourcePath);
    }

    private static List<Map<String, String>> parse(String yaml, String resourcePath) {
        rejectDuplicateKeys(yaml, resourcePath);
        Object loaded;
        try {
            LoadSettings settings = LoadSettings.builder()
                    .setLabel(resourcePath)
                    .setAllowDuplicateKeys(false)
                    .build();
            loaded = new Load(settings).loadFromString(yaml);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Invalid YAML contract resource " + resourcePath
                    + " (duplicate keys, indentation, or syntax error): " + e.getMessage(), e);
        }

        if (!(loaded instanceof Map<?, ?>)) {
            throw new IllegalArgumentException("YAML contract resource must be a map: " + resourcePath);
        }
        Map<?, ?> root = (Map<?, ?>) loaded;
        requireSchemaVersion(root, resourcePath);
        Object contracts = root.get(CONTRACTS_KEY);
        if (!(contracts instanceof List<?>)) {
            throw new IllegalArgumentException("YAML contract resource must contain contracts: list in " + resourcePath);
        }

        List<Map<String, String>> entries = new ArrayList<>();
        int index = 0;
        for (Object item : (List<?>) contracts) {
            if (!(item instanceof Map<?, ?>)) {
                throw new IllegalArgumentException("YAML contract entry #" + index
                        + " must be a map in " + resourcePath);
            }
            Map<String, String> flattened = new LinkedHashMap<>();
            flatten("", (Map<?, ?>) item, flattened, resourcePath);
            entries.add(Collections.unmodifiableMap(new LinkedHashMap<>(flattened)));
            index++;
        }
        return List.copyOf(entries);
    }

    private static void rejectDuplicateKeys(String yaml, String resourcePath) {
        Map<Integer, Set<String>> keysByIndent = new HashMap<>();
        String[] lines = yaml.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        for (int index = 0; index < lines.length; index++) {
            String line = stripComment(lines[index]);
            if (line.trim().isEmpty()) {
                continue;
            }
            String trimmed = line.trim();
            int indent = leadingSpaces(line);
            int mapIndent = indent;
            String keyValue = trimmed;
            if (trimmed.startsWith("- ")) {
                mapIndent = indent + 2;
                removeScopesAtOrBelow(keysByIndent, mapIndent);
                keyValue = trimmed.substring(2).trim();
                if (keyValue.isEmpty()) {
                    continue;
                }
            } else {
                removeScopesBelow(keysByIndent, mapIndent);
            }
            int colon = keyValue.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String key = keyValue.substring(0, colon).trim();
            if (key.isEmpty()) {
                continue;
            }
            Set<String> keys = keysByIndent.computeIfAbsent(mapIndent, ignored -> new HashSet<>());
            if (!keys.add(key)) {
                throw new IllegalArgumentException("Duplicate YAML key '" + key + "' in "
                        + resourcePath + " near line " + (index + 1));
            }
        }
    }

    private static void removeScopesBelow(Map<Integer, Set<String>> keysByIndent, int indent) {
        keysByIndent.keySet().removeIf(existingIndent -> existingIndent > indent);
    }

    private static void removeScopesAtOrBelow(Map<Integer, Set<String>> keysByIndent, int indent) {
        keysByIndent.keySet().removeIf(existingIndent -> existingIndent >= indent);
    }

    private static void requireSchemaVersion(Map<?, ?> root, String resourcePath) {
        Object version = root.get(SCHEMA_VERSION_KEY);
        if (version == null) {
            throw new IllegalArgumentException("Missing " + SCHEMA_VERSION_KEY
                    + " in YAML contract resource: " + resourcePath);
        }
        String actual = String.valueOf(version).trim();
        if (!SUPPORTED_SCHEMA_VERSION.equals(actual)) {
            throw new IllegalArgumentException("Unsupported YAML contract schemaVersion " + actual
                    + " in " + resourcePath + "; supported=" + SUPPORTED_SCHEMA_VERSION);
        }
    }

    private static void flatten(String prefix,
                                Map<?, ?> source,
                                Map<String, String> target,
                                String resourcePath) {
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            String key = String.valueOf(Objects.requireNonNull(entry.getKey(), "YAML null key")).trim();
            if (key.isEmpty()) {
                throw new IllegalArgumentException("Blank YAML contract key in " + resourcePath);
            }
            String flattenedKey = prefix.isEmpty() ? key : prefix + "." + key;
            Object value = entry.getValue();
            if (value instanceof Map<?, ?>) {
                flatten(flattenedKey, (Map<?, ?>) value, target, resourcePath);
            } else {
                target.put(flattenedKey, scalar(value, flattenedKey, resourcePath));
            }
        }
    }

    private static String scalar(Object value, String key, String resourcePath) {
        if (value instanceof List<?>) {
            return scalarList((List<?>) value, key, resourcePath);
        }
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static String scalarList(List<?> values, String key, String resourcePath) {
        List<String> flattened = new ArrayList<>();
        for (Object item : values) {
            if (item instanceof Map<?, ?> || item instanceof List<?>) {
                throw new IllegalArgumentException("YAML contract list value must contain scalars for "
                        + key + " in " + resourcePath);
            }
            flattened.add(item == null ? "" : String.valueOf(item).trim());
        }
        return String.join(",", flattened);
    }

    private static String stripComment(String line) {
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '#' && (i == 0 || Character.isWhitespace(line.charAt(i - 1)))) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static int leadingSpaces(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }
}
