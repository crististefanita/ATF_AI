package com.endava.ai.atf.core.reporting.adapters.css;

import com.endava.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;
import com.microsoft.playwright.Page;
import org.testng.Assert;

public final class ExtentSparkShellPaletteAssertions extends ExtentSparkPresentationContractSupport {
    private static final String SIDE_NAV_SELECTED_BACKGROUND_RGB = "rgb(22, 35, 56)";

    private ExtentSparkShellPaletteAssertions() {
    }

    public static void assertShellSectionsRenderWithSharedDarkPalette(Page page) {
        String testViewState = (String) page.evaluate("() => {"
                + "const color = selector => {"
                + "const node = document.querySelector(selector);"
                + "return node ? getComputedStyle(node).backgroundColor : 'missing';"
                + "};"
                + "const text = selector => {"
                + "const node = document.querySelector(selector);"
                + "return node ? getComputedStyle(node).color : 'missing';"
                + "};"
                + "return ["
                + "'body=' + color('body'),"
                + "'bodyText=' + text('body'),"
                + "'header=' + color('.header.navbar'),"
                + "'navLeft=' + color('.nav-left'),"
                + "'navRight=' + color('.nav-right'),"
                + "'sideNav=' + color('.side-nav'),"
                + "'mainContent=' + color('.main-content'),"
                + "'testWrapper=' + color('.test-wrapper'),"
                + "'testsToolbar=' + color('.test-list-tools'),"
                + "'testsList=' + color('.test-list'),"
                + "'testsListItem=' + color('.test-list-item'),"
                + "'testsWrapper=' + color('.test-list-wrapper'),"
                + "'inactiveItem=' + color('.test-list li.test-item:not(.active), .test-list .test-item:not(.active)'),"
                + "'testContent=' + color('.test-content'),"
                + "'testContentText=' + text('.test-content'),"
                + "'detailBody=' + color('.detail-body'),"
                + "'detailBodyText=' + text('.detail-body'),"
                + "'testCard=' + color('.test-view .test-contents .card'),"
                + "'testCardText=' + text('.test-view .test-contents .card'),"
                + "'testCardHeader=' + color('.test-view .test-contents .card-header'),"
                + "'testCardBody=' + color('.test-view .test-contents .card-body'),"
                + "'testCardBodyText=' + text('.test-view .test-contents .card-body'),"
                + "'activeItem=' + color('.test-list li.active, .test-list .test-item.active')"
                + "].join(';');"
                + "}");

        assertTestViewPalette(testViewState);
        assertDashboardPalette(page);
        assertCategoryPalette(page);
    }

    public static void assertTestViewUsesSingleRuntimeBackground(Page page) {
        String state = String.valueOf(page.evaluate("() => {"
                + "const bg = selector => {"
                + "const node = document.querySelector(selector);"
                + "return node ? getComputedStyle(node).backgroundColor : 'missing';"
                + "};"
                + "return ["
                + "'testList=' + bg('.test-list'),"
                + "'testContent=' + bg('.test-content'),"
                + "'detailBody=' + bg('.detail-body'),"
                + "'contents=' + bg('.test-contents'),"
                + "'card=' + bg('.test-contents .card'),"
                + "'nestedCard=' + bg('.test-contents .card .card'),"
                + "'deepCard=' + bg('.test-contents .card .card .card'),"
                + "'header=' + bg('.test-contents .card-header'),"
                + "'body=' + bg('.test-contents .card-body'),"
                + "'nodeAttr=' + bg('.test-contents .node-attr'),"
                + "'nodeAttrBadge=' + bg('.test-contents .node-attr .badge')"
                + "].join(';');"
                + "}"));

        Assert.assertFalse(
                state.contains("missing"),
                "Extent test view must expose all audited containers. State: " + state
        );
        Assert.assertTrue(
                allStateValuesEqual(state, EXTENT_BACKGROUND_RGB),
                "Extent test view must use the single configured runtime background across list, content, time badges, cards, metadata strips and nested cards. State: "
                        + state
        );
    }

