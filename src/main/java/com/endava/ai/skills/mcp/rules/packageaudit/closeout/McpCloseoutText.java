package com.endava.ai.skills.mcp.rules.packageaudit.closeout;

import com.endava.ai.skills.mcp.fs.HashService;

import java.nio.file.Path;
import java.util.regex.Pattern;

final class McpCloseoutText {
    private McpCloseoutText() {
    }

    static String firstSha(String content) {
        return HashService.firstSha256(content);
    }

    static String sha256(Path path) {
        return HashService.sha256OrPath(path);
    }

    static boolean jsonHasField(String json, String field) {
        return Pattern.compile("(?is)\"" + Pattern.quote(field) + "\"\\s*:").matcher(json).find();
    }

    static String jsonString(String json, String field) {
        java.util.regex.Matcher matcher = Pattern.compile("(?is)\"" + Pattern.quote(field)
                + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    static Integer firstInt(String text, String... labels) {
        Long value = firstLong(text, labels);
        if (value == null || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            return null;
        }
        return value.intValue();
    }

    static Long firstLong(String text, String... labels) {
        for (String label : labels) {
            java.util.regex.Matcher matcher = Pattern.compile("(?is)(?:\"|\\b)" + Pattern.quote(label)
                    + "(?:\"|\\b)\\s*[:=]\\s*\"?(\\d+)\"?").matcher(text);
            if (matcher.find()) {
                return Long.parseLong(matcher.group(1));
            }
        }
        return null;
    }
}

