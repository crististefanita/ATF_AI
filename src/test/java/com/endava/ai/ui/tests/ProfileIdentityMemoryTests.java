package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.ProfileAccountComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.CustomerData;
import com.endava.ai.ui.validation.ProfileAccountValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ProfileIdentityMemoryTests extends BaseTestUI {
    private final ProfileAccountComponent account = new ProfileAccountComponent();
    private final ProfileAccountValidation verify = new ProfileAccountValidation();
    private CustomerData customer;

    @BeforeClass(alwaysRun = true)
    public void createCustomerProfile() {
        customer = account.registerLoginAndOpenProfile();
    }

    @Test
    public void profilePageHasBusinessTitle() {
        account.openProfile();
        verify.assertProfileTitle();
    }

    @Test
    public void profileKeepsRegisteredFirstName() {
        account.openProfile();
        verify.assertProfileIdentity(customer);
    }

    @Test
    public void profileKeepsRegisteredLastName() {
        account.openProfile();
        verify.assertProfileIdentity(customer);
    }

    @Test
    public void profileKeepsRegisteredEmail() {
        account.openProfile();
        verify.assertProfileIdentity(customer);
    }

    @Test
    public void authenticatedMenuNamesRegisteredCustomer() {
        account.openAccount();
        verify.assertNavMenuNamesCustomer(customer);
    }
}
