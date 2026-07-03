package com.ai.skills.mcp.validation.report;

import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.ai.skills.mcp.validation.profile.McpReportTemplateProfile;
import com.ai.skills.mcp.validation.profile.html.McpReportTemplateConsensus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class McpReportVisualShellAssertions {
    private final McpPackageAuditValidation audit;

    void assertAvoidsPlainWhiteDefaultTheme() throws IOException {
        String css = Files.readString(audit.context.packageRoot().resolve("analysis/shared-report-reference.css"));
        String normalized = css.toLowerCase(Locale.ROOT).replace(" ", "");
        List<String> blockers = new ArrayList<>();
        if (normalized.contains("body{") && audit.containsAny(normalized,
                "background:#fff", "background:white", "background:#ffffff", "background:#fafafa")) {
            blockers.add("Body background still looks like a plain white/default document shell");
        }
        if (audit.containsAny(normalized,
                "details{border:1pxsolid#c9d2dc;padding:12px;margin:12px0;background:#fff",
                "details{background:#fff",
                "details{background:white",
                "details{background:#ffffff")) {
            blockers.add("Details/cards still use plain white panel styling");
        }
        if (!audit.containsAny(normalized, "box-shadow", "linear-gradient", "radial-gradient", "border-radius")) {
            blockers.add("Shared CSS lacks any intentional visual treatment: box-shadow, gradient, or border-radius");
        }
        blockers.addAll(audit.canonicalSharedCssBlockers(css));
        audit.assertNoSyntheticViolations("Final report non-plain-white visual theme", blockers, audit.finalReport(),
                "report.visual-theme.plain-white",
                audit.details(
                        "Asserts: final report is not a plain white/default browser document.",
                        "Artifact: analysis/shared-report-reference.css.",
                        "Expected: non-white page atmosphere, non-white/intentional panels, and at least one visual treatment.",
                        "Accepted treatment tokens: box-shadow, gradient, or border-radius."));
    }

    void assertUsesHtmlExampleVisualShell() throws IOException {
        String html = audit.readFinalReport();
        McpReportTemplateConsensus reference = McpReportTemplateProfile.htmlExConsensus();
        McpReportTemplateProfile actual = McpReportTemplateProfile.fromHtml(html);
        List<String> blockers = new ArrayList<>();
        for (String required : List.of("wrap", "hero", "score-wall", "score-card")) {
            if (!actual.visualClasses().contains(required)) {
                blockers.add("Missing required report shell class: " + required);
            }
        }
        audit.requireToken(html, "<header", "Missing report header/hero header", blockers);
        blockers.addAll(missing("reusable visual classes", reference.visualClasses(), actual.visualClasses()));
        audit.assertNoSyntheticViolations("Final report html-ex visual shell", blockers, audit.finalReport(),
                "report.visual-shell.missing",
                audit.details(
                        "Asserts: final report uses the reusable html-ex report shell, not browser-default document flow.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Fixed invariants: wrap, header, hero, score-wall and score-card.",
                        "Reference-driven classes: " + reference.visualClasses() + ".",
                        "Reference corpus: " + reference.sources() + "."));
    }

    private static List<String> missing(String label, List<String> expected, List<String> actual) {
        List<String> blockers = new ArrayList<>();
        for (String value : expected) {
            if (!actual.contains(value)) {
                blockers.add(label + " missing value from primary html-ex profile: " + value);
            }
        }
        return blockers;
    }
}
