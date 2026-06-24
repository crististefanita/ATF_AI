package com.endava.ai.mcp.validation;

import com.endava.ai.mcp.validation.artifacts.McpArtifactAssertions;
import com.endava.ai.mcp.validation.graph.McpBusinessGraphAssertions;
import com.endava.ai.mcp.validation.profile.McpReportSections;
import com.endava.ai.mcp.validation.profile.RunAudit;
import com.endava.ai.mcp.validation.report.McpReportAssertions;
import com.endava.ai.mcp.validation.run.McpRunAssertions;
import com.endava.ai.mcp.validation.score.McpScoreAssertions;

import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.McpViolationSeverity;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class McpPackageAuditValidation {
    public static final Pattern HREF = Pattern.compile("(?i)href\\s*=\\s*[\"']([^\"']+)[\"']");
    public static final Pattern BASE64_SCREENSHOT = Pattern.compile("data:image/png;base64,[A-Za-z0-9+/=]+");
    public static final Pattern ROOT_RUN_TIMESTAMP = Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}_\\d{2}\\.\\d{2}\\.\\d{2}\\b");

    public final McpPackageAuditContext context;
    private final McpArtifactAssertions artifactAssertions;
    private final McpReportAssertions reportAssertions;
    private final McpBusinessGraphAssertions businessGraphAssertions;
    private final McpScoreAssertions scoreAssertions;
    private final McpRunAssertions runAssertions;

    private McpPackageAuditValidation(McpPackageAuditContext context) {
        this.context = context;
        this.artifactAssertions = new McpArtifactAssertions(this);
        this.reportAssertions = new McpReportAssertions(this);
        this.businessGraphAssertions = new McpBusinessGraphAssertions(this);
        this.scoreAssertions = new McpScoreAssertions(this);
        this.runAssertions = new McpRunAssertions(this);
    }

    public static McpPackageAuditValidation latestPackage() throws IOException {
        return new McpPackageAuditValidation(McpPackageAuditContext.load());
    }

    public McpArtifactAssertions artifacts() {
        return artifactAssertions;
    }

    public McpReportAssertions report() {
        return reportAssertions;
    }

    public McpBusinessGraphAssertions businessGraph() {
        return businessGraphAssertions;
    }

    public McpScoreAssertions score() {
        return scoreAssertions;
    }

    public McpRunAssertions runs() {
        return runAssertions;
    }

    public void assertNoBlockers(String axis, Predicate<McpPackageViolation> filter, List<String> details) {
        List<McpPackageViolation> matching = context.result().violations().stream()
                .filter(filter)
                .collect(Collectors.toList());
        List<McpPackageViolation> blockers = matching.stream()
                .filter(violation -> violation.severity() == McpViolationSeverity.BLOCKER)
                .collect(Collectors.toList());
        List<McpPackageViolation> warnings = matching.stream()
                .filter(violation -> violation.severity() == McpViolationSeverity.WARNING)
                .collect(Collectors.toList());
        if (blockers.isEmpty()) {
            McpAuditSteps.pass(axis, context.packageRoot(), details, warnings);
            return;
        }

        McpAuditSteps.fail(axis, context.packageRoot(), details, blockers, warnings);
        throw new AssertionError(format(context.packageRoot(), blockers));
    }

    public void assertContainsAll(String axis, String content, Path path, List<String> expectedValues, List<String> details) {
        List<String> missing = expectedValues.stream()
                .filter(expected -> !content.contains(expected))
                .collect(Collectors.toList());
        assertNoSyntheticViolations(axis, missing, path, "content.missing", details);
    }

    public void assertNoSyntheticViolations(String axis, List<String> messages, Path path, String code, List<String> details) {
        if (messages.isEmpty()) {
            McpAuditSteps.pass(axis, context.packageRoot(), details);
            return;
        }

        List<McpPackageViolation> blockers = messages.stream()
                .map(message -> McpPackageViolation.blocker(code, message, path))
                .collect(Collectors.toList());
        assertNoViolations(axis, blockers, details);
    }

    public void assertNoViolations(String axis, List<McpPackageViolation> blockers, List<String> details) {
        if (blockers.isEmpty()) {
            McpAuditSteps.pass(axis, context.packageRoot(), details);
            return;
        }

        McpAuditSteps.fail(axis, context.packageRoot(), details, blockers);
        throw new AssertionError(format(context.packageRoot(), blockers));
    }

    public List<String> details(String... values) {
        return List.of(values);
    }

    public List<String> canonicalReportSections() {
        return McpReportSections.MAIN;
    }

    public void requireToken(String content, String token, String message, List<String> blockers) {
        if (!content.contains(token)) {
            blockers.add(message + " [" + token + "]");
        }
    }

    public void requireAnyToken(String content, List<String> tokens, String message, List<String> blockers) {
        for (String token : tokens) {
            if (content.contains(token)) {
                return;
            }
        }
        blockers.add(message + " [" + String.join(" | ", tokens) + "]");
    }

    public List<String> canonicalSharedCssBlockers(String css) {
        List<String> blockers = new ArrayList<>();
        for (String token : List.of("--bg", "--panel", "--panel2", "--text", "--muted", "--border",
                "--accent", "--good", "--warn", "--bad", "--shadow", "--sans", "--mono")) {
            requireToken(css, token, "Shared CSS missing canonical variable", blockers);
        }
        for (String token : List.of(".wrap", ".hero", ".card", "details", ".subdetails", "summary",
                ".summary-title", ".sumhint", ".pill-row", ".pill", ".badge", ".diag", ".score-card",
                "table", "code.inline", ".note", ".warn", ".rule", ".executive-context",
                ".graph-legend", ".graph-legend-list", ".score-summary", ".score-table", ".score-calculation-table",
                ".proof-table", ".run-audit-list", ".artifact-links", ".closeout-summary", "@media")) {
            requireToken(css, token, "Shared CSS missing canonical presentation primitive", blockers);
        }
        requireToken(css, ".nt", "Shared CSS missing canonical graph title text class", blockers);
        requireToken(css, ".ns", "Shared CSS missing canonical graph secondary text class", blockers);
        return blockers;
    }

    public List<DetailsBlock> detailsBlocks(String html) {
        Matcher matcher = Pattern.compile("(?is)<details\\b([^>]*)>\\s*<summary>(.*?)</summary>(.*?)</details>")
                .matcher(html);
        List<DetailsBlock> blocks = new ArrayList<>();
        while (matcher.find()) {
            blocks.add(new DetailsBlock(
                    matcher.group(1).toLowerCase(Locale.ROOT).contains("open"),
                    stripTags(matcher.group(2)).trim(),
                    matcher.group(3)));
        }
        return blocks;
    }

    public String detailsBody(String html, String summary) {
        return detailsBlocks(html).stream()
                .filter(block -> block.summary().contains(summary))
                .map(DetailsBlock::body)
                .findFirst()
                .orElse("");
    }

    public String graphLegendBody(String html) {
        Matcher matcher = Pattern.compile("(?is)<details\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\bgraph-legend\\b[^\"']*[\"'][^>]*>\\s*<summary>.*?Legend.*?</summary>(.*?)</details>")
                .matcher(html);
        if (!matcher.find()) {
            return "";
        }
        return matcher.group(1);
    }

    public String stripTags(String value) {
        return value.replaceAll("(?is)<[^>]+>", " ");
    }

    public static final class DetailsBlock {
        private final boolean open;
        private final String summary;
        private final String body;

        private DetailsBlock(boolean open, String summary, String body) {
            this.open = open;
            this.summary = summary;
            this.body = body;
        }

        public boolean open() {
            return open;
        }

        public String summary() {
            return summary;
        }

        public String body() {
            return body;
        }
    }

    public String readFinalReport() throws IOException {
        return Files.readString(finalReport());
    }

    public Path finalReport() {
        return context.packageRoot().resolve("analysis/langgraph-business-understanding.html");
    }

    public void requireContains(String content, String expected, List<String> missing) {
        if (!content.contains(expected)) {
            missing.add(expected);
        }
    }

    public boolean isExternalOrInline(String href) {
        String lower = href.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://")
                || lower.startsWith("https://")
                || lower.startsWith("mailto:")
                || lower.startsWith("data:")
                || lower.startsWith("javascript:")
                || lower.startsWith("#");
    }

    public Path resolve(Path reportDirectory, String href) {
        try {
            URI uri = URI.create(href.replace("\\", "/"));
            if (uri.getScheme() != null && uri.getScheme().length() == 1) {
                return Path.of(href).toAbsolutePath().normalize();
            }
        } catch (IllegalArgumentException ignored) {
            // Fall through to filesystem resolution.
        }

        Path path = Path.of(href);
        if (path.isAbsolute()) {
            return path.normalize();
        }
        return reportDirectory.resolve(path).normalize();
    }

    public void forEachRun(RunAudit audit) throws IOException {
        for (int index = 1; index <= context.runtime().metaIterationCount(); index++) {
            audit.accept(String.format("run_%02d", index));
        }
    }

    public Path runRoot(String run) {
        return context.packageRoot().resolve("runs").resolve(run);
    }

    public String readRunFile(String run, String relativePath) throws IOException {
        return Files.readString(runRoot(run).resolve(relativePath));
    }

    public List<Path> extentReports(String run) throws IOException {
        Path validation = runRoot(run).resolve("validation");
        List<Path> reports = new ArrayList<>();
        reports.addAll(findHtml(validation.resolve("reports")));
        reports.addAll(findHtml(validation).stream()
                .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).contains("extentreport"))
                .collect(Collectors.toList()));
        return reports.stream().distinct().collect(Collectors.toList());
    }

    public List<Path> findHtml(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".html"))
                    .collect(Collectors.toList());
        }
    }

    public void compareXmlAttribute(String run, String xml, String attribute, int expected, List<String> mismatches) {
        Matcher matcher = Pattern.compile(attribute + "=\"(\\d+)\"").matcher(xml);
        if (!matcher.find()) {
            mismatches.add(run + " missing XML attribute: " + attribute);
            return;
        }
        int actual = Integer.parseInt(matcher.group(1));
        if (actual != expected) {
            mismatches.add(run + " " + attribute + " expected " + expected + " but was " + actual);
        }
    }

    public boolean containsAny(String content, String... values) {
        for (String value : values) {
            if (content.contains(value)) {
                return true;
            }
        }
        return false;
    }

    public BigDecimal extractOptionalDelta(String score) {
        Matcher matcher = Pattern.compile("(?im)^\\s*-?\\s*actual_delta_quality\\s*[:=]\\s*([0-9]+(?:\\.[0-9]+)?)\\s*$")
                .matcher(score);
        if (!matcher.find()) {
            return null;
        }
        return new BigDecimal(matcher.group(1));
    }

    public boolean isPackageArtifactProblem(McpPackageViolation violation) {
        if (!violation.code().equals("file.missing") && !violation.code().equals("file.empty")) {
            return false;
        }
        return violation.path() == null || !containsPathPart(violation, "runs");
    }

    public boolean containsPathPart(McpPackageViolation violation, String part) {
        return violation.path() != null
                && violation.path().toString().replace('\\', '/').contains("/" + part + "/");
    }

    public String format(Path packageRoot, List<McpPackageViolation> blockers) {
        return "\nPackage: " + packageRoot + "\n"
                + "Blockers: " + blockers.size() + "\n"
                + blockers.stream()
                .map(violation -> "- " + violation)
                .collect(Collectors.joining("\n"));
    }
}
