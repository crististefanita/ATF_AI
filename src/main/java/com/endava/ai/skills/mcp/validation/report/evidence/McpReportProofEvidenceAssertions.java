package com.endava.ai.skills.mcp.validation.report.evidence;

import lombok.RequiredArgsConstructor;


import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.skills.mcp.validation.profile.McpArtifactSpineProfile;
import com.endava.ai.skills.mcp.validation.profile.McpReportSections;
import com.endava.ai.skills.mcp.validation.report.link.McpReportLinkProbe;
import com.endava.ai.skills.mcp.validation.support.McpHtmlAuditSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class McpReportProofEvidenceAssertions {
    private final McpPackageAuditValidation audit;

    public void assertProofMappingLinksAllMandatoryArtifactFamilies() throws IOException {
        String html = audit.readFinalReport();
        String proofSection = audit.detailsBody(html, McpReportSections.PROOF_MAPPING);
        String artifactSection = audit.detailsBody(html, McpReportSections.ARTIFACT_INDEX);
        List<String> blockers = new ArrayList<>();
        requireTokens(proofSection, McpArtifactSpineProfile.proofMappingTokens(),
                McpReportSections.PROOF_MAPPING, blockers);
        requireTokens(artifactSection, McpArtifactSpineProfile.artifactIndexTokens(),
                McpReportSections.ARTIFACT_INDEX, blockers);
        audit.assertNoSyntheticViolations("Final report proof and artifact link families", blockers, audit.finalReport(),
                "report.proof-family.missing",
                audit.details(
                        "Asserts: report exposes the full audit spine, not just a narrative summary.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected " + McpReportSections.PROOF_MAPPING + ": "
                                + String.join(", ", McpArtifactSpineProfile.proofMappingTokens()) + ".",
                        "Expected " + McpReportSections.ARTIFACT_INDEX + ": "
                                + String.join(", ", McpArtifactSpineProfile.artifactIndexTokens()) + "."));
    }

    public void assertProofMappingUsesBalancedTableColumns() throws IOException {
        String html = audit.readFinalReport();
        String proofSection = audit.detailsBody(html, McpReportSections.PROOF_MAPPING);
        List<String> blockers = new ArrayList<>();
        ProofTable table = proofTable(proofSection);
        if (table.headers.isEmpty() || table.rows.isEmpty()) {
            blockers.add("Proof Mapping must publish an auditable table with headers and data rows");
        }
        int sourceIndex = sourceColumnIndex(table.headers);
        if (sourceIndex < 0) {
            blockers.add("Proof Mapping table must expose a short Source/Business source column");
        } else {
            inspectSourceColumnBalance(table, sourceIndex, blockers);
        }
        audit.assertNoSyntheticViolations("Final report Proof Mapping balanced table columns",
                blockers, audit.finalReport(), "report.proof-mapping.source-column-too-wide",
                audit.details(
                        "Asserts: Proof Mapping remains scannable; no single Source column dominates the table.",
                        "Expected shape: the visible Source/Business source cell is a short business label.",
                        "Allowed audit detail: full source path may live in title, href, code tooltip or adjacent proof artifact.",
                        "Repair: replace long visible source paths or source inventories with labels such as 'account payment ledger', 'settlement handshake', or 'staff support response', then keep the complete Java path in a title attribute or proof link.",
                        "Why: long Source cells make the report visually harder to compare than the calibrated html-ex tables."));
    }

    public void assertRunAuditUsesStructuredEvidenceRows() throws IOException {
        String html = audit.readFinalReport();
        String runAudit = audit.detailsBody(html, McpReportSections.RUN_AUDIT);
        List<String> blockers = new ArrayList<>();
        if (!runAudit.contains("<table") && !runAudit.contains("run-audit-card")
                && !runAudit.contains("run-audit-grid")) {
            blockers.add("Run Audit must use an aligned table/card-grid, not a flat one-line bullet list");
        }
        audit.requireAnyToken(runAudit, List.of("diff", "thin-no-index.diff"),
                "Run Audit rows must link the run diff", blockers);
        audit.requireAnyToken(runAudit, List.of("Extent", "ExtentReport"),
                "Run Audit rows must link Extent proof", blockers);
        audit.requireAnyToken(runAudit, List.of("XML", "testng-results.xml", "TEST-"),
                "Run Audit rows must link machine XML proof", blockers);
        audit.requireToken(runAudit, "run-state.json",
                "Run Audit rows must link run-state.json", blockers);
        audit.assertNoSyntheticViolations("Final report Run Audit structured evidence rows",
                blockers, audit.finalReport(), "report.run-audit.structure",
                audit.details(
                        "Asserts: Run Audit is an aligned audit surface, not prose-only bookkeeping.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected shape: table or card/grid rows per run.",
                        "Expected links per row: run diff, Extent, XML, run-state."));
    }

    public void assertArtifactIndexUsesGroupedEvidenceFamilies() throws IOException {
        String html = audit.readFinalReport();
        String artifactSection = audit.detailsBody(html, McpReportSections.ARTIFACT_INDEX);
        List<String> blockers = new ArrayList<>();
        if (!artifactSection.contains("<table") && !artifactSection.contains("artifact-links")
                && !artifactSection.contains("artifact-family")) {
            blockers.add("Artifact Index must use a structured table/grid/family layout, not prose-only links");
        }
        requireFamily(artifactSection, "MCP close-out", List.of("MCP close-out", "MCP Gate", "validation/mcp"), blockers);
        requireFamily(artifactSection, "run evidence", List.of("Run evidence", "Run Audit", "runs/run_", "run_01"), blockers);
        requireFamily(artifactSection, "governance", List.of("Governance", "package-state", "source-evolution-ledger", "root-source-thin-no-index"), blockers);
        requireFamily(artifactSection, "KPI/calibration", List.of("KPI", "formula trace", "repair board", "score-cap"), blockers);
        requireNonFlatArtifactIndex(artifactSection, blockers);
        rejectGenericRepeatedStatuses(artifactSection, blockers);
        audit.assertNoSyntheticViolations("Final report Artifact Index grouped evidence families",
                blockers, audit.finalReport(), "report.artifact-index.family-grouping",
                audit.details(
                        "Asserts: Artifact Index is a reader-friendly evidence map, not a flat dump of hyperlinks.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected families: MCP close-out, run evidence, governance/source evolution, KPI/calibration.",
                        "Expected shape: a well-organized table or cards: caption/family headings, divider rows, rowspans, or artifact-family groups with meaningful status/provenance text.",
                        "Forbidden: one long flat Family/Artifact/Status table where every row repeats the family name and most statuses say only 'linked proof'."));
    }

    public void assertLinksAuditedFrameworkPropertiesSnapshot() throws IOException {
        String html = audit.readFinalReport();
        String artifactSection = audit.detailsBody(html, McpReportSections.ARTIFACT_INDEX);
        Path frameworkSnapshot = audit.context.packageRoot().resolve(McpArtifactPaths.FRAMEWORK_PROPERTIES_SNAPSHOT);
        String frameworkLink = McpArtifactPaths.linkFromFinalReport(McpArtifactPaths.FRAMEWORK_PROPERTIES_SNAPSHOT);
        List<String> blockers = new ArrayList<>();
        if (!Files.isRegularFile(frameworkSnapshot)) {
            blockers.add("Missing audited framework runtime snapshot: " + McpArtifactPaths.FRAMEWORK_PROPERTIES_SNAPSHOT);
        }
        McpReportLinkProbe reportLinks = McpReportLinkProbe.in(html);
        McpReportLinkProbe artifactLinks = McpReportLinkProbe.in(artifactSection);
        if (!reportLinks.hasHrefTo(frameworkLink)) {
            blockers.add("Final report must hyperlink the audited framework.properties snapshot as " + frameworkLink);
        }
        if (!artifactSection.contains("framework.properties")) {
            blockers.add("Artifact Index must name framework.properties because screenshot/Extent obligations depend on it");
        }
        if (!artifactLinks.hasHrefTo(frameworkLink)) {
            blockers.add("Artifact Index must hyperlink framework.properties as " + frameworkLink);
        }
        audit.assertNoSyntheticViolations("Final report audited framework.properties hyperlink",
                blockers, audit.finalReport(), "report.framework-properties-link.missing",
                audit.details(
                        "Asserts: final report exposes the exact framework runtime snapshot used by the package.",
                        "Expected artifact: " + McpArtifactPaths.FRAMEWORK_PROPERTIES_SNAPSHOT + ".",
                        "Expected link from " + McpArtifactPaths.FINAL_REPORT + ": " + frameworkLink + ".",
                        "Why: ui.screenshots.capture.final.state and Extent screenshot obligations are audited from this file."));
    }

    private void requireTokens(String html, List<String> tokens, String section, List<String> blockers) {
        for (String token : tokens) {
            audit.requireToken(html, token, section + " missing " + token, blockers);
        }
    }

    private void inspectSourceColumnBalance(ProofTable table, int sourceIndex, List<String> blockers) {
        for (int rowIndex = 0; rowIndex < table.rows.size(); rowIndex++) {
            List<Cell> row = table.rows.get(rowIndex);
            if (sourceIndex >= row.size()) {
                blockers.add("Proof Mapping row " + (rowIndex + 1)
                        + " has fewer cells than the header; all rows must match the table shape");
                continue;
            }
            Cell source = row.get(sourceIndex);
            String visibleSource = normalizeText(McpHtmlAuditSupport.stripTags(source.html));
            String lower = visibleSource.toLowerCase(Locale.ROOT);
            int longestOther = longestOtherVisibleCell(row, sourceIndex);
            List<String> reasons = new ArrayList<>();
            if (visibleSource.length() > 72) {
                reasons.add("visible length " + visibleSource.length() + " > 72");
            }
            if (looksLikeVisibleSourcePath(lower)) {
                reasons.add("raw source path is visible");
            }
            if (visibleSource.length() > 48 && visibleSource.length() > Math.max(60, Math.round(longestOther * 1.2f))) {
                reasons.add("dominates row data: " + visibleSource.length()
                        + " chars vs next longest " + longestOther);
            }
            if (visibleSource.length() > 44 && !hasHiddenAuditDetail(source.html)) {
                reasons.add("long cell has no title/link/code audit detail");
            }
            if (!reasons.isEmpty()) {
                blockers.add("Proof Mapping source cell in row " + (rowIndex + 1)
                        + " is not scan-friendly (" + String.join("; ", reasons)
                        + "): '" + preview(visibleSource)
                        + "'. Use a short business label and move full source detail to title/href/code.");
            }
        }
    }

    private ProofTable proofTable(String proofSection) {
        Matcher tableMatcher = Pattern.compile("(?is)<table\\b[^>]*>(.*?)</table>").matcher(proofSection);
        if (!tableMatcher.find()) {
            return new ProofTable(List.of(), List.of());
        }
        String table = tableMatcher.group(1);
        List<String> headers = new ArrayList<>();
        Matcher headerMatcher = Pattern.compile("(?is)<th\\b[^>]*>(.*?)</th>").matcher(table);
        while (headerMatcher.find()) {
            headers.add(normalizeText(McpHtmlAuditSupport.stripTags(headerMatcher.group(1))));
        }
        List<List<Cell>> rows = new ArrayList<>();
        Matcher rowMatcher = Pattern.compile("(?is)<tr\\b[^>]*>(.*?)</tr>").matcher(table);
        while (rowMatcher.find()) {
            String rowHtml = rowMatcher.group(1);
            if (rowHtml.toLowerCase(Locale.ROOT).contains("<th")) {
                continue;
            }
            List<Cell> cells = new ArrayList<>();
            Matcher cellMatcher = Pattern.compile("(?is)<td\\b[^>]*>(.*?)</td>").matcher(rowHtml);
            while (cellMatcher.find()) {
                cells.add(new Cell(cellMatcher.group(1)));
            }
            if (!cells.isEmpty()) {
                rows.add(cells);
            }
        }
        return new ProofTable(headers, rows);
    }

    private int sourceColumnIndex(List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i).toLowerCase(Locale.ROOT);
            if (header.equals("source") || header.equals("business source") || header.contains("source")) {
                return i;
            }
        }
        return -1;
    }

    private int longestOtherVisibleCell(List<Cell> row, int sourceIndex) {
        int longest = 0;
        for (int i = 0; i < row.size(); i++) {
            if (i == sourceIndex) {
                continue;
            }
            longest = Math.max(longest, normalizeText(McpHtmlAuditSupport.stripTags(row.get(i).html)).length());
        }
        return longest;
    }

    private boolean looksLikeVisibleSourcePath(String text) {
        return text.contains("src/test/java")
                || text.contains("src\\test\\java")
                || text.contains(".java")
                || text.matches(".*com[./\\\\]endava[./\\\\]ai.*");
    }

    private boolean hasHiddenAuditDetail(String html) {
        String lower = html.toLowerCase(Locale.ROOT);
        return lower.contains(" title=")
                || lower.contains(" href=")
                || lower.contains("<code")
                || lower.contains("data-full-text");
    }

    private String normalizeText(String value) {
        return value.replaceAll("\\s+", " ").trim();
    }

    private String preview(String value) {
        return value.length() <= 96 ? value : value.substring(0, 93) + "...";
    }

    private void requireFamily(String artifactSection, String family, List<String> tokens, List<String> blockers) {
        boolean present = tokens.stream().anyMatch(artifactSection::contains);
        if (!present) {
            blockers.add("Artifact Index must expose a grouped " + family + " family; expected one of "
                    + String.join(", ", tokens));
        }
    }

    private void requireNonFlatArtifactIndex(String artifactSection, List<String> blockers) {
        boolean hasFamilyCards = artifactSection.contains("artifact-family");
        boolean hasFamilyHeadings = Pattern.compile("(?is)<(h3|h4|caption|summary|strong)\\b[^>]*>\\s*(MCP close-out|Run evidence|Governance|KPI)")
                .matcher(artifactSection).find();
        boolean hasRowspanGrouping = Pattern.compile("(?is)<td\\b[^>]*rowspan=").matcher(artifactSection).find();
        boolean hasDividerRows = Pattern.compile("(?is)<(td|th)\\b[^>]*(colspan|scope=[\"']rowgroup[\"'])[^>]*>\\s*(MCP close-out|Run evidence|Governance|KPI)")
                .matcher(artifactSection).find();
        boolean flatFamilyTable = Pattern.compile("(?is)<th>\\s*Family\\s*</th>\\s*<th>\\s*Artifact\\s*</th>\\s*<th>\\s*Status\\s*</th>")
                .matcher(artifactSection).find();
        if (flatFamilyTable && !hasFamilyCards && !hasFamilyHeadings && !hasRowspanGrouping && !hasDividerRows) {
            blockers.add("Artifact Index is a flat Family/Artifact/Status table; keep the table if desired, but group families with caption/headings, divider rows, rowspans, or artifact-family blocks");
        }
    }

    private void rejectGenericRepeatedStatuses(String artifactSection, List<String> blockers) {
        int linkCount = count(Pattern.compile("(?is)<a\\b"), artifactSection);
        int linkedProofCount = count(Pattern.compile("(?is)>\\s*linked proof\\s*<"), artifactSection);
        if (linkCount >= 8 && linkedProofCount > Math.max(6, linkCount / 2)) {
            blockers.add("Artifact Index repeats generic status 'linked proof' for "
                    + linkedProofCount + " of " + linkCount
                    + " links; status cells must explain PASS/count/provenance/freshness instead of repeating filler text");
        }
    }

    private int count(Pattern pattern, String text) {
        int count = 0;
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static final class ProofTable {
        private final List<String> headers;
        private final List<List<Cell>> rows;

        private ProofTable(List<String> headers, List<List<Cell>> rows) {
            this.headers = headers;
            this.rows = rows;
        }
    }

    private static final class Cell {
        private final String html;

        private Cell(String html) {
            this.html = html == null ? "" : html;
        }
    }
}
