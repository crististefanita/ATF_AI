package com.ai.atf.core.reporting.adapters.tags;

import com.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;
import com.ai.atf.core.reporting.adapters.dom.ExtentRenderedState;
import com.microsoft.playwright.Page;
import org.testng.Assert;

public final class ExtentSparkTagSelectionPaletteAssertions extends ExtentSparkPresentationContractSupport {
    private static final String BACKGROUND_RGB = EXTENT_BACKGROUND_RGB;
    private static final String TEXT_RGB = EXTENT_TEXT_RGB;
    private static final String BORDER_RGB = EXTENT_RUNTIME_BORDER_RGB;

    private ExtentSparkTagSelectionPaletteAssertions() {
    }

    public static void assertTagSelectionClosesFilterOverlaysAndKeepsTestListStyle(Page page) {
        String state = String.valueOf(page.evaluate("async () => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const css = el => {"
                + "const s = el ? getComputedStyle(el) : null;"
                + "return s ? {"
                + "bg: s.backgroundColor,"
                + "color: s.color,"
                + "border: s.borderBottomColor,"
                + "borderWidth: Number.parseFloat(s.borderBottomWidth || '0'),"
                + "fontSize: s.fontSize,"
                + "fontWeight: s.fontWeight"
                + "} : {bg:'missing', color:'missing', border:'missing', borderWidth:-1, fontSize:'missing', fontWeight:'missing'};"
                + "};"
                + "const statusToggle = document.querySelector('.test-list-tools .fa-exclamation-circle')"
                + "?.closest('a,button');"
                + "const tagToggle = document.querySelector('.test-list-tools .fa-bug')?.closest('a,button');"
                + "if (statusToggle) { statusToggle.click(); }"
                + "await new Promise(resolve => setTimeout(resolve, 120));"
                + "if (tagToggle) { tagToggle.click(); }"
                + "await new Promise(resolve => setTimeout(resolve, 120));"
                + "const tagItem = document.querySelector('#tag-toggle .dropdown-item');"
                + "if (tagItem) { tagItem.click(); }"
                + "await new Promise(resolve => setTimeout(resolve, 850));"
                + "const tagMenu = document.querySelector('#tag-toggle');"
                + "const statusMenu = document.querySelector('#status-toggle');"
                + "const list = document.querySelector('.test-view .test-list');"
                + "const row = document.querySelector('.test-view .test-list li.test-item, .test-view .test-list .test-item');"
                + "const detail = document.querySelector('.test-view .test-list .test-detail');"
                + "const rootCard = document.querySelector('.test-view .test-content .card.extent-atf-root-node, "
                + ".test-view .test-content .card');"
                + "const subCard = document.querySelector('.test-view .test-content .card.extent-atf-sub-node, "
                + ".test-view .test-content .card .card');"
                + "const tableHeader = document.querySelector('.test-view .test-content table th');"
                + "const tableCell = document.querySelector('.test-view .test-content table td');"
                + "const watched = Array.from(document.querySelectorAll("
                + "'.test-view .test-content .card, "
                + ".test-view .detail-body .card, "
                + ".test-view .atf-runtime-tag-selection .card, "
                + ".test-view .atf-runtime-status-selection .card, "
                + ".test-view .atf-runtime-tag-selected-item, "
                + ".test-view .test-content table th, "
                + ".test-view .test-content table td, "
                + ".test-view .detail-body table th, "
                + ".test-view .detail-body table td'))"
                + ".filter(visible);"
                + "const badBackgrounds = watched.filter(node => {"
                + "const s = getComputedStyle(node);"
                + "return s.backgroundColor !== '" + BACKGROUND_RGB + "'"
                + "&& s.backgroundColor !== 'rgba(0, 0, 0, 0)';"
                + "}).length;"
                + "const badBorders = watched.filter(node => {"
                + "const s = getComputedStyle(node);"
                + "const width = Number.parseFloat(s.borderBottomWidth || '0');"
                + "return width > 0 && s.borderBottomColor !== '" + BORDER_RGB + "';"
                + "}).length;"
                + "const rowStyle = css(row);"
                + "const detailStyle = css(detail);"
                + "const rootStyle = css(rootCard);"
                + "const subStyle = css(subCard);"
                + "const headerStyle = css(tableHeader);"
                + "const cellStyle = css(tableCell);"
                + "const listStyle = getComputedStyle(list);"
                + "return ["
                + "'tagVisible=' + visible(tagMenu),"
                + "'statusVisible=' + visible(statusMenu),"
                + "'listBg=' + listStyle.backgroundColor,"
                + "'listBorder=' + listStyle.borderRightColor,"
                + "'listBorderWidth=' + Number.parseFloat(listStyle.borderRightWidth || '0'),"
                + "'rowBg=' + rowStyle.bg,"
                + "'rowBorder=' + rowStyle.border,"
                + "'rowBorderWidth=' + rowStyle.borderWidth,"
                + "'detailBg=' + detailStyle.bg,"
                + "'rootBg=' + rootStyle.bg,"
                + "'rootBorder=' + rootStyle.border,"
                + "'rootBorderWidth=' + rootStyle.borderWidth,"
                + "'subBg=' + subStyle.bg,"
                + "'subBorder=' + subStyle.border,"
                + "'subBorderWidth=' + subStyle.borderWidth,"
                + "'tableHeaderBorder=' + headerStyle.border,"
                + "'tableHeaderBorderWidth=' + headerStyle.borderWidth,"
                + "'tableCellBorder=' + cellStyle.border,"
                + "'tableCellBorderWidth=' + cellStyle.borderWidth,"
                + "'badBackgrounds=' + badBackgrounds,"
                + "'badBorders=' + badBorders"
                + "].join(';');"
                + "}"));

        assertTagSelectionOverlayState(state);
        assertTagSelectionLeftListStyle(state);
        assertTagSelectionRightContentStyle(state);
    }

