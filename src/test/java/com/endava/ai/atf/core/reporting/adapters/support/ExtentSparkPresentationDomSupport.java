package com.endava.ai.atf.core.reporting.adapters.support;

import com.endava.ai.atf.core.reporting.adapters.dom.ExtentSparkDomAssertions;
import com.microsoft.playwright.Page;

public abstract class ExtentSparkPresentationDomSupport extends ExtentSparkPresentationHtmlSupport {

    protected static void clickNode(Page page, String text) {
        ExtentSparkDomAssertions.clickNode(page, text);
    }

    protected static void waitForPreparedExtentTree(Page page) {
        ExtentSparkDomAssertions.waitForPreparedExtentTree(page);
    }

    protected static void assertTagSelectionKeepsCollectionVisible(
            Page page,
            String tag,
            String expectedVisible
    ) {
        ExtentSparkDomAssertions.assertTagSelectionKeepsCollectionVisible(page, tag, expectedVisible);
    }

    protected static void assertTagSelectionUsesAtfCollectionShell(
            Page page,
            String tag,
            int expectedMatchingClasses,
            String expectedVisible
    ) {
        ExtentSparkDomAssertions.assertTagSelectionUsesAtfCollectionShell(
                page,
                tag,
                expectedMatchingClasses,
                expectedVisible
        );
    }

    protected static Boolean selectExtentTag(Page page, String tag) {
        return ExtentSparkDomAssertions.selectExtentTag(page, tag);
    }

    protected static void assertPackageTagSelectionKeepsOnlyExpectedClasses(
            Page page,
            String packageTag,
            String[] expectedVisible,
            String[] forbiddenVisible
    ) {
        ExtentSparkDomAssertions.assertPackageTagSelectionKeepsOnlyExpectedClasses(
                page,
                packageTag,
                expectedVisible,
                forbiddenVisible
        );
    }

    protected static String tagFilterState(Page page) {
        return ExtentSparkDomAssertions.tagFilterState(page);
    }

    protected static String mainFilterState(Page page) {
        return ExtentSparkDomAssertions.mainFilterState(page);
    }

    protected static boolean isVisibleText(Page page, String text) {
        return ExtentSparkDomAssertions.isVisibleText(page, text);
    }

    protected static boolean isVisibleNode(Page page, String text) {
        return ExtentSparkDomAssertions.isVisibleNode(page, text);
    }

    protected static int arrowCount(Page page, String text) {
        return ExtentSparkDomAssertions.arrowCount(page, text);
    }

    protected static void assertNodeHeaderContains(Page page, String nodeText, String expected) {
        ExtentSparkDomAssertions.assertNodeHeaderContains(page, nodeText, expected);
    }

    protected static void assertNodeHeaderDoesNotContain(Page page, String nodeText, String forbidden) {
        ExtentSparkDomAssertions.assertNodeHeaderDoesNotContain(page, nodeText, forbidden);
    }

    protected static String directNodeHeaderText(Page page, String text) {
        return ExtentSparkDomAssertions.directNodeHeaderText(page, text);
    }

    protected static String nativePseudoArrow(Page page, String text) {
        return ExtentSparkDomAssertions.nativePseudoArrow(page, text);
    }

    protected static String extentNodeDebug(Page page, String parentText, String childText) {
        return ExtentSparkDomAssertions.extentNodeDebug(page, parentText, childText);
    }
}
