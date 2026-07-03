package com.endava.ai.core.reporting.listener.internal.runtime.render;

import com.endava.ai.core.reporting.listener.internal.runtime.model.*;

import com.endava.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditSection;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;

import java.util.List;
import java.util.Set;

public final class ExtentRuntimeFailureRenderer {
    private ExtentRuntimeFailureRenderer() {
    }

    public static String section(ExtentRuntimeSummary summary) {
        return RuntimeAuditSection.FAILURE_GROUPS.wrap(
                "<div class=\"atf-runtime-tags atf-runtime-failures\">"
                + "<div class=\"atf-runtime-tags-heading\">"
                + "<h3>" + ExtentMarkers.RUNTIME_FAILURE_GROUPS_LABEL + "</h3>"
                + "<p>Failed test cases grouped from the reconciled TestListener ledger for quick triage.</p>"
                + "</div>"
                + failureGroups(summary)
                + "</div>"
        );
    }

    private static String failureGroups(ExtentRuntimeSummary summary) {
        if (summary.failures.isEmpty()) {
            return "<div class=\"atf-runtime-failure-grid\" data-atf-runtime-failure-table=\"failures\">"
                    + "<div class=\"atf-runtime-failure-empty\">"
                    + "<strong>No runtime failures</strong>"
                    + "<p>No runtime failures recorded.</p>"
                    + "</div></div>";
        }
        List<FailureGroup> groups = FailureGroup.from(summary.failures);
        List<FailureSignature> signatures = FailureSignature.from(summary.failures);
        StringBuilder html = new StringBuilder()
                .append("<div class=\"atf-runtime-failure-grid\" data-atf-runtime-failure-table=\"failures\">")
                .append("<div class=\"atf-runtime-failure-summary\">")
                .append("<span class=\"atf-runtime-failure-status\">FAILED</span>")
                .append("<strong>")
                .append(signatures.size())
                .append(signatures.size() == 1 ? " root-cause signature" : " root-cause signatures")
                .append(" -> ")
                .append(summary.failures.size())
                .append(summary.failures.size() == 1 ? " failed test case" : " failed test cases")
                .append(" -> ")
                .append(groups.size())
                .append(groups.size() == 1 ? " affected Java class" : " affected Java classes")
                .append("</strong>")
                .append("<span>One repeated root cause is shown once, then linked to every affected method for drill-down.</span>")
                .append("</div>")
                .append(failureSignatureSummary(signatures))
                .append("<details class=\"atf-runtime-failure-drilldown\">")
                .append("<summary>Class-level failure drill-down</summary>")
                .append("<div class=\"atf-runtime-failure-lines\">");
        groups.forEach(group -> appendClassFailureGroup(html, group));
        html.append("</div></details>");
        return html.append("</div>").toString();
    }

    private static void appendClassFailureGroup(StringBuilder html, FailureGroup group) {
        html.append("<section class=\"atf-runtime-failure-class\" data-atf-runtime-failure-group=\"")
                .append(ExtentRuntimeHtml.escape(group.className()))
                .append("\"><header class=\"atf-runtime-failure-class-header\">")
                .append("<div><span class=\"atf-runtime-failure-package\">")
                .append(ExtentRuntimeHtml.escape(group.packageName()))
                .append("</span><strong>")
                .append(ExtentRuntimeHtml.escape(group.className()))
                .append("</strong></div><span class=\"atf-runtime-failure-count\">")
                .append(group.failedCount())
                .append(" failed")
                .append("</span></header>");
        group.invocations().forEach(invocation -> html
                .append("<article class=\"atf-runtime-failure-line\" data-atf-runtime-failure-row=\"")
                .append(ExtentRuntimeHtml.escape(ExtentRuntimeHtml.simpleClassName(invocation.className)))
                .append("\"><div class=\"atf-runtime-failure-line-main\">")
                .append("<span class=\"atf-runtime-failure-status\">FAILED</span>")
                .append("<a class=\"atf-runtime-failure-method\" href=\"#\" data-atf-runtime-method=\"")
                .append(ExtentRuntimeHtml.escape(invocation.methodName))
                .append("\" title=\"Search this method in the Tests tab\">")
                .append(ExtentRuntimeHtml.escape(invocation.methodName))
                .append("</a></div>")
                .append("<details class=\"atf-runtime-failure-details\"><summary>")
                .append(ExtentRuntimeHtml.escape(invocation.failureDisplaySummary()))
                .append("</summary>")
                .append("<pre class=\"atf-runtime-failure-code\"><code>")
                .append(ExtentRuntimeHtml.escape(invocation.hasFailureDetail()
                        ? invocation.failureDetail
                        : "Failure detail unavailable"))
                .append("</code></pre></details></article>"));
        html.append("</section>");
    }

    private static String failureSignatureSummary(List<FailureSignature> signatures) {
        StringBuilder html = new StringBuilder()
                .append("<section class=\"atf-runtime-failure-signatures\" ")
                .append("data-atf-runtime-failure-signatures=\"true\">")
                .append("<header><strong>")
                .append(signatures.size() == 1 ? "Repeated Root Cause" : "Root Cause Map")
                .append("</strong><span>Grouped by failure message first; methods are navigation targets, not separate diagnoses.</span></header>");
        signatures.forEach(signature -> html
                .append("<article class=\"atf-runtime-failure-signature\">")
                .append("<div class=\"atf-runtime-failure-signature-main\">")
                .append("<span class=\"atf-runtime-failure-signature-severity\">")
                .append(signature.severityLabel())
                .append("</span>")
                .append("<span class=\"atf-runtime-failure-signature-count\">")
                .append(signature.invocationLabel())
                .append("</span><div class=\"atf-runtime-failure-signature-text\"><strong>")
                .append(ExtentRuntimeHtml.escape(signature.displaySummary()))
                .append("</strong><span>")
                .append(signature.scopeLabel())
                .append("</span>")
                .append("<span>")
                .append(signature.methodCountLabel())
                .append("</span></div></div>")
                .append("<div class=\"atf-runtime-failure-signature-affected\">")
                .append(signatureInvocationList(signature))
                .append("</div></article>"));
        return html.append("</section>").toString();
    }

    private static String signatureInvocationList(FailureSignature signature) {
        StringBuilder html = new StringBuilder();
        signature.methodsByClass().forEach((className, methods) -> html
                .append("<div class=\"atf-runtime-failure-signature-target\">")
                .append("<span class=\"atf-runtime-failure-signature-class\">")
                .append(ExtentRuntimeHtml.escape(className))
                .append("</span><span class=\"atf-runtime-failure-signature-methods\">")
                .append(signatureMethodChips(methods))
                .append("</span></div>"));
        return html.toString();
    }

    private static String signatureMethodChips(Set<String> methods) {
        StringBuilder html = new StringBuilder();
        methods.forEach(method -> html
                .append("<a class=\"atf-runtime-failure-signature-method\" href=\"#\" ")
                .append("data-atf-runtime-method=\"")
                .append(ExtentRuntimeHtml.escape(method))
                .append("\" title=\"Search this method in the Tests tab\">")
                .append(ExtentRuntimeHtml.escape(method))
                .append("</a>"));
        return html.toString();
    }
}
