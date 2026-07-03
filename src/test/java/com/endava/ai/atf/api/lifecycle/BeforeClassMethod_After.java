package com.endava.ai.atf.api.lifecycle;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.api.support.factory.UserFactory;
import com.endava.ai.api.support.model.UserRequest;
import com.endava.ai.api.support.model.UserResponse;
import com.endava.ai.api.support.steps.UsersSteps;
import com.endava.ai.api.core.validation.ResponseValidator;
import com.endava.ai.core.reporting.listener.TestListener;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.*;

@SuppressWarnings("ALL")
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.BEFORE_CLASS_METHOD_AFTER_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.BEFORE_CLASS_METHOD_AFTER_CLASS)
@Listeners(TestListener.class)
public class BeforeClassMethod_After extends ApiLifecycleContractSupport {

    private UsersSteps users;
    private Integer createdUserId;
    private UserResponse created;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        users = new UsersSteps();
        createdUserId = null;
    }

    @BeforeTest(alwaysRun = true)
    public void beforeTestSetup() {
        users = new UsersSteps();
        created = users.createValidUser();
        Assert.assertTrue(created.id > 0);
        createdUserId = created.id;
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethodSetup() {
        if (createdUserId == null) {
            created = users.createValidUser();
            Assert.assertTrue(created.id > 0);
            createdUserId = created.id;
        }

        Response getResp = users.waitUntilAvailable(createdUserId);
        Assert.assertEquals(getResp.getStatusCode(), 200);
    }

    @Test(description = "Update User",
            groups = {"api", "api.lifecycle", "api.framework", "api.framework.lifecycle"})
    @AtfContractRef(AtfContractNarrativeIds.Contract.BEFORE_CLASS_METHOD_AFTER_UPDATE_USER)
    public void update_User() {
        UserRequest patch = UserFactory.fromResponse(created);
        patch.status = "inactive";

        Response patchResp = users.patchUser(createdUserId, patch);
        ResponseValidator.statusIs(patchResp, 200);
        observedLifecycle(
                "BeforeTest.create -> BeforeMethod.wait -> Test.patch -> AfterMethod.delete",
                createdUserId,
                patchResp
        );
    }

    @AfterMethod(alwaysRun = true)
    public void deleteUser_after() {
        Response delResp = users.deleteUser(createdUserId);
        ResponseValidator.statusIs(delResp, 204);
        createdUserId = null;

        Response getAfterDel = users.getUser(created.id);
        ResponseValidator.statusIs(getAfterDel, 404);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        if (createdUserId != null) {
            users.deleteUserSilently(createdUserId);
        }
    }
}
