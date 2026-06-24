package com.endava.ai.ui.component;

import com.endava.ai.ui.factory.CustomerDataFactory;
import com.endava.ai.ui.model.CustomerData;
import com.endava.ai.ui.pages.AccountPage;
import com.endava.ai.ui.pages.LoginPage;
import com.endava.ai.ui.pages.ProfilePage;
import com.endava.ai.ui.pages.RegisterPage;
import com.endava.ai.ui.utils.UIActions;
import com.endava.ai.ui.utils.WaitUtils;

public final class ProfileAccountComponent {
    public CustomerData registerLoginAndOpenProfile() {
        CustomerData data = CustomerDataFactory.validCustomer();
        openRegister();
        register(data);
        WaitUtils.waitForUrlContains("/auth/login");
        login(data);
        openProfile();
        return data;
    }

    public void openAccount() {
        UIActions.openRelativeWithoutUrlWait("/account");
        WaitUtils.waitForVisible(AccountPage.PAGE_TITLE);
    }

    public void openProfile() {
        UIActions.openRelativeWithoutUrlWait("/account/profile");
        WaitUtils.waitForVisible(ProfilePage.PAGE_TITLE);
    }

    public void openFavorites() {
        UIActions.openRelativeWithoutUrlWait("/account/favorites");
        WaitUtils.waitForVisible(AccountPage.PAGE_TITLE);
    }

    public void openInvoices() {
        UIActions.openRelativeWithoutUrlWait("/account/invoices");
        WaitUtils.waitForVisible(AccountPage.PAGE_TITLE);
    }

    public void openMessages() {
        UIActions.openRelativeWithoutUrlWait("/account/messages");
        WaitUtils.waitForVisible(AccountPage.PAGE_TITLE);
    }

    private void openRegister() {
        UIActions.navigateToRelative("/auth/register");
        WaitUtils.waitForVisible(RegisterPage.FIRST_NAME);
    }

    private void register(CustomerData data) {
        UIActions.type(RegisterPage.FIRST_NAME, "First name", data.getFirstName());
        UIActions.type(RegisterPage.LAST_NAME, "Last name", data.getLastName());
        UIActions.type(RegisterPage.DOB, "Date of Birth", data.getDob());
        UIActions.type(RegisterPage.PHONE, "Phone", data.getPhone());
        UIActions.type(RegisterPage.EMAIL, "Email address", data.getEmail());
        UIActions.type(RegisterPage.PASSWORD, "Password", data.getPassword());
        UIActions.select(RegisterPage.COUNTRY, "Country", data.getCountry());
        WaitUtils.pause(java.time.Duration.ofMillis(300));
        UIActions.type(RegisterPage.POSTAL, "Postal code", data.getPostalCode());
        UIActions.type(RegisterPage.HOUSE_NUMBER, "House number", data.getHouseNumber());
        UIActions.type(RegisterPage.STREET, "Street", data.getStreet());
        UIActions.type(RegisterPage.CITY, "City", data.getCity());
        UIActions.type(RegisterPage.STATE, "State", data.getState());
        UIActions.type(RegisterPage.HOUSE_NUMBER, "House number", data.getHouseNumber());
        UIActions.click(RegisterPage.REGISTER_BUTTON, "Register");
    }

    private void login(CustomerData data) {
        UIActions.type(LoginPage.EMAIL, "Email address", data.getEmail());
        UIActions.type(LoginPage.PASSWORD, "Password", data.getPassword());
        UIActions.click(LoginPage.LOGIN_BUTTON, "Login");
        WaitUtils.waitForUrlContains("/account");
    }
}
