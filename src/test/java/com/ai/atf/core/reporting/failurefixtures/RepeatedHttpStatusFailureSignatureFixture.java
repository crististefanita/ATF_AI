package com.ai.atf.core.reporting.failurefixtures;

import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
public final class RepeatedHttpStatusFailureSignatureFixture {

    private static final String REPEATED_HTTP_STATUS_ERROR =
            "Unexpected HTTP status expected [201] but found [530]";

    @Test(groups = AtfTestGroups.CONTROLLED_FAILURE_FIXTURE)
    public void create_user_contract_failed() {
        failWithRepeatedSignature();
    }

    @Test(groups = AtfTestGroups.CONTROLLED_FAILURE_FIXTURE)
    public void delete_user_contract_failed() {
        failWithRepeatedSignature();
    }

    @Test(groups = AtfTestGroups.CONTROLLED_FAILURE_FIXTURE)
    public void ui_setup_failed_from_same_backend_signature() {
        failWithRepeatedSignature();
    }

    private static void failWithRepeatedSignature() {
        if (!FailureFixtureSwitch.isEnabled()) {
            return;
        }
        Assert.fail(REPEATED_HTTP_STATUS_ERROR);
    }
}