    public static void assertCategoryViewReusesTestViewVisualContract(Page page) {
        String testViewState = readVisualContractState(page, ".test-view");

        page.click("#nav-category");
        page.waitForTimeout(500);
        String categoryViewState = readVisualContractState(page, ".category-view");

        Assert.assertFalse(
                testViewState.contains("missing") || categoryViewState.contains("missing"),
                "Test and tag/category views must expose the same audited visual surfaces. Test view: "
                        + testViewState + " Category view: " + categoryViewState
        );
        Assert.assertTrue(
                allStateValuesEqual(testViewState, EXTENT_BACKGROUND_RGB)
                        && allStateValuesEqual(categoryViewState, EXTENT_BACKGROUND_RGB),
                "Switching to the tag/category icon must not change background, card, tag or table surfaces. Test view: "
                        + testViewState + " Category view: " + categoryViewState
        );
    }

    private static void assertTestViewPalette(String testViewState) {
        assertComputedColor(testViewState, "body", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "header", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "navLeft", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "navRight", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "sideNav", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "mainContent", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "testWrapper", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "testsToolbar", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "testsList", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "testsListItem", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "testsWrapper", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "inactiveItem", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "testContent", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "detailBody", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "testCard", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "testCardHeader", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "testCardBody", EXTENT_BACKGROUND_RGB);
        assertComputedColor(testViewState, "activeItem", EXTENT_ACTIVE_BACKGROUND_RGB);
        assertComputedColor(testViewState, "bodyText", EXTENT_TEXT_RGB);
        assertComputedColor(testViewState, "testContentText", EXTENT_TEXT_RGB);
        assertComputedColor(testViewState, "detailBodyText", EXTENT_TEXT_RGB);
        assertComputedColor(testViewState, "testCardText", EXTENT_TEXT_RGB);
        assertComputedColor(testViewState, "testCardBodyText", EXTENT_TEXT_RGB);
    }

    private static void assertDashboardPalette(Page page) {
        page.click("#nav-dashboard");
        page.waitForTimeout(500);
        String dashboardState = (String) page.evaluate("() => {"
                + "const color = selector => {"
                + "const node = document.querySelector(selector);"
                + "return node ? getComputedStyle(node).backgroundColor : 'missing';"
                + "};"
                + "return ["
                + "'dashboard=' + color('.dashboard-view'),"
                + "'dashboardCard=' + color('.dashboard-view > .row:first-of-type .card'),"
                + "'dashboardSummary=' + color('.dashboard-view .atf-runtime-dashboard-tests-card, "
                + ".dashboard-view > .row:first-of-type .card'),"
                + "'timelineCard=' + color('.dashboard-view .atf-runtime-timeline-card'),"
                + "'timelineHeader=' + color('.dashboard-view .atf-runtime-timeline-card .card-header'),"
                + "'timelineBody=' + color('.dashboard-view .atf-runtime-timeline-card .card-body'),"
                + "'tagHeading=' + color('.dashboard-view .atf-runtime-tags-heading'),"
                + "'tagHeader=' + color('.dashboard-view .atf-runtime-tag-card .card-header'),"
                + "'tagBody=' + color('.dashboard-view .atf-runtime-tag-card .card-body'),"
                + "'tagTableHead=' + color('.dashboard-view .atf-runtime-tag-table thead th'),"
                + "'tagCard=' + color('.dashboard-view .atf-runtime-tag-card')"
                + "].join(';');"
                + "}");
        assertComputedColor(dashboardState, "dashboard", EXTENT_BACKGROUND_RGB);
        assertComputedColor(dashboardState, "dashboardCard", EXTENT_BACKGROUND_RGB);
        assertComputedColor(dashboardState, "dashboardSummary", EXTENT_BACKGROUND_RGB);
        assertComputedColor(dashboardState, "timelineCard", EXTENT_BACKGROUND_RGB);
        assertComputedColor(dashboardState, "timelineHeader", EXTENT_BACKGROUND_RGB);
        assertComputedColor(dashboardState, "timelineBody", EXTENT_BACKGROUND_RGB);
        assertComputedColor(dashboardState, "tagHeading", EXTENT_BACKGROUND_RGB);
        assertComputedColor(dashboardState, "tagHeader", EXTENT_BACKGROUND_RGB);
        assertComputedColor(dashboardState, "tagBody", EXTENT_BACKGROUND_RGB);
        assertComputedColor(dashboardState, "tagTableHead", EXTENT_BACKGROUND_RGB);
        assertComputedColor(dashboardState, "tagCard", EXTENT_BACKGROUND_RGB);
    }

