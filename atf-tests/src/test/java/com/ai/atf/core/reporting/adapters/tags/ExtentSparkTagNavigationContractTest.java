package com.ai.atf.core.reporting.adapters.tags;

import com.ai.atf.core.reporting.adapters.ExtentSparkPresentationContractSupport;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.atf.core.reporting.adapters.tags.ExtentSparkTagNavigationAssertions;
import com.ai.atf.core.reporting.adapters.tags.ExtentSparkTagSelectionPaletteAssertions;
import com.ai.atf.core.reporting.adapters.dom.ExtentRenderedState;
import com.ai.atf.core.reporting.adapters.dom.ExtentSparkBrowserSession;
import com.ai.atf.core.reporting.adapters.dom.ExtentSparkDomAssertions;
import com.ai.core.reporting.listener.NoFrameworkReporting;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_TAG_NAVIGATION_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_TAG_NAVIGATION_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkTagNavigationContractTest extends ExtentSparkPresentationContractSupport {
    private static final String ATF_UI_PACKAGE = "com.ai.atf.ui";

    @Test
    public void extent_spark_exposes_collection_tags_without_breaking_main_view_in_browser() throws Exception {
        Path report = multiCollectionTaggedExtentReport();

        ExtentSparkBrowserSession.openReport(report, 1800, 1000, page -> {
            assertTagSelectionKeepsCollectionVisible(
                    page,
                    "RegistrationTests",
                    "negative_invalid_email_should_fail"
            );
            ExtentSparkBrowserSession.navigateToReport(page, report);
            assertTagSelectionKeepsCollectionVisible(
                    page,
                    "PositiveUserTests",
                    "create_user_success"
            );
        });
    }

    @Test
    public void extent_spark_package_tag_keeps_only_exact_package_classes_in_browser() throws Exception {
        Path report = multiCollectionTaggedExtentReport();

        ExtentSparkBrowserSession.openReport(report, 1800, 1000, page ->
            assertPackageTagSelectionKeepsOnlyExpectedClasses(
                    page,
                    ATF_UI_PACKAGE,
                    new String[]{
                            "negative_invalid_email_should_fail",
                            "registration_audit_contract"
                    },
                    new String[]{
                            "create_user_success",
                            "ui_defaults_are_read_from_framework_properties"
                    }
            )
        );
    }

    @Test
    public void extent_spark_class_and_package_tags_share_same_atf_collection_shell_in_browser() throws Exception {
        Path report = multiCollectionTaggedExtentReport();

        ExtentSparkBrowserSession.openReport(report, 1800, 1000, page -> {
            assertTagSelectionUsesAtfCollectionShell(
                    page,
                    ATF_UI_PACKAGE,
                    2,
                    "negative_invalid_email_should_fail"
            );

            ExtentSparkBrowserSession.navigateToReport(page, report);

            assertTagSelectionUsesAtfCollectionShell(
                    page,
                    "RegistrationTests",
                    1,
                    "negative_invalid_email_should_fail"
            );
        });
    }

    @Test
    public void extent_spark_tag_filtered_view_keeps_class_package_metadata_only_on_root_nodes_in_browser()
            throws Exception {
        Path report = multiCollectionTaggedExtentReport();

        ExtentSparkBrowserSession.openReport(report, 1680, 960, page -> {
            String state = ExtentSparkDomAssertions.selectTagExpandAndReadNodeMetadata(page, "RegistrationTests");

            Assert.assertFalse(
                    state.contains("missing-"),
                    "Tag-filtered view must be reachable before checking nested metadata. State: " + state
            );
            Assert.assertTrue(
                    ExtentRenderedState.numericValue(state, "rootVisibleNodeAttrs") > 0,
                    "Filtered root method card must keep the class/package orientation badges. State: " + state
            );
            Assert.assertEquals(
                    ExtentRenderedState.numericValue(state, "nestedVisibleNodeAttrs"),
                    0.0,
                    "After tag switching, nested setup/body/action cards must not repeat class/package badges. State: "
                            + state
            );
        });
    }

    @Test
    public void extent_spark_tag_dropdown_is_dark_scrollable_and_kept_inside_left_panel() throws Exception {
        String html = nestedExtentReportHtml();

        Assert.assertTrue(
                html.contains("#tag-toggle.dropdown-menu {")
                        && html.contains("position: fixed !important;")
                        && html.contains("left: 4.75rem !important;")
                        && html.contains("max-height: calc(100vh - 9rem) !important;")
                        && html.contains("overflow-y: auto !important;")
                        && html.contains("background: " + EXTENT_BACKGROUND_HEX + " !important;")
                        && html.contains("border: 0.8px solid rgba(184,194,207,0.12) !important;"),
                "Extent tag dropdown must be dark, scrollable, and pinned after the left sidebar."
        );
        Assert.assertTrue(
                html.contains("#tag-toggle.dropdown-menu .dropdown-item {")
                        && html.contains("white-space: normal !important;")
                        && html.contains("overflow-wrap: anywhere !important;")
                        && html.contains("color: " + EXTENT_TEXT_HEX + " !important;")
                        && html.contains("border-bottom: 0.8px solid rgba(184,194,207,0.12) !important;"),
                "Long class/package tag names must wrap and remain readable in the dark dropdown."
        );
        Assert.assertTrue(
                html.contains(".test-list-tools .fa-bug:before {")
                        && html.contains("content: \"\\f02b\" !important;"),
                "The tag dropdown trigger must render as a tag icon instead of Spark's bug/failure-looking icon."
        );
    }

    @Test
    public void extent_spark_status_dropdown_is_dark_labeled_and_includes_skipped() throws Exception {
        String html = nestedExtentReportHtml();

        Assert.assertTrue(
                html.contains("#status-toggle.dropdown-menu {")
                        && html.contains("#status-toggle.dropdown-menu .atf-status-filter-label")
                        && html.contains("#status-toggle.dropdown-menu .dropdown-item {")
                        && html.contains("background: " + EXTENT_BACKGROUND_HEX + " !important;")
                        && html.contains("color: " + EXTENT_TEXT_HEX + " !important;")
                        && html.contains("border: 0.8px solid rgba(184,194,207,0.12) !important;"),
                "Status dropdown must reuse the same dark visual contract as the tag dropdown."
        );
        Assert.assertTrue(
                html.contains("function normalizeStatusMenu()")
                        && html.contains("label.textContent='Status'")
                        && html.contains("ensureStatusItem('pass','Pass','success'")
                        && html.contains("ensureStatusItem('fail','Fail','danger'")
                        && html.contains("ensureStatusItem('skip','Skip','warning'")
                        && html.contains("normalizeStatusItem('skip','Skip','warning');"),
                "Status dropdown normalizer must publish visible Status, Pass, Fail and Skip options."
        );
    }

    @Test
    public void extent_spark_status_dropdown_renders_dark_labeled_skipped_option_in_browser() throws Exception {
        Path report = multiCollectionTaggedExtentReport();

        ExtentSparkBrowserSession.openReport(
                report,
                1680,
                960,
                ExtentSparkTagNavigationAssertions::assertStatusDropdownRendersDarkLabeledSkipOption
        );
    }

    @Test
    public void extent_spark_selection_toolbar_buttons_keep_fontawesome_icons_in_browser() throws Exception {
        Path report = runtimeAuditedFailureFixtureReport();

        ExtentSparkBrowserSession.openReport(
                report,
                1680,
                960,
                ExtentSparkTagNavigationAssertions::assertSelectionToolbarButtonsKeepFontawesomeIcons
        );
    }

    @Test
    public void extent_spark_tag_dropdown_renders_as_scrollable_dark_menu_in_browser() throws Exception {
        Path report = multiCollectionTaggedExtentReport();

        ExtentSparkBrowserSession.openReport(
                report,
                1680,
                960,
                ExtentSparkTagNavigationAssertions::assertTagDropdownRendersAsScrollableDarkMenu
        );
    }

    @Test
    public void extent_spark_tag_selection_closes_filter_overlays_and_keeps_test_list_style_in_browser()
            throws Exception {
        Path report = multiCollectionTaggedExtentReport();

        ExtentSparkBrowserSession.openReport(
                report,
                1848,
                900,
                ExtentSparkTagSelectionPaletteAssertions::assertTagSelectionClosesFilterOverlaysAndKeepsTestListStyle
        );
    }

    @Test
    public void extent_spark_side_category_tab_uses_same_runtime_palette_in_browser() throws Exception {
        Path report = runtimeAuditedFailureFixtureReport();

        ExtentSparkBrowserSession.openReport(
                report,
                1848,
                900,
                ExtentSparkTagSelectionPaletteAssertions::assertSideCategoryTabUsesSameRuntimePalette
        );
    }

}
