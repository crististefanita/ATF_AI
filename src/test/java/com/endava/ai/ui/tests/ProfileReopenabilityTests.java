package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.ProfileAccountComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.CustomerData;
import com.endava.ai.ui.validation.ProfileAccountValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ProfileReopenabilityTests extends BaseTestUI {
    private final ProfileAccountComponent account = new ProfileAccountComponent();
    private final ProfileAccountValidation verify = new ProfileAccountValidation();
    private CustomerData customer;

    @BeforeClass(alwaysRun = true)
    public void createCustomerProfile() {
        customer = account.registerLoginAndOpenProfile();
    }

    @Test
    public void profileIdentitySurvivesAccountRoundTrip() {
        account.openAccount();
        account.openProfile();
        verify.assertProfileIdentity(customer);
    }

    @Test
    public void profileContactSurvivesAccountRoundTrip() {
        account.openAccount();
        account.openProfile();
        verify.assertProfileContact(customer);
    }

    @Test
    public void profileAddressSurvivesAccountRoundTrip() {
        account.openAccount();
        account.openProfile();
        verify.assertProfileAddress(customer);
    }

    @Test
    public void profileUpdateControlSurvivesReopen() {
        account.openAccount();
        account.openProfile();
        verify.assertUpdateProfileAvailable();
    }

    @Test
    public void profileSecurityControlsSurviveReopen() {
        account.openAccount();
        account.openProfile();
        verify.assertPasswordControlsVisible();
    }
}
