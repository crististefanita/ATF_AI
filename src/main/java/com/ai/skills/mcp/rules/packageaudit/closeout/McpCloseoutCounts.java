package com.ai.skills.mcp.rules.packageaudit.closeout;

import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;
import lombok.AccessLevel;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class McpCloseoutCounts {
    final Integer total;
    final Integer passed;
    final Integer skipped;
    final Integer failures;
    final Integer errors;

    static McpCloseoutCounts fromXml(String xml) {
        Integer total = firstAttribute(xml, "tests", "total");
        Integer skipped = firstAttribute(xml, "skipped", "skip");
        Integer failures = firstAttribute(xml, "failures", "failed");
        Integer errors = firstAttribute(xml, "errors");
        Integer passed = firstAttribute(xml, "passed");
        if (passed == null && total != null && skipped != null && failures != null && errors != null) {
            passed = total - skipped - failures - errors;
        }
        return new McpCloseoutCounts(total, passed, skipped, failures, errors);
    }

    static McpCloseoutCounts fromLedger(String text) {
        return new McpCloseoutCounts(firstLabel(text, "Tests run", "Tests"),
                firstLabel(text, "Passed"),
                firstLabel(text, "Skipped"),
                firstLabel(text, "Failures"),
                firstLabel(text, "Errors"));
    }

    static McpCloseoutCounts fromGate(String text) {
        return fromLedger(text);
    }

    static McpCloseoutCounts fromExtent(String html) {
        int passed = countStatus(html, "pass");
        int skipped = countStatus(html, "skip") + countStatus(html, "skipped");
        int failed = countStatus(html, "fail");
        int errors = countStatus(html, "error");
        return new McpCloseoutCounts(passed + skipped + failed + errors, passed, skipped, failed, errors);
    }

    boolean complete() {
        return total != null && passed != null && skipped != null && failures != null && errors != null;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof McpCloseoutCounts)) {
            return false;
        }
        McpCloseoutCounts counts = (McpCloseoutCounts) other;
        return Objects.equals(total, counts.total)
                && Objects.equals(passed, counts.passed)
                && Objects.equals(skipped, counts.skipped)
                && Objects.equals(failures, counts.failures)
                && Objects.equals(errors, counts.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(total, passed, skipped, failures, errors);
    }

    @Override
    public String toString() {
        return "total=" + total + ", passed=" + passed + ", skipped=" + skipped
                + ", failures=" + failures + ", errors=" + errors;
    }

    private static Integer firstAttribute(String text, String... names) {
        for (String name : names) {
            java.util.regex.Matcher matcher = Pattern.compile("(?is)\\b" + Pattern.quote(name)
                    + "\\s*=\\s*\"(\\d+)\"").matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return null;
    }

    private static Integer firstLabel(String text, String... labels) {
        for (String label : labels) {
            java.util.regex.Matcher matcher = Pattern.compile("(?im)^\\s*-?\\s*" + Pattern.quote(label)
                    + "\\s*[:=]\\s*(\\d+)\\b").matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return null;
    }

    private static int countStatus(String html, String status) {
        return (int) Pattern.compile("(?is)<li\\s+class=[\"'][^\"']*\\btest-item\\b[^\"']*[\"'][^>]*\\bstatus=[\"']"
                + Pattern.quote(status) + "[\"']").matcher(html).results().count();
    }
}

