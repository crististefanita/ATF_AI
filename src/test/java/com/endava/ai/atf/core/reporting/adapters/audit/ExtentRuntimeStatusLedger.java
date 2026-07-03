package com.endava.ai.atf.core.reporting.adapters.audit;

import com.endava.ai.core.reporting.adapters.extent.inspection.ExtentStatusGroupSnapshot;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.testng.Assert;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExtentRuntimeStatusLedger {
    private final int total;
    private final int passed;
    private final int failed;
    private final int skipped;

    public static ExtentRuntimeStatusLedger expected(int total, int passed, int failed, int skipped) {
        return new ExtentRuntimeStatusLedger(total, passed, failed, skipped);
    }

    public void assertMatches(String html) {
        ExtentStatusGroupSnapshot snapshot = ExtentStatusGroupSnapshot.fromHtml(html);
        Assert.assertEquals(snapshot.parentCount(), total, "Spark parent count must match runtime total.");
        Assert.assertEquals(snapshot.passParent(), passed, "Spark parent pass count must match runtime pass total.");
        Assert.assertEquals(snapshot.failParent(), failed, "Spark parent fail count must match runtime fail total.");
        Assert.assertEquals(snapshot.skipParent(), skipped, "Spark parent skip count must match runtime skip total.");
        Assert.assertEquals(
                snapshot.parentStatusTotal(),
                total,
                "Spark parent status counts must add up to the runtime total exactly once."
        );
        Assert.assertTrue(
                snapshot.hasZeroChildAndEventTotals(),
                "Runtime audit must not duplicate parent totals as Spark child, grand-child, or event counts."
        );
    }
}
