package com.endava.ai.atf.api.lifecycle;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.api.support.model.UserResponse;
import com.endava.ai.api.support.steps.UsersSteps;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.config.ConfigManager;
import com.endava.ai.core.reporting.listener.TestListener;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@SuppressWarnings("ALL")
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.BEFORE_TEST_TEST_ONE_AFTER_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.BEFORE_TEST_TEST_ONE_AFTER_TEST_CLASS)
@Listeners(TestListener.class)
public class BeforeTest_TestOne_AfterTest extends ApiLifecycleContractSupport {

    private UsersSteps users;
    private UserResponse created;
    private String cleanupAuthToken;

    @BeforeTest(alwaysRun = true)
    public void beforeTestSetup() {
        users = new UsersSteps();
        cleanupAuthToken = ConfigManager.get("auth.token", "");
        created = users.createValidUser();
    }

    @Test(description = "Delete previously created user",
            groups = {"api", "api.lifecycle", "api.framework", "api.framework.lifecycle"})
    @AtfContractRef(AtfContractNarrativeIds.Contract.BEFORE_TEST_TEST_ONE_AFTER_TEST_DELETE_USER)
    public void delete_user() {
        Response getResp = users.waitUntilAvailable(created.id);
        Assert.assertEquals(getResp.getStatusCode(), 200);
        observedLifecycle(
                "BeforeTest.create -> Test.wait -> AfterMethod.delete",
                created.id,
                getResp
        );
    }

    @AfterMethod(alwaysRun = true)
    public void afterTestCleanup() {
        if (users == null || created == null || created.id == null) {
            return;
        }
        if (ConfigManager.get("auth.token", "").isBlank() && cleanupAuthToken != null && !cleanupAuthToken.isBlank()) {
            try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                    .setConfig("auth.token", cleanupAuthToken)) {
                deleteCreatedUser();
            }
            return;
        }
        deleteCreatedUser();
    }

    private void deleteCreatedUser() {
        Response deleteResp = users.deleteUser(created.id);
        Assert.assertEquals(deleteResp.getStatusCode(), 204);
        created = null;
    }
}
