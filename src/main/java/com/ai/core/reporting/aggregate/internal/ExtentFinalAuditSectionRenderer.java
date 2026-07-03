package com.ai.core.reporting.aggregate.internal;

import com.ai.core.reporting.FrameworkContractReportLabels;
import com.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.ai.core.reporting.aggregate.model.AggregateTimingSummary;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDomIds;
import com.ai.core.reporting.adapters.extent.contract.ExtentReportSectionIds;
import com.ai.core.support.html.HtmlText;

final class ExtentFinalAuditSectionRenderer {
    private static final String AUDIT_DESCRIPTION =
            "Reconciled from TestListener results and aligned with the runtime test-case ledger.";

    private ExtentFinalAuditSectionRenderer() {
    }

    static String render(AggregateReportDescriptor descriptor,
                         int suiteTotal,
                         int reportableTotal,
                         long passed,
                         long failed,
                         long skipped,
                         AggregateTimingSummary timing,
                         String inventoryLines) {
        AggregateTimingSummary safeTiming = timing == null ? AggregateTimingSummary.empty() : timing;
        return "\n<section id=\"" + ExtentReportSectionIds.AGGREGATE_FINAL_AUDIT
                + "\" class=\"atf-core-final-audit\">"
                + "<h2>" + escapeHtml(descriptor.testTitle()) + " Final Audit</h2>"
                + "<p>" + AUDIT_DESCRIPTION + "</p>"
                + "<table class=\"atf-core-final-audit-table\">"
                + auditCell("Suite test cases", String.valueOf(suiteTotal))
                + auditCell("Reportable Extent test cases", String.valueOf(reportableTotal))
                + auditCell("Passed", String.valueOf(passed))
                + auditCell("Failed", String.valueOf(failed))
                + auditCell("Skipped", String.valueOf(skipped))
                + auditCell("Wall-clock duration", safeTiming.wallClockDuration())
                + auditCell("Cumulative test duration", safeTiming.cumulativeDuration())
                + auditCell("Slowest test", safeTiming.slowestTest())
                + auditCell("Slowest class/group", safeTiming.slowestClass())
                + "</table>"
                + "<p class=\"atf-core-final-audit-verdict\">"
                + escapeHtml(FrameworkContractReportLabels.COLD_VERDICT)
                + ": "
                + verdict(failed, skipped)
                + "</p>"
                + "<details><summary>Show test-case inventory</summary>"
                + "<pre class=\"atf-core-final-audit-code\">"
                + escapeHtml(inventoryLines)
                + "</pre></details>"
                + "</section>"
                + relocationScript();
    }

    private static String verdict(long failed, long skipped) {
        if (failed == 0 && skipped == 0) {
            return "PASS - all observed ATF-core test cases are green.";
        }
        return "CHECK - failed or skipped ATF-core test cases exist.";
    }

    private static String relocationScript() {
        return "<script>(function(){"
                + "function attachAtfCoreFinalAudit(){"
                + "var audit=document.getElementById('" + ExtentReportSectionIds.AGGREGATE_FINAL_AUDIT + "');"
                + "var dashboard=document.querySelector('." + ExtentDomIds.DASHBOARD_VIEW + "');"
                + "if(audit&&dashboard&&!dashboard.contains(audit)){dashboard.appendChild(audit);}"
                + "}"
                + "attachAtfCoreFinalAudit();"
                + "document.addEventListener('DOMContentLoaded',attachAtfCoreFinalAudit);"
                + "setTimeout(attachAtfCoreFinalAudit,0);"
                + "})();</script>\n";
    }

    private static String auditCell(String label, String value) {
        return "<tr><th>"
                + escapeHtml(label)
                + "</th><td>"
                + escapeHtml(value)
                + "</td></tr>";
    }

    private static String escapeHtml(String value) {
        return HtmlText.html(value);
    }
}
