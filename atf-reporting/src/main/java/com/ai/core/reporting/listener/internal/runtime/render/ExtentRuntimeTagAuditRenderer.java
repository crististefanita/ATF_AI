package com.ai.core.reporting.listener.internal.runtime.render;

import com.ai.core.reporting.listener.internal.runtime.model.ExtentRuntimeHtml;
import com.ai.core.reporting.listener.internal.runtime.model.ExtentRuntimeSummary;

import com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditSection;
import com.ai.core.reporting.listener.internal.runtime.support.ExtentRuntimePercentages;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class ExtentRuntimeTagAuditRenderer {

    private ExtentRuntimeTagAuditRenderer() {
    }

    public static String section(ExtentRuntimeSummary summary) {
        return "<div class=\"row atf-runtime-overview-tags\">"
                + "<div class=\"col-md-12\">"
                + tagAuditContent(summary)
                + "</div>"
                + "</div>";
    }

    private static String tagAuditContent(ExtentRuntimeSummary summary) {
        return RuntimeAuditSection.TAG_AUDIT.wrap(
                "<div class=\"atf-runtime-tags\">"
                + "<div class=\"atf-runtime-tags-heading\">"
                + "<h3>" + ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL + "</h3>"
                + "<p>Runtime tags are split by responsibility: test-method distribution by class, test-method distribution by package, and optional annotations/tags.</p>"
                + "</div>"
                + "<div class=\"atf-runtime-tag-grid\">"
                + distributionTable(ExtentDashboardLabels.TEST_METHODS_BY_CLASS, ExtentDataAttributes.TAG_TABLE_CLASSES,
                        summary.classCounts, summary.total, true)
                + distributionTable(ExtentDashboardLabels.TEST_METHODS_BY_PACKAGE, ExtentDataAttributes.TAG_TABLE_PACKAGES,
                        summary.packageCounts, summary.total, true)
                + distributionTable(ExtentDashboardLabels.OTHER_ANNOTATIONS, ExtentDataAttributes.TAG_TABLE_ANNOTATIONS,
                        summary.annotationCounts, summary.total, false)
                + "</div>"
                + "</div>"
        );
    }

    private static String distributionTable(
            String title,
            String tableKind,
            Map<String, Integer> counts,
            int total,
            boolean percentMustSumToOneHundred
    ) {
        Map<String, Integer> ordered = sortedCounts(counts);
        Map<String, Integer> percentages = percentMustSumToOneHundred
                ? ExtentRuntimePercentages.exactIntegerShares(ordered, total)
                : Map.of();
        StringBuilder html = new StringBuilder()
                .append("<div class=\"card atf-runtime-tag-card atf-runtime-tag-card-")
                .append(ExtentRuntimeHtml.escape(tableKind))
                .append("\"><div class=\"card-header\"><p>")
                .append(ExtentRuntimeHtml.escape(title))
                .append("</p></div><div class=\"card-body\"><div class=\"atf-runtime-tag-scroll\"><table ")
                .append(ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.RUNTIME_TAG_TABLE))
                .append(ExtentRuntimeHtml.escape(tableKind))
                .append("\" class=\"table table-sm atf-runtime-tag-table\">")
                .append("<thead><tr><th>Name</th><th>Count</th>");
        if (percentMustSumToOneHundred) {
            html.append("<th>Share</th>");
        }
        html.append("</tr></thead><tbody>");
        ordered.forEach((name, count) -> {
            html.append("<tr><td>").append(ExtentRuntimeHtml.escape(name)).append("</td><td>").append(count).append("</td>");
            if (percentMustSumToOneHundred) {
                html.append("<td>").append(percentages.getOrDefault(name, 0)).append("%</td>");
            }
            html.append("</tr>");
        });
        if (percentMustSumToOneHundred) {
            html.append("<tr class=\"atf-runtime-tag-total\"><th>")
                    .append(ExtentRuntimeHtml.escape(title))
                    .append(" percentage total</th><td>")
                    .append(total)
                    .append("</td><td>100%</td></tr>");
        } else {
            html.append("<tr class=\"atf-runtime-tag-total\"><th>")
                    .append(ExtentRuntimeHtml.escape(title))
                    .append(" total tag assignments</th><td>")
                    .append(ordered.values().stream().mapToInt(Integer::intValue).sum())
                    .append("</td></tr>");
        }
        return html.append("</tbody></table></div></div></div>").toString();
    }

    private static Map<String, Integer> sortedCounts(Map<String, Integer> counts) {
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }
}
