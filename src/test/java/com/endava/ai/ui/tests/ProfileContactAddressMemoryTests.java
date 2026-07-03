package com.endava.ai.ui.tests;

import com.endava.ai.ui.support.component.ProfileAccountComponent;
import com.endava.ai.ui.core.runtime.BaseTestUI;
import com.endava.ai.ui.support.model.CustomerData;
import com.endava.ai.ui.support.validation.ProfileAccountValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ProfileContactAddressMemoryTests extends BaseTestUI {
    private final ProfileAccountComponent account = new ProfileAccountComponent();
    private final ProfileAccountValidation verify = new ProfileAccountValidation();
    private CustomerData customer;

    @BeforeClass(alwaysRun = true)
    public void createCustomerProfile() {
        customer = account.registerLoginAndOpenProfile();
    }

    @Test
    public void profileKeepsRegisteredPhone() {
        account.openProfile();
        verify.assertProfileContact(customer);
    }

    @Test
    public void profileKeepsRegisteredStreet() {
        account.openProfile();
        verify.assertProfileContact(customer);
    }

    @Test
    public void profileKeepsRegisteredPostalCode() {
        account.openProfile();
        verify.assertProfileAddress(customer);
    }

    @Test
    public void profileKeepsRegisteredCityAndState() {
        account.openProfile();
        verify.assertProfileAddress(customer);
    }

    @Test
    public void profileKeepsRegisteredCountryCode() {
        account.openProfile();
        verify.assertProfileAddress(customer);
    }
}
