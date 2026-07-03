package com.ai.skills.mcp.rules.business;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpClaimEvidence;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.context.McpRunArtifacts;
import com.ai.skills.mcp.context.McpSourceAuditScope;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.rules.McpRuleSupport;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

final class ClaimActionEvidenceFactory {
    private static final Pattern DIFF_SOURCE_FILE = Pattern.compile("(?m)^\\+\\+\\+ b/(source/[^\\r\\n]+|src/[^\\r\\n]+)");
    private ClaimActionEvidenceFactory() {
    }

    static McpClaimEvidence create(McpSourceAuditScope sourceAuditScope,
                                   McpPackageContext context,
                                   McpPackageValidationResult result,
                                   Path runDirectory,
                                   String normalizedReport) {
        Path runState = runDirectory.resolve(McpRunArtifacts.STATE);
        Path runDiff = runDirectory.resolve(McpRunArtifacts.DIFF);
        String state = context.files().readIfRegular(runState, result);
        String diff = context.files().readIfRegular(runDiff, result);
        String claim = firstPresent(
                McpRuleSupport.extractStringValue(state, "claim"),
                McpRuleSupport.extractStringValue(state, "progression"),
                McpRuleSupport.extractStringValue(state, "notes"));
        String businessObject = McpRuleSupport.extractStringValue(state, "business_object");
        String status = firstPresent(
                McpRuleSupport.extractStringValue(state, "verdict"),
                McpRuleSupport.extractStringValue(state, "target_status"));
        String verb = detectVerb(claim + " " + businessObject + " " + status);
        List<Path> sources = sourceFiles(sourceAuditScope, context, diff);
        String sourceText = sources.stream()
                .map(path -> context.files().readIfRegular(path, result))
                .collect(Collectors.joining("\n"));
        List<Path> machineProofs = List.of(
                runDirectory.resolve(McpRunArtifacts.TESTNG_RESULTS),
                runDirectory.resolve(McpRunArtifacts.COMMAND_RESULT));
        Path extent = runDirectory.resolve(McpRunArtifacts.REPORTS_ROOT).resolve("ExtentReport.html");
        String proofText = context.files().readIfRegular(machineProofs.get(0), result)
                + "\n" + context.files().readIfRegular(machineProofs.get(1), result)
                + "\n" + context.files().readIfRegular(extent, result);
        String proofMappingRow = proofMappingSlice(normalizedReport, runDirectory.getFileName().toString());
        return new McpClaimEvidence(claim, verb, businessObject, runDirectory.getFileName().toString(),
                runState, runDiff, sources, machineProofs, extent, proofMappingRow, status,
                normalize(diff + "\n" + sourceText), normalize(proofText + "\n" + proofMappingRow));
    }

    private static List<Path> sourceFiles(McpSourceAuditScope sourceAuditScope, McpPackageContext context, String diff) {
        List<Path> files = new ArrayList<>();
        Matcher matcher = DIFF_SOURCE_FILE.matcher(diff);
        while (matcher.find()) {
            Path path = context.packageRoot().resolve(matcher.group(1)).normalize();
            if (Files.isRegularFile(path)) {
                files.add(path);
                continue;
            }
            Path projectPath = sourceAuditScope.sourceRoot()
                    .resolve(matcher.group(1).replaceFirst("^source/", ""))
                    .normalize();
            if (Files.isRegularFile(projectPath)) {
                files.add(projectPath);
            }
        }
        return files.stream().distinct().collect(Collectors.toList());
    }

    private static String proofMappingSlice(String normalizedReport, String runId) {
        int start = normalizedReport.indexOf(runId.toLowerCase(Locale.ROOT));
        if (start < 0) {
            return "";
        }
        int end = normalizedReport.indexOf("</tr>", start);
        if (end < 0) {
            end = Math.min(normalizedReport.length(), start + 900);
        }
        return normalizedReport.substring(start, Math.min(normalizedReport.length(), end + 5));
    }

    private static String detectVerb(String text) {
        String normalized = normalize(text);
        for (String verb : claimActionVerbs()) {
            if (normalized.contains(verb)) {
                return verb;
            }
        }
        return "";
    }

    static List<String> claimActionVerbs() {
        return ContractCatalog.claimActionContracts().stream()
                .flatMap(spec -> spec.verbs().stream())
                .distinct()
                .sorted((left, right) -> Integer.compare(right.length(), left.length()))
                .collect(Collectors.toUnmodifiableList());
    }

    private static String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    static String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
