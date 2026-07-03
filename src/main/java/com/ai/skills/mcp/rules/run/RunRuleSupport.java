package com.ai.skills.mcp.rules.run;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.context.McpRunArtifacts;
import com.ai.skills.mcp.validation.run.McpTestNgXmlMethods;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

final class RunRuleSupport {
    static final int MIN_EXTENT_REPORT_CHARS = 10_000;
    static final int MIN_MEANINGFUL_BUSINESS_WORD_LENGTH = 5;
    static final Pattern REPEATED_PASS_PLACEHOLDER = Pattern.compile("(?i)\\bpass\\s+pass\\s+pass\\s+pass\\s+pass\\b");
    static final Pattern LOCAL_SPARK_ASSET = Pattern.compile("(?i)(?:href|src)=[\"']([^\"']*spark/[^\"']+)[\"']");

    private RunRuleSupport() {
    }

    static List<Path> runDirectories(McpPackageContext context, McpPackageValidationResult result) {
        return context.files().runDirectories(context.paths().runsRoot(), result);
    }

    static List<Path> extentReports(McpPackageContext context, Path runDirectory, McpPackageValidationResult result) {
        Path validation = runDirectory.resolve(McpRunArtifacts.VALIDATION_ROOT);
        Path reports = runDirectory.resolve(McpRunArtifacts.REPORTS_ROOT);
        List<Path> extentReports = new ArrayList<>(new LinkedHashSet<>(context.files().findFiles(reports, ".html", result)));
        context.files().findFiles(validation, ".html", result).stream()
                .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).contains("extentreport"))
                .forEach(path -> {
                    if (!extentReports.contains(path)) {
                        extentReports.add(path);
                    }
                });
        return extentReports;
    }

    static List<Path> canonicalExtentReports(McpPackageContext context, Path runDirectory, McpPackageValidationResult result) {
        Path reports = runDirectory.resolve(McpRunArtifacts.REPORTS_ROOT);
        List<Path> reportCopies = new ArrayList<>(new LinkedHashSet<>(context.files().findFiles(reports, ".html", result)));
        return reportCopies.isEmpty() ? extentReports(context, runDirectory, result) : reportCopies;
    }

    static Set<String> passedXmlMethodNames(String xml) {
        return McpTestNgXmlMethods.passedRealMethodNames(xml);
    }
}
