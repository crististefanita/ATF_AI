package com.endava.ai.core.reporting.adapters.extent.patch.component;

import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.endava.ai.core.support.html.HtmlText;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

final class ExtentComponentOverviewAuditRenderer {
    static final String COMPONENT_TAG_AUDIT_ROW = "<div class=\"row atf-component-overview-tags\">";

    private ExtentComponentOverviewAuditRenderer() {
    }

    static String render(String html) {
        ExtentComponentInventory inventory = ExtentComponentInventory.from(html);
        return COMPONENT_TAG_AUDIT_ROW
                + "<div class=\"col-md-12\">"
                + "<!-- " + ExtentMarkers.COMPONENT_TAG_AUDIT_START + " -->"
                + "<div class=\"atf-runtime-tags atf-component-tags\">"
                + "<div class=\"atf-runtime-tags-heading\"><h3>" + ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL + "</h3>"
                + "<p>Component reports use Extent-visible class and package evidence; runtime-audited reports add TestListener test-case ledgers.</p></div>"
                + "<div class=\"atf-runtime-tag-grid\">"
                + distributionTable(ExtentDashboardLabels.TEST_CLASSES, ExtentDataAttributes.TAG_TABLE_CLASSES,
                        inventory.classCounts(), inventory.totalClasses(), true)
                + distributionTable(ExtentDashboardLabels.TEST_PACKAGES, ExtentDataAttributes.TAG_TABLE_PACKAGES,
                        inventory.packageCounts(), inventory.totalClasses(), true)
                + distributionTable(ExtentDashboardLabels.OTHER_ANNOTATIONS, ExtentDataAttributes.TAG_TABLE_ANNOTATIONS,
                        Map.of(), 0, false)
                + "</div></div><!-- " + ExtentMarkers.COMPONENT_TAG_AUDIT_END + " -->"
                + "</div></div>";
    }

    private static String distributionTable(
            String title,
            String tableKind,
            Map<String, Integer> counts,
            int total,
            boolean percentMustSumToOneHundred
    ) {
        Map<String, Integer> ordered = sortedCounts(counts);
        StringBuilder html = new StringBuilder()
                .append("<div class=\"card atf-runtime-tag-card atf-runtime-tag-card-")
                .append(HtmlText.html(tableKind))
                .append("\"><div class=\"card-header\"><p>")
                .append(HtmlText.html(title))
                .append("</p></div><div class=\"card-body\"><div class=\"atf-runtime-tag-scroll\"><table ")
                .append(ExtentHtmlStructure.dataAttributePrefix(ExtentDataAttributes.COMPONENT_TAG_TABLE))
                .append(HtmlText.html(tableKind))
                .append("\" class=\"table table-sm atf-runtime-tag-table\">")
                .append("<thead><tr><th>Name</th><th>Count</th>");
        if (percentMustSumToOneHundred) {
            html.append("<th>Share</th>");
        }
        html.append("</tr></thead><tbody>");
        ordered.forEach((name, count) -> {
            html.append("<tr><td>").append(HtmlText.html(name)).append("</td><td>").append(count).append("</td>");
            if (percentMustSumToOneHundred) {
                html.append("<td>").append(share(count, total)).append("%</td>");
            }
            html.append("</tr>");
        });
        if (percentMustSumToOneHundred) {
            html.append("<tr class=\"atf-runtime-tag-total\"><th>")
                    .append(HtmlText.html(title))
                    .append(" percentage total</th><td>")
                    .append(total)
                    .append("</td><td>100%</td></tr>");
        } else {
            html.append("<tr class=\"atf-runtime-tag-total\"><th>")
                    .append(HtmlText.html(title))
                    .append(" total tag assignments</th><td>0</td></tr>");
        }
        return html.append("</tbody></table></div></div></div>").toString();
    }

    private static int share(int count, int total) {
        return total <= 0 ? 0 : Math.round((count * 100f) / total);
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
