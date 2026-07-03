package com.endava.ai.skills.atlas.rules.audit;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.contract.AtlasRuleIds;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasTestAuditExtentSummaryCommandRule implements AtlasValidationRule {
    private static final Pattern TABLE_ROW = Pattern.compile("(?is)<tr\\b[^>]*>(.*?)</tr>");
    private static final Pattern TABLE_CELL = Pattern.compile("(?is)<td\\b[^>]*>(.*?)</td>");
    private static final Pattern HREF = Pattern.compile("(?is)<a\\b[^>]*href\\s*=\\s*[\"']([^\"']+)[\"']");
    private static final Pattern FAILED_COUNT = Pattern.compile("(?i)failed\\s*[:/]?\\s*(\\d+)|(\\d+)\\s*failed");

    @Override
    public String id() {
        return AtlasRuleIds.Candidate.TEST_AUDIT_EXTENT_SUMMARY_COMMAND;
    }

    @Override
    public String expected() {
        return "Every Extent report row in Atlas Test Audit publishes passed/failed totals, the exact Maven command, and a failure interpretation.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String auditSection = auditSection(context.atlasText());
        if (auditSection.isBlank()) {
            result.add(id(), "Atlas Test Audit section is missing", context.atlasHtml(),
                    "Add section 6. Atlas Test Audit before publishing Extent evidence.");
            return;
        }

        int extentRows = 0;
        Matcher matcher = TABLE_ROW.matcher(auditSection);
        while (matcher.find()) {
            String rowHtml = matcher.group(1);
            String href = firstHref(rowHtml);
            if (!isExtentHtmlRow(rowHtml, href)) {
                continue;
            }
            extentRows++;
            validateExtentRow(context, result, rowHtml, href);
        }
        if (extentRows == 0) {
            result.add(id(), "Atlas Test Audit does not contain any Extent HTML artifact row",
                    context.atlasHtml(),
                    "Publish at least the Atlas validation Extent row with link, passed/failed totals, and Maven command.");
        }
    }

    private void validateExtentRow(AtlasContext context,
                                   AtlasValidationResult result,
                                   String rowHtml,
                                   String href) {
        String text = plainText(rowHtml).toLowerCase(Locale.ROOT);
        if (!text.contains("passed") || !text.contains("failed")) {
            result.add(id(), "Extent audit row lacks passed/failed summary for " + href,
                    context.atlasHtml(),
                    "In the same table row as the Extent link, publish a cold summary such as 'Passed: N; Failed: M'.");
        }
        if (!text.contains("mvn ")) {
            result.add(id(), "Extent audit row lacks Maven generation command for " + href,
                    context.atlasHtml(),
                    "In the same table row as the Extent link, publish the exact mvn command used to generate the report.");
        }
        validateInterpretation(context, result, rowHtml, href);
    }

    private void validateInterpretation(AtlasContext context,
                                        AtlasValidationResult result,
                                        String rowHtml,
                                        String href) {
        String[] cells = cells(rowHtml);
        if (cells.length < 4) {
            result.add(id(), "Extent audit row lacks a dedicated interpretation column for " + href,
                    context.atlasHtml(),
                    "Use Artifact / Result / Command / Interpretare columns so failure meaning is not hidden in prose.");
            return;
        }

        String interpretation = plainText(cells[cells.length - 1]).toLowerCase(Locale.ROOT);
        if (interpretation.length() < 40) {
            result.add(id(), "Extent audit row interpretation is too thin for " + href,
                    context.atlasHtml(),
                    "Explain whether failures are zero, diagnostic, blocking, or outside the Atlas publication verdict.");
            return;
        }

        int failed = failedCount(rowHtml);
        if (failed > 0 && !containsAny(interpretation,
                "diagnostic", "rosu", "nealiniere", "block", "bloc", "parity", "failed", "picate", "impact")) {
            result.add(id(), "Extent audit row has failures but no failure impact interpretation for " + href,
                    context.atlasHtml(),
                    "When Failed > 0, state why it failed and what claim it blocks, for example Selenium parity.");
        }
        if (failed == 0 && !containsAny(interpretation, "0 failed", "zero", "verde", "confirma", "pass")) {
            result.add(id(), "Extent audit row is green but does not interpret the zero-failure result for " + href,
                    context.atlasHtml(),
                    "When Failed = 0, state what the green result proves and what it does not prove.");
        }
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

    private static boolean isExtentHtmlRow(String rowHtml, String href) {
        String lower = rowHtml.toLowerCase(Locale.ROOT);
        return !href.isBlank()
                && href.toLowerCase(Locale.ROOT).endsWith(".html")
                && lower.contains("extent");
    }

    private static String firstHref(String rowHtml) {
        Matcher matcher = HREF.matcher(rowHtml);
        return matcher.find() ? matcher.group(1).trim() : "";
    }

    private static String[] cells(String rowHtml) {
        return TABLE_CELL.matcher(rowHtml)
                .results()
                .map(match -> match.group(1))
                .toArray(String[]::new);
    }

    private static int failedCount(String rowHtml) {
        Matcher matcher = FAILED_COUNT.matcher(plainText(rowHtml));
        if (!matcher.find()) {
            return -1;
        }
        String first = matcher.group(1);
        String second = matcher.group(2);
        return Integer.parseInt(first == null ? second : first);
    }

    private static boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private static String plainText(String html) {
        return html.replaceAll("(?is)<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
