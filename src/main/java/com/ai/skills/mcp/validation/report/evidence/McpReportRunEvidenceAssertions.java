package com.ai.skills.mcp.validation.report.evidence;

import lombok.RequiredArgsConstructor;


import com.ai.skills.mcp.context.McpRunArtifacts;
import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.ai.skills.mcp.validation.report.link.McpReportLinkProbe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class McpReportRunEvidenceAssertions {
    private final McpPackageAuditValidation audit;

    public void assertLinksEveryRunEvidenceFamily() throws IOException {
        String html = audit.readFinalReport();
        McpReportLinkProbe links = McpReportLinkProbe.in(html);
        List<String> missing = new ArrayList<>();
        for (int index = 1; index <= audit.context.runtime().metaIterationCount(); index++) {
            requireRunLinks(links, String.format("run_%02d", index), missing);
        }
        audit.assertNoSyntheticViolations("Final report per-run evidence links", missing, audit.finalReport(),
                "report.run-evidence.missing",
                audit.details(
                        "Asserts: final report hyperlinks every run to summary, diff, run-state, command result, XML, and Extent proof.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Relative path pattern: " + McpRunArtifacts.linkFromFinalReport("run_##", McpRunArtifacts.SUMMARY)
                                + "; " + McpRunArtifacts.linkFromFinalReport("run_##", McpRunArtifacts.DIFF)
                                + "; " + McpRunArtifacts.linkFromFinalReport("run_##", McpRunArtifacts.STATE)
                                + "; " + McpRunArtifacts.linkFromFinalReport("run_##", McpRunArtifacts.COMMAND_RESULT)
                                + "; " + McpRunArtifacts.linkFromFinalReport("run_##", McpRunArtifacts.TESTNG_RESULTS)
                                + "; " + McpRunArtifacts.linkFromFinalReport("run_##", McpRunArtifacts.EXTENT_REPORT) + "."));
    }

    private static void requireRunLinks(McpReportLinkProbe links, String run, List<String> missing) {
        links.requireHrefTo(McpRunArtifacts.linkFromFinalReport(run, McpRunArtifacts.SUMMARY),
                "Final report must hyperlink " + run + " SUMMARY.md", missing);
        links.requireHrefTo(McpRunArtifacts.linkFromFinalReport(run, McpRunArtifacts.DIFF),
                "Final report must hyperlink " + run + " thin-no-index.diff", missing);
        links.requireHrefTo(McpRunArtifacts.linkFromFinalReport(run, McpRunArtifacts.STATE),
                "Final report must hyperlink " + run + " run-state.json", missing);
        links.requireHrefTo(McpRunArtifacts.linkFromFinalReport(run, McpRunArtifacts.COMMAND_RESULT),
                "Final report must hyperlink " + run + " command-result.md", missing);
        links.requireAnyHrefTo(List.of(
                        McpRunArtifacts.linkFromFinalReport(run, McpRunArtifacts.TESTNG_RESULTS),
                        McpRunArtifacts.linkFromFinalReport(run, "validation/TEST-TestSuite.xml")),
                "Final report must hyperlink " + run + " machine XML", missing);
        links.requireHrefTo(McpRunArtifacts.linkFromFinalReport(run, McpRunArtifacts.EXTENT_REPORT),
                "Final report must hyperlink " + run + " ExtentReport.html", missing);
    }
}
