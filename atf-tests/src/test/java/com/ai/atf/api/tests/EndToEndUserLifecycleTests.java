package com.ai.atf.api.tests;

import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.api.core.BaseTestAPI;
import com.ai.api.support.factory.UserFactory;
import com.ai.api.support.model.UserRequest;
import com.ai.api.support.model.UserResponse;
import com.ai.api.support.steps.UsersSteps;
import com.ai.api.core.validation.ResponseValidator;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.atf.core.support.contract.failure.AtfExpectedFailure;
import com.ai.core.support.contract.failure.AtfExpectedFailureContractRef;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.StepLogger;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.END_TO_END_USER_LIFECYCLE_TESTS_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.END_TO_END_USER_LIFECYCLE_TESTS_CLASS)
@Listeners(TestListener.class)
public class EndToEndUserLifecycleTests extends BaseTestAPI {

    private UsersSteps users;
    private Integer createdUserId;

    @BeforeMethod
    public void setUp() {
        users = new UsersSteps();
        createdUserId = null;
    }

    @AfterMethod
    public void cleanUp() {
        if (createdUserId != null) {
            users.deleteUserSilently(createdUserId);
        }
    }

    @Test(description = "E2E: Create -> Get -> Update -> Delete -> Verify deletion with controlled negative assertion",
            groups = {"api", "api.users", "api.lifecycle", "api.expected-failure", "api.contract"})
    @AtfExpectedFailureContractRef(AtfContractNarrativeIds.ExpectedFailure.API_DELETED_RESOURCE_NOT_READABLE)
    @AtfContractRef(AtfContractNarrativeIds.Contract.END_TO_END_USER_LIFECYCLE_TESTS_USER_LIFECYCLE_E2E)
    public void user_lifecycle_e2e() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "user_lifecycle_e2e"
        );
        AtfContractSteps.expected(contract);

        StepLogger.startStep("API lifecycle flow");
        StepLogger.logDetail("Create -> Get -> Patch -> Delete -> Verify deleted resource");
        StepLogger.logDetail("Layer path: BaseTestAPI -> UsersSteps -> ApiActions -> ResponseValidator");
        StepLogger.pass("API lifecycle path declared");

        UserResponse created = users.createValidUser();
        Assert.assertTrue(created.id > 0);
        createdUserId = created.id;

        Response getResp = users.getUser(createdUserId);
        ResponseValidator.statusIs(getResp, 200);

        UserRequest patch = UserFactory.fromResponse(created);
        patch.status = "inactive";

        Response patchResp = users.patchUser(createdUserId, patch);
        ResponseValidator.statusIs(patchResp, 200);

        Response delResp = users.deleteUser(createdUserId);
        ResponseValidator.statusIs(delResp, 204);
        createdUserId = null;

        Response getAfterDel = users.getUser(created.id);

        AssertionError expectedFailure = AtfExpectedFailure.expect(
                "Deleted resource must not validate as active/readable",
                AssertionError.class,
                () -> ResponseValidator.statusIs(getAfterDel, 200),
                "Unexpected HTTP status"
        );

        ResponseValidator.statusIs(getAfterDel, 404);

        AtfContractSteps.observed("created.user.id=" + created.id
                + " | patch.status=200"
                + " | delete.status=204"
                + " | deleted.read.status=404"
                + " | controlled.failure=" + expectedFailure.getMessage());
        AtfContractSteps.verdict(
                "Live API lifecycle and expected negative validator behavior are auditable in one green test.",
                contract.repairHint()
        );
    }

}
