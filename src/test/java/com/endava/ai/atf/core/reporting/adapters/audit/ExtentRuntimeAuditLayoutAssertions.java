package com.endava.ai.atf.core.reporting.adapters.audit;

import com.endava.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;
import com.endava.ai.core.reporting.adapters.extent.spark.style.ExtentSparkVisualTokens;
import org.testng.Assert;

public final class ExtentRuntimeAuditLayoutAssertions extends ExtentSparkPresentationContractSupport {
    private ExtentRuntimeAuditLayoutAssertions() {
    }

    public static void assertFullWidthReadableLayout(String html) {
        assertVisualTokensAreCanonical();
        assertRuntimeTagAuditLayout(html);
        assertSharedDarkDetailPalette(html);
        assertShellAndNavigationPalette(html);
        assertRuntimeTagTableReadability(html);
        assertFailureTriageReadability(html);
        assertRuntimeTimelineLayout(html);
    }

    private static void assertVisualTokensAreCanonical() {
        Assert.assertEquals(
                ExtentSparkVisualTokens.BACKGROUND_COLOR,
                EXTENT_BACKGROUND_HEX,
                "Extent shared background must be owned by the canonical visual token profile."
        );
        Assert.assertEquals(
                ExtentSparkVisualTokens.ACTIVE_BACKGROUND_COLOR,
                EXTENT_ACTIVE_BACKGROUND_HEX,
                "Extent active-row background must be owned by the canonical visual token profile."
        );
        Assert.assertEquals(
                ExtentSparkVisualTokens.TEXT_COLOR,
                EXTENT_TEXT_HEX,
                "Extent readable foreground must be owned by the canonical visual token profile."
        );
    }

    private static void assertRuntimeTagAuditLayout(String html) {
        Assert.assertTrue(
                html.contains(".category-view .atf-runtime-tags {")
                        && html.contains("flex: 0 0 100%;")
                        && html.contains("max-width: 100%;"),
                "Runtime tag audit must occupy a full-width row before Spark's native category list."
        );
        Assert.assertTrue(
                html.contains(".category-view .atf-runtime-tag-grid {")
                        && html.contains("grid-template-columns: repeat(3, minmax(322px, 1fr));"),
                "Class, package, and annotation tag tables must sit in wider equal-width columns on desktop."
        );
        Assert.assertTrue(
                html.contains(".dashboard-view .atf-runtime-tags-heading,")
                        && html.contains("border: 0.8px solid rgba(184,194,207,0.12);")
                        && html.contains("font-size: 1rem;"),
                "Runtime tag audit heading must stay visually aligned with Spark's native overview cards."
        );
        Assert.assertTrue(
                html.contains("border-radius: 2px !important;")
                        && html.contains("box-shadow: none;"),
                "Runtime audit cards must use the same simple card style as the aggregate Extent reference."
        );
    }

    private static void assertSharedDarkDetailPalette(String html) {
        Assert.assertTrue(
                html.contains(".test-view .test-contents .card,")
                        && html.contains(".category-view .card,")
                        && html.contains(".exception-view .card,")
                        && html.contains(".author-view .card,")
                        && html.contains(".device-view .card {")
                        && html.contains("background: " + EXTENT_BACKGROUND_HEX + " !important;")
                        && html.contains("border: 0.8px solid rgba(184,194,207,0.12) !important;"),
                "Extent detail tabs must reuse the same dark card palette as the Overview cards."
        );
        Assert.assertTrue(
                html.contains(".test-view .test-contents .card-body,")
                        && html.contains(".category-view .card-body,")
                        && html.contains(".exception-view .card-body,")
                        && html.contains("color: " + EXTENT_TEXT_HEX + " !important;"),
                "Extent detail tab card bodies must stay readable on the shared dark palette."
        );
        Assert.assertTrue(
                html.contains(".test-list *, .test-content *, .dashboard-view *,")
                        && html.contains(".category-view *, .exception-view *, .author-view *, .device-view * {")
                        && html.contains("font-family: " + EXTENT_FONT_FAMILY + " !important;")
                        && html.contains("font-size: 14px;")
                        && html.contains("line-height: 1.55;")
                        && html.contains("font-weight: 400 !important;")
                        && html.contains(".card-title, .card-title span, .card-title a,")
                        && html.contains("font-weight: 400 !important;"),
                "Extent typography must be globally readable across Overview, Tests, Bugs, Tags, and other tabs."
        );
        Assert.assertTrue(
                html.contains(".test-wrapper.row.view.test-view > .test-list {")
                        && html.contains("flex: 0 0 32% !important;")
                        && html.contains("max-width: 32% !important;")
                        && html.contains(".test-wrapper.row.view.test-view > .test-content {")
                        && html.contains("flex: 0 0 68% !important;")
                        && html.contains("max-width: 68% !important;")
                        && html.contains(".test-content .detail-body,")
                        && html.contains(".test-view .test-contents {")
                        && html.contains("max-width: none !important;"),
                "Extent Tests view must allocate more horizontal room to the readable content pane while keeping the left hierarchy usable."
        );
    }

