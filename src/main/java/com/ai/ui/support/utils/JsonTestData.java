package com.ai.ui.support.utils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Tiny JSON reader for demo testdata (kept minimal to avoid extra deps).
 * This is NOT a general JSON parser; it extracts simple string values by key from a flat JSON object.
 */
public final class JsonTestData {
    private JsonTestData() {}

    public static String readResource(String resourcePath) {
        InputStream is = null;

        try {
            for (String candidate : resourceCandidates(resourcePath)) {
                is = JsonTestData.class.getClassLoader().getResourceAsStream(candidate);
                if (is != null) {
                    break;
                }
            }

            if (is == null) {
                for (String candidate : resourceCandidates(resourcePath)) {
                    Path path = Path.of("src/test/resources", candidate);
                    if (!Files.exists(path)) {
                        path = Path.of("src/main/resources", candidate);
                    }
                    if (Files.exists(path)) {
                        is = Files.newInputStream(path);
                        break;
                    }
                }
            }

            if (is == null) {
                throw new IllegalStateException("Missing resource: " + resourcePath);
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed reading resource: " + resourcePath, e
            );
        } finally {
            try {
                if (is != null) is.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static List<String> resourceCandidates(String resourcePath) {
        if (resourcePath.startsWith("testdata/")) {
            return List.of(resourcePath, "tests/" + resourcePath);
        }
        return List.of(resourcePath);
    }

    public static String getString(String json, String key) {
        String search = "\"" + key + "\":";
        int i = json.indexOf(search);
        if (i < 0) return "";
        int start = json.indexOf("\"", i + search.length()) + 1;
        int end = json.indexOf("\"", start);
        if (start <= 0 || end <= start) return "";
        return json.substring(start, end);
    }
}
