package com.endava.ai.atf.core.reporting.adapters.css;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.adapters.extent.spark.style.ExtentSparkTheme;
import org.testng.Assert;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_RUNTIME_AUDIT_CSS_COMPONENT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_RUNTIME_AUDIT_CSS_COMPONENT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkRuntimeAuditCssComponentTest {

    @Test
    public void sharedSparkThemeKeepsRuntimeAuditVisualSectionsInStableOrder() {
        String css = ExtentSparkTheme.sharedDarkThemeCss();

        int timeline = css.indexOf(".dashboard-view .atf-runtime-timeline-card");
        int tagAudit = css.indexOf(".dashboard-view .atf-runtime-tags,");
        int failureTriage = css.indexOf(".category-view .atf-runtime-failure-grid");
        int responsive = css.indexOf("@media (max-width: 1100px)");

        Assert.assertTrue(timeline >= 0, "Runtime audit CSS must include the timeline section.");
        Assert.assertTrue(tagAudit > timeline, "Runtime tag audit CSS must follow the timeline shell.");
        Assert.assertTrue(failureTriage > tagAudit, "Failure triage CSS must follow the tag audit tables.");
        Assert.assertTrue(responsive > failureTriage, "Responsive overrides must remain last.");
    }

    @Test
    public void sharedSparkThemeComposesRuntimeAuditSectionsOnlyOnce() {
        String css = ExtentSparkTheme.sharedDarkThemeCss();

        Assert.assertEquals(count(css, ".dashboard-view .atf-runtime-timeline-card {"), 1,
                "Timeline CSS should be composed once.");
        Assert.assertEquals(count(css, ".dashboard-view .atf-runtime-tags,"), 1,
                "Runtime tag audit CSS should be composed once.");
        Assert.assertEquals(count(css, ".category-view .atf-runtime-failure-grid {"), 1,
                "Failure triage CSS should be composed once.");
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