    private static void assertShellAndNavigationPalette(String html) {
        Assert.assertTrue(
                html.contains("html, body, body.spa, body.dark,")
                        && html.contains(".header.navbar, .nav-left, .nav-right,")
                        && html.contains(".side-nav, .side-nav-inner, .side-nav-menu,")
                        && html.contains(".view, .test-wrapper, .test-view,")
                        && html.contains(".test-list, .test-list-wrapper, .test-list-item,")
                        && html.contains(".test-list-tools, .test-list-tools .tools,")
                        && html.contains(".test-content, .detail-body, .details,")
                        && html.contains(".dashboard-view, .category-view, .exception-view, .author-view, .device-view {")
                        && html.contains("background: " + EXTENT_BACKGROUND_HEX + " !important;"),
                "Extent shell, left hierarchy, and tab content must reuse the same Overview background."
        );
        Assert.assertTrue(
                html.contains(".test-list-tools {")
                        && html.contains("border-bottom: 0.8px solid rgba(184,194,207,0.12) !important;")
                        && html.contains(".test-list-tools p,")
                        && html.contains(".test-list-tools span,")
                        && html.contains(".test-list-tools a {")
                        && html.contains("color: " + EXTENT_TEXT_HEX + " !important;"),
                "Extent left Tests toolbar must use the same background and readable text palette as the rest of the shell."
        );
        Assert.assertTrue(
                html.contains(".test-list .test-item,")
                        && html.contains(".test-list .test-list-item > li,")
                        && html.contains(".test-list .test-list-item > li .test-detail {")
                        && html.contains(".test-list .test-item.active,")
                        && html.contains("background: " + EXTENT_ACTIVE_BACKGROUND_HEX + " !important;"),
                "Extent left hierarchy must have the shared background and a deliberate active-row highlight."
        );
        Assert.assertTrue(
                html.contains("body.dark .side-nav,")
                        && html.contains("body.dark .test-list,")
                        && html.contains("body.dark .test-list-tools,")
                        && html.contains("body.dark .test-content,")
                        && html.contains("body.dark .test-list li.test-item,")
                        && html.contains("body.dark .test-list li.test-item.active,"),
                "Extent shell palette must use high-specificity body.dark selectors so Spark defaults cannot override it."
        );
        Assert.assertTrue(
                html.contains(".view.hide,")
                        && html.contains(".test-wrapper.view.hide,")
                        && html.contains(".container-fluid.view.hide,")
                        && html.contains("display: none !important;")
                        && html.contains("visibility: hidden !important;"),
                "Inactive Spark views must be forcibly hidden so Tests screenshots cannot overlap the Overview dashboard."
        );
        Assert.assertTrue(
                html.contains(".dashboard-view .atf-runtime-overview-tags {")
                        && html.contains("position: relative;")
                        && html.contains("z-index: 4;")
                        && html.contains("isolation: isolate;")
                        && html.contains("clear: both;"),
                "Overview runtime tag audit must establish its own stacking context and clear prior Spark layout content."
        );
    }

