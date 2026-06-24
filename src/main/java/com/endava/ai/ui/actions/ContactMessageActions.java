package com.endava.ai.ui.actions;

import com.endava.ai.ui.core.DriverManager;
import com.endava.ai.ui.factory.CustomerDataFactory;
import com.endava.ai.ui.model.ContactMessageContext;
import com.endava.ai.ui.model.CustomerData;
import com.endava.ai.ui.pages.AccountPage;
import com.endava.ai.ui.pages.ContactPage;
import com.endava.ai.ui.pages.LoginPage;
import com.endava.ai.ui.service.RegistrationService;
import com.endava.ai.ui.utils.UIActions;
import com.endava.ai.ui.utils.WaitUtils;
import com.endava.ai.ui.validation.RegistrationValidation;

import java.time.Duration;
import java.util.UUID;

public final class ContactMessageActions {
    private static final String SUBJECT_LABEL = "Status of my order";
    private static final String SUBJECT_SLUG = "status-of-order";

    private final RegistrationService registration = new RegistrationService();
    private final RegistrationValidation registrationValidation = new RegistrationValidation();

    public ContactMessageContext createMessageAndOpenMessages() {
        UIActions.clearSession();
        CustomerData customer = registerCustomerWithRetry();
        login(customer);
        String message = uniqueMessage();
        submitMessage(message);
        openMessages();
        waitForMessageRow(message);
        return capture(customer, message, currentBody(), "", DriverManager.getEngine().getCurrentUrl());
    }

    public ContactMessageContext createMessageAndOpenDetails() {
        ContactMessageContext context = createMessageAndOpenMessages();
        return openMessageDetails(context);
    }

    public ContactMessageContext createMessageReopenMessages() {
        ContactMessageContext context = createMessageAndOpenMessages();
        openAccount();
        openMessages();
        waitForMessageRow(context.getMessage());
        return capture(context.getCustomer(), context.getMessage(), currentBody(), "", DriverManager.getEngine().getCurrentUrl());
    }

    public ContactMessageContext createMessageReopenDetails() {
        ContactMessageContext context = createMessageReopenMessages();
        return openMessageDetails(context);
    }

    public ContactMessageContext createMessageReloginAndOpenMessages() {
        ContactMessageContext context = createMessageAndOpenMessages();
        signOut();
        login(context.getCustomer());
        openMessages();
        waitForMessageRow(context.getMessage());
        return capture(context.getCustomer(), context.getMessage(), currentBody(), "", DriverManager.getEngine().getCurrentUrl());
    }

    public ContactMessageContext createMessageReloginAndOpenDetails() {
        ContactMessageContext context = createMessageReloginAndOpenMessages();
        return openMessageDetails(context);
    }

    public ContactMessageContext openMessageDetails(ContactMessageContext context) {
        UIActions.click("a:has-text('Details')", "Message details row");
        WaitUtils.waitForCondition(
                "message details contain submitted message",
                () -> currentBody().contains(context.getMessage()),
                "Expected message details to contain full submitted message."
        );
        return capture(
                context.getCustomer(),
                context.getMessage(),
                context.getListBody().isEmpty() ? "" : context.getListBody(),
                currentBody(),
                DriverManager.getEngine().getCurrentUrl()
        );
    }

    private void submitMessage(String message) {
        UIActions.navigateToRelative(ContactPage.CONTACT_PATH);
        WaitUtils.waitForVisible(ContactPage.SUBJECT);
        UIActions.select(ContactPage.SUBJECT, "Contact subject", SUBJECT_LABEL);
        UIActions.type(ContactPage.MESSAGE, "Contact message", message);
        UIActions.click(ContactPage.SUBMIT, "Submit contact message");
        WaitUtils.waitForCondition(
                "contact success message is visible",
                () -> currentBody().contains("Thanks for your message"),
                "Expected contact form to publish success confirmation."
        );
        WaitUtils.pause(Duration.ofSeconds(2));
    }

    private void waitForMessageRow(String message) {
        String preview = message.substring(0, Math.min(24, message.length()));
        WaitUtils.waitForCondition(
                "created support message row is visible",
                () -> currentBody().contains(SUBJECT_SLUG)
                        && currentBody().contains(preview)
                        && currentBody().contains("NEW"),
                "Expected messages page to expose the created support message row."
        );
    }

    public void openMessages() {
        UIActions.openRelativeWithoutUrlWait("/account/messages");
        WaitUtils.waitForVisible(AccountPage.PAGE_TITLE);
        WaitUtils.waitForCondition(
                "messages page title is visible",
                () -> currentBody().contains("Messages"),
                "Expected account messages page."
        );
    }

    public void openAccount() {
        UIActions.openRelativeWithoutUrlWait("/account");
        WaitUtils.waitForVisible(AccountPage.PAGE_TITLE);
    }

    public void signOut() {
        UIActions.click(AccountPage.NAV_MENU, "Account menu");
        UIActions.click(AccountPage.SIGN_OUT, "Sign out");
        WaitUtils.waitForCondition(
                "login navigation is visible after sign out",
                () -> safeVisible(LoginPage.LOGIN_BUTTON) || safeVisible("a[data-test='nav-sign-in']"),
                "Expected sign out to expose login navigation."
        );
    }

    private void login(CustomerData customer) {
        UIActions.openRelativeWithoutUrlWait("/auth/login");
        WaitUtils.waitForVisible(LoginPage.EMAIL);
        UIActions.type(LoginPage.EMAIL, "Login email", customer.getEmail());
        UIActions.type(LoginPage.PASSWORD, "Login password", customer.getPassword());
        UIActions.click(LoginPage.LOGIN_BUTTON, "Login");
        WaitUtils.waitForCondition(
                "account surface is visible after login",
                () -> safeVisible(AccountPage.NAV_MENU) || safeVisible(AccountPage.PROFILE_NAV),
                "Expected login to expose the account surface."
        );
    }

    private CustomerData registerCustomerWithRetry() {
        AssertionError lastFailure = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            CustomerData customer = uniqueCustomer();
            try {
                registration.openRegister();
                registration.register(customer);
                registrationValidation.assertRedirectedToLogin();
                return customer;
            } catch (AssertionError failure) {
                lastFailure = failure;
                UIActions.clearSession();
            }
        }
        throw lastFailure == null ? new AssertionError("Registration did not produce a customer.") : lastFailure;
    }

    private ContactMessageContext capture(CustomerData customer,
                                          String message,
                                          String listBody,
                                          String detailsBody,
                                          String detailsUrl) {
        return new ContactMessageContext(customer, SUBJECT_LABEL, SUBJECT_SLUG, message, listBody, detailsBody, detailsUrl);
    }

    private String currentBody() {
        return DriverManager.getEngine().getText(ContactPage.BODY);
    }

    private boolean safeVisible(String locator) {
        try {
            return DriverManager.getEngine().isVisible(locator);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private CustomerData uniqueCustomer() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return CustomerDataFactory.validCustomer()
                .toBuilder()
                .password("Aa1!" + suffix)
                .build();
    }

    private String uniqueMessage() {
        return "Authenticated support message " + UUID.randomUUID()
                + " proves customer message row persistence and details route ownership.";
    }
}
