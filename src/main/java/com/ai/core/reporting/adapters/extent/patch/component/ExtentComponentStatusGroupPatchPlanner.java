package com.ai.core.reporting.adapters.extent.patch.component;

import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlDocument;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchOperation;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchPlan;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.ai.core.reporting.adapters.extent.mode.ExtentReportMode;
import com.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExtentComponentStatusGroupPatchPlanner {
    private static final String STATUS_GROUP_TOKEN = "var statusGroup";
    private static final String TEST_METHOD_LEVEL_NOUNS = alternatives(
            ExtentDashboardLabels.TEST_NOUN,
            ExtentDashboardLabels.CONFIGURATION_METHODS.toLowerCase(java.util.Locale.ROOT)
    );
    private static final Pattern TESTS_PASSED = scoreLinePattern(TEST_METHOD_LEVEL_NOUNS, "passed");
    private static final Pattern TESTS_FAILED = scoreLinePattern(TEST_METHOD_LEVEL_NOUNS, "failed");
    private static final Pattern TOP_TESTS_PASSED = Pattern.compile(
            "(" + ExtentHtmlStructure.topCardLabel("pass", ExtentDashboardLabels.TESTS_PASSED)
                    + "\\s*<h3[^>]*>)\\d+(</h3>)"
    );
    private static final Pattern TOP_TESTS_FAILED = Pattern.compile(
            "(" + ExtentHtmlStructure.topCardLabel("fail", ExtentDashboardLabels.TESTS_FAILED)
                    + "\\s*<h3[^>]*>)\\d+(</h3>)"
    );

    private ExtentComponentStatusGroupPatchPlanner() {
    }

    public static ExtentHtmlPatchPlan plan(ExtentHtmlDocument document, ExtentReportMode mode) {
        if (mode.type() != ExtentReportModeType.COMPONENT_ADAPTER || !document.contains(STATUS_GROUP_TOKEN)) {
            return ExtentHtmlPatchPlan.of(List.of());
        }
        return ExtentHtmlPatchPlan.of(List.of(new ExtentHtmlPatchOperation(
                "extent.component-status-group.reconcile",
                "",
                ExtentComponentStatusGroupPatchPlanner::reconcileStatusGroup
        )));
    }

    private static String reconcileStatusGroup(String html) {
        ExtentComponentStatus status = ExtentComponentStatusExtractor.from(html);
        if (!status.available()) {
            return html;
        }
        String reconciledCards = reconcileDashboardCards(html, status);
        return replaceScriptContaining(reconciledCards, STATUS_GROUP_TOKEN, ExtentStatusGroupScript.render(status));
    }

    private static String reconcileDashboardCards(String html, ExtentComponentStatus status) {
        String reconciled = replaceFirst(TESTS_PASSED, html,
                "<b>" + status.testPassed() + "</b> " + ExtentDashboardLabels.TEST_NOUN + " passed");
        reconciled = replaceFirst(TESTS_FAILED, reconciled,
                "<b>" + status.testFailed() + "</b> " + ExtentDashboardLabels.TEST_NOUN + " failed");
        reconciled = replaceFirst(
                TOP_TESTS_PASSED,
                reconciled,
                "$1" + status.classPassed() + "$2"
        );
        return replaceFirst(
                TOP_TESTS_FAILED,
                reconciled,
                "$1" + status.classFailed() + "$2"
        );
    }

    private static String replaceFirst(Pattern pattern, String html, String replacement) {
        Matcher matcher = pattern.matcher(html);
        return matcher.find() ? matcher.replaceFirst(replacement) : html;
    }

    private static String replaceScriptContaining(String html, String token, String replacement) {
        int tokenIndex = html.indexOf(token);
        if (tokenIndex < 0) {
            return html;
        }
        int scriptStart = html.lastIndexOf("<script", tokenIndex);
        int scriptEnd = html.indexOf("</script>", tokenIndex);
        if (scriptStart < 0 || scriptEnd < tokenIndex) {
            return html;
        }
        return html.substring(0, scriptStart)
                + replacement
                + html.substring(scriptEnd + "</script>".length());
    }

    private static Pattern scoreLinePattern(String nounAlternatives, String status) {
        return Pattern.compile("<b>(\\d+)</b>\\s*" + nounAlternatives + " " + Pattern.quote(status));
    }

    private static String alternatives(String... values) {
        return "(?:"
                + java.util.Arrays.stream(values)
                .map(Pattern::quote)
                .collect(java.util.stream.Collectors.joining("|"))
                + ")";
    }
}
