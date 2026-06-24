package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.ProfileAccountComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.CustomerData;
import com.endava.ai.ui.validation.ProfileAccountValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ProfileSecuritySurfaceTests extends BaseTestUI {
    private final ProfileAccountComponent account = new ProfileAccountComponent();
    private final ProfileAccountValidation verify = new ProfileAccountValidation();
    private CustomerData customer;

    @BeforeClass(alwaysRun = true)
    public void createCustomerProfile() {
        customer = account.registerLoginAndOpenProfile();
    }

    @Test
    public void profileExposesPasswordChangeSurface() {
        account.openProfile();
        verify.assertPasswordControlsVisible();
    }

    @Test
    public void profileExposesCurrentPasswordInput() {
        account.openProfile();
        verify.assertPasswordControlsVisible();
    }

    @Test
    public void profileExposesNewPasswordConfirmationInput() {
        account.openProfile();
        verify.assertPasswordControlsVisible();
    }

    @Test
    public void profileExposesTotpSecret() {
        account.openProfile();
        verify.assertTotpControlsVisible();
    }

    @Test
    public void profileExposesTotpVerificationControls() {
        account.openProfile();
        verify.assertTotpControlsVisible();
    }
}
