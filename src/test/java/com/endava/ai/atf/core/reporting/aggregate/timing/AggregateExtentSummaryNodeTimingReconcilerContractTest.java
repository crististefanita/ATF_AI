package com.endava.ai.atf.core.reporting.aggregate.timing;

import com.endava.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.endava.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.endava.ai.core.reporting.aggregate.AggregateStatus;
import com.endava.ai.core.reporting.aggregate.model.AggregateTimingSummary;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class AggregateExtentSummaryNodeTimingReconcilerContractTest {
    private static final AggregateReportDescriptor DESCRIPTOR = AggregateReportDescriptor.builder()
            .testTitle("ATF Core Aggregate Summary")
            .build();

    @Test
    public void summaryNodeDurationIsReconciledFromObservedTimingWithoutTouchingDetails() {
        String html = "<html>"
                + "<section><p class=\"name\">ATF Core Aggregate Summary</p>"
                + "<span>00:00:00:000</span>"
                + "<div class=\"detail-body\"><span>00:00:00:000</span></div></section>"
                + "<section><p class=\"name\">Other Summary</p><span>00:00:00:000</span></section>"
                + "</html>";

        String updated = reconcile(html, timing(1_000L, 3_100L));

        Assert.assertTrue(updated.contains("<p class=\"name\">ATF Core Aggregate Summary</p><span>00:00:02:100</span>"),
                "Summary node must receive the observed overview duration.");
        Assert.assertTrue(updated.contains("<div class=\"detail-body\"><span>00:00:00:000</span></div>"),
                "Detail body placeholders belong to case nodes and must not be patched by the summary reconciler.");
        Assert.assertTrue(updated.contains("<p class=\"name\">Other Summary</p><span>00:00:00:000</span>"),
                "The reconciler must only patch the descriptor-owned summary node.");
    }

    @Test
    public void zeroObservedWallClockFallsBackToMinimumVisibleDuration() {
        String html = "<p class=\"name\">ATF Core Aggregate Summary</p><span>00:00:000</span>";

        String updated = reconcile(html, timing(2_000L, 2_000L));

        Assert.assertTrue(updated.contains("00:00:00:001"),
                "A zero wall-clock observation must still render as a visible non-zero Spark duration.");
    }

    @Test
    public void missingTimingOrSummaryTitleLeavesHtmlUntouched() {
        String html = "<p class=\"name\">Different Summary</p><span>00:00:00:000</span>";

        Assert.assertEquals(reconcile(html, timing(1_000L, 2_000L)), html);
        Assert.assertEquals(reconcile(html, AggregateTimingSummary.empty()), html);
    }

    private static String reconcile(String html, AggregateTimingSummary timing) {
        try {
            Class<?> reconciler = Class.forName(
                    "com.endava.ai.core.reporting.aggregate.AggregateExtentSummaryNodeTimingReconciler");
            Method method = reconciler.getDeclaredMethod(
                    "reconcile",
                    String.class,
                    AggregateReportDescriptor.class,
                    AggregateTimingSummary.class);
            method.setAccessible(true);
            return (String) method.invoke(null, html, DESCRIPTOR, timing);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException exception) {
            throw new AssertionError("Unable to invoke aggregate summary timing reconciler", exception);
        } catch (InvocationTargetException exception) {
            throw new AssertionError("Aggregate summary timing reconciler failed", exception.getCause());
        }
    }

    private static AggregateTimingSummary timing(long startMillis, long endMillis) {
        return AggregateTimingSummary.from(List.of(new CaseFixture(startMillis, endMillis)));
    }

    private static final class CaseFixture implements AggregateExtentReportWriter.CaseRecord {
        private final long startMillis;
        private final long endMillis;

        private CaseFixture(long startMillis, long endMillis) {
            this.startMillis = startMillis;
            this.endMillis = endMillis;
        }

        @Override public String suiteName() { return "ATF Core"; }
        @Override public String contextName() { return "aggregate"; }
        @Override public String className() { return "com.endava.ai.atf.core.reporting.AggregateTimingContract"; }
        @Override public String methodName() { return "recordsDuration"; }
        @Override public String parameters() { return "n/a"; }
        @Override public String suiteProfile() { return "reporting"; }
        @Override public String capability() { return "aggregate-reporting"; }
        @Override public String purpose() { return "Verify aggregate summary timing."; }
        @Override public String area() { return "reporting"; }
        @Override public String owner() { return "ATF Core Reporting"; }
        @Override public String expected() { return "duration is visible"; }
        @Override public String observed() { return "duration observed"; }
        @Override public String repairHint() { return "Patch only the summary node."; }
        @Override public String statusName() { return "PASS"; }
        @Override public AggregateStatus status() { return AggregateStatus.PASSED; }
        @Override public long startMillis() { return startMillis; }
        @Override public long endMillis() { return endMillis; }
        @Override public String stackTrace() { return ""; }
        @Override public String displayName() { return "recordsDuration"; }
        @Override public String description() { return "records duration"; }
        @Override public long durationMillis() { return Math.max(0L, endMillis - startMillis); }
    }
}
