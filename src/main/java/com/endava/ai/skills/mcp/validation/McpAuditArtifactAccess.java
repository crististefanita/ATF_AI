package com.endava.ai.skills.mcp.validation;

import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.validation.profile.RunAudit;
import com.endava.ai.skills.mcp.validation.support.McpHtmlAuditSupport;
import com.endava.ai.skills.mcp.validation.support.McpTokenAssertions;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class McpAuditArtifactAccess {
    private static final Pattern ACTUAL_DELTA_QUALITY = Pattern.compile(
            "(?im)^\\s*-?\\s*actual_delta_quality\\s*[:=]\\s*([0-9]+(?:\\.[0-9]+)?)\\s*$"
    );
    private final McpPackageAuditContext context;

    McpAuditArtifactAccess(McpPackageAuditContext context) {
        this.context = context;
    }

    String readFinalReport() throws IOException {
        return Files.readString(finalReport());
    }

    Path finalReport() {
        return context.packageRoot().resolve(McpArtifactPaths.FINAL_REPORT);
    }

    void requireContains(String content, String expected, List<String> missing) {
        if (!content.contains(expected)) {
            missing.add(expected);
        }
    }

    Path runRoot(String run) {
        return context.packageRoot().resolve(McpArtifactPaths.RUNS_ROOT).resolve(run);
    }

    String readRunFile(String run, String relativePath) throws IOException {
        return Files.readString(runRoot(run).resolve(relativePath));
    }

    List<Path> extentReports(String run) throws IOException {
        return McpHtmlAuditSupport.extentReports(runRoot(run));
    }

    List<Path> findHtml(Path directory) throws IOException {
        return McpHtmlAuditSupport.findHtml(directory);
    }

    void forEachRun(RunAudit audit) throws IOException {
        for (int index = 1; index <= context.runtime().metaIterationCount(); index++) {
            audit.accept(String.format("run_%02d", index));
        }
    }

    void compareXmlAttribute(String run, String xml, String attribute, int expected, List<String> mismatches) {
        Matcher matcher = xmlAttribute(attribute).matcher(xml);
        if (!matcher.find()) {
            mismatches.add(run + " missing XML attribute: " + attribute);
            return;
        }
        int actual = Integer.parseInt(matcher.group(1));
        if (actual != expected) {
            mismatches.add(run + " " + attribute + " expected " + expected + " but was " + actual);
        }
    }

    BigDecimal extractOptionalDelta(String score) {
        if (score == null) {
            return null;
        }
        Matcher matcher = ACTUAL_DELTA_QUALITY.matcher(score);
        if (!matcher.find()) {
            return null;
        }
        return new BigDecimal(matcher.group(1));
    }

    boolean isPackageArtifactProblem(McpPackageViolation violation) {
        if (!violation.code().equals("file.missing") && !violation.code().equals("file.empty")) {
            return false;
        }
        return violation.path() == null || !containsPathPart(violation, "runs");
    }

    boolean containsPathPart(McpPackageViolation violation, String part) {
        return violation.path() != null
                && violation.path().toString().replace('\\', '/').contains("/" + part + "/");
    }

    boolean containsAny(String content, String... values) {
        return McpTokenAssertions.containsAny(content, values);
    }

    private static Pattern xmlAttribute(String attribute) {
        return Pattern.compile(Pattern.quote(attribute) + "=\"(\\d+)\"");
    }
}
