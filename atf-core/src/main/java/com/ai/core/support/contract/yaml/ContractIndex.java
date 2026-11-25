package com.ai.core.support.contract.yaml;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Immutable id index for typed contract specs.
 */
public final class ContractIndex<T> {
    private final String label;
    @Getter
    @Accessors(fluent = true)
    private final List<T> entries;
    private final Map<String, T> byId;
    @Getter
    @Accessors(fluent = true)
    private final List<String> duplicateIds;

    private ContractIndex(String label, List<T> entries, Function<T, String> idExtractor) {
        this.label = Objects.requireNonNull(label, "label");
        this.entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
        Objects.requireNonNull(idExtractor, "idExtractor");
        List<String> ids = ids(this.entries, idExtractor, label);
        this.byId = index(this.entries, ids);
        this.duplicateIds = duplicateIds(ids);
    }

    public static <T> ContractIndex<T> of(String label, List<T> entries, Function<T, String> idExtractor) {
        return new ContractIndex<>(label, entries, idExtractor);
    }

    public static <T> ContractIndex<T> unique(String label, List<T> entries, Function<T, String> idExtractor) {
        ContractIndex<T> index = of(label, entries, idExtractor);
        index.requireUniqueIds();
        return index;
    }

    public T require(String id) {
        String normalized = normalizedRequiredId(id);
        if (duplicateIds.contains(normalized)) {
            throw new IllegalStateException("Duplicate contract id [" + normalized + "] in " + label);
        }
        T entry = byId.get(normalized);
        if (entry == null) {
            throw new IllegalArgumentException("Missing contract [" + normalized + "] in " + label);
        }
        return entry;
    }

    public void requireUniqueIds() {
        if (!duplicateIds.isEmpty()) {
            throw new IllegalStateException("Duplicate contract ids " + duplicateIds + " in " + label);
        }
    }

    private static <T> List<String> ids(List<T> entries, Function<T, String> idExtractor, String label) {
        return entries.stream()
                .map(entry -> normalizedId(entry, idExtractor, label))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<String> duplicateIds(List<String> ids) {
        Map<String, Long> counts = ids.stream()
                .collect(Collectors.groupingBy(id -> id, LinkedHashMap::new, Collectors.counting()));
        return counts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableList());
    }

    private static <T> Map<String, T> index(List<T> entries, List<String> ids) {
        Map<String, T> indexed = new LinkedHashMap<>();
        for (int index = 0; index < entries.size(); index++) {
            indexed.putIfAbsent(ids.get(index), entries.get(index));
        }
        return Map.copyOf(indexed);
    }

    private static <T> String normalizedId(T entry, Function<T, String> idExtractor, String label) {
        String id = Objects.requireNonNull(idExtractor.apply(entry), "Contract id is null in " + label).trim();
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Blank contract id in " + label);
        }
        return id;
    }

    private String normalizedRequiredId(String id) {
        String normalized = Objects.requireNonNull(id, "Contract id lookup is null in " + label).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Blank contract id lookup in " + label);
        }
        return normalized;
    }
}
