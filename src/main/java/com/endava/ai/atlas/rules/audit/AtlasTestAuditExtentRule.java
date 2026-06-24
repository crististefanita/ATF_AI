package com.endava.ai.atlas.rules.audit;

import com.endava.ai.atlas.rules.AtlasValidationRule;

import com.endava.ai.atlas.AtlasContext;
import com.endava.ai.atlas.AtlasValidationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasTestAuditExtentRule implements AtlasValidationRule {
    private static final String AUDIT_SECTION = "atlas test audit";
    private static final String EXTENT_LINK = "validation/atlas-tests-extent-report.html";
    private static final String EXTENT_PATH = "docs/atlas/" + EXTENT_LINK;
    private static final Pattern EXPECTED_RESULT_PATTERN = Pattern.compile(
            "(?is)result\\s+expected\\s+by\\s+this\\s+atlas.*?<code>\\s*(\\d+)\\s+tests\\s*/\\s*(\\d+)\\s+failures\\s*</code>");
    private static final Pattern TEST_ITEM_PATTERN = Pattern.compile("(?is)<li\\b[^>]*class=\"[^\"]*test-item[^\"]*\"");
    private static final Pattern FAILED_TEST_ITEM_PATTERN = Pattern.compile("(?is)<li\\b[^>]*class=\"[^\"]*test-item[^\"]*\"[^>]*status=\"fail\"");

    @Override
    public String id() {
        return "atlas.candidate.test-audit.extent-linked";
    }

    @Override
    public String expected() {
        return "Candidate atlas links a persisted Extent report for the Atlas*Test validation run and its declared test/failure totals match that report.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        Path extent = context.projectRoot().resolve(EXTENT_PATH).normalize();
        String html = context.lowerAtlasText();
        if (!Files.exists(extent)) {
            result.add(id(), "Missing persisted atlas test Extent report", extent,
                    "Copy the latest Atlas*Test Extent report to " + EXTENT_PATH + " after running the atlas validation suite.");
        }
        if (!html.contains(AUDIT_SECTION)) {
            result.add(id(), "Atlas Test Audit section is missing", context.atlasHtml(),
                    "Add a collapsed Atlas Test Audit section with command, result and audit artifact link.");
        }
        if (!html.contains(EXTENT_LINK)) {
            result.add(id(), "Atlas does not link the persisted atlas test Extent report", context.atlasHtml(),
                    "Link the audit artifact as " + EXTENT_LINK + " so the validation proof is reopenable.");
        }
        validateDeclaredResultMatchesExtent(context, result, extent, html);
    }

    private static void validateDeclaredResultMatchesExtent(AtlasContext context,
                                                            AtlasValidationResult result,
                                                            Path extent,
                                                            String atlasHtml) {
        Matcher expectedMatcher = EXPECTED_RESULT_PATTERN.matcher(atlasHtml);
        if (!expectedMatcher.find()) {
            result.add(idStatic(), "Atlas Test Audit does not declare expected test/failure totals",
                    context.atlasHtml(),
                    "Add a row like Result expected by this atlas = N tests / M failures, then keep it synchronized with the persisted Extent report.");
            return;
        }
        if (!Files.exists(extent)) {
            return;
        }
        try {
            String extentHtml = Files.readString(extent).toLowerCase(java.util.Locale.ROOT);
            int expectedTests = Integer.parseInt(expectedMatcher.group(1));
            int expectedFailures = Integer.parseInt(expectedMatcher.group(2));
            int actualTests = count(TEST_ITEM_PATTERN, extentHtml);
            int actualFailures = count(FAILED_TEST_ITEM_PATTERN, extentHtml);
            if (expectedTests != actualTests || expectedFailures != actualFailures) {
                result.add(idStatic(),
                        "Atlas Test Audit result is stale: declared "
                                + expectedTests + " tests / " + expectedFailures
                                + " failures, Extent has " + actualTests + " tests / "
                                + actualFailures + " failures",
                        context.atlasHtml(),
                        "Rerun the atlas validation suite and update Result expected by this atlas so it matches "
                                + EXTENT_LINK + ". Do not publish stale green numbers.");
            }
        } catch (IOException e) {
            result.add(idStatic(), "Cannot read persisted atlas test Extent report: " + extent,
                    extent,
                    "Ensure " + EXTENT_PATH + " is readable and was copied after the latest atlas validation run.");
        }
    }

    private static int count(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static String idStatic() {
        return "atlas.candidate.test-audit.extent-linked";
    }
}
