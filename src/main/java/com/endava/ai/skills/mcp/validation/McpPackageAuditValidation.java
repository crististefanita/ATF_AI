package com.endava.ai.skills.mcp.validation;

import com.endava.ai.skills.mcp.validation.artifacts.McpArtifactAssertions;
import com.endava.ai.skills.mcp.validation.candidate.McpHtmlExampleCandidateValidation;
import com.endava.ai.skills.mcp.validation.graph.McpBusinessGraphAssertions;
import com.endava.ai.skills.mcp.validation.profile.McpReportSections;
import com.endava.ai.skills.mcp.validation.profile.RunAudit;
import com.endava.ai.skills.mcp.validation.report.McpReportAssertions;
import com.endava.ai.skills.mcp.validation.run.McpRunAssertions;
import com.endava.ai.skills.mcp.validation.score.McpScoreAssertions;
import com.endava.ai.skills.mcp.validation.support.McpHtmlAuditSupport;
import com.endava.ai.skills.mcp.validation.support.McpTokenAssertions;

import com.endava.ai.skills.mcp.McpPackageViolation;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class McpPackageAuditValidation {
    public static final Pattern HREF = Pattern.compile("(?i)href\\s*=\\s*[\"']([^\"']+)[\"']");
    public static final Pattern ROOT_RUN_TIMESTAMP = Pattern.compile("\\b\\d{4}-\\d{2}-\\d{2}_\\d{2}\\.\\d{2}\\.\\d{2}\\b");

    public final McpPackageAuditContext context;
    private final McpAuditViolationAssertions violationAssertions;
    private final McpAuditArtifactAccess artifactAccess;
    private final McpArtifactAssertions artifactAssertions;
    private final McpReportAssertions reportAssertions;
    private final McpBusinessGraphAssertions businessGraphAssertions;
    private final McpScoreAssertions scoreAssertions;
    private final McpRunAssertions runAssertions;

    private McpPackageAuditValidation(McpPackageAuditContext context) {
        this.context = context;
        this.violationAssertions = new McpAuditViolationAssertions(context);
        this.artifactAccess = new McpAuditArtifactAccess(context);
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

    public void assertHtmlExampleCandidateContract() throws IOException {
        McpHtmlExampleCandidateValidation.assertContract(context);
    }

    public void assertNoBlockers(String axis, Predicate<McpPackageViolation> filter, List<String> details) {
        violationAssertions.assertNoBlockers(axis, filter, details);
    }

    public void assertContainsAll(String axis, String content, Path path, List<String> expectedValues, List<String> details) {
        List<String> missing = expectedValues.stream()
                .filter(expected -> !content.contains(expected))
                .collect(Collectors.toList());
        assertNoSyntheticViolations(axis, missing, path, "content.missing", details);
    }

    public void assertNoSyntheticViolations(String axis, List<String> messages, Path path, String code, List<String> details) {
        violationAssertions.assertNoSyntheticViolations(axis, messages, path, code, details);
    }

    public void assertNoViolations(String axis, List<McpPackageViolation> blockers, List<String> details) {
        violationAssertions.assertNoViolations(axis, blockers, details);
    }

    public List<String> details(String... values) {
        return List.of(values);
    }

    public List<String> canonicalReportSections() {
        return McpReportSections.MAIN;
    }

    public void requireToken(String content, String token, String message, List<String> blockers) {
        McpTokenAssertions.requireToken(content, token, message, blockers);
    }

    public void requireAnyToken(String content, List<String> tokens, String message, List<String> blockers) {
        McpTokenAssertions.requireAnyToken(content, tokens, message, blockers);
    }

    public List<String> canonicalSharedCssBlockers(String css) {
        return McpTokenAssertions.canonicalSharedCssBlockers(css);
    }

    public List<McpHtmlAuditSupport.DetailsBlock> detailsBlocks(String html) {
        return McpHtmlAuditSupport.detailsBlocks(html);
    }

    public String detailsBody(String html, String summary) {
        return McpHtmlAuditSupport.detailsBody(html, summary);
    }

    public String graphLegendBody(String html) {
        return McpHtmlAuditSupport.graphLegendBody(html);
    }

    public String stripTags(String value) {
        return McpHtmlAuditSupport.stripTags(value);
    }

    public String readFinalReport() throws IOException {
        return artifactAccess.readFinalReport();
    }

    public Path finalReport() {
        return artifactAccess.finalReport();
    }

    public void requireContains(String content, String expected, List<String> missing) {
        artifactAccess.requireContains(content, expected, missing);
    }

    public boolean isExternalOrInline(String href) {
        return McpHtmlAuditSupport.isExternalOrInline(href);
    }

    public Path resolve(Path reportDirectory, String href) {
        return McpHtmlAuditSupport.resolve(reportDirectory, href);
    }

    public void forEachRun(RunAudit audit) throws IOException {
        artifactAccess.forEachRun(audit);
    }

    public Path runRoot(String run) {
        return artifactAccess.runRoot(run);
    }

    public String readRunFile(String run, String relativePath) throws IOException {
        return artifactAccess.readRunFile(run, relativePath);
    }

    public List<Path> extentReports(String run) throws IOException {
        return artifactAccess.extentReports(run);
    }

    public List<Path> findHtml(Path directory) throws IOException {
        return artifactAccess.findHtml(directory);
    }

    public void compareXmlAttribute(String run, String xml, String attribute, int expected, List<String> mismatches) {
        artifactAccess.compareXmlAttribute(run, xml, attribute, expected, mismatches);
    }

    public boolean containsAny(String content, String... values) {
        return artifactAccess.containsAny(content, values);
    }

    public BigDecimal extractOptionalDelta(String score) {
        return artifactAccess.extractOptionalDelta(score);
    }

    public boolean isPackageArtifactProblem(McpPackageViolation violation) {
        return artifactAccess.isPackageArtifactProblem(violation);
    }

    public boolean containsPathPart(McpPackageViolation violation, String part) {
        return artifactAccess.containsPathPart(violation, part);
    }

}
