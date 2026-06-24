package com.endava.ai.mcp.validation.report;

import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.mcp.validation.profile.McpReportSections;
import com.endava.ai.mcp.validation.profile.McpReportTemplateProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class McpReportBrowserAssertions {
    private final McpPackageAuditValidation audit;

    public McpReportBrowserAssertions(McpPackageAuditValidation audit) {
        this.audit = audit;
    }

    public void assertFinalReportUsesSharedCssAndDocumentChrome() throws IOException {
        String html = audit.readFinalReport();
        String css = Files.readString(audit.context.packageRoot().resolve("analysis/shared-report-reference.css"));
        List<String> blockers = new ArrayList<>();
        audit.requireAnyToken(html, List.of("<!doctype html", "<!DOCTYPE html"),
                "Missing <!doctype html>", blockers);
        audit.requireToken(html, "<meta charset=\"utf-8\"", "Missing UTF-8 meta charset", blockers);
        audit.requireAnyToken(html, List.of("name=\"viewport\"", "name='viewport'"),
                "Missing responsive viewport meta tag", blockers);
        audit.requireToken(html, "<title>", "Missing HTML title", blockers);
        audit.requireToken(html, "href=\"shared-report-reference.css\"",
                "Missing shared-report-reference.css stylesheet link", blockers);
        audit.requireAnyToken(css, List.of("body", "details", "summary"),
                "Shared CSS must style body/details/summary report shell", blockers);
        audit.requireAnyToken(css, List.of(".graph", "svg", ".business", ".score"),
                "Shared CSS must style graph/score/business presentation classes", blockers);
        blockers.addAll(audit.canonicalSharedCssBlockers(css));
        audit.assertNoSyntheticViolations("Final report shared CSS and document chrome", blockers, audit.finalReport(),
                "report.chrome.missing",
                audit.details(
                        "Asserts: final report has reusable document shell and shared CSS styling.",
                        "Artifacts: analysis/langgraph-business-understanding.html and analysis/shared-report-reference.css.",
                        "Expected HTML: doctype, UTF-8 charset, viewport meta, title, stylesheet link.",
                        "Expected CSS: body/details/summary plus graph/score/business styling."));
    }

    public void assertFinalReportAvoidsPlainWhiteDefaultTheme() throws IOException {
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

    public void assertFinalReportUsesHtmlExampleVisualShell() throws IOException {
        String html = audit.readFinalReport();
        McpReportTemplateProfile reference = McpReportTemplateProfile.primaryReference();
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
                        "Reference-driven classes: " + reference.visualClasses() + "."));
    }

    public void assertFinalReportOpeningHasVisiblePillRow() throws IOException {
        String html = audit.readFinalReport();
        List<String> blockers = new ArrayList<>();
        int hero = html.indexOf("class=\"hero\"");
        int executive = html.indexOf(McpReportSections.EXECUTIVE_CONTEXT);
        if (hero < 0 || executive < 0 || hero > executive) {
            blockers.add("Cannot locate opening hero before Executive Context");
        } else {
            String opening = html.substring(hero, executive);
            if (!opening.contains("class=\"pill-row\"") && !opening.contains("class='pill-row'")) {
                blockers.add("Opening must expose a visible pill-row before Executive Context; hidden runtime strips inside collapsed sections do not count");
            }
            if (!opening.contains("class=\"pill\"") && !opening.contains("class='pill'")) {
                blockers.add("Opening pill-row must contain visible .pill items for business/audit orientation");
            }
        }
        audit.assertNoSyntheticViolations("Final report opening visible pill row",
                blockers, audit.finalReport(), "report.opening.pill-row.missing",
                audit.details(
                        "Asserts: opening has visible navigation/calibration pills, not only collapsed runtime details.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected location: after hero/score opening and before Executive Context.",
                        "Expected classes: pill-row and pill."));
    }

    public void assertFinalReportDoesNotExposeRootRunTimestampIdentity() throws IOException {
        String html = audit.readFinalReport();
        List<String> blockers = new ArrayList<>();
        if (html.contains("ROOT_RUN")) {
            blockers.add("Final business report leaks ROOT_RUN identity; timestamp belongs in artifacts, not in the page story");
        }
        Matcher timestamp = McpPackageAuditValidation.ROOT_RUN_TIMESTAMP.matcher(html);
        if (timestamp.find()) {
            blockers.add("Final business report leaks raw ROOT_RUN timestamp: " + timestamp.group());
        }
        audit.assertNoSyntheticViolations("Final report hides raw ROOT_RUN identity", blockers, audit.finalReport(),
                "report.root-run.identity-leak",
                audit.details(
                        "Asserts: final report is business-readable and does not title itself by ROOT_RUN/timestamp.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected: package identity is expressed as business frontier; raw timestamp remains in filesystem/artifacts only."));
    }

    public void assertSharedCssLooksLikeCanonicalReferenceStylesheet() throws IOException {
        Path css = audit.context.packageRoot().resolve("analysis/shared-report-reference.css");
        List<String> blockers = audit.canonicalSharedCssBlockers(Files.readString(css));
        audit.assertNoSyntheticViolations("Final report shared CSS canonical reference contract", blockers, css,
                "report.css.reference-drift",
                audit.details(
                        "Asserts: shared-report-reference.css reuses the canonical html-ex visual vocabulary.",
                        "Artifact: analysis/shared-report-reference.css.",
                        "Expected: canonical variables, page shell, collapsible details, score primitives, graph typography, note/warn/rule helpers."));
    }

    public void assertAnalysisSharedCssIsExactCopyOfProjectReference() throws IOException {
        Path projectReference = Path.of(System.getProperty("mcp.project.root", "."))
                .toAbsolutePath()
                .normalize()
                .resolve("docs/shared-report-reference.css");
        Path packageReference = audit.context.packageRoot().resolve("analysis/shared-report-reference.css");
        List<String> blockers = new ArrayList<>();
        if (!Files.isRegularFile(projectReference)) {
            blockers.add("Missing project-level shared CSS reference: " + projectReference);
        }
        if (!Files.isRegularFile(packageReference)) {
            blockers.add("Missing package-level shared CSS copy: " + packageReference);
        }
        if (blockers.isEmpty()) {
            String expected = Files.readString(projectReference).replace("\r\n", "\n");
            String actual = Files.readString(packageReference).replace("\r\n", "\n");
            if (!actual.equals(expected)) {
                blockers.add("analysis/shared-report-reference.css is not an exact copy of docs/shared-report-reference.css");
            }
        }
        audit.assertNoSyntheticViolations("Final report shared CSS exact reuse", blockers, packageReference,
                "report.css.shared-reference-copy-drift",
                audit.details(
                        "Asserts: package report reuses the project shared CSS verbatim, instead of inventing a local visual system.",
                        "Project artifact: docs/shared-report-reference.css.",
                        "Package artifact: analysis/shared-report-reference.css.",
                        "Allowed difference: line endings only."));
    }

    public void assertFinalReportDoesNotUseInlineStyleOverrides() throws IOException {
        String html = audit.readFinalReport();
        List<String> blockers = new ArrayList<>();
        if (html.toLowerCase(Locale.ROOT).contains("<style")) {
            blockers.add("Final report contains an inline <style> block");
        }
        if (Pattern.compile("(?i)\\sstyle\\s*=").matcher(html).find()) {
            blockers.add("Final report contains inline style= attributes");
        }
        audit.assertNoSyntheticViolations("Final report has no inline CSS overrides", blockers, audit.finalReport(),
                "report.css.inline-override",
                audit.details(
                        "Asserts: report visual design is controlled by shared-report-reference.css only.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Forbidden: inline <style> blocks and style= attributes.",
                        "Reason: agents must reuse the shared template, not patch visual drift locally."));
    }
    private List<String> missing(String label, List<String> expected, List<String> actual) {
        List<String> blockers = new ArrayList<>();
        for (String value : expected) {
            if (!actual.contains(value)) {
                blockers.add(label + " missing value from primary html-ex profile: " + value);
            }
        }
        return blockers;
    }
}
