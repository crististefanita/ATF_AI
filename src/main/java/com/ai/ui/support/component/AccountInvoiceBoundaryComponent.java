package com.ai.ui.support.component;

import com.ai.ui.support.factory.CustomerDataFactory;
import com.ai.ui.support.model.CustomerData;
import com.ai.ui.support.pages.AccountPage;
import com.ai.ui.support.pages.CheckoutPage;
import com.ai.ui.support.pages.LoginPage;
import com.ai.ui.support.pages.ProductPage;
import com.ai.ui.support.pages.RegisterPage;
import com.ai.ui.support.actions.UIActions;
import com.ai.ui.support.wait.WaitUtils;

import java.time.Duration;

public final class AccountInvoiceBoundaryComponent {
    private CustomerData customer;

    public CustomerData getCustomer() {
        return customer;
    }

    public void completeAuthenticatedCheckoutAndOpenInvoices() {
        registerAndLogin();
        addPliersToCart();
        completeAuthenticatedCheckout();
        openInvoices();
    }

    public void completeAuthenticatedCheckoutAndRemainOnSuccess() {
        registerAndLogin();
        addPliersToCart();
        completeAuthenticatedCheckout();
    }

    public void registerLoginAndOpenAccount() {
        registerAndLogin();
        openAccount();
    }

    public void registerLoginAndOpenAccountMenu() {
        registerLoginAndOpenAccount();
        openAccountMenu();
    }

    public void openAccountMenu() {
        UIActions.click(AccountPage.NAV_MENU, "Account menu");
        WaitUtils.waitForVisible(AccountPage.NAV_MY_INVOICES);
    }

    public void reopenInvoicesAfterRelogin() {
        CustomerData current = customer;
        signOut();
        login(current);
        openInvoices();
    }

    public void openAccount() {
        UIActions.openRelativeWithoutUrlWait("/account");
        WaitUtils.waitForVisible(AccountPage.PAGE_TITLE);
    }

    public void openInvoices() {
        UIActions.openRelativeWithoutUrlWait("/account/invoices");
        WaitUtils.waitForVisible(AccountPage.PAGE_TITLE);
    }

    public void signOut() {
        UIActions.click(AccountPage.NAV_MENU, "Account menu");
        WaitUtils.waitForVisible(AccountPage.NAV_SIGN_OUT);
        UIActions.click(AccountPage.NAV_SIGN_OUT, "Sign out");
        WaitUtils.waitForUrlContains("/");
    }

    private void registerAndLogin() {
        UIActions.clearSession();
        customer = CustomerDataFactory.validCustomer();
        openRegister();
        register(customer);
        WaitUtils.waitForUrlContains("/auth/login");
        login(customer);
    }

    private void openRegister() {
        UIActions.navigateToRelative("/auth/register");
        WaitUtils.waitForVisible(RegisterPage.FIRST_NAME);
    }

    private void register(CustomerData data) {
        UIActions.type(RegisterPage.FIRST_NAME, "First name", data.getFirstName());
        UIActions.type(RegisterPage.LAST_NAME, "Last name", data.getLastName());
        UIActions.type(RegisterPage.DOB, "Date of Birth", data.getDob());
        UIActions.select(RegisterPage.COUNTRY, "Country", data.getCountry());
        UIActions.type(RegisterPage.POSTAL, "Postal code", data.getPostalCode());
        UIActions.type(RegisterPage.HOUSE_NUMBER, "House number", data.getHouseNumber());
        UIActions.type(RegisterPage.STREET, "Street", data.getStreet());
        UIActions.type(RegisterPage.CITY, "City", data.getCity());
        UIActions.type(RegisterPage.STATE, "State", data.getState());
        UIActions.type(RegisterPage.PHONE, "Phone", data.getPhone());
        UIActions.type(RegisterPage.EMAIL, "Email address", data.getEmail());
        UIActions.type(RegisterPage.PASSWORD, "Password", data.getPassword());
        UIActions.click(RegisterPage.REGISTER_BUTTON, "Register");
    }

    private void login(CustomerData data) {
        UIActions.navigateToRelative("/auth/login");
        UIActions.type(LoginPage.EMAIL, "Email address", data.getEmail());
        UIActions.type(LoginPage.PASSWORD, "Password", data.getPassword());
        UIActions.click(LoginPage.LOGIN_BUTTON, "Login");
        WaitUtils.waitForUrlContains("/account");
    }

    private void addPliersToCart() {
        UIActions.navigateToRelative("/");
        WaitUtils.waitForVisible(ProductPage.PRODUCT_CARD);
        UIActions.clickVisible(ProductPage.PRODUCT_CARD, "available Pliers product card", 2);
        WaitUtils.waitForVisible(ProductPage.ADD_TO_CART);
        UIActions.click(ProductPage.ADD_TO_CART, "Add Pliers to cart");
        WaitUtils.waitForCondition(
                "cart counter becomes visible",
                () -> "1".equals(UIActions.getText(ProductPage.CART_LINK, "cart counter").trim()),
                "Expected cart counter to show one item."
        );
    }

    private void completeAuthenticatedCheckout() {
        UIActions.openRelativeWithoutUrlWait("/checkout");
        WaitUtils.waitForVisible(CheckoutPage.PROCEED_FROM_CART);
        UIActions.click(CheckoutPage.PROCEED_FROM_CART, "Proceed from cart");
        WaitUtils.waitForVisible(CheckoutPage.PROCEED_FROM_ACCOUNT);
        UIActions.click(CheckoutPage.PROCEED_FROM_ACCOUNT, "Proceed with authenticated account");
        WaitUtils.waitForVisible(CheckoutPage.COUNTRY);
        fillBillingAddress();
        UIActions.click(CheckoutPage.PROCEED_FROM_BILLING, "Proceed from billing address");
        WaitUtils.waitForVisible(CheckoutPage.PAYMENT_METHOD);
        UIActions.select(CheckoutPage.PAYMENT_METHOD, "Payment method", "cash-on-delivery");
        WaitUtils.pause(Duration.ofMillis(300));
        UIActions.click(CheckoutPage.FINISH, "Confirm payment");
        WaitUtils.waitForCondition(
                "payment success message appears",
                () -> UIActions.getText(CheckoutPage.BODY, "checkout body").contains("Payment was successful"),
                "Expected checkout to show payment success."
        );
    }

    private void fillBillingAddress() {
        UIActions.select(CheckoutPage.COUNTRY, "Billing country", customer.getCountry());
        UIActions.type(CheckoutPage.POSTAL_CODE, "Billing postal code", customer.getPostalCode());
        UIActions.type(CheckoutPage.HOUSE_NUMBER, "Billing house number", customer.getHouseNumber());
        UIActions.type(CheckoutPage.STREET, "Billing street", customer.getStreet());
        UIActions.type(CheckoutPage.CITY, "Billing city", customer.getCity());
        UIActions.type(CheckoutPage.STATE, "Billing state", customer.getState());
        WaitUtils.waitForCondition(
                "billing proceed button enabled",
                () -> com.ai.ui.core.runtime.DriverManager.getEngine().isEnabled(CheckoutPage.PROCEED_FROM_BILLING),
                "Expected billing proceed button to be enabled for authenticated checkout."
        );
    }
}
