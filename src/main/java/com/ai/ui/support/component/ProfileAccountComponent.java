package com.ai.ui.support.component;

import com.ai.ui.support.factory.CustomerDataFactory;
import com.ai.ui.support.model.CustomerData;
import com.ai.ui.support.pages.AccountPage;
import com.ai.ui.support.pages.LoginPage;
import com.ai.ui.support.pages.ProfilePage;
import com.ai.ui.support.actions.UIActions;
import com.ai.ui.support.service.RegistrationService;
import com.ai.ui.support.wait.WaitUtils;

public final class ProfileAccountComponent {
    private final RegistrationService registration = new RegistrationService();

    public CustomerData registerLoginAndOpenProfile() {
        CustomerData data = CustomerDataFactory.validCustomer();
        registration.openRegister();
        registration.register(data);
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

    private void login(CustomerData data) {
        UIActions.type(LoginPage.EMAIL, "Email address", data.getEmail());
        UIActions.type(LoginPage.PASSWORD, "Password", data.getPassword());
        UIActions.click(LoginPage.LOGIN_BUTTON, "Login");
        WaitUtils.waitForUrlContains("/account");
    }
}