    private static void assertRuntimeTagTableReadability(String html) {
        Assert.assertTrue(
                html.contains(".category-view .atf-runtime-tag-card-annotations {")
                        && html.contains("grid-column: auto;"),
                "Overlapping annotation tags must not be wider than the class and package distribution tables."
        );
        Assert.assertTrue(
                html.contains(".category-view .atf-runtime-tag-scroll {")
                        && html.contains("max-height: 340px;")
                        && html.contains("overflow: auto;"),
                "Long class/package/tag inventories must scroll inside their card instead of breaking the overview layout."
        );
        Assert.assertTrue(
                html.contains(".dashboard-view .atf-runtime-tag-table tbody tr:nth-child(odd),")
                        && html.contains("background: rgba(255,255,255,0.025) !important;")
                        && html.contains("grid-template-columns: repeat(3, minmax(322px, 1fr));")
                        && html.contains(".dashboard-view .atf-runtime-tag-table th:nth-child(n+2),")
                        && html.contains("text-align: right;")
                        && html.contains("width: 5.5rem;"),
                "Runtime tag tables must be scan-friendly: wider cards, striped rows, narrow numeric columns, and more room for names."
        );
        Assert.assertTrue(
                html.contains(".test-view table td:nth-child(3) {")
                        && html.contains("min-width: 32rem;"),
                "Test detail tables must keep method details such as blocked-before-method messages readable without cramped wrapping."
        );
        Assert.assertTrue(
                html.contains(".category-view .atf-runtime-tag-table thead th {")
                        && html.contains("position: sticky;"),
                "Tag tables must keep their headers readable while scrolling long inventories."
        );
    }

    private static void assertFailureTriageReadability(String html) {
        Assert.assertTrue(
                html.contains(".category-view .atf-runtime-failure-grid {")
                        && html.contains("display: block;")
                        && html.contains(".category-view .atf-runtime-failure-summary {")
                        && html.contains(".category-view .atf-runtime-failure-lines {")
                        && html.contains(".category-view .atf-runtime-failure-class-header {")
                        && html.contains(".category-view .atf-runtime-failure-line-main {")
                        && html.contains(".category-view .atf-runtime-failure-details {")
                        && html.contains(".category-view .atf-runtime-failure-details summary {")
                        && html.contains(".category-view .atf-runtime-failure-code {")
                        && html.contains("white-space: pre-wrap;")
                        && html.contains("overflow-wrap: anywhere;")
                        && html.contains("font-size: 0.98rem;")
                        && html.contains("font-size: 1rem;")
                        && html.contains("font-size: 0.81rem;")
                        && html.contains("line-height: 1.5;")
                        && html.contains("font-weight: 360;")
                        && html.contains("font-weight: 360 !important;"),
                "Failure tab must render readable single collapsed failure details with compact, stable code typography."
        );
        Assert.assertTrue(
                html.contains(".test-view table th,")
                        && html.contains(".test-view table td,")
                        && html.contains("font-size: 0.74rem !important;")
                        && html.contains("line-height: 1.34 !important;")
                        && html.contains(".test-view .test-contents .badge,")
                        && html.contains("font-size: 0.72rem !important;"),
                "Status, timestamp, detail-table cells, and pass/fail badges must stay visually secondary to method names and failure details."
        );
        Assert.assertTrue(
                html.contains(".test-contents .card-header a.node span,")
                        && html.contains("font-size: 1rem !important;")
                        && html.contains(".test-contents .card .card .card-header .card-title,")
                        && html.contains("font-size: 1rem !important;")
                        && html.contains(".test-contents .card .card .card .card-header .card-title,")
                        && html.contains("font-size: 1rem !important;"),
                "Method, step, and nested configuration labels must be more readable than the metadata table."
        );
        Assert.assertFalse(
                html.contains(".category-view .atf-runtime-failure-preview {"),
                "Failure tab CSS must not keep the removed duplicate preview block."
        );
    }

    private static void assertRuntimeTimelineLayout(String html) {
        Assert.assertTrue(
                html.contains(".dashboard-view .atf-runtime-timeline-track {")
                        && html.contains("height: 1.35rem;")
                        && html.contains("width: 92%;")
                        && html.contains(".dashboard-view .atf-runtime-timeline-axis {"),
                "Dashboard timeline must use a Spark-like segmented timeline, not an empty canvas or custom legend."
        );
    }
}
