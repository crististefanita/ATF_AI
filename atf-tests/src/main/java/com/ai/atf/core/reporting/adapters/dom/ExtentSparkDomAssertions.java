package com.ai.atf.core.reporting.adapters.dom;

import com.microsoft.playwright.Page;
import org.testng.Assert;

public final class ExtentSparkDomAssertions {

    private ExtentSparkDomAssertions() {
    }

    public static void clickNode(Page page, String text) {
        boolean clicked = ExtentSparkDomActions.clickNode(page, text);
        Assert.assertTrue(clicked, "Expected node to exist in rendered Extent report: " + text);
    }

    public static void waitForPreparedExtentTree(Page page) {
        ExtentSparkDomActions.waitForPreparedExtentTree(page);
    }

    public static void assertTagSelectionKeepsCollectionVisible(Page page, String tag, String expectedVisible) {
        boolean selected = ExtentSparkDomActions.selectExtentTag(page, tag);
        Assert.assertTrue(selected, "Spark tag dropdown must allow selecting collection tag: " + tag);

        page.waitForTimeout(500);

        String state = tagFilterState(page);
        String main = mainFilterState(page);
        Assert.assertTrue(
                main.contains(expectedVisible),
                "Selecting tag [" + tag + "] must keep expected collection test visible. State: " + state
        );
    }

    public static void assertTagSelectionUsesAtfCollectionShell(
            Page page,
            String tag,
            int expectedMatchingClasses,
            String expectedVisible
    ) {
        boolean selected = ExtentSparkDomActions.selectExtentTag(page, tag);
        Assert.assertTrue(selected, "Spark tag dropdown must allow selecting tag: " + tag);

        page.waitForTimeout(500);

        String state = ExtentSparkDomState.collectionShellState(page);

        Assert.assertTrue(
                state.contains("shell=true"),
                "Selecting tag [" + tag + "] must render the ATF collection shell, not Spark's native single-class view. "
                        + "State: " + state
        );
        Assert.assertTrue(
                state.contains("heading=" + tag),
                "ATF collection shell must keep the selected tag as the heading. State: " + state
        );
        Assert.assertTrue(
                state.contains("badge=" + expectedMatchingClasses + " matching test classes"),
                "ATF collection shell must publish a deterministic matching-class count. State: " + state
        );
        Assert.assertTrue(
                state.contains(expectedVisible),
                "ATF collection shell must keep expected test content visible: " + expectedVisible + ". State: " + state
        );
    }

    public static Boolean selectExtentTag(Page page, String tag) {
        return ExtentSparkDomActions.selectExtentTag(page, tag);
    }

    public static void assertPackageTagSelectionKeepsOnlyExpectedClasses(
            Page page,
            String packageTag,
            String[] expectedVisible,
            String[] forbiddenVisible
    ) {
        boolean selected = ExtentSparkDomActions.selectExtentTag(page, packageTag);
        Assert.assertTrue(selected, "Spark tag dropdown must allow selecting package tag: " + packageTag);

        page.waitForTimeout(500);

        String state = tagFilterState(page);
        String main = mainFilterState(page);
        for (String expected : expectedVisible) {
            Assert.assertTrue(
                    main.contains(expected),
                    "Selecting package [" + packageTag + "] must keep exact-package test visible: "
                            + expected + ". State: " + state
            );
        }
        for (String forbidden : forbiddenVisible) {
            Assert.assertFalse(
                    main.contains(forbidden),
                    "Selecting package [" + packageTag + "] must exclude non-package or subpackage test: "
                            + forbidden + ". State: " + state
            );
        }
    }

    public static String tagFilterState(Page page) {
        return ExtentSparkDomState.tagFilterState(page);
    }

    public static String mainFilterState(Page page) {
        return ExtentSparkDomState.mainFilterState(page);
    }

    public static boolean isVisibleText(Page page, String text) {
        return ExtentSparkDomState.isVisibleText(page, text);
    }

    public static boolean isVisibleNode(Page page, String text) {
        return ExtentSparkDomState.isVisibleNode(page, text);
    }

    public static int arrowCount(Page page, String text) {
        return ExtentSparkDomState.arrowCount(page, text);
    }

    public static void assertNodeHeaderContains(Page page, String nodeText, String expected) {
        Assert.assertTrue(
                directNodeHeaderText(page, nodeText).contains(expected),
                "Test method header must keep its class/package metadata badge: node="
                        + nodeText + ", expected=" + expected
        );
    }

    public static void assertNodeHeaderDoesNotContain(Page page, String nodeText, String forbidden) {
        Assert.assertFalse(
                directNodeHeaderText(page, nodeText).contains(forbidden),
                "Substep headers must not repeat test class/package metadata: node="
                        + nodeText + ", forbidden=" + forbidden
        );
    }

    public static String directNodeHeaderText(Page page, String text) {
        return ExtentSparkDomState.directNodeHeaderText(page, text);
    }

    public static String nativePseudoArrow(Page page, String text) {
        return ExtentSparkDomState.nativePseudoArrow(page, text);
    }

    public static String extentNodeDebug(Page page, String parentText, String childText) {
        return ExtentSparkDomState.extentNodeDebug(page, parentText, childText);
    }

    public static String selectTagExpandAndReadNodeMetadata(Page page, String tag) {
        return ExtentSparkDomActions.selectTagExpandAndReadNodeMetadata(page, tag);
    }
}
