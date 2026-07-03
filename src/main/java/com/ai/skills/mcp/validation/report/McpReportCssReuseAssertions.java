package com.ai.skills.mcp.validation.report;

import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.ai.skills.mcp.context.McpProjectLocator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class McpReportCssReuseAssertions {
    private final McpPackageAuditValidation audit;

    void assertLooksLikeCanonicalReferenceStylesheet() throws IOException {
        Path css = audit.context.packageRoot().resolve("analysis/shared-report-reference.css");
        List<String> blockers = audit.canonicalSharedCssBlockers(Files.readString(css));
        audit.assertNoSyntheticViolations("Final report shared CSS canonical reference contract", blockers, css,
                "report.css.reference-drift",
                audit.details(
                        "Asserts: shared-report-reference.css reuses the canonical html-ex visual vocabulary.",
                        "Artifact: analysis/shared-report-reference.css.",
                        "Expected: canonical variables, page shell, collapsible details, score primitives, graph typography, note/warn/rule helpers."));
    }

    void assertAnalysisCopyIsExactProjectReference() throws IOException {
        Path projectReference = Path.of(System.getProperty(McpProjectLocator.PROJECT_ROOT_PROPERTY, "."))
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

    void assertFinalReportDoesNotUseInlineStyleOverrides() throws IOException {
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
}
