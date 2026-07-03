package com.endava.ai.skills.mcp.validation.profile.graph;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class McpGraphCss {
    private final String css;
    private final String normalizedCss;

    McpGraphCss(String css) {
        this.css = css;
        this.normalizedCss = css.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    String block(String normalizedSelector) {
        int start = normalizedCss.indexOf(normalizedSelector + "{");
        if (start < 0) {
            return "";
        }
        int end = normalizedCss.indexOf("}", start);
        if (end < 0) {
            return normalizedCss.substring(start);
        }
        return normalizedCss.substring(start, end + 1);
    }

    boolean containsSelector(String normalizedSelector) {
        return normalizedCss.contains(normalizedSelector);
    }

    boolean classHasPropertyValue(String cssClass, String property, String value) {
        Pattern blockPattern = Pattern.compile("(?is)([^{}]+)\\{([^{}]+)}");
        Matcher matcher = blockPattern.matcher(css);
        while (matcher.find()) {
            String selector = matcher.group(1).toLowerCase(Locale.ROOT);
            String declarations = matcher.group(2).toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
            if (selector.contains(cssClass.toLowerCase(Locale.ROOT))
                    && declarations.contains(property.toLowerCase(Locale.ROOT) + ":" + value.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    boolean classHasNonNoneProperty(String cssClass, String property) {
        Pattern blockPattern = Pattern.compile("(?is)([^{}]+)\\{([^{}]+)}");
        Matcher matcher = blockPattern.matcher(css);
        while (matcher.find()) {
            String selector = matcher.group(1).toLowerCase(Locale.ROOT);
            String declarations = matcher.group(2).toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
            if (selector.contains(cssClass.toLowerCase(Locale.ROOT))) {
                Matcher propertyMatcher = Pattern.compile(Pattern.quote(property.toLowerCase(Locale.ROOT)) + ":([^;]+)")
                        .matcher(declarations);
                if (propertyMatcher.find() && !"none".equals(propertyMatcher.group(1).trim())) {
                    return true;
                }
            }
        }
        return false;
    }
}
