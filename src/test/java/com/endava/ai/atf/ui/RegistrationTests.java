package com.endava.ai.atf.ui;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.ui.core.runtime.BaseTestUI;
import com.endava.ai.ui.support.actions.UIActions;
import com.endava.ai.ui.support.factory.UserDataFactory;
import com.endava.ai.ui.support.model.RegistrationData;
import com.endava.ai.ui.support.pages.RegisterPage;
import com.endava.ai.ui.support.service.RegistrationService;
import com.endava.ai.ui.support.validation.RegistrationValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.REGISTRATION_TESTS_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.REGISTRATION_TESTS_CLASS)
@Listeners(TestListener.class)
public class RegistrationTests extends BaseTestUI {

    private final RegistrationService service = new RegistrationService();
    private final RegistrationValidation validation = new RegistrationValidation();

    @BeforeMethod
    public void before() {
        StepLogger.startStep("Registration preconditions");
        StepLogger.pass("Ready");
    }

    @Test(
            description = "Positive: successful registration redirects to login",
            groups = {"ui.product-smoke", "ui.live-boundary"}
    )
    public void positive_registration_redirects_to_login() {
        service.openRegister();
        RegistrationData data = UserDataFactory.validRegistrationData();
        service.register(data);

        validation.assertRedirectedToLogin();
    }

    @Test(
            description = "Negative: missing required fields should show validation error",
            groups = {"ui.product-smoke", "ui.live-boundary"}
    )
    public void negative_missing_required_fields_shows_error() {
        service.openRegister();

        UIActions.click(RegisterPage.REGISTER_BUTTON, "Register");

        validation.assertStillOnRegister();
        validation.assertAnyErrorVisibleOrPresent();
    }

    @Test(
            description = "Negative: invalid email format should not redirect to login",
            groups = {"ui.product-smoke", "ui.live-boundary"}
    )
    public void negative_invalid_email_should_fail() {
        service.openRegister();

        RegistrationData data = UserDataFactory.validRegistrationData()
                .withEmail("invalid-email-format");
        service.register(data);

        validation.assertStillOnRegister();
        validation.assertErrorContains("Email format is invalid");
    }

    @Test(
            description = "Negative: duplicate email should fail on second registration attempt",
            groups = {"ui.product-smoke", "ui.live-boundary"}
    )
    public void negative_duplicate_email_should_fail() {
        RegistrationData data = service.registerValidUser();
        validation.assertRedirectedToLogin();

        service.openRegister();
        service.register(data);
        validation.assertStillOnRegister();
        validation.assertDuplicateEmailRejected();
    }

    @Test(
            description = "Negative: weak password should show password policy feedback",
            groups = {"ui.product-smoke", "ui.live-boundary"}
    )
    public void negative_weak_password_should_show_validation_message() {
        service.openRegister();

        RegistrationData data = UserDataFactory.validRegistrationData()
                .withPassword("short");
        service.register(data);

        validation.assertStillOnRegister();
        validation.assertPasswordErrorContains("Password must be minimal 6 characters long.");
    }

    @Test(
            description = "Negative: phone with letters should fail validation",
            groups = {"ui.product-smoke", "ui.live-boundary"}
    )
    public void negative_phone_with_letters_should_fail() {
        service.openRegister();

        RegistrationData data = UserDataFactory.validRegistrationData()
                .withPhone("abc");
        service.register(data);

        validation.assertStillOnRegister();
        validation.assertPhoneErrorContains("Only numbers are allowed.");
    }

}
