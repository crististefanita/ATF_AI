package com.ai.atf.core.reporting.failurefixtures;

import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
public final class TestBodyFailureFixture {

    @Test(groups = AtfTestGroups.CONTROLLED_FAILURE_FIXTURE)
    public void fail_inside_test_body() {
        if (!FailureFixtureSwitch.isEnabled()) {
            return;
        }
        Assert.fail("fixture-test-body-failure");
    }
}
