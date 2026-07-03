package com.ai.skills.mcp.validation.report;

import lombok.RequiredArgsConstructor;


import com.ai.skills.mcp.html.HtmlLinkExtractor;
import com.ai.skills.mcp.validation.McpPackageAuditValidation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
public final class McpReportLinkAssertions {
    private final McpPackageAuditValidation audit;
    private final HtmlLinkExtractor links = new HtmlLinkExtractor();

    public void assertFinalReportLocalHyperlinksResolve() {
        audit.assertNoBlockers("Final report local hyperlinks",
                violation -> violation.code().equals("report.link.broken"),
                audit.details(
                        "Asserts: every local href in final report resolves from the report directory.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Relative link families: ../runs/run_##/..., ../review/..., shared CSS, Extent/XML/diff/state links."));
    }

    public void assertEveryLocalHrefInFinalReportResolves() throws IOException {
        String html = audit.readFinalReport();
        Path report = audit.finalReport();
        List<String> broken = new ArrayList<>();
        for (String href : links.hrefs(html)) {
            if (audit.isExternalOrInline(href)) {
                continue;
            }

            String pathOnly = href.split("#", 2)[0];
            if (pathOnly.isBlank()) {
                continue;
            }

            Path target = audit.resolve(report.getParent(), pathOnly);
            if (!Files.exists(target)) {
                broken.add(href + " -> " + target);
            }
        }
        audit.assertNoSyntheticViolations("Final report local href filesystem resolution", broken, report, "report.link.broken",
                audit.details(
                        "Asserts: parsed href values resolve on disk when treated as local links.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Resolution base: analysis/ directory of the ROOT_RUN package."));
    }

    public void assertSavedLinkAuditReportsNoMissingLinks() throws IOException {
        Path auditFile = audit.context.packageRoot().resolve("review/local-link-check.txt");
        List<String> blockers = new ArrayList<>();
        if (!Files.isRegularFile(auditFile)) {
            blockers.add("Missing saved link audit");
        } else {
            String content = Files.readString(auditFile).toLowerCase(Locale.ROOT);
            if (content.contains("missing=1") || content.contains("missing: 1") || content.contains("broken: 1")) {
                blockers.add("Saved link audit reports missing or broken links");
            }
            if (!content.contains("missing=0")
                    && !content.contains("missing: 0")
                    && !content.contains("broken: 0")
                    && !content.contains("all_links_ok")) {
                blockers.add("Saved link audit does not publish a zero-missing verdict");
            }
        }
        audit.assertNoSyntheticViolations("Saved local-link-check verdict", blockers, auditFile, "link-audit.invalid",
                audit.details(
                        "Asserts: saved link audit artifact also says zero missing/broken links.",
                        "Artifact: review/local-link-check.txt.",
                        "Accepted verdict tokens: missing=0, missing: 0, broken: 0, all_links_ok."));
    }
}
