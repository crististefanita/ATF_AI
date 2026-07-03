package com.endava.ai.skills.mcp.rules.source.inventory;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Final-report facts that should be visible to a reviewer without memory.
 */
@Getter
@Accessors(fluent = true)
public final class ReportEvidenceInventory {
    private static final Pattern HREF = Pattern.compile("(?is)\\bhref\\s*=\\s*['\"]([^'\"]+)['\"]");
    private static final Pattern CLAIM_ROW = Pattern.compile("(?is)<(?:tr|li|article|section)\\b[^>]*>(.*?)</(?:tr|li|article|section)>");

    private final int businessClaimsOwned;
    private final int finalReportMcpLinkCompleteness;
    private final int scoreDeltaJustificationScore;

    private ReportEvidenceInventory(int businessClaimsOwned,
                                    int finalReportMcpLinkCompleteness,
                                    int scoreDeltaJustificationScore) {
        this.businessClaimsOwned = businessClaimsOwned;
        this.finalReportMcpLinkCompleteness = finalReportMcpLinkCompleteness;
        this.scoreDeltaJustificationScore = scoreDeltaJustificationScore;
    }

    public static ReportEvidenceInventory load(McpPackageContext context, McpPackageValidationResult result) {
        String report = readPackageFile(context, result, McpArtifactPaths.FINAL_REPORT);
        if (report.isBlank()) {
            return new ReportEvidenceInventory(0, 0, 0);
        }
        Set<String> links = hrefs(report);
        return new ReportEvidenceInventory(
                businessClaimsOwned(report),
                finalReportMcpLinkCompleteness(report, links),
                scoreDeltaJustificationScore(context, result, report)
        );
    }

    private static int businessClaimsOwned(String report) {
        int ownedRows = 0;
        Matcher matcher = CLAIM_ROW.matcher(report.toLowerCase(Locale.ROOT));
        while (matcher.find()) {
            String row = stripTags(matcher.group(1));
            if (row.contains("owned") && (row.contains("claim") || row.contains("business") || row.contains("object"))) {
                ownedRows++;
            }
        }
        return ownedRows;
    }

    private static int finalReportMcpLinkCompleteness(String report, Set<String> links) {
        String normalizedReport = normalize(report);
        String[] required = {
                McpArtifactPaths.ROOT_DIFF,
                McpArtifactPaths.SOURCE_EVOLUTION_LEDGER,
                McpArtifactPaths.PACKAGE_STATE,
                McpArtifactPaths.MCP_GATE_RESULT,
                McpArtifactPaths.MCP_EXTENT_REPORT,
                McpArtifactPaths.MCP_TESTNG_XML,
                McpArtifactPaths.MCP_METHOD_LEDGER,
                McpArtifactPaths.MCP_KPI_MATRIX,
                McpArtifactPaths.MCP_KPI_MATRIX_HTML,
                McpArtifactPaths.MCP_KPI_FORMULA_TRACE,
                McpArtifactPaths.MCP_KPI_REPAIR_BOARD,
                McpArtifactPaths.MCP_KPI_REPAIR_BOARD_HTML,
                McpArtifactPaths.MCP_SCORE_CAP_RATIONALE
        };
        int present = 0;
        for (String artifact : required) {
            if (linksContain(links, artifact) || normalizedReport.contains(normalize(artifact))) {
                present++;
            }
        }
        return percentage(present, required.length);
    }

    private static int scoreDeltaJustificationScore(McpPackageContext context,
                                                    McpPackageValidationResult result,
                                                    String report) {
        String evidence = (report + "\n"
                + readPackageFile(context, result, McpArtifactPaths.SOURCE_EVOLUTION_LEDGER))
                .toLowerCase(Locale.ROOT);
        int present = 0;
        for (String token : new String[]{"score_delta", "cap", "blocker", "repair", "mcp-kpi-matrix"}) {
            if (evidence.contains(token)) {
                present++;
            }
        }
        return percentage(present, 5);
    }

    private static Set<String> hrefs(String html) {
        Set<String> links = new LinkedHashSet<>();
        Matcher matcher = HREF.matcher(html);
        while (matcher.find()) {
            links.add(normalize(matcher.group(1)));
        }
        return links;
    }

    private static boolean linksContain(Set<String> links, String artifact) {
        String normalized = normalize(artifact);
        String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
        return links.stream().anyMatch(link -> link.endsWith(normalized) || link.endsWith(fileName));
    }

    private static String stripTags(String html) {
        return html.replaceAll("(?is)<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace('\\', '/');
    }

    private static int percentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return numerator == 0 ? 0 : 100;
        }
        return Math.round((numerator * 100.0f) / denominator);
    }

    private static String readPackageFile(McpPackageContext context,
                                          McpPackageValidationResult result,
                                          String relativePath) {
        Path path = context.packageRoot().resolve(relativePath);
        if (!Files.isRegularFile(path)) {
            return "";
        }
        return context.files().read(path, result);
    }
}
