package com.endava.ai.atf.core.reporting.regression.overview;

import com.endava.ai.core.reporting.adapters.extent.overview.ExtentOverviewTopCards;
import com.endava.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
public class ExtentOverviewTopCardsTimingContractTest {
    @Test
    public void overviewTimingAddsDurationFromSparkStartedAndEndedCards() {
        String html = overviewHtml(
                "Jun 30, 2026 5:00:00 PM",
                "Jun 30, 2026 5:00:01 PM",
                false
        );

        String updated = ExtentOverviewTopCards.alignWithExistingTiming(html);

        Assert.assertTrue(
                updated.contains("<p class=\"m-b-0\">" + ExtentDashboardLabels.DURATION + "</p><h3>1.000 s</h3>"),
                "Duration must be derived from Started/Ended cards when Extent omits the ATF duration score-card."
        );
    }

    @Test
    public void overviewTimingLeavesDurationAbsentWhenSparkDatesAreInvalid() {
        String html = overviewHtml("not-a-date", "Jun 30, 2026 5:00:01 PM", false);

        String updated = ExtentOverviewTopCards.alignWithExistingTiming(html);

        Assert.assertFalse(
                updated.contains(ExtentDashboardLabels.DURATION),
                "Invalid Spark timing should not invent a duration card."
        );
    }

    @Test
    public void overviewTimingKeepsExistingDurationWhenPresent() {
        String html = overviewHtml(
                "Jun 30, 2026 5:00:00 PM",
                "Jun 30, 2026 5:00:01 PM",
                true
        );

        String updated = ExtentOverviewTopCards.alignWithExistingTiming(html);

        Assert.assertEquals(count(updated, ExtentDashboardLabels.DURATION), 1,
                "Existing duration card must be normalized once, not duplicated.");
        Assert.assertTrue(updated.contains("<h3>1.000 s</h3>"),
                "Existing duration card should be reconciled to the observed Started/Ended timing.");
    }

    private static String overviewHtml(String startedAt, String endedAt, boolean withDuration) {
        return "<html><body>"
                + ExtentHtmlStructure.DASHBOARD_CONTAINER
                + "<div class=\"row\">"
                + topCard(ExtentDashboardLabels.STARTED, startedAt)
                + topCard(ExtentDashboardLabels.ENDED, endedAt)
                + (withDuration ? topCard(ExtentDashboardLabels.DURATION, "stale") : "")
                + "</div>"
                + "<div class=\"row atf-runtime-dashboard-tests-row\"></div>"
                + "</div></body></html>";
    }

    private static String topCard(String label, String value) {
        return "<div class=\"col-md-3\"><div class=\"card\"><div class=\"card-body\">"
                + "<p class=\"m-b-0\">" + label + "</p><h3>" + value + "</h3>"
                + "</div></div></div>";
    }

    private static int count(String value, String token) {
        return value.split(token, -1).length - 1;
    }
}