    public static void assertSideCategoryTabUsesSameRuntimePalette(Page page) {
        page.click(".side-nav li[onclick*='category-view']");
        page.waitForTimeout(500);

        String state = String.valueOf(page.evaluate("() => {"
                + "const visible = el => !!(el && (el.offsetWidth || el.offsetHeight || el.getClientRects().length));"
                + "const css = el => {"
                + "const s = el ? getComputedStyle(el) : null;"
                + "return s ? {"
                + "bg: s.backgroundColor,"
                + "color: s.color,"
                + "border: s.borderTopColor,"
                + "borderWidth: Number.parseFloat(s.borderTopWidth || '0'),"
                + "font: s.fontFamily,"
                + "fontSize: s.fontSize"
                + "} : {bg:'missing', color:'missing', border:'missing', borderWidth:-1, font:'missing', fontSize:'missing'};"
                + "};"
                + "const category = document.querySelector('.category-view');"
                + "const heading = category?.querySelector('h3, h5, strong, .card-title');"
                + "const firstBox = category?.querySelector('.atf-runtime-failure-empty, .atf-runtime-failure-class, "
                + ".atf-runtime-failure-signatures, .atf-runtime-tag-card, .card');"
                + "const list = category?.querySelector('.test-list');"
                + "const listItem = category?.querySelector('.test-list li, .test-list .test-item, .test-list .test-list-item > li');"
                + "const watched = category ? Array.from(category.querySelectorAll("
                + "'.card, .card-header, .card-body, .atf-runtime-failure-class, "
                + ".atf-runtime-failure-class-header, .atf-runtime-failure-line, "
                + ".atf-runtime-failure-drilldown, .atf-runtime-failure-details, "
                + ".atf-runtime-tag-card, table th, table td'))"
                + ".filter(visible) : [];"
                + "const badCategoryBorders = watched.filter(node => {"
                + "const s = getComputedStyle(node);"
                + "const widths = [s.borderTopWidth, s.borderRightWidth, s.borderBottomWidth, s.borderLeftWidth]"
                + ".map(value => Number.parseFloat(value || '0'));"
                + "const colors = [s.borderTopColor, s.borderRightColor, s.borderBottomColor, s.borderLeftColor];"
                + "return colors.some((color, index) => widths[index] > 0 && color !== '" + BORDER_RGB + "');"
                + "}).length;"
                + "const badCategoryBackgrounds = watched.filter(node => {"
                + "const s = getComputedStyle(node);"
                + "return s.backgroundColor !== '" + BACKGROUND_RGB + "'"
                + "&& s.backgroundColor !== 'rgba(0, 0, 0, 0)';"
                + "}).length;"
                + "const categoryStyle = css(category);"
                + "const boxStyle = css(firstBox);"
                + "const listStyle = css(list);"
                + "const itemStyle = css(listItem);"
                + "return ["
                + "'categoryVisible=' + visible(category),"
                + "'categoryBg=' + categoryStyle.bg,"
                + "'categoryColor=' + categoryStyle.color,"
                + "'boxBorder=' + boxStyle.border,"
                + "'boxBorderWidth=' + boxStyle.borderWidth,"
                + "'listBg=' + listStyle.bg,"
                + "'listBorder=' + listStyle.border,"
                + "'itemBg=' + itemStyle.bg,"
                + "'itemBorder=' + itemStyle.border,"
                + "'itemBorderWidth=' + itemStyle.borderWidth,"
                + "'headingFont=' + css(heading).font,"
                + "'badCategoryBorders=' + badCategoryBorders,"
                + "'badCategoryBackgrounds=' + badCategoryBackgrounds"
                + "].join(';');"
                + "}"));

        Assert.assertTrue(state.contains("categoryVisible=true"),
                "Side category/tag tab must be reachable from the vertical navigation. State: " + state);
        Assert.assertTrue(state.contains("categoryBg=" + BACKGROUND_RGB) && state.contains("categoryColor=" + TEXT_RGB),
                "Side category/tag tab must reuse the same background and text palette as the normal test view. State: "
                        + state);
        Assert.assertFalse(
                state.contains("boxBorder=rgba(184, 194, 207, 0.16)")
                        || state.contains("itemBorder=rgba(184, 194, 207, 0.16)"),
                "Side category/tag tab must not use a heavier border than the normal test tree. State: " + state
        );
        Assert.assertTrue(state.contains("boxBorder=missing") || state.contains("boxBorder=" + BORDER_RGB),
                "Side category/tag tab boxes must either be borderless or use the shared runtime border. State: " + state);
        Assert.assertTrue(
                ExtentRenderedState.numericValue(state, "boxBorderWidth") <= 1.0
                        && (state.contains("itemBorderWidth=-1")
                        || ExtentRenderedState.numericValue(state, "itemBorderWidth") <= 1.0),
                "Side category/tag tab borders must remain visually thin. State: " + state
        );
        Assert.assertEquals(ExtentRenderedState.numericValue(state, "badCategoryBorders"), 0.0,
                "Side category/tag tab must not contain mixed white/heavy borders in any visible runtime block. State: "
                        + state);
        Assert.assertEquals(ExtentRenderedState.numericValue(state, "badCategoryBackgrounds"), 0.0,
                "Side category/tag tab must keep the same runtime background in every visible runtime block. State: "
                        + state);
    }

