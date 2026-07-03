package com.ai.atf.core.reporting.adapters.tags;

import com.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;
import com.ai.atf.core.reporting.adapters.dom.ExtentRenderedState;
import com.microsoft.playwright.Page;
import org.testng.Assert;

import java.util.Locale;

public final class ExtentSparkTagNavigationAssertions extends ExtentSparkPresentationContractSupport {
    private static final String BACKGROUND_RGB = EXTENT_BACKGROUND_RGB;
    private static final String TEXT_RGB = EXTENT_TEXT_RGB;
    private static final String BORDER_RGB = EXTENT_RUNTIME_BORDER_RGB;

    private ExtentSparkTagNavigationAssertions() {
    }

    public static void assertStatusDropdownRendersDarkLabeledSkipOption(Page page) {
        String state = String.valueOf(page.evaluate("() => {"
                + "const menu = document.querySelector('#status-toggle.dropdown-menu');"
                + "if (!menu) { return 'missing-menu'; }"
                + "menu.classList.add('show');"
                + "menu.style.display = 'block';"
                + "const style = getComputedStyle(menu);"
                + "const label = menu.querySelector('.atf-status-filter-label');"
                + "const items = Array.from(menu.querySelectorAll('.dropdown-item'))"
                + ".map(item => (item.textContent || '').replace(/\\s+/g, ' ').trim()).join('|');"
                + "const skip = menu.querySelector('[status=\"skip\"]');"
                + "const skipMarker = skip && skip.querySelector('.status.warning');"
                + "const firstItem = menu.querySelector('.dropdown-item');"
                + "const firstItemStyle = firstItem ? getComputedStyle(firstItem) : null;"
                + "return ["
                + "'bg=' + style.backgroundColor,"
                + "'label=' + (label ? label.textContent.trim() : 'missing'),"
                + "'items=' + items,"
                + "'skip=' + !!skip,"
                + "'skipMarker=' + !!skipMarker,"
                + "'itemBg=' + (firstItemStyle ? firstItemStyle.backgroundColor : 'missing'),"
                + "'itemColor=' + (firstItemStyle ? firstItemStyle.color : 'missing'),"
                + "'border=' + style.borderTopColor,"
                + "'borderWidth=' + Number.parseFloat(style.borderTopWidth || '0'),"
                + "'itemBorder=' + (firstItemStyle ? firstItemStyle.borderBottomColor : 'missing'),"
                + "'itemBorderWidth=' + (firstItemStyle ? Number.parseFloat(firstItemStyle.borderBottomWidth || '0') : -1)"
                + "].join(';');"
                + "}"));

        Assert.assertTrue(
                state.contains("bg=" + BACKGROUND_RGB)
                        && state.contains("itemBg=" + BACKGROUND_RGB)
                        && state.contains("itemColor=" + TEXT_RGB),
                "Status dropdown must render with the same dark palette as the tag dropdown. State: " + state
        );
        Assert.assertTrue(
                state.contains("border=" + BORDER_RGB)
                        && state.contains("itemBorder=" + BORDER_RGB),
                "Status dropdown borders must match the shared runtime border color. State: " + state
        );
        Assert.assertTrue(
                ExtentRenderedState.numericValue(state, "borderWidth") <= 1.0
                        && ExtentRenderedState.numericValue(state, "itemBorderWidth") <= 1.0,
                "Status dropdown borders must stay visually thin like the test tree. State: " + state
        );
        Assert.assertTrue(
                state.contains("label=Status"),
                "Status dropdown must expose a visible label. State: " + state
        );
        Assert.assertTrue(
                state.contains("items=Pass|Fail|Skip|Clear")
                        && state.contains("skip=true")
                        && state.contains("skipMarker=true"),
                "Status dropdown must expose Pass, Fail, Skip and Clear actions with a warning skip marker. State: "
                        + state
        );
    }

