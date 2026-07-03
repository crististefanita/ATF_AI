package com.ai.atf.core.reporting.adapters.css;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.core.reporting.listener.NoFrameworkReporting;
import com.ai.core.reporting.adapters.extent.spark.style.ExtentSparkTheme;
import com.ai.core.reporting.adapters.extent.spark.sections.ExtentSparkFailureTriageCss;
import com.ai.core.reporting.adapters.extent.spark.tokens.ExtentSparkStatusTokens;
import org.testng.Assert;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_FAILURE_TRIAGE_CSS_COMPONENT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_FAILURE_TRIAGE_CSS_COMPONENT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkFailureTriageCssComponentTest {

    @Test
    public void sharedSparkThemeContainsFailureTriageSectionVerbatim() {
        String css = ExtentSparkTheme.sharedDarkThemeCss();
        String failureTriage = ExtentSparkFailureTriageCss.css();

        Assert.assertTrue(
                failureTriage.startsWith(".category-view .atf-runtime-failure-grid {"),
                "Failure triage CSS should expose the failure grid as the section root."
        );
        Assert.assertTrue(
                failureTriage.contains("@media (max-width: 1100px)"),
                "Failure triage CSS should own its responsive overrides."
        );
        Assert.assertTrue(
                failureTriage.contains(ExtentSparkStatusTokens.FAILURE_ACCENT_COLOR),
                "Failure triage CSS should render failure badges from status tokens."
        );
        Assert.assertTrue(
                failureTriage.contains(ExtentSparkStatusTokens.METHOD_LINK_COLOR),
                "Failure triage CSS should render method links from status tokens."
        );
        Assert.assertTrue(
                css.contains(failureTriage),
                "Shared Spark theme should compose the failure triage section without rewriting it."
        );
    }

    @Test
    public void failureTriageSectionKeepsInternalVisualSectionsInStableOrder() {
        String failureTriage = ExtentSparkFailureTriageCss.css();

        int overview = failureTriage.indexOf(".category-view .atf-runtime-failure-grid {");
        int signatures = failureTriage.indexOf(".category-view .atf-runtime-failure-signatures {");
        int drilldown = failureTriage.indexOf(".category-view .atf-runtime-failure-drilldown {");
        int responsive = failureTriage.indexOf("@media (max-width: 1100px)");

        Assert.assertTrue(overview >= 0, "Failure triage CSS must start with the overview grid.");
        Assert.assertTrue(signatures > overview, "Failure signatures must follow the overview summary.");
        Assert.assertTrue(drilldown > signatures, "Failure drilldown must follow signature grouping.");
        Assert.assertTrue(responsive > drilldown, "Responsive overrides must remain the final failure triage section.");
    }
}
