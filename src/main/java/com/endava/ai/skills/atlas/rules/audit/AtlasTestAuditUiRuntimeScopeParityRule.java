package com.endava.ai.skills.atlas.rules.audit;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.contract.AtlasRuleIds;
import com.endava.ai.skills.atlas.rules.AtlasSourceInventory;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasTestAuditUiRuntimeScopeParityRule implements AtlasValidationRule {
    private static final Pattern TABLE_ROW = Pattern.compile("(?is)<tr\\b[^>]*>(.*?)</tr>");
    private static final Pattern HREF = Pattern.compile("(?is)<a\\b[^>]*href\\s*=\\s*[\"']([^\"']+)[\"']");
    private static final Pattern PASSED = Pattern.compile("(?i)passed\\s*[:/]?\\s*(\\d+)|(\\d+)\\s*passed");
    private static final Pattern FAILED = Pattern.compile("(?i)failed\\s*[:/]?\\s*(\\d+)|(\\d+)\\s*failed");
    private static final Pattern SKIPPED = Pattern.compile("(?i)skipped\\s*[:/]?\\s*(\\d+)|(\\d+)\\s*skipped");

    @Override
    public String id() {
        return AtlasRuleIds.Candidate.TEST_AUDIT_UI_RUNTIME_SCOPE_PARITY;
    }

    @Override
    public String expected() {
        return "Atlas Test Audit publishes Playwright and Selenium UI Extent rows for the same @Test scope as the analyzed input package.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String auditSection = auditSection(context.atlasText());
        if (auditSection.isBlank()) {
            result.add(id(), "Atlas Test Audit section is missing", context.atlasHtml(),
                    "Publish section 6 with Atlas, Playwright and Selenium Extent evidence.");
            return;
        }

        Optional<RuntimeRow> playwright = runtimeRow(auditSection, RuntimeKind.PLAYWRIGHT);
        Optional<RuntimeRow> selenium = runtimeRow(auditSection, RuntimeKind.SELENIUM);
        int expected = AtlasSourceInventory.load(context).sourceTestAnnotationCount();

        if (playwright.isEmpty()) {
            result.add(id(), "Atlas Test Audit does not publish a Playwright UI Extent row",
                    context.atlasHtml(),
                    "Add the Playwright UI Agentic Extent report row for the exact analyzed @Test package.");
        }
        if (selenium.isEmpty()) {
            result.add(id(), "Atlas Test Audit does not publish a Selenium UI Extent row",
                    context.atlasHtml(),
                    "Add the Selenium UI Agentic Extent report row for the exact same @Test package as Playwright.");
        }
        if (playwright.isEmpty() || selenium.isEmpty()) {
            return;
        }

        RuntimeRow playwrightRow = playwright.get();
        RuntimeRow seleniumRow = selenium.get();
        validateRowScope(context, result, playwrightRow, expected);
        validateRowScope(context, result, seleniumRow, expected);

        if (playwrightRow.total() != seleniumRow.total()) {
            result.add(id(),
                    "Playwright/Selenium UI runtime totals differ: Playwright total="
                            + playwrightRow.total() + ", Selenium total=" + seleniumRow.total(),
                    context.atlasHtml(),
                    "Regenerate both UI runtime Extent reports from the same input test package. "
                            + "Do not publish Selenium evidence generated from a broader or stale suite.");
        }
    }

    private static void validateRowScope(AtlasContext context,
                                         AtlasValidationResult result,
                                         RuntimeRow row,
                                         int expectedTests) {
        if (!row.hasCounts()) {
            result.add(AtlasRuleIds.Candidate.TEST_AUDIT_UI_RUNTIME_SCOPE_PARITY,
                    row.kind().label + " UI Extent row does not expose Passed/Failed/Skipped counts",
                    context.atlasHtml(),
                    "Publish explicit counts in the same row: Passed: N; Failed: M; Skipped: K.");
            return;
        }
        if (row.total() != expectedTests) {
            result.add(AtlasRuleIds.Candidate.TEST_AUDIT_UI_RUNTIME_SCOPE_PARITY,
                    row.kind().label + " UI Extent total does not match analyzed @Test scope: total="
                            + row.total() + ", expected=" + expectedTests,
                    context.atlasHtml(),
                    "Regenerate " + row.kind().label
                            + " evidence from INPUT_TEST_PACKAGE_ROOT only, then update section 6 from that artifact.");
        }
    }

    private static Optional<RuntimeRow> runtimeRow(String auditSection, RuntimeKind kind) {
        Matcher matcher = TABLE_ROW.matcher(auditSection);
        while (matcher.find()) {
            String rowHtml = matcher.group(1);
            String text = plainText(rowHtml).toLowerCase(Locale.ROOT);
            if (isRuntimeRow(rowHtml, text, kind)) {
                return Optional.of(new RuntimeRow(
                        kind,
                        href(rowHtml),
                        count(PASSED, rowHtml),
                        count(FAILED, rowHtml),
                        count(SKIPPED, rowHtml)));
            }
        }
        return Optional.empty();
    }

    private static boolean isRuntimeRow(String rowHtml, String text, RuntimeKind kind) {
        String link = href(rowHtml).toLowerCase(Locale.ROOT);
        if (!link.endsWith(".html") || !link.contains("_ui-agentic-")) {
            return false;
        }
        if (kind == RuntimeKind.SELENIUM) {
            return link.contains("selenium");
        }
        return !link.contains("selenium")
                && (link.contains("current") || leadingCellsText(rowHtml).contains(kind.marker));
    }

    private static String leadingCellsText(String rowHtml) {
        int commandStart = rowHtml.toLowerCase(Locale.ROOT).indexOf("<td><code>mvn");
        String identityCells = commandStart < 0 ? rowHtml : rowHtml.substring(0, commandStart);
        return plainText(identityCells).toLowerCase(Locale.ROOT);
    }

    private static String auditSection(String html) {
        String lower = html.toLowerCase(Locale.ROOT);
        int start = lower.indexOf("6. atlas test audit");
        if (start < 0) {
            start = lower.indexOf("atlas test audit");
        }
        if (start < 0) {
            return "";
        }
        int end = lower.indexOf("</details>", start);
        return end < 0 ? html.substring(start) : html.substring(start, end);
    }

    private static String href(String rowHtml) {
        Matcher matcher = HREF.matcher(rowHtml);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private static int count(Pattern pattern, String rowHtml) {
        Matcher matcher = pattern.matcher(plainText(rowHtml));
        if (!matcher.find()) {
            return -1;
        }
        String first = matcher.group(1);
        String second = matcher.group(2);
        return Integer.parseInt(first == null ? second : first);
    }

    private static String plainText(String html) {
        return html.replaceAll("(?is)<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private enum RuntimeKind {
        PLAYWRIGHT("Playwright", "playwright"),
        SELENIUM("Selenium", "selenium");

        private final String label;
        private final String marker;

        RuntimeKind(String label, String marker) {
            this.label = label;
            this.marker = marker;
        }
    }

    private static final class RuntimeRow {
        private final RuntimeKind kind;
        private final String href;
        private final int passed;
        private final int failed;
        private final int skipped;

        private RuntimeRow(RuntimeKind kind, String href, int passed, int failed, int skipped) {
            this.kind = kind;
            this.href = href;
            this.passed = passed;
            this.failed = failed;
            this.skipped = skipped;
        }

        private RuntimeKind kind() {
            return kind;
        }

        private boolean hasCounts() {
            return passed >= 0 && failed >= 0 && skipped >= 0;
        }

        private int total() {
            return Math.max(0, passed) + Math.max(0, failed) + Math.max(0, skipped);
        }

        @SuppressWarnings("unused")
        private String href() {
            return href;
        }
    }
}