    public static void assertSelectionToolbarButtonsKeepFontawesomeIcons(Page page) {
        String state = String.valueOf(page.evaluate("() => {"
                + "const icons = Array.from(document.querySelectorAll('.test-list-tools i.fa, .side-nav i.fa'));"
                + "const fonts = icons.map(icon => getComputedStyle(icon).fontFamily).join('|');"
                + "const labels = icons.map(icon => icon.className).join('|');"
                + "return ['count=' + icons.length, 'fonts=' + fonts, 'labels=' + labels].join(';');"
                + "}"));

        Assert.assertTrue(
                state.contains("count=") && !state.contains("count=0"),
                "Extent selection toolbar must expose icon elements for status/tag navigation. State: " + state
        );
        Assert.assertTrue(
                state.toLowerCase(Locale.ROOT).contains("fontawesome")
                        || state.toLowerCase(Locale.ROOT).contains("font awesome"),
                "Extent selection toolbar icons must keep the FontAwesome font instead of rendering square glyphs. State: "
                        + state
        );
    }

    public static void assertTagDropdownRendersAsScrollableDarkMenu(Page page) {
        String state = String.valueOf(page.evaluate("() => {"
                + "const menu = document.querySelector('#tag-toggle.dropdown-menu');"
                + "if (!menu) { return 'missing-menu'; }"
                + "menu.classList.add('show');"
                + "menu.style.display = 'block';"
                + "const item = menu.querySelector('.dropdown-item');"
                + "const menuStyle = getComputedStyle(menu);"
                + "const itemStyle = item ? getComputedStyle(item) : null;"
                + "const rect = menu.getBoundingClientRect();"
                + "return ["
                + "'display=' + menuStyle.display,"
                + "'bg=' + menuStyle.backgroundColor,"
                + "'overflowY=' + menuStyle.overflowY,"
                + "'maxHeight=' + menuStyle.maxHeight,"
                + "'left=' + Math.round(rect.left),"
                + "'width=' + Math.round(rect.width),"
                + "'border=' + menuStyle.borderTopColor,"
                + "'borderWidth=' + Number.parseFloat(menuStyle.borderTopWidth || '0'),"
                + "'itemWhiteSpace=' + (itemStyle ? itemStyle.whiteSpace : 'missing'),"
                + "'itemOverflowWrap=' + (itemStyle ? itemStyle.overflowWrap : 'missing'),"
                + "'itemBorder=' + (itemStyle ? itemStyle.borderBottomColor : 'missing'),"
                + "'itemBorderWidth=' + (itemStyle ? Number.parseFloat(itemStyle.borderBottomWidth || '0') : -1),"
                + "'itemFont=' + (itemStyle ? itemStyle.fontFamily : 'missing'),"
                + "'itemSize=' + (itemStyle ? itemStyle.fontSize : 'missing')"
                + "].join(';');"
                + "}"));

        Assert.assertFalse(state.contains("missing-"),
                "Tag dropdown trigger and menu must exist. State: " + state);
        Assert.assertTrue(state.contains("display=block") || state.contains("display=flex"),
                "Tag dropdown must have a readable opened state. State: " + state);
        Assert.assertTrue(state.contains("bg=" + BACKGROUND_RGB) && state.contains("overflowY=auto"),
                "Tag dropdown must render as a dark scrollable menu, not Spark's white default. State: " + state);
        Assert.assertTrue(state.contains("border=" + BORDER_RGB) && state.contains("itemBorder=" + BORDER_RGB),
                "Tag dropdown borders must match the shared runtime border color. State: " + state);
        Assert.assertTrue(
                ExtentRenderedState.numericValue(state, "borderWidth") <= 1.0
                        && ExtentRenderedState.numericValue(state, "itemBorderWidth") <= 1.0,
                "Tag dropdown borders must stay visually thin like the normal test list. State: " + state
        );
        Assert.assertTrue(ExtentRenderedState.numericValue(state, "left") >= 60,
                "Tag dropdown must start after the sidebar instead of being clipped on the left. State: " + state);
        Assert.assertTrue(state.contains("itemWhiteSpace=normal") && state.contains("itemOverflowWrap=anywhere"),
                "Long tag labels must wrap inside the menu. State: " + state);
    }

}
