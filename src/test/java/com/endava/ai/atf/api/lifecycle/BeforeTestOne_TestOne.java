package com.endava.ai.atf.api.lifecycle;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.api.support.model.UserResponse;
import com.endava.ai.api.support.steps.UsersSteps;
import com.endava.ai.core.reporting.listener.TestListener;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@SuppressWarnings("ALL")
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.BEFORE_TEST_ONE_TEST_ONE_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.BEFORE_TEST_ONE_TEST_ONE_CLASS)
@Listeners(TestListener.class)
public class BeforeTestOne_TestOne extends ApiLifecycleContractSupport {

    private UsersSteps users;
    private UserResponse created;

    @BeforeTest(alwaysRun = true)
    public void beforeTestSetup() {
        users = new UsersSteps();
        created = users.createValidUser();
    }

    @Test(description = "Delete previously created user",
            groups = {"api", "api.lifecycle", "api.framework", "api.framework.lifecycle"})
    @AtfContractRef(AtfContractNarrativeIds.Contract.BEFORE_TEST_ONE_TEST_ONE_DELETE_USER_4)
    public void delete_user_4() {
        Response getResp = users.waitUntilAvailable(created.id);
        Assert.assertEquals(getResp.getStatusCode(), 200);

        Response deleteResp = users.deleteUser(created.id);
        Assert.assertEquals(deleteResp.getStatusCode(), 204);
        observedLifecycle(
                "BeforeTest.create -> Test.wait -> Test.delete",
                created.id,
                getResp,
                deleteResp
        );
    }

}