    private static void assertTagSelectionOverlayState(String state) {
        Assert.assertTrue(
                state.contains("tagVisible=false") && state.contains("statusVisible=false"),
                "Selecting a tag must close all filter overlays instead of leaving dropdown rows over the test list. State: "
                        + state
        );
    }

    private static void assertTagSelectionLeftListStyle(String state) {
        Assert.assertTrue(
                state.contains("listBg=" + BACKGROUND_RGB)
                        && state.contains("rowBg=" + BACKGROUND_RGB)
                        && state.contains("detailBg=" + BACKGROUND_RGB),
                "After tag selection the left test list must keep the same runtime background as the default test view. State: "
                        + state
        );
        Assert.assertTrue(
                state.contains("listBorder=" + BORDER_RGB) && state.contains("rowBorder=" + BORDER_RGB),
                "After tag selection the left test list must keep the shared runtime border color. State: " + state
        );
        Assert.assertTrue(
                ExtentRenderedState.numericValue(state, "listBorderWidth") <= 1.0
                        && ExtentRenderedState.numericValue(state, "rowBorderWidth") <= 1.0,
                "After tag selection the left test list borders must remain visually thin. State: " + state
        );
    }

    private static void assertTagSelectionRightContentStyle(String state) {
        Assert.assertTrue(
                state.contains("rootBg=" + BACKGROUND_RGB) && state.contains("subBg=" + BACKGROUND_RGB),
                "After tag selection the right-side test/sub-step cards must keep the same runtime background. State: "
                        + state
        );
        Assert.assertTrue(
                state.contains("rootBorder=" + BORDER_RGB)
                        && state.contains("subBorder=" + BORDER_RGB)
                        && state.contains("tableHeaderBorder=" + BORDER_RGB)
                        && state.contains("tableCellBorder=" + BORDER_RGB),
                "After tag selection the right-side cards and detail tables must keep the shared runtime border color. State: "
                        + state
        );
        Assert.assertTrue(
                ExtentRenderedState.numericValue(state, "rootBorderWidth") <= 1.0
                        && ExtentRenderedState.numericValue(state, "subBorderWidth") <= 1.0
                        && ExtentRenderedState.numericValue(state, "tableHeaderBorderWidth") <= 1.0
                        && ExtentRenderedState.numericValue(state, "tableCellBorderWidth") <= 1.0,
                "After tag selection the right-side cards and detail tables must keep visually thin borders. State: "
                        + state
        );
        Assert.assertEquals(ExtentRenderedState.numericValue(state, "badBackgrounds"), 0.0,
                "After tag/status selection every visible reconstructed test surface must keep the shared runtime background. State: "
                        + state);
        Assert.assertEquals(ExtentRenderedState.numericValue(state, "badBorders"), 0.0,
                "After tag/status selection every visible reconstructed test surface must keep the shared runtime border. State: "
                        + state);
    }
}
