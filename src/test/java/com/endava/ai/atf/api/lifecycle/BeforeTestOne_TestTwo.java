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
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.BEFORE_TEST_ONE_TEST_TWO_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.BEFORE_TEST_ONE_TEST_TWO_CLASS)
@Listeners(TestListener.class)
public class BeforeTestOne_TestTwo extends ApiLifecycleContractSupport {

    private UsersSteps users;
    private UserResponse created;

    @BeforeTest(alwaysRun = true)
    public void beforeTestSetup() {
        users = new UsersSteps();
        created = users.createValidUser();
    }

    @Test(description = "Wait until created user is available",
            groups = {"api", "api.lifecycle", "api.framework", "api.framework.lifecycle"})
    @AtfContractRef(AtfContractNarrativeIds.Contract.BEFORE_TEST_ONE_TEST_TWO_WAIT_UNTIL_USER_IS_AVAILABLE)
    public void wait_until_user_is_available() {
        Response getResp = users.waitUntilAvailable(created.id);
        Assert.assertEquals(getResp.getStatusCode(), 200);
        observedLifecycle(
                "BeforeTest.create -> Test.wait",
                created.id,
                getResp
        );
    }

    @Test(
            description = "Delete previously created user",
            groups = {"api", "api.lifecycle", "api.framework", "api.framework.lifecycle"},
            dependsOnMethods = "wait_until_user_is_available"
    )
    @AtfContractRef(AtfContractNarrativeIds.Contract.BEFORE_TEST_ONE_TEST_TWO_DELETE_USER_5)
    public void delete_user_5() {
        Response deleteResp = users.deleteUser(created.id);
        Assert.assertEquals(deleteResp.getStatusCode(), 204);
        observedLifecycle(
                "BeforeTest.create -> Test.wait dependency -> Test.delete",
                created.id,
                deleteResp
        );
    }
}
