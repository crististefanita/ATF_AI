package com.endava.ai.skills.atlas.rules.audit;

import com.endava.ai.skills.atlas.rules.AtlasText;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AtlasAuditTotalsParser {
    private static final Pattern EXPECTED_RESULT_PATTERN = Pattern.compile(
            "(?is)(?:result\\s+expected\\s+by\\s+this\\s+atlas|rezultat\\s+asteptat\\s+de\\s+acest\\s+atlas)"
                    + ".*?<code>\\s*(\\d+)\\s+(?:test\\s+cases|tests|teste)\\s*/\\s*(\\d+)\\s+(?:failures|failure|esecuri)\\s*</code>");
    private static final Pattern TEST_STATUS_PATTERN = Pattern.compile("test-status\\s+text-(?:pass|fail|skip)");
    private static final Pattern FAILED_STATUS_PATTERN = Pattern.compile("test-status\\s+text-fail");
    private static final Pattern TEST_ITEM_PATTERN = Pattern.compile("(?is)<li\\b[^>]*class=\"[^\"]*test-item[^\"]*\"");
    private static final Pattern FAILED_TEST_ITEM_PATTERN =
            Pattern.compile("(?is)<li\\b[^>]*class=\"[^\"]*test-item[^\"]*\"[^>]*status=\"fail\"");
    private static final Pattern DASHBOARD_TESTS_PASSED_PATTERN = Pattern.compile(
            "(?is)<p[^>]*>\\s*tests\\s+passed\\s*</p>\\s*<h3>\\s*(\\d+)\\s*</h3>");
    private static final Pattern DASHBOARD_TESTS_FAILED_PATTERN = Pattern.compile(
            "(?is)<p[^>]*>\\s*tests\\s+failed\\s*</p>\\s*<h3>\\s*(\\d+)\\s*</h3>");
    private static final Pattern RUNTIME_CLASS_TOTAL_PATTERN = Pattern.compile(
            "(?is)test\\s+(?:methods\\s+by\\s+class|classes)\\s+percentage\\s+total\\s*</th>\\s*<td>\\s*(\\d+)\\s*</td>");
    private static final Pattern RUNTIME_FAILED_INVOCATIONS_PATTERN = Pattern.compile(
            "(?is)failed\\s+runtime\\s+(?:invocations|test\\s+cases)\\s*:?\\s*(\\d+)"
                    + "|->\\s*(\\d+)\\s+failed\\s+(?:invocation|test\\s+case)");

    private AtlasAuditTotalsParser() {
    }

    static AtlasTestAuditSupport.AuditTotals declaredTotals(String atlasHtml) {
        Matcher matcher = EXPECTED_RESULT_PATTERN.matcher(atlasHtml);
        if (!matcher.find()) {
            return AtlasTestAuditSupport.AuditTotals.missing();
        }
        return AtlasTestAuditSupport.AuditTotals.present(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)));
    }

    static AtlasTestAuditSupport.AuditTotals extentTotals(Path extent) throws IOException {
        String html = Files.readString(extent).toLowerCase(Locale.ROOT);
        AtlasTestAuditSupport.AuditTotals dashboardTotals = dashboardScoreTotals(html);
        if (dashboardTotals.present()) {
            return dashboardTotals;
        }
        AtlasTestAuditSupport.AuditTotals runtimeLedgerTotals = runtimeLedgerTotals(html);
        if (runtimeLedgerTotals.present()) {
            return runtimeLedgerTotals;
        }
        AtlasTestAuditSupport.AuditTotals topLevelNodeTotals = AtlasAuditTopLevelCards.count(html);
        int statusTotal = AtlasText.count(TEST_STATUS_PATTERN, html);
        int statusFailures = AtlasText.count(FAILED_STATUS_PATTERN, html);
        if (topLevelNodeTotals.present() && topLevelNodeTotals.testCount() > statusTotal) {
            return topLevelNodeTotals;
        }
        if (statusTotal > 0 || statusFailures > 0) {
            return AtlasTestAuditSupport.AuditTotals.present(statusTotal, statusFailures);
        }
        return AtlasTestAuditSupport.AuditTotals.present(
                AtlasText.count(TEST_ITEM_PATTERN, html),
                AtlasText.count(FAILED_TEST_ITEM_PATTERN, html));
    }

    private static AtlasTestAuditSupport.AuditTotals dashboardScoreTotals(String html) {
        int passed = first(DASHBOARD_TESTS_PASSED_PATTERN, html);
        int failed = first(DASHBOARD_TESTS_FAILED_PATTERN, html);
        if (passed == 0 && failed == 0) {
            return AtlasTestAuditSupport.AuditTotals.missing();
        }
        return AtlasTestAuditSupport.AuditTotals.present(passed + failed, failed);
    }

    private static AtlasTestAuditSupport.AuditTotals runtimeLedgerTotals(String html) {
        Matcher totalMatcher = RUNTIME_CLASS_TOTAL_PATTERN.matcher(html);
        if (!totalMatcher.find()) {
            return AtlasTestAuditSupport.AuditTotals.missing();
        }
        return AtlasTestAuditSupport.AuditTotals.present(
                Integer.parseInt(totalMatcher.group(1)),
                runtimeFailureCount(html));
    }

    private static int runtimeFailureCount(String html) {
        if (html.contains("no runtime failures")) {
            return 0;
        }
        Matcher matcher = RUNTIME_FAILED_INVOCATIONS_PATTERN.matcher(html);
        if (!matcher.find()) {
            return 0;
        }
        String directCount = matcher.group(1);
        return Integer.parseInt(directCount != null ? directCount : matcher.group(2));
    }

    private static int first(Pattern pattern, String html) {
        Matcher matcher = pattern.matcher(html);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }
}
