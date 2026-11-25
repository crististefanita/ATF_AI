package com.ai.ui.core.engine.syntax;

public final class UiSelectorSyntax {
    public static final String XPATH_PREFIX = "xpath=";
    public static final String XPATH_ABSOLUTE_PREFIX = "//";
    public static final String TEXT_PREFIX = "text=";
    public static final String VISIBLE_PSEUDO_CLASS = ":visible";

    private UiSelectorSyntax() {
    }

    public static boolean isXpath(String selector) {
        return startsWith(selector, XPATH_PREFIX) || startsWith(selector, XPATH_ABSOLUTE_PREFIX);
    }

    public static String xpathExpression(String selector) {
        return startsWith(selector, XPATH_PREFIX) ? selector.substring(XPATH_PREFIX.length()) : selector;
    }

    public static boolean isTextSelector(String selector) {
        return startsWith(selector, TEXT_PREFIX);
    }

    public static boolean isNonCssSelector(String selector) {
        String normalized = selector == null ? "" : selector.trim();
        return isXpath(normalized) || isTextSelector(normalized);
    }

    public static boolean hasVisiblePseudo(String selector) {
        return selector != null && selector.contains(VISIBLE_PSEUDO_CLASS);
    }

    public static String withVisiblePseudo(String selector) {
        return selector + VISIBLE_PSEUDO_CLASS;
    }

    private static boolean startsWith(String selector, String prefix) {
        return selector != null && selector.startsWith(prefix);
    }
}
