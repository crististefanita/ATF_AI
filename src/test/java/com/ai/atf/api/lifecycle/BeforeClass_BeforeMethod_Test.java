package com.ai.atf.api.lifecycle;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.api.support.model.UserResponse;
import com.ai.api.support.steps.UsersSteps;
import com.ai.core.reporting.listener.TestListener;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@SuppressWarnings("ALL")
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.BEFORE_CLASS_BEFORE_METHOD_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.BEFORE_CLASS_BEFORE_METHOD_TEST_CLASS)
@Listeners(TestListener.class)
public class BeforeClass_BeforeMethod_Test extends ApiLifecycleContractSupport {

    private UsersSteps users;
    private UserResponse created;

    @BeforeClass(alwaysRun = true)
    public void beforeClassSetup() {
        users = new UsersSteps();
        created = users.createValidUser();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethodSetup() {
        Response getResp = users.waitUntilAvailable(created.id);
        Assert.assertEquals(getResp.getStatusCode(), 200);
    }

    @Test(description = "Delete previously created user",
            groups = {"api", "api.lifecycle", "api.framework", "api.framework.lifecycle"})
    @AtfContractRef(AtfContractNarrativeIds.Contract.BEFORE_CLASS_BEFORE_METHOD_TEST_DELETE_USER)
    public void delete_user_1() {
        Response deleteResp = users.deleteUser(created.id);
        Assert.assertEquals(deleteResp.getStatusCode(), 204);
        observedLifecycle(
                "BeforeClass.create -> BeforeMethod.wait -> Test.delete",
                created.id,
                deleteResp
        );
    }
}
