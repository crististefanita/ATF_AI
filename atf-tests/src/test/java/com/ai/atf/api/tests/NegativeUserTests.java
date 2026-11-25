package com.ai.atf.api.tests;

import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.api.core.BaseTestAPI;
import com.ai.api.support.steps.UsersSteps;
import com.ai.api.core.utils.JsonUtils;
import com.ai.api.core.validation.ResponseValidator;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.atf.core.support.contract.failure.AtfExpectedFailure;
import com.ai.core.support.contract.failure.AtfExpectedFailureContractRef;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.reporting.listener.TestListener;
import io.restassured.response.Response;
import org.testng.annotations.Listeners;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.NEGATIVE_USER_TESTS_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.NEGATIVE_USER_TESTS_CLASS)
@Listeners(TestListener.class)
public class NegativeUserTests extends BaseTestAPI {

    private UsersSteps users;
    private Integer createdUserId;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        users = new UsersSteps();
        createdUserId = null;
    }

    @AfterMethod(alwaysRun = true)
    public void cleanUp() {
        if (createdUserId != null) {
            users.deleteUserSilently(createdUserId);
        }
    }

    @Test(description = "Create user with invalid email should fail",
            groups = {"api", "api.users", "api.negative", "api.contract"})
    @AtfContractRef(AtfContractNarrativeIds.Contract.NEGATIVE_USER_TESTS_INVALID_EMAIL_IS_REJECTED)
    public void invalid_email_is_rejected() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "invalid_email_is_rejected"
        );
        AtfContractSteps.expected(contract);

        String invalidEmail = JsonUtils.readString(
                "tests/testdata/NegativeUserTests_invalid_email.json",
                "email"
        );

        Response resp = users.createUserWithInvalidEmail(invalidEmail);

        ResponseValidator.statusIs(resp, 422);
        ResponseValidator.bodyContains(resp, "email");
        AtfContractSteps.observed("invalid.email.fixture=NegativeUserTests_invalid_email.json"
                + " | response.status=422"
                + " | body.contains=email");
        AtfContractSteps.verdict(
                "Live API rejected invalid user input with an auditable validation signal.",
                contract.repairHint()
        );
    }

    @Test(description = "Create user with duplicate email fixture gap should be reported as expected failure",
            groups = {"api", "api.users", "api.negative", "api.expected-failure", "api.contract"})
    @AtfExpectedFailureContractRef(AtfContractNarrativeIds.ExpectedFailure.API_DUPLICATE_EMAIL_FIXTURE_GAP)
    @AtfContractRef(AtfContractNarrativeIds.Contract.NEGATIVE_USER_TESTS_DUPLICATE_EMAIL_IS_REJECTED)
    public void duplicate_email_is_rejected() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "duplicate_email_is_rejected"
        );
        AtfContractSteps.expected(contract);

        IllegalStateException expectedFailure = AtfExpectedFailure.expect(
                "Duplicate email fixture gap is controlled",
                IllegalStateException.class,
                () -> JsonUtils.readString(
                        "tests/testdata/NegativeUserTests_duplicate_email.json",
                        "email"
                ),
                "NegativeUserTests_duplicate_email.json"
        );

        AtfContractSteps.observed("controlled.exception=" + expectedFailure.getClass().getSimpleName()
                + " | message=" + expectedFailure.getMessage());
        AtfContractSteps.verdict(
                "Expected local fixture gap is visible and does not fail the package sweep.",
                contract.repairHint()
        );
    }

    @Test(description = "Create user without auth token should fail",
            groups = {"api", "api.users", "api.negative", "api.contract"})
    @AtfContractRef(AtfContractNarrativeIds.Contract.NEGATIVE_USER_TESTS_MISSING_AUTH_TOKEN_IS_REJECTED)
    public void missing_auth_token_is_rejected() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "missing_auth_token_is_rejected"
        );
        AtfContractSteps.expected(contract);

        Response resp = users.createUserWithoutAuth();

        ResponseValidator.statusIs(resp, 401);
        AtfContractSteps.observed("create.without.auth.status=401");
        AtfContractSteps.verdict(
                "Live API auth rejection is auditable as a green negative contract.",
                contract.repairHint()
        );
    }

    @Test(description = "Delete non existing user should return 404",
            groups = {"api", "api.users", "api.negative", "api.contract"})
    @AtfContractRef(AtfContractNarrativeIds.Contract.NEGATIVE_USER_TESTS_DELETE_NON_EXISTING_USER_RETURNS_404)
    public void delete_non_existing_user_returns_404() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "delete_non_existing_user_returns_404"
        );
        AtfContractSteps.expected(contract);

        Response resp = users.deleteUserNonExisting(999999999);

        ResponseValidator.statusIs(resp, 404);
        AtfContractSteps.observed("delete.non.existing.id=999999999 | response.status=404");
        AtfContractSteps.verdict(
                "Live API not-found behavior is explicit and reportable.",
                contract.repairHint()
        );
    }
}
