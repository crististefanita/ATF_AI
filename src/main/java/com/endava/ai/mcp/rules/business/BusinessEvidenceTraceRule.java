package com.endava.ai.mcp.rules.business;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.context.McpRunArtifacts;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@com.endava.ai.mcp.contract.McpContract(id = "business.evidence.trace",
        category = com.endava.ai.mcp.contract.McpRuleCategory.PACKAGE,
        htmlReq = "business-flow-graph-rubric.html",
        artifact = "state -> diff -> test -> proof -> graph -> score")
public final class BusinessEvidenceTraceRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }

        String state = context.files().read(packageState, result);
        String businessObject = McpRuleSupport.extractStringValue(state, "business_object");
        String frontier = McpRuleSupport.extractStringValue(state, "new_frontier");

        requireTrace(context, result, "business-object", businessObject, packageState);
        requireTrace(context, result, "business-frontier", frontier, packageState);
    }

    private void requireTrace(McpPackageContext context,
                              McpPackageValidationResult result,
                              String label,
                              String value,
                              Path source) {
        Set<String> tokens = meaningfulTokens(value);
        if (tokens.isEmpty()) {
            return;
        }

        String report = normalize(context.files().readIfRegular(context.paths().report(), result));
        String graph = normalize(reportSection(report, "business flow graph"));
        String proofMapping = normalize(reportSection(report, "proof mapping"));
        String runStates = normalize(readRunArtifacts(context, result, McpRunArtifacts.STATE));
        String runDiffs = normalize(readRunArtifacts(context, result, McpRunArtifacts.DIFF));
        String executionProof = normalize(readRunArtifacts(context, result,
                McpRunArtifacts.TESTNG_RESULTS,
                McpRunArtifacts.COMMAND_RESULT));

        requireTokenPresence(tokens, report, "final report", label, source, result);
        requireTokenPresence(tokens, graph, "Business Flow Graph section", label, source, result);
        requireTokenPresence(tokens, proofMapping, "Proof Mapping section", label, source, result);
        requireTokenPresence(tokens, runStates, "run-state business handoff", label, source, result);
        requireTokenPresence(tokens, runDiffs, "run/root diff history", label, source, result);
        requireTokenPresence(tokens, executionProof, "machine execution proof", label, source, result);
    }

    private void requireTokenPresence(Set<String> tokens,
                                      String haystack,
                                      String artifactFamily,
                                      String label,
                                      Path source,
                                      McpPackageValidationResult result) {
        boolean found = tokens.stream().anyMatch(haystack::contains);
        if (!found) {
            result.add(McpPackageViolation.blocker("business-evidence." + label + ".missing-in-artifact-family",
                    label + " must be traceable into " + artifactFamily + ". Expected one of: " + tokens,
                    source));
        }
    }

    private String readRunArtifacts(McpPackageContext context,
                                    McpPackageValidationResult result,
                                    String... relativeArtifacts) {
        StringBuilder builder = new StringBuilder();
        for (Path runDirectory : context.files().runDirectories(context.paths().runsRoot(), result)) {
            for (String artifact : relativeArtifacts) {
                builder.append('\n').append(context.files().readIfRegular(runDirectory.resolve(artifact), result));
            }
        }
        return builder.toString();
    }

    private Set<String> meaningfulTokens(String value) {
        if (value == null) {
            return Set.of();
        }
        Set<String> stopWords = Set.of("state", "object", "business", "frontier", "memory", "owned", "real");
        return Arrays.stream(value.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(token -> token.length() >= 5)
                .filter(token -> !stopWords.contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }

    private String reportSection(String normalizedReport, String sectionTitle) {
        int start = normalizedReport.indexOf(sectionTitle);
        if (start < 0) {
            return "";
        }
        int next = normalizedReport.indexOf("<details", start + sectionTitle.length());
        return next > start ? normalizedReport.substring(start, next) : normalizedReport.substring(start);
    }
}