    private static void assertCategoryPalette(Page page) {
        page.click("#nav-category");
        page.waitForTimeout(500);
        String categoryState = (String) page.evaluate("() => {"
                + "const color = selector => {"
                + "const node = document.querySelector(selector);"
                + "return node ? getComputedStyle(node).backgroundColor : 'missing';"
                + "};"
                + "return ["
                + "'category=' + color('.category-view'),"
                + "'categoryHeading=' + color('.category-view .atf-runtime-tags-heading'),"
                + "'categoryTagHeader=' + color('.category-view .atf-runtime-tag-card .card-header'),"
                + "'categoryTagBody=' + color('.category-view .atf-runtime-tag-card .card-body'),"
                + "'categoryTagTableHead=' + color('.category-view .atf-runtime-tag-table thead th'),"
                + "'categoryTagCard=' + color('.category-view .atf-runtime-tag-card')"
                + "].join(';');"
                + "}");
        assertComputedColor(categoryState, "category", EXTENT_BACKGROUND_RGB);
        assertComputedColor(categoryState, "categoryHeading", EXTENT_BACKGROUND_RGB);
    }

    public static void assertSideNavSelectedStateIsVisible(Page page) {
        String state = String.valueOf(page.evaluate("() => {"
                + "const ids = ['nav-test','nav-category','nav-dashboard'];"
                + "const entries = [];"
                + "const widths = [];"
                + "const heights = [];"
                + "ids.forEach(id => {"
                + "const link = document.getElementById(id);"
                + "if (!link) { entries.push(id + ':missing=true'); return; }"
                + "link.click();"
                + "const activeItems = Array.from(document.querySelectorAll('.side-nav-menu .nav-item.active'));"
                + "const active = link.closest('.nav-item');"
                + "const inactive = Array.from(document.querySelectorAll('.side-nav-menu .nav-item')).find(node => node !== active);"
                + "const activeIcon = active ? active.querySelector('i') : null;"
                + "const inactiveIcon = inactive ? inactive.querySelector('i') : null;"
                + "const activeStyle = active ? getComputedStyle(active) : null;"
                + "const inactiveStyle = inactive ? getComputedStyle(inactive) : null;"
                + "const activeIconStyle = activeIcon ? getComputedStyle(activeIcon) : null;"
                + "const inactiveIconStyle = inactiveIcon ? getComputedStyle(inactiveIcon) : null;"
                + "const rect = active ? active.getBoundingClientRect() : {width:0,height:0,left:0};"
                + "widths.push(Math.round(rect.width));"
                + "heights.push(Math.round(rect.height));"
                + "entries.push(["
                + "id + ':activeCount=' + activeItems.length,"
                + "id + ':activeSelf=' + (active && active.classList.contains('active')),"
                + "id + ':activeBg=' + (activeStyle ? activeStyle.backgroundColor : 'missing'),"
                + "id + ':inactiveBg=' + (inactiveStyle ? inactiveStyle.backgroundColor : 'missing'),"
                + "id + ':activeShadow=' + (activeStyle ? activeStyle.boxShadow : 'missing'),"
                + "id + ':activeIconOpacity=' + (activeIconStyle ? activeIconStyle.opacity : 'missing'),"
                + "id + ':inactiveIconOpacity=' + (inactiveIconStyle ? inactiveIconStyle.opacity : 'missing'),"
                + "id + ':width=' + Math.round(rect.width),"
                + "id + ':height=' + Math.round(rect.height)"
                + "].join(';'));"
                + "});"
                + "entries.push('widthSpread=' + (Math.max.apply(Math,widths)-Math.min.apply(Math,widths)));"
                + "entries.push('heightSpread=' + (Math.max.apply(Math,heights)-Math.min.apply(Math,heights)));"
                + "return entries.join(';');"
                + "}"));

        for (String id : new String[]{"nav-test", "nav-category", "nav-dashboard"}) {
            Assert.assertTrue(state.contains(id + ":activeCount=1") && state.contains(id + ":activeSelf=true"),
                    "Exactly one Extent left sidebar item must be selected after clicking " + id
                            + ". State: " + state);
            Assert.assertTrue(state.contains(id + ":activeBg=" + SIDE_NAV_SELECTED_BACKGROUND_RGB),
                    "Extent left sidebar selected tab must be visually distinct from inactive tabs. "
                            + "Logical active class exists, but selected marker is not visible enough. State: " + state);
            Assert.assertTrue(state.contains(id + ":inactiveBg=" + EXTENT_BACKGROUND_RGB),
                    "Inactive Extent sidebar tabs must stay quiet on the shared background. State: " + state);
            Assert.assertTrue(state.contains(id + ":activeShadow=rgb(66, 165, 245) 3px 0px 0px 0px inset"),
                    "Extent left sidebar selected tab must expose a stable structural marker. State: " + state);
            Assert.assertTrue(state.contains(id + ":activeIconOpacity=1")
                            && state.contains(id + ":inactiveIconOpacity=0.62"),
                    "Extent left sidebar selected icon must be stronger than inactive icons. State: " + state);
        }
        Assert.assertTrue(state.contains("widthSpread=0") && state.contains("heightSpread=0"),
                "Switching Extent sidebar tabs must not change sidebar item dimensions. State: " + state);
    }

