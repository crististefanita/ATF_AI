package com.endava.ai.skills.mcp.validation.report;

import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.skills.mcp.validation.report.model.ReportOpeningModel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class McpReportOpeningAssertions {
    private final McpPackageAuditValidation audit;

    void assertHasVisiblePillRow() throws IOException {
        String html = audit.readFinalReport();
        ReportOpeningModel opening = ReportOpeningModel.fromHtml(html);
        List<String> blockers = new ArrayList<>();
        if (!opening.hasExecutiveContextLabel()) {
            blockers.add("Cannot locate Executive Context");
        }
        if (!opening.hasVisiblePillRowBeforeExecutiveContext()) {
            blockers.add("Opening must expose a visible pill-row before Executive Context; hidden runtime strips inside collapsed sections do not count");
        }
        if (!opening.hasVisiblePillBeforeExecutiveContext()) {
            blockers.add("Opening pill-row must contain visible .pill items for business/audit orientation");
        }
        audit.assertNoSyntheticViolations("Final report opening visible pill row",
                blockers, audit.finalReport(), "report.opening.pill-row.missing",
                audit.details(
                        "Asserts: opening has visible navigation/calibration pills, not only collapsed runtime details.",
                        "Artifact: analysis/langgraph-business-understanding.html.",
                        "Expected location: after hero/score opening and before Executive Context.",
                        "Expected classes: pill-row and pill."));
    }

    void assertDoesNotExposeRootRunTimestampIdentity() throws IOException {
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
}
