package com.endava.ai.atf.core.reporting.adapters.css;

import com.endava.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;
import com.microsoft.playwright.Page;
import org.testng.Assert;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExtentSparkTypographyAssertions extends ExtentSparkPresentationContractSupport {
    private ExtentSparkTypographyAssertions() {
    }

    public static void assertHierarchicalTypographyForFailureMetadataAndCodeBlocks(Page page) {
        String state = String.valueOf(page.evaluate("() => {"
                + "const px = node => node ? Number.parseFloat(getComputedStyle(node).fontSize || '0') : 0;"
                + "const font = node => node ? getComputedStyle(node).fontFamily : 'missing';"
                + "const statusBadge = selector => Array.from(document.querySelectorAll(selector))"
                + ".find(node => ['Pass','Fail','Skip','Skipped'].includes((node.textContent || '').trim()));"
                + "const testName = document.querySelector('.test-list .test-detail .name');"
                + "const testTime = document.querySelector('.test-list .test-detail .text-sm span');"
                + "const testStatus = document.querySelector('.test-content h5.test-status');"
                + "const leftStatusBadge = statusBadge('.test-list .badge');"
                + "const rightStatusBadge = statusBadge('.test-contents .card-header .badge, .test-contents .node-attr .badge');"
                + "const nodeName = Array.from(document.querySelectorAll('.test-contents .card-header a.node span'))"
                + ".find(node => !node.classList.contains('extent-atf-arrow'));"
                + "const visible = node => !!(node && (node.offsetWidth || node.offsetHeight || node.getClientRects().length));"
                + "const nodeTag = document.querySelector('.test-contents .card.extent-atf-root-node > .card-header .node-attr .badge');"
                + "const rootVisibleNodeAttrs = Array.from(document.querySelectorAll('.test-contents .card.extent-atf-root-node > .card-header .node-attr'))"
                + ".filter(visible).length;"
                + "const subVisibleNodeAttrs = Array.from(document.querySelectorAll('.test-contents .card.extent-atf-sub-node > .card-header .node-attr'))"
                + ".filter(visible).length;"
                + "const nestedVisibleNodeAttrs = Array.from(document.querySelectorAll("
                + "'.test-contents .card .card > .card-header .node-attr, "
                + ".test-content .detail-body .card .card > .card-header .node-attr, "
                + ".test-content .detail-body .accordion .accordion .card > .card-header .node-attr'))"
                + ".filter(visible).length;"
                + "const code = document.querySelector('.test-view textarea.code-block');"
                + "const logBadge = document.querySelector('.test-view table .badge');"
                + "const tableHeader = document.querySelector('.test-view table th');"
                + "const logTimestamp = document.querySelector('.test-view table td:nth-child(2)');"
                + "const logDetails = document.querySelector('.test-view table td:nth-child(3)');"
                + "const tagHeading = document.querySelector('.atf-runtime-tags-heading h3');"
                + "const tagHeader = document.querySelector('.atf-runtime-tag-table thead th:first-child');"
                + "const tagRow = document.querySelector('.atf-runtime-tag-table tbody td:first-child');"
                + "const arrow = document.querySelector('.test-contents .extent-atf-arrow');"
                + "const card = document.querySelector('.test-contents .card');"
                + "const nestedCard = document.querySelector('.test-contents .card .card');"
                + "const tableCell = document.querySelector('.test-view table td');"
                + "const testList = document.querySelector('.test-view .test-list');"
                + "const testListItem = document.querySelector('.test-view .test-list .test-item');"
                + "const arrowStyle = arrow ? getComputedStyle(arrow) : null;"
                + "const arrowBefore = arrow ? getComputedStyle(arrow, '::before') : null;"
                + "const cardStyle = card ? getComputedStyle(card) : null;"
                + "const nestedCardStyle = nestedCard ? getComputedStyle(nestedCard) : null;"
                + "const tableCellStyle = tableCell ? getComputedStyle(tableCell) : null;"
                + "const testListStyle = testList ? getComputedStyle(testList) : null;"
                + "const testListItemStyle = testListItem ? getComputedStyle(testListItem) : null;"
                + "return ["
                + "'testName=' + px(testName),"
                + "'testTime=' + px(testTime),"
                + "'testTimeColor=' + (testTime ? getComputedStyle(testTime).color : 'missing'),"
                + "'testStatus=' + px(testStatus),"
                + "'testStatusColor=' + (testStatus ? getComputedStyle(testStatus).color : 'missing'),"
                + "'testStatusFont=' + font(testStatus),"
                + "'leftStatusBadge=' + px(leftStatusBadge),"
                + "'leftStatusBadgeBg=' + (leftStatusBadge ? getComputedStyle(leftStatusBadge).backgroundColor : 'missing'),"
                + "'rightStatusBadge=' + px(rightStatusBadge),"
                + "'rightStatusBadgeBg=' + (rightStatusBadge ? getComputedStyle(rightStatusBadge).backgroundColor : 'missing'),"
                + "'nodeName=' + px(nodeName),"
                + "'nodeNameFont=' + font(nodeName),"
                + "'nodeTag=' + px(nodeTag),"
                + "'nodeTagColor=' + (nodeTag ? getComputedStyle(nodeTag).color : 'missing'),"
                + "'rootVisibleNodeAttrs=' + rootVisibleNodeAttrs,"
                + "'subVisibleNodeAttrs=' + subVisibleNodeAttrs,"
                + "'nestedVisibleNodeAttrs=' + nestedVisibleNodeAttrs,"
                + "'code=' + px(code),"
                + "'logBadge=' + px(logBadge),"
                + "'tableHeader=' + px(tableHeader),"
                + "'logTimestamp=' + px(logTimestamp),"
                + "'logDetails=' + px(logDetails),"
                + "'tagHeading=' + px(tagHeading),"
                + "'tagHeadingFont=' + font(tagHeading),"
                + "'tagHeader=' + px(tagHeader),"
                + "'tagRow=' + px(tagRow),"
                + "'codeFont=' + font(code),"
                + "'codeWeight=' + (code ? getComputedStyle(code).fontWeight : 'missing'),"
                + "'arrowBorderLeft=' + (arrowStyle ? Number.parseFloat(arrowStyle.borderLeftWidth || '0') : 0),"
                + "'arrowBefore=' + (arrowBefore ? arrowBefore.content : 'missing'),"
                + "'arrowFontSize=' + (arrowStyle ? Number.parseFloat(arrowStyle.fontSize || '0') : -1),"
                + "'cardBorder=' + (cardStyle ? cardStyle.borderTopColor : 'missing'),"
                + "'cardBorderWidth=' + (cardStyle ? Number.parseFloat(cardStyle.borderTopWidth || '0') : -1),"
                + "'nestedCardBorder=' + (nestedCardStyle ? nestedCardStyle.borderTopColor : 'missing'),"
                + "'nestedCardBorderWidth=' + (nestedCardStyle ? Number.parseFloat(nestedCardStyle.borderTopWidth || '0') : -1),"
                + "'tableCellBorder=' + (tableCellStyle ? tableCellStyle.borderTopColor : 'missing'),"
                + "'tableCellBorderWidth=' + (tableCellStyle ? Number.parseFloat(tableCellStyle.borderTopWidth || '0') : -1),"
                + "'testListBorder=' + (testListStyle ? testListStyle.borderRightColor : 'missing'),"
                + "'testListBorderWidth=' + (testListStyle ? Number.parseFloat(testListStyle.borderRightWidth || '0') : -1),"
                + "'testListItemBorder=' + (testListItemStyle ? testListItemStyle.borderBottomColor : 'missing'),"
                + "'testListItemBorderWidth=' + (testListItemStyle ? Number.parseFloat(testListItemStyle.borderBottomWidth || '0') : -1)"
                + "].join(';');"
                + "}"));

        TypographyState values = TypographyState.from(state);
        assertListAndTitleTypography(state, values);
        assertNodeMetadataTypography(state, values);
        assertLogAndCodeTypography(state, values);
        assertSharedBorderTypography(state, values);
    }

    private static void assertListAndTitleTypography(String state, TypographyState values) {
        Assert.assertTrue(
                values.testNameSize > values.testTimeSize,
                "Left-list timestamp must stay smaller than the test name. State: " + state
        );
        Assert.assertTrue(
                state.contains("testTimeColor=" + EXTENT_RUNTIME_TIME_RGB),
                "Left-list timestamp/duration must use the darker runtime time color. State: " + state
        );
        Assert.assertTrue(
                Math.abs(values.testNameSize - values.tagRowSize) <= 1.0,
                "Left-list test name must use the same readable scale as runtime tag rows. State: " + state
        );
        Assert.assertTrue(
                Math.abs(values.testStatusSize - (values.tagRowSize * 1.10)) <= 1.0
                        && values.testStatusSize > values.tagRowSize
                        && state.contains("testStatusColor=" + EXTENT_TEXT_RGB),
                "Main test title must be neutral and about 10% larger than the runtime tag row text. State: "
                        + state
        );
        Assert.assertTrue(
                Math.abs(values.rightStatusBadgeSize - values.leftStatusBadgeSize) <= 1.0
                        && stateValue(state, "rightStatusBadgeBg").equals(stateValue(state, "leftStatusBadgeBg")),
                "Right-side Pass/Skip/Fail badges must use the same font scale and color contract as left-list status badges. State: "
                        + state
        );
        Assert.assertTrue(
                sameFontFamily(state, "testStatusFont", "tagHeadingFont"),
                "Main test title must use the same font family as ATF Runtime Tag Audit. State: " + state
        );
    }

    private static void assertNodeMetadataTypography(String state, TypographyState values) {
        Assert.assertTrue(
                values.nodeNameSize > values.nodeTagSize,
                "Node metadata tags must stay smaller than the test method name. State: " + state
        );
        Assert.assertTrue(
                state.contains("nodeTagColor=" + EXTENT_RUNTIME_TIME_RGB),
                "Top-level class/package metadata labels must use the same subtle color as left-list runtime timestamps. State: "
                        + state
        );
        Assert.assertTrue(
                values.rootVisibleNodeAttrs > 0,
                "Root test method cards must keep one visible class/package metadata strip for orientation. State: " + state
        );
        Assert.assertEquals(values.subVisibleNodeAttrs, 0.0,
                "Nested setup/body/action substeps must not repeat class/package metadata labels. State: " + state);
        Assert.assertEquals(values.nestedVisibleNodeAttrs, 0.0,
                "Nested cards must structurally hide class/package metadata even if Spark rebuilds the view. State: "
                        + state);
        Assert.assertTrue(Math.abs(values.nodeNameSize - values.tagRowSize) <= 1.0,
                "Collapsible test-step labels must use the same readable scale as runtime tag rows. State: " + state);
        Assert.assertTrue(sameFontFamily(state, "nodeNameFont", "tagHeadingFont"),
                "Collapsible test-step labels must use the same font family as ATF Runtime Tag Audit. State: " + state);
    }

    private static void assertLogAndCodeTypography(String state, TypographyState values) {
        Assert.assertTrue(values.codeSize > 0 && values.codeSize < values.nodeNameSize,
                "Failure code blocks must use compact exception typography. State: " + state);
        Assert.assertTrue(values.logBadgeSize > 0 && values.logBadgeSize < values.nodeNameSize,
                "Info/Pass/Fail badges inside collapsed content must stay smaller than the collapse label. State: " + state);
        Assert.assertTrue(Math.abs(values.tableHeaderSize - (values.nodeNameSize * 0.80)) <= 1.0,
                "STATUS/TIMESTAMP/DETAILS headers must be about 20% smaller than the collapse label. State: " + state);
        Assert.assertTrue(Math.abs(values.logTimestampSize - (values.nodeNameSize * 0.90)) <= 1.0,
                "Log timestamps inside collapsed content must be about 10% smaller than the collapse-label text scale. State: "
                        + state);
        Assert.assertTrue(Math.abs(values.logDetailsSize - (values.nodeNameSize * 0.90)) <= 1.0,
                "Log details inside collapsed content must be about 10% smaller than the collapse-label text scale. State: "
                        + state);
        Assert.assertTrue(numericStateValue(state, "codeWeight") < 400,
                "Failure code blocks, stack traces and attachment bodies must use the thinner code weight. State: " + state);
        Assert.assertTrue(
                values.arrowBorderLeft > 0 && values.arrowFontSize == 0 && state.contains("arrowBefore=none"),
                "Collapse arrows must be CSS triangles, not FontAwesome glyphs that can render as squares. State: " + state
        );
        Assert.assertTrue(sameFontFamily(state, "codeFont", "tagHeadingFont"),
                "Failure code blocks must keep the same report font family while using a thinner weight. State: " + state);
    }

    private static void assertSharedBorderTypography(String state, TypographyState values) {
        Assert.assertTrue(
                state.contains("cardBorder=" + EXTENT_RUNTIME_BORDER_RGB)
                        && state.contains("nestedCardBorder=" + EXTENT_RUNTIME_BORDER_RGB)
                        && state.contains("tableCellBorder=rgba(184, 194, 207, 0.12)")
                        && state.contains("testListBorder=rgba(184, 194, 207, 0.12)")
                        && state.contains("testListItemBorder=rgba(184, 194, 207, 0.12)"),
                "Test/sub-step cards, table cells and left-list rows must use one shared discreet runtime border color. State: "
                        + state
        );
        Assert.assertTrue(
                values.cardBorderWidth <= 1.0
                        && values.nestedCardBorderWidth <= 1.0
                        && values.tableCellBorderWidth <= 1.0
                        && values.testListBorderWidth <= 1.0
                        && values.testListItemBorderWidth <= 1.0,
                "Test/sub-step card, table and left-list borders must stay visually thin instead of returning to heavy Spark defaults. State: "
                        + state
        );
    }

    private static double numericStateValue(String state, String key) {
        Pattern pattern = Pattern.compile(Pattern.quote(key) + "=([0-9]+(?:\\.[0-9]+)?)");
        Matcher matcher = pattern.matcher(state);
        Assert.assertTrue(matcher.find(), "State must expose numeric value for " + key + ": " + state);
        return Double.parseDouble(matcher.group(1));
    }

    private static boolean sameFontFamily(String state, String firstKey, String secondKey) {
        String first = stateValue(state, firstKey).toLowerCase(Locale.ROOT);
        String second = stateValue(state, secondKey).toLowerCase(Locale.ROOT);
        return !first.isBlank() && !second.isBlank() && (first.equals(second)
                || first.contains(second)
                || second.contains(first));
    }

    private static String stateValue(String state, String key) {
        Pattern pattern = Pattern.compile(Pattern.quote(key) + "=([^;]+)");
        Matcher matcher = pattern.matcher(state);
        Assert.assertTrue(matcher.find(), "State must expose value for " + key + ": " + state);
        return matcher.group(1).trim();
    }

    private static final class TypographyState {
        private final double testNameSize;
        private final double testTimeSize;
        private final double testStatusSize;
        private final double leftStatusBadgeSize;
        private final double rightStatusBadgeSize;
        private final double nodeNameSize;
        private final double nodeTagSize;
        private final double rootVisibleNodeAttrs;
        private final double subVisibleNodeAttrs;
        private final double nestedVisibleNodeAttrs;
        private final double codeSize;
        private final double logBadgeSize;
        private final double tableHeaderSize;
        private final double logTimestampSize;
        private final double logDetailsSize;
        private final double tagRowSize;
        private final double arrowBorderLeft;
        private final double arrowFontSize;
        private final double cardBorderWidth;
        private final double nestedCardBorderWidth;
        private final double tableCellBorderWidth;
        private final double testListBorderWidth;
        private final double testListItemBorderWidth;

        private TypographyState(String state) {
            this.testNameSize = numericStateValue(state, "testName");
            this.testTimeSize = numericStateValue(state, "testTime");
            this.testStatusSize = numericStateValue(state, "testStatus");
            this.leftStatusBadgeSize = numericStateValue(state, "leftStatusBadge");
            this.rightStatusBadgeSize = numericStateValue(state, "rightStatusBadge");
            this.nodeNameSize = numericStateValue(state, "nodeName");
            this.nodeTagSize = numericStateValue(state, "nodeTag");
            this.rootVisibleNodeAttrs = numericStateValue(state, "rootVisibleNodeAttrs");
            this.subVisibleNodeAttrs = numericStateValue(state, "subVisibleNodeAttrs");
            this.nestedVisibleNodeAttrs = numericStateValue(state, "nestedVisibleNodeAttrs");
            this.codeSize = numericStateValue(state, "code");
            this.logBadgeSize = numericStateValue(state, "logBadge");
            this.tableHeaderSize = numericStateValue(state, "tableHeader");
            this.logTimestampSize = numericStateValue(state, "logTimestamp");
            this.logDetailsSize = numericStateValue(state, "logDetails");
            this.tagRowSize = numericStateValue(state, "tagRow");
            this.arrowBorderLeft = numericStateValue(state, "arrowBorderLeft");
            this.arrowFontSize = numericStateValue(state, "arrowFontSize");
            this.cardBorderWidth = numericStateValue(state, "cardBorderWidth");
            this.nestedCardBorderWidth = numericStateValue(state, "nestedCardBorderWidth");
            this.tableCellBorderWidth = numericStateValue(state, "tableCellBorderWidth");
            this.testListBorderWidth = numericStateValue(state, "testListBorderWidth");
            this.testListItemBorderWidth = numericStateValue(state, "testListItemBorderWidth");
        }

        private static TypographyState from(String state) {
            return new TypographyState(state);
        }
    }
}
