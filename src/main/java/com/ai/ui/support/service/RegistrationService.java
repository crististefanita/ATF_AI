package com.ai.ui.support.service;

import com.ai.ui.support.factory.UserDataFactory;
import com.ai.ui.core.runtime.DriverManager;
import com.ai.ui.support.model.CustomerData;
import com.ai.ui.support.model.RegistrationData;
import com.ai.ui.support.pages.RegisterPage;
import com.ai.ui.support.actions.UIActions;
import com.ai.ui.support.wait.WaitUtils;

import java.time.Duration;

/**
 * Services → workflows only. Must remain UI-engine agnostic.
 * Must handle optional/conditionally rendered UI (best-effort for demo).
 */
public final class RegistrationService {
    public void openRegister() {
        UIActions.navigateToRelative("/auth/register");
        WaitUtils.waitForVisible(RegisterPage.FIRST_NAME);
    }

    public RegistrationData registerValidUser() {
        RegistrationData data = UserDataFactory.validRegistrationData();
        return submitWithRetry(data, UserDataFactory::validRegistrationData);
    }

    public CustomerData registerValidCustomer() {
        CustomerData data = com.ai.ui.support.factory.CustomerDataFactory.validCustomer();
        return submitWithRetry(data, com.ai.ui.support.factory.CustomerDataFactory::validCustomer);
    }

    public void register(RegistrationData data) {
        fillIdentityFields(data);
        fillAddressLookup(data.country(), data.postal(), data.houseNumber());
        fillManualAddress(data.street(), data.city(), data.state());
        submitRegistration();
    }

    public void register(CustomerData data) {
        register(
                data.getFirstName(),
                data.getLastName(),
                data.getDob(),
                data.getStreet(),
                data.getPostalCode(),
                data.getHouseNumber(),
                data.getCity(),
                data.getState(),
                data.getCountry(),
                data.getPhone(),
                data.getEmail(),
                data.getPassword()
        );
    }

    public void register(String firstName, String lastName, String dob, String street,
                         String postal, String houseNumber, String city, String state, String country,
                         String phone, String email, String password) {
        fillIdentityFields(firstName, lastName, dob, phone, email, password);
        fillAddressLookup(country, postal, houseNumber);
        fillManualAddress(street, city, state);
        submitRegistration();
    }

    public void fillIdentityFields(RegistrationData data) {
        fillIdentityFields(
                data.firstName(),
                data.lastName(),
                data.dob(),
                data.phone(),
                data.email(),
                data.password()
        );
    }

    public void fillIdentityFields(String firstName, String lastName, String dob,
                                   String phone, String email, String password) {
        UIActions.type(RegisterPage.FIRST_NAME, "First name", firstName);
        UIActions.type(RegisterPage.LAST_NAME, "Last name", lastName);

        // DOB is mandatory on the demo page; still handle as "optional" if selector isn't present in a future UI variation
        try {
            UIActions.type(RegisterPage.DOB, "Date of Birth", dob);
        } catch (Exception ignored) {}

        UIActions.type(RegisterPage.PHONE, "Phone", phone);
        UIActions.type(RegisterPage.EMAIL, "Email address", email);
        UIActions.type(RegisterPage.PASSWORD, "Password", password);
    }

    public void fillAddressLookup(String country, String postal, String houseNumber) {
        UIActions.select(RegisterPage.COUNTRY, "Country", country);
        UIActions.type(RegisterPage.POSTAL, "Postal code", postal);
        UIActions.type(RegisterPage.HOUSE_NUMBER, "House number", houseNumber);
        WaitUtils.pause(Duration.ofMillis(750));
    }

    public void changeCountry(String country) {
        UIActions.select(RegisterPage.COUNTRY, "Country", country);
    }

    public void fillManualAddress(String street, String city, String state) {
        fillManualAddressOnce(street, city, state);
        WaitUtils.pause(Duration.ofMillis(300));
        fillManualAddressOnce(street, city, state);
        WaitUtils.waitForInputValue(RegisterPage.STREET, "registration street stabilized", street::equals);
        WaitUtils.waitForInputValue(RegisterPage.CITY, "registration city stabilized", city::equals);
        WaitUtils.waitForInputValue(RegisterPage.STATE, "registration state stabilized", state::equals);
    }

    private void fillManualAddressOnce(String street, String city, String state) {
        UIActions.type(RegisterPage.STREET, "Street", street);
        UIActions.type(RegisterPage.CITY, "City", city);
        UIActions.type(RegisterPage.STATE, "State", state);
    }

    public void registerWithAutofilledAddress(RegistrationData data) {
        fillIdentityFields(data);
        fillAddressLookup(data.country(), data.postal(), data.houseNumber());
        submitRegistration();
    }

    public void submitEmptyForm() {
        UIActions.click(RegisterPage.REGISTER_BUTTON, "Register");
    }

    public void submitRegistration() {
        UIActions.click(RegisterPage.REGISTER_BUTTON, "Register");
    }

    private RegistrationData submitWithRetry(RegistrationData data,
                                             java.util.function.Supplier<RegistrationData> retryFactory) {
        openRegister();
        register(data);
        if (!registrationStayedOnErrorSurface()) {
            return data;
        }

        UIActions.clearSession();
        RegistrationData retryData = retryFactory.get();
        openRegister();
        register(retryData);
        return retryData;
    }

    private CustomerData submitWithRetry(CustomerData data,
                                         java.util.function.Supplier<CustomerData> retryFactory) {
        openRegister();
        register(data);
        if (!registrationStayedOnErrorSurface()) {
            return data;
        }

        UIActions.clearSession();
        CustomerData retryData = retryFactory.get();
        openRegister();
        register(retryData);
        return retryData;
    }

    private boolean registrationStayedOnErrorSurface() {
        try {
            String url = DriverManager.getEngine().getCurrentUrl();
            String errorText = DriverManager.getEngine().getText(RegisterPage.ANY_ERROR);
            return url.contains("/auth/register") && errorText != null && !errorText.trim().isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }
}