    private static String readVisualContractState(Page page, String viewSelector) {
        return String.valueOf(page.evaluate("viewSelector => {"
                + "const bg = selector => {"
                + "const node = document.querySelector(selector);"
                + "return node ? getComputedStyle(node).backgroundColor : 'missing';"
                + "};"
                + "if (viewSelector === '.category-view') {"
                + "return ["
                + "'view=' + bg('.category-view'),"
                + "'heading=' + bg('.category-view .atf-runtime-tags-heading'),"
                + "'failurePanel=' + bg('.category-view .atf-runtime-failure-empty, "
                + ".category-view .atf-runtime-failure-summary, .category-view .atf-runtime-failure-details, "
                + ".category-view .atf-runtime-tag-card'),"
                + "'failureCode=' + bg('.category-view .atf-runtime-failure-code, "
                + ".category-view .atf-runtime-failure-empty, .category-view .atf-runtime-tag-card')"
                + "].join(';');"
                + "}"
                + "return ["
                + "'view=' + bg(viewSelector),"
                + "'list=' + bg(viewSelector + ' .test-list'),"
                + "'content=' + bg(viewSelector + ' .test-content'),"
                + "'card=' + bg(viewSelector + ' .test-contents .card'),"
                + "'header=' + bg(viewSelector + ' .test-contents .card-header'),"
                + "'body=' + bg(viewSelector + ' .test-contents .card-body')"
                + "].join(';');"
                + "}", viewSelector));
    }

    private static boolean allStateValuesEqual(String state, String expectedValue) {
        String[] entries = state.split(";");
        for (String entry : entries) {
            int separator = entry.indexOf('=');
            if (separator < 0 || !entry.substring(separator + 1).trim().equals(expectedValue)) {
                return false;
            }
        }
        return entries.length > 0;
    }
}
