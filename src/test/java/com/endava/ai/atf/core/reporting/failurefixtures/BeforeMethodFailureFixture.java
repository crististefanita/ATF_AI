package com.endava.ai.atf.core.reporting.failurefixtures;

import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
public final class BeforeMethodFailureFixture {

    @BeforeMethod
    public void fail_before_test_body() {
        if (!FailureFixtureSwitch.isEnabled()) {
            return;
        }
        Assert.fail("fixture-before-method-failure");
    }

    @Test(groups = AtfTestGroups.CONTROLLED_FAILURE_FIXTURE)
    public void test_body_is_blocked_by_before_failure() {
        if (!FailureFixtureSwitch.isEnabled()) {
            return;
        }
        Assert.fail("test body should not run after before-method failure");
    }
}
