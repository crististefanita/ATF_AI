package com.endava.ai.atf.core.reporting.failurefixtures;

import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
public final class AfterMethodFailureFixture {

    @Test(groups = AtfTestGroups.CONTROLLED_FAILURE_FIXTURE)
    public void test_body_passes_before_after_failure() {
        Assert.assertTrue(true, "fixture test body passes before teardown failure");
    }

    @AfterMethod
    public void fail_after_test_body() {
        if (!FailureFixtureSwitch.isEnabled()) {
            return;
        }
        Assert.fail("fixture-after-method-failure");
    }
}
