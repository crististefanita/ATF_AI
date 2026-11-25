package com.ai.atf.core.reporting.adapters.css;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.core.reporting.listener.NoFrameworkReporting;
import com.ai.core.reporting.adapters.extent.spark.style.ExtentSparkTheme;
import com.ai.core.reporting.adapters.extent.spark.sections.ExtentSparkTreeCollapseCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkContentBadgeCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkContentSurfaceCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkContentTextCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkNestedNodeTypographyCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkTestViewSurfaceCss;
import com.ai.core.reporting.adapters.extent.spark.sections.theme.ExtentSparkThemeShellCss;
import com.ai.core.reporting.adapters.extent.spark.tokens.ExtentSparkSpacingTokens;
import org.testng.Assert;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_THEME_COMPOSITION_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_THEME_COMPOSITION_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkThemeCompositionContractTest {

    @Test
    public void sharedSparkThemeStartsWithShellAndEndsWithRuntimeAudit() {
        String css = ExtentSparkTheme.sharedDarkThemeCss();

        int shell = css.indexOf(ExtentSparkThemeShellCss.css());
        int dashboard = css.indexOf(".dashboard-view .atf-core-final-audit {");
        int runtimeAudit = css.indexOf(".dashboard-view .atf-runtime-timeline-track {");

        Assert.assertEquals(shell, 0, "Shared Spark theme must start with the shell skin.");
        Assert.assertTrue(dashboard > shell, "Dashboard CSS must follow shell/layout CSS.");
        Assert.assertTrue(runtimeAudit > dashboard, "Runtime audit CSS must follow dashboard shell CSS.");
    }

    @Test
    public void sharedSparkThemeComposesShellSectionsBeforeRuntimeSections() {
        String css = ExtentSparkTheme.sharedDarkThemeCss();

        int shell = css.indexOf(ExtentSparkThemeShellCss.css());
        int dropdown = css.indexOf("#tag-toggle.dropdown-menu {");
        int nestedTypography = css.indexOf(ExtentSparkNestedNodeTypographyCss.css());
        int surface = css.indexOf(ExtentSparkTestViewSurfaceCss.css());
        int dashboard = css.indexOf(".dashboard-view .atf-core-final-audit {");

        Assert.assertEquals(shell, 0, "Shared Spark theme must start with the shell skin.");
        Assert.assertTrue(dropdown > shell, "Dropdown CSS must follow the shell skin.");
        Assert.assertTrue(nestedTypography > dropdown, "Nested node typography must follow dropdown/tree/content CSS.");
        Assert.assertTrue(surface > nestedTypography, "Test-view surface CSS must follow nested typography.");
        Assert.assertTrue(dashboard > surface, "Dashboard CSS must follow test-view surface CSS.");
    }

    @Test
    public void sharedSparkThemeComposesDropdownBeforeTreeCollapseBeforeTypography() {
        String css = ExtentSparkTheme.sharedDarkThemeCss();
        String treeCollapse = ExtentSparkTreeCollapseCss.css();

        int dropdown = css.indexOf("#tag-toggle.dropdown-menu {");
        int collapsedNodes = css.indexOf(".test-contents .card.extent-atf-collapsed > .accordion,");
        int typography = css.indexOf(".test-view .test-contents .card,");

        Assert.assertTrue(dropdown >= 0, "Shared Spark theme must include tag/status dropdown CSS.");
        Assert.assertTrue(collapsedNodes > dropdown, "Dropdown CSS must stay before collapsed node styling.");
        Assert.assertTrue(typography > collapsedNodes, "Content typography CSS must follow tree-collapse styling.");
        Assert.assertTrue(
                treeCollapse.contains(ExtentSparkSpacingTokens.TREE_ARROW_MARGIN_RIGHT),
                "Tree-collapse CSS should render the arrow from spacing tokens."
        );
        Assert.assertTrue(
                css.contains(treeCollapse),
                "Shared Spark theme should compose tree-collapse CSS without rewriting it."
        );
    }

    @Test
    public void sharedSparkThemeComposesCoreSectionsOnlyOnce() {
        String css = ExtentSparkTheme.sharedDarkThemeCss();

        Assert.assertEquals(count(css, "#status-toggle.dropdown-menu {"), 1,
                "Status dropdown CSS should be composed once.");
        Assert.assertEquals(count(css, "#tag-toggle.dropdown-menu {"), 1,
                "Tag dropdown CSS should be composed once.");
        Assert.assertEquals(count(css, ".test-view textarea.code-block,"), 1,
                "Code typography CSS should be composed once.");
        Assert.assertEquals(count(css, ".dashboard-view .atf-core-final-audit {"), 1,
                "Dashboard/final-audit CSS should be composed once.");
        Assert.assertEquals(count(css, ".dashboard-view .atf-report-mode-card {"), 0,
                "Component/adapter mode must not reserve visible Overview card CSS.");
    }

    @Test
    public void sharedSparkThemeDoesNotCarryComponentAdapterFallbackCardCss() {
        String css = ExtentSparkTheme.sharedDarkThemeCss();

        Assert.assertFalse(css.contains("atf-report-mode-card"),
                "Component/adapter report mode must be represented by metadata, not visible fallback card CSS.");
        Assert.assertFalse(css.contains("atf-report-mode-pill"),
                "Component/adapter mode must not render an Overview pill.");
    }

    @Test
    public void sharedSparkThemeComposesContentTypographyAsFocusedSections() {
        String css = ExtentSparkTheme.sharedDarkThemeCss();

        int surface = css.indexOf(ExtentSparkContentSurfaceCss.css());
        int text = css.indexOf(ExtentSparkContentTextCss.css());
        int badge = css.indexOf(ExtentSparkContentBadgeCss.css());
        int nestedTypography = css.indexOf(ExtentSparkNestedNodeTypographyCss.css());

        Assert.assertTrue(surface >= 0, "Content surface CSS must be composed by the shared Spark theme.");
        Assert.assertTrue(text > surface, "Content text CSS must follow content surface CSS.");
        Assert.assertTrue(badge > text, "Content badge CSS must follow content text CSS.");
        Assert.assertTrue(nestedTypography > badge,
                "Nested node typography must follow the composed content typography sections.");
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
