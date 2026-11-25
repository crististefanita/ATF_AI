package com.ai.atf.core.reporting.adapters.css;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.core.reporting.listener.NoFrameworkReporting;
import com.ai.core.reporting.adapters.extent.spark.style.ExtentSparkTheme;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkSideNavSelectionCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkThemeShellCss;
import org.testng.Assert;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_SIDE_NAV_SELECTION_CSS_COMPONENT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_SIDE_NAV_SELECTION_CSS_COMPONENT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkSideNavSelectionCssComponentTest {

    @Test
    public void sideNavActiveStateHasBackgroundStructuralMarkerAndIconReinforcement() {
        String css = ExtentSparkSideNavSelectionCss.css();

        Assert.assertTrue(css.contains(".side-nav-menu .nav-item.active"),
                "Shared CSS must target the active left sidebar item.");
        Assert.assertTrue(css.contains(".side-nav .nav-item.active") && css.contains(".side-nav li.active"),
                "Shared CSS must cover Spark side-nav variants.");
        Assert.assertTrue(css.contains("background-color:") || css.contains("background:"),
                "Extent left sidebar selected tab must be visually distinct from inactive tabs. "
                        + "Logical active class exists, but selected background is missing.");
        Assert.assertTrue(css.contains("box-shadow:") || css.contains("border-left"),
                "Extent left sidebar selected tab must have a structural marker, not color alone.");
        Assert.assertTrue(css.contains(".side-nav-menu .nav-item.active i")
                        && (css.contains("opacity: 1") || css.contains("color:")),
                "Selected sidebar icon/link must be visually reinforced.");
    }

    @Test
    public void sideNavSelectedStateIsNotHoverOnlyAndDoesNotMergeWithTestListActiveRows() {
        String css = ExtentSparkSideNavSelectionCss.css();

        int active = css.indexOf(".side-nav-menu .nav-item.active,");
        int hover = css.indexOf(".side-nav-menu .nav-item.active:hover");
        Assert.assertTrue(active >= 0 && (hover < 0 || active < hover),
                "Selected sidebar state must exist independently before hover reinforcement.");
        Assert.assertFalse(css.contains(".test-list"),
                "Side-nav selected-state CSS must not leak into test-list row selectors.");
        Assert.assertTrue(ExtentSparkThemeShellCss.css().contains(".test-list .test-item.active"),
                "Test-list active selectors must remain owned by shell/test-list styling.");
    }

    @Test
    public void sharedThemeComposesSideNavSelectionAfterShellAndBeforeDropdowns() {
        String theme = ExtentSparkTheme.sharedDarkThemeCss();

        int shell = theme.indexOf(ExtentSparkThemeShellCss.css());
        int sideNav = theme.indexOf(ExtentSparkSideNavSelectionCss.css());
        int dropdown = theme.indexOf("#tag-toggle.dropdown-menu {");

        Assert.assertEquals(shell, 0, "Shared Spark theme must still start with shell CSS.");
        Assert.assertTrue(sideNav > shell, "Side-nav selection CSS must follow the shell skin.");
        Assert.assertTrue(dropdown > sideNav, "Dropdown CSS must follow selected side-nav styling.");
        Assert.assertEquals(count(theme, ".side-nav-menu .nav-item.active,"), 1,
                "Selected side-nav CSS should be composed once.");
    }

    private static int count(String text, String needle) {
        int matches = 0;
        int next = text.indexOf(needle);
        while (next >= 0) {
            matches++;
            next = text.indexOf(needle, next + needle.length());
        }
        return matches;
    }
}
