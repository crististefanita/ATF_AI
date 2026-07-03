package com.ai.skills.mcp.rules.report;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.McpViolationSeverity;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.html.HtmlDetailsParser;
import com.ai.skills.mcp.html.HtmlScoreExtractor;
import com.ai.skills.mcp.html.HtmlSectionFinder;
import com.ai.skills.mcp.rules.McpRuleSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;

final class ReportRuleSupport {
    static final HtmlSectionFinder SECTIONS = new HtmlSectionFinder();
    static final HtmlDetailsParser DETAILS = new HtmlDetailsParser();
    static final HtmlScoreExtractor SCORES = new HtmlScoreExtractor();

    private ReportRuleSupport() {
    }

    static boolean hasStructuralBlockers(McpPackageValidationResult result) {
        return result.violations().stream()
                .anyMatch(violation -> violation.severity() == McpViolationSeverity.BLOCKER
                        && !violation.code().startsWith("score.cap.")
                        && !isRegenerableMcpCloseoutEvidence(violation.code()));
    }

    private static boolean isRegenerableMcpCloseoutEvidence(String code) {
        return code.startsWith("mcp.extent.")
                || code.startsWith("mcp.method-ledger.")
                || code.startsWith("mcp.evidence-counts.")
                || code.startsWith("mcp.gate-result.")
                || code.startsWith("mcp.xml.")
                || code.startsWith("mcp.skip-reason.");
    }

    static void requireForbiddenWordsAbsent(String html, Path report, McpPackageValidationResult result) {
        String lower = html.toLowerCase(Locale.ROOT);
        for (String forbidden : ContractCatalog.reportTemplateTokens("forbidden-calibration-token")) {
            if (lower.contains(forbidden)) {
                result.add(McpPackageViolation.blocker("report.external-calibration-leak",
                        "Final report must not mention external calibration word: " + forbidden, report));
            }
        }
    }

    static void requireUnverifiableLanguageAbsent(String html, Path report, McpPackageValidationResult result) {
        String text = html.replaceAll("(?is)<a\\b[^>]*>.*?</a>", "[linked-proof]")
                .replaceAll("(?is)<[^>]+>", " ");
        for (String sentence : text.split("[.!?]\\s+")) {
            String lower = sentence.toLowerCase(Locale.ROOT);
            boolean highConfidence = McpRuleSupport.containsAny(lower,
                    "fully proven", "fully owned", "complete", "production ready", "ready to replace");
            boolean hasEvidenceWord = McpRuleSupport.containsAny(lower,
                    "diff", "extent", "xml", "run-state", "package-state",
                    "proof", "evidence", "[linked-proof]", "partial", "blocked", "capped");
            if (highConfidence && !hasEvidenceWord) {
                result.add(McpPackageViolation.blocker("report.unverifiable-language",
                        "High-confidence report language must be tied to evidence or a cap/partial verdict: "
                                + sentence.trim(),
                        report));
            }
        }
    }

    static void requireArtifactFreshness(McpPackageContext context, Path report, McpPackageValidationResult result) {
        for (Path artifact : java.util.List.of(context.paths().renderProof(),
                context.packageRoot().resolve("review/local-link-check.txt"),
                context.paths().packageState())) {
            if (!Files.isRegularFile(artifact)) {
                continue;
            }
            try {
                var reportTime = Files.getLastModifiedTime(report).toInstant();
                var artifactTime = Files.getLastModifiedTime(artifact).toInstant();
                if (artifactTime.plus(Duration.ofSeconds(60)).isBefore(reportTime)) {
                    result.add(McpPackageViolation.warning("artifact.freshness.stale",
                            "Artifact appears older than final report; refresh it after report edits", artifact));
                }
            } catch (IOException exception) {
                result.add(McpPackageViolation.warning("artifact.freshness.unreadable",
                        exception.getMessage(), artifact));
            }
        }
    }
}
