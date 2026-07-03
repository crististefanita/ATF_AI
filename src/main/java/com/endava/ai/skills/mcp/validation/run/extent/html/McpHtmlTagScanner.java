package com.endava.ai.skills.mcp.validation.run.extent.html;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

public final class McpHtmlTagScanner {
    private McpHtmlTagScanner() {
    }

    public static List<McpHtmlTagSnapshot> tags(String html) {
        List<McpHtmlTagSnapshot> tags = new ArrayList<>();
        int cursor = 0;
        while (cursor < html.length()) {
            int tagStart = html.indexOf('<', cursor);
            if (tagStart < 0) {
                break;
            }
            int tagEnd = html.indexOf('>', tagStart + 1);
            if (tagEnd < 0) {
                break;
            }
            String tag = html.substring(tagStart, tagEnd + 1);
            tags.add(new McpHtmlTagSnapshot(tag, elementName(tag), tagStart, tagEnd + 1, attributes(tag)));
            cursor = tagEnd + 1;
        }
        return List.copyOf(tags);
    }

    private static String elementName(String tag) {
        int start = elementNameStart(tag);
        int end = elementNameEnd(tag, start);
        return tag.substring(start, end).toLowerCase(Locale.ROOT);
    }

    private static int elementNameStart(String tag) {
        int index = 1;
        while (index < tag.length() && Character.isWhitespace(tag.charAt(index))) {
            index++;
        }
        return index;
    }

    private static int elementNameEnd(String tag) {
        return elementNameEnd(tag, elementNameStart(tag));
    }

    private static int elementNameEnd(String tag, int start) {
        int index = start;
        while (index < tag.length()) {
            char current = tag.charAt(index);
            if (!Character.isLetterOrDigit(current) && current != '-' && current != ':') {
                break;
            }
            index++;
        }
        return index;
    }

    private static Map<String, String> attributes(String tag) {
        Map<String, String> attributes = new LinkedHashMap<>();
        int index = elementNameEnd(tag);
        while (index < tag.length()) {
            index = skipWhitespace(tag, index);
            if (index >= tag.length() || tag.charAt(index) == '>' || tag.charAt(index) == '/') {
                break;
            }
            int nameStart = index;
            while (index < tag.length()) {
                char current = tag.charAt(index);
                if (Character.isWhitespace(current) || current == '=' || current == '>' || current == '/') {
                    break;
                }
                index++;
            }
            String name = tag.substring(nameStart, index).toLowerCase(Locale.ROOT);
            index = skipWhitespace(tag, index);
            if (index >= tag.length() || tag.charAt(index) != '=') {
                attributes.putIfAbsent(name, "");
                continue;
            }
            index = skipWhitespace(tag, index + 1);
            AttributeValue value = readAttributeValue(tag, index);
            attributes.put(name, value.value());
            index = value.nextIndex();
        }
        return Map.copyOf(attributes);
    }

    private static int skipWhitespace(String tag, int index) {
        while (index < tag.length() && Character.isWhitespace(tag.charAt(index))) {
            index++;
        }
        return index;
    }

    private static AttributeValue readAttributeValue(String tag, int index) {
        if (index < tag.length() && (tag.charAt(index) == '"' || tag.charAt(index) == '\'')) {
            char quote = tag.charAt(index++);
            int valueStart = index;
            while (index < tag.length() && tag.charAt(index) != quote) {
                index++;
            }
            String value = tag.substring(valueStart, Math.min(index, tag.length()));
            return new AttributeValue(value, index < tag.length() ? index + 1 : index);
        }
        int valueStart = index;
        while (index < tag.length()) {
            char current = tag.charAt(index);
            if (Character.isWhitespace(current) || current == '>' || current == '/') {
                break;
            }
            index++;
        }
        return new AttributeValue(tag.substring(valueStart, index), index);
    }

    @Getter(AccessLevel.PRIVATE)
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class AttributeValue {
        private final String value;
        private final int nextIndex;
    }
}
