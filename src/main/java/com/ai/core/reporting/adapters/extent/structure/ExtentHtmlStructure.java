package com.ai.core.reporting.adapters.extent.structure;

import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDomIds;

public final class ExtentHtmlStructure {
    public static final String TOP_CARD_LABEL_PREFIX = "<p class=\"m-b-0 text-";
    public static final String TOP_CARD_LABEL_SEPARATOR = "\">";
    public static final String TOP_CARD_LABEL_SUFFIX = "</p>";
    public static final String CARD_TITLE_PREFIX = "<h6 class=\"card-title\">";
    public static final String CARD_TITLE_SUFFIX = "</h6>";
    public static final String BLANK_CARD_TITLE = CARD_TITLE_PREFIX + CARD_TITLE_SUFFIX;
    public static final String DASHBOARD_CONTAINER = "<div class=\"container-fluid p-4 view "
            + ExtentDomIds.DASHBOARD_VIEW
            + "\">";
    public static final String CATEGORY_CONTAINER = "<div class=\"test-wrapper row view "
            + ExtentDomIds.CATEGORY_VIEW
            + " attributes-view\">";
    public static final String TEST_CONTAINER = "<div class=\"test-wrapper row view "
            + ExtentDomIds.TEST_VIEW
            + "\">";
    public static final String DASHBOARD_VIEW_CLASS = "container-fluid p-4 view " + ExtentDomIds.DASHBOARD_VIEW;
    public static final String NATIVE_TAGS_CARD_HEADER = "<div class=\"card-header\"><p>"
            + ExtentDashboardLabels.LEGACY_TAGS
            + "</p></div>";
    public static final String COLLAPSE_FUNCTION = "collapseExtentTreeByDefault";

    private ExtentHtmlStructure() {
    }

    public static String cardTitle(String label) {
        return CARD_TITLE_PREFIX + label + CARD_TITLE_SUFFIX;
    }

    public static String topCardLabel(String statusClass, String label) {
        return TOP_CARD_LABEL_PREFIX + statusClass + TOP_CARD_LABEL_SEPARATOR + label + TOP_CARD_LABEL_SUFFIX;
    }

    public static String dataAttributePrefix(String attributeName) {
        return attributeName + "=\"";
    }

    public static String dataAttributeEquals(String attributeName, String value) {
        return attributeName + "=\"" + value + "\"";
    }

    public static String idAttribute(String id) {
        return "id=\"" + id + "\"";
    }
}
