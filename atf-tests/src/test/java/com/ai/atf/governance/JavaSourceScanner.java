package com.ai.atf.governance;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Small lexical guard adapted from J0; source examples are not dependencies. */
public final class JavaSourceScanner {
    private JavaSourceScanner() { }

    static boolean containsNamespace(String source, String namespace) {
        return containsCodeNamespace(codeOnly(source), namespace);
    }

    static boolean containsCodeNamespace(String code, String namespace) {
        if (!namespace.matches("[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*"
                + "(?:\\.[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*)*")) {
            throw new IllegalArgumentException("Invalid Java namespace: " + namespace);
        }
        String qualified = Arrays.stream(namespace.split("\\."))
                .map(Pattern::quote).collect(Collectors.joining("\\s*\\.\\s*"));
        return Pattern.compile("(?<![\\p{javaJavaIdentifierPart}.])" + qualified
                + "(?![\\p{javaJavaIdentifierPart}])").matcher(code).find();
    }

    /** Java translates Unicode escapes before identifying comments and literals. */
    static String codeOnly(String source) {
        String translated = translateUnicodeEscapes(Objects.requireNonNull(source, "source"));
        return maskNonCode(translated);
    }

    static String commentsOnly(String source) {
        return selectRegions(translateUnicodeEscapes(Objects.requireNonNull(source, "source")), false, true, false);
    }

    static String literalsOnly(String source) {
        return selectRegions(translateUnicodeEscapes(Objects.requireNonNull(source, "source")), false, false, true);
    }

    /** Matches a literal-argument marker only when its prefix is executable Java, not an example. */
    public static boolean containsExecutableMarker(String source, String marker) {
        String translated = translateUnicodeEscapes(Objects.requireNonNull(source, "source"));
        Objects.requireNonNull(marker, "marker");
        int quote = marker.indexOf('"');
        String prefix = quote < 0 ? marker : marker.substring(0, quote);
        if (prefix.isBlank()) throw new IllegalArgumentException("Marker must start with executable tokens");
        String code = maskNonCode(translated);
        for (int offset = translated.indexOf(marker); offset >= 0; offset = translated.indexOf(marker, offset + 1)) {
            if (code.startsWith(prefix, offset)) return true;
        }
        return false;
    }

    private static String maskNonCode(String translated) {
        return selectRegions(translated, true, false, false);
    }

    private static String selectRegions(String translated, boolean keepCode, boolean keepComments, boolean keepLiterals) {
        StringBuilder code = new StringBuilder(translated.length());
        for (int cursor = 0; cursor < translated.length();) {
            int start = cursor;
            char current = translated.charAt(cursor);
            if (translated.startsWith("//", cursor)) {
                cursor += 2;
                while (cursor < translated.length() && translated.charAt(cursor) != '\n'
                        && translated.charAt(cursor) != '\r') cursor++;
                appendRegion(code, translated, start, cursor, keepComments);
            } else if (translated.startsWith("/*", cursor)) {
                int end = translated.indexOf("*/", cursor + 2);
                cursor = end < 0 ? translated.length() : end + 2;
                appendRegion(code, translated, start, cursor, keepComments);
            } else if (current == '"' || current == '\'') {
                boolean block = translated.startsWith("\"\"\"", cursor);
                cursor = skipLiteral(translated, cursor + (block ? 3 : 1), current, block);
                appendRegion(code, translated, start, cursor, keepLiterals);
            } else {
                code.append(keepCode || current == '\n' || current == '\r' ? current : ' ');
                cursor++;
            }
        }
        return code.toString();
    }

    private static void appendRegion(StringBuilder target, String source, int start, int end, boolean keep) {
        if (keep) target.append(source, start, end);
        else mask(target, source, start, end);
    }

    private static String translateUnicodeEscapes(String source) {
        StringBuilder translated = new StringBuilder(source.length());
        int backslashes = 0;
        boolean previousTranslated = false;
        for (int cursor = 0; cursor < source.length();) {
            char current = source.charAt(cursor);
            boolean eligible = current == '\\' && (previousTranslated || backslashes % 2 == 0);
            if (eligible && cursor + 1 < source.length() && source.charAt(cursor + 1) == 'u') {
                int end = cursor + 1;
                while (end < source.length() && source.charAt(end) == 'u') end++;
                if (end + 4 > source.length()) throw new IllegalArgumentException("Incomplete Java Unicode escape");
                int value = 0;
                for (int index = end; index < end + 4; index++) {
                    char hex = source.charAt(index);
                    int digit = hex >= '0' && hex <= '9' ? hex - '0'
                            : hex >= 'a' && hex <= 'f' ? hex - 'a' + 10
                            : hex >= 'A' && hex <= 'F' ? hex - 'A' + 10 : -1;
                    if (digit < 0) throw new IllegalArgumentException("Invalid Java Unicode escape");
                    value = (value << 4) | digit;
                }
                char decoded = (char) value;
                translated.append(decoded);
                backslashes = decoded == '\\' ? backslashes + 1 : 0;
                previousTranslated = true;
                cursor = end + 4;
            } else {
                translated.append(current);
                backslashes = current == '\\' ? backslashes + 1 : 0;
                previousTranslated = false;
                cursor++;
            }
        }
        return translated.toString();
    }

    private static int skipLiteral(String source, int cursor, char quote, boolean block) {
        while (cursor < source.length()) {
            if (source.charAt(cursor) == '\\') cursor = Math.min(source.length(), cursor + 2);
            else if (block && source.startsWith("\"\"\"", cursor)) return cursor + 3;
            else if (!block && source.charAt(cursor) == quote) return cursor + 1;
            else cursor++;
        }
        return cursor;
    }

    private static void mask(StringBuilder target, String source, int start, int end) {
        for (int index = start; index < end; index++) {
            char value = source.charAt(index);
            target.append(value == '\n' || value == '\r' ? value : ' ');
        }
    }
}
