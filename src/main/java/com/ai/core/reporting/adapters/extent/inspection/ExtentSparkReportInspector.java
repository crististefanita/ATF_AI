package com.ai.core.reporting.adapters.extent.inspection;

import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDomIds;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExtentSparkReportInspector {
    private static final String DASHBOARD_START = "<div class=\"container-fluid p-4 view "
            + ExtentDomIds.DASHBOARD_VIEW
            + "\">";
    private static final String CATEGORY_START = "<div class=\"test-wrapper row view "
            + ExtentDomIds.CATEGORY_VIEW
            + " attributes-view\">";
    private final String html;

    private ExtentSparkReportInspector(String html) {
        this.html = html == null ? "" : html;
    }

    public static ExtentSparkReportInspector inspect(String html) {
        return new ExtentSparkReportInspector(html);
    }

    public static int countOccurrences(String value, String needle) {
        if (value == null || needle == null || needle.isEmpty()) {
            return 0;
        }
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }

    public String dashboardFragment() {
        int start = html.indexOf(DASHBOARD_START);
        int end = html.indexOf("<script src=", start);
        require(start >= 0 && end > start, "Spark dashboard fragment must be present.");
        return html.substring(start, end);
    }

    public String dashboardTopCount(String label) {
        String regex = "<p class=\"m-b-0 text-(?:pass|fail)\">"
                + Pattern.quote(label)
                + "</p>\\s*<h3[^>]*>([^<]+)</h3>";
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(dashboardFragmentOrWholeHtml());
        require(matcher.find(), "Dashboard top card must exist for label: " + label);
        return matcher.group(1).trim();
    }

    public String dashboardSummaryCount(String title) {
        String fragment = dashboardFragment();
        String regex = "<h6 class=\"card-title\">"
                + Pattern.quote(title)
                + "</h6></div><div class=\"card-body\"><h3[^>]*>([^<]+)</h3>";
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(fragment);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        if (isTestCaseSummaryTitle(title)) {
            return dashboardTestsCardCount();
        }

        int titleIndex = fragment.indexOf(ExtentHtmlStructure.cardTitle(title));
        require(titleIndex >= 0, "Dashboard runtime summary card must exist for title: " + title);
        int nextTitle = fragment.indexOf(ExtentHtmlStructure.CARD_TITLE_PREFIX, titleIndex + title.length());
        String card = nextTitle > titleIndex ? fragment.substring(titleIndex, nextTitle) : fragment.substring(titleIndex);
        return String.valueOf(sumBoldIntegers(card));
    }

    public String dashboardTestsCardCount() {
        String testsCard = dashboardTestsCard();
        Matcher explicitTotal = Pattern
                .compile(Pattern.quote(ExtentHtmlStructure.dataAttributeEquals(
                        ExtentDataAttributes.TEST_CASES_TOTAL,
                        "true"
                )) + ">\\s*(\\d+)\\s*</h3>", Pattern.DOTALL)
                .matcher(testsCard);
        if (explicitTotal.find()) {
            return explicitTotal.group(1).trim();
        }
        return String.valueOf(sumBoldIntegers(testsCard));
    }

    public String dashboardTestsCard() {
        String fragment = dashboardFragment();
        int titleIndex = dashboardTestsCardTitleIndex(fragment);
        require(titleIndex >= 0, "Dashboard Test cases card must exist.");
        int nextTitle = fragment.indexOf(ExtentHtmlStructure.CARD_TITLE_PREFIX, titleIndex + 1);
        int rowEnd = fragment.indexOf("<div class=\"row atf-runtime-timeline-row\">", titleIndex);
        int end = nextTitle > titleIndex ? nextTitle : rowEnd;
        require(end > titleIndex, "Dashboard Test cases card must have a bounded fragment.");
        return fragment.substring(titleIndex, end);
    }

    private static int dashboardTestsCardTitleIndex(String fragment) {
        return fragment.indexOf(ExtentHtmlStructure.cardTitle(ExtentDashboardLabels.TEST_CASES));
    }

    private static boolean isTestCaseSummaryTitle(String title) {
        return ExtentDashboardLabels.TEST_CASES.equals(title);
    }

    public int runtimeTagTableCountSum(String tableKind) {
        String tableMarker = ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.RUNTIME_TAG_TABLE)
                + tableKind
                + "\"";
        int tableStart = html.indexOf(tableMarker);
        require(tableStart >= 0, "Runtime tag audit table must exist for kind: " + tableKind);
        int tableEnd = html.indexOf("</table>", tableStart);
        require(tableEnd > tableStart, "Runtime tag audit table must be closed for kind: " + tableKind);
        String table = html.substring(tableStart, tableEnd);
        Matcher matcher = Pattern.compile(
                "<tr>\\s*<td>.*?</td>\\s*<td>(\\d+)</td>\\s*<td>.*?</td>\\s*</tr>",
                Pattern.DOTALL
        ).matcher(table);
        int total = 0;
        while (matcher.find()) {
            total += Integer.parseInt(matcher.group(1));
        }
        return total;
    }

    public String categoryFragment() {
        int start = html.indexOf(CATEGORY_START);
        int end = html.indexOf(DASHBOARD_START, start);
        require(start >= 0 && end > start, "Spark category fragment must be present.");
        return html.substring(start, end);
    }

    private String dashboardFragmentOrWholeHtml() {
        int start = html.indexOf(DASHBOARD_START);
        if (start < 0) {
            return html;
        }
        int end = html.indexOf("<script src=", start);
        return end > start ? html.substring(start, end) : html.substring(start);
    }

    public int[] runtimeTagCardWidths(String renderedState) {
        return ExtentRenderedStateParser.parse(renderedState).pipeSeparatedIntegers("cardWidths", 3);
    }

    public String renderedStateValue(String renderedState, String key) {
        return ExtentRenderedStateParser.parse(renderedState).value(key);
    }

    private static int sumBoldIntegers(String fragment) {
        Matcher countMatcher = Pattern.compile("<b[^>]*>(\\d+)</b>").matcher(fragment);
        int total = 0;
        while (countMatcher.find()) {
            total += Integer.parseInt(countMatcher.group(1));
        }
        return total;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
