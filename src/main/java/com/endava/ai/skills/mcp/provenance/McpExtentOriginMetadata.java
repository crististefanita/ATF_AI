package com.endava.ai.skills.mcp.provenance;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;

final class McpExtentOriginMetadata {
    private static final Pattern SHA256 = Pattern.compile("(?i)^[a-f0-9]{64}$");
    private final JsonObject root;

    private McpExtentOriginMetadata(JsonObject root) {
        this.root = root;
    }

    static Optional<McpExtentOriginMetadata> parse(String content) {
        try {
            JsonElement element = JsonParser.parseString(content);
            if (!element.isJsonObject()) {
                return Optional.empty();
            }
            return Optional.of(new McpExtentOriginMetadata(element.getAsJsonObject()));
        } catch (JsonSyntaxException | IllegalStateException exception) {
            return Optional.empty();
        }
    }

    boolean has(String name) {
        return root.has(name) && !root.get(name).isJsonNull();
    }

    String stringValue(String name) {
        JsonElement element = root.get(name);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        try {
            return element.getAsString();
        } catch (UnsupportedOperationException | ClassCastException | IllegalStateException exception) {
            return null;
        }
    }

    Long longValue(String name) {
        JsonElement element = root.get(name);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        try {
            return element.getAsLong();
        } catch (NumberFormatException | UnsupportedOperationException | ClassCastException | IllegalStateException exception) {
            return null;
        }
    }

    Integer intValue(String name) {
        Long value = longValue(name);
        if (value == null || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            return null;
        }
        return value.intValue();
    }

    String sha256() {
        String value = stringValue("sha256");
        if (value == null || !SHA256.matcher(value).matches()) {
            return null;
        }
        return value.toLowerCase();
    }

    Instant timestamp(String name) {
        String value = stringValue(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException offsetDateTimeException) {
            return parseLocalDateTime(value);
        }
    }

    private static Instant parseLocalDateTime(String value) {
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException exception) {
            return null;
        }
    }
}
