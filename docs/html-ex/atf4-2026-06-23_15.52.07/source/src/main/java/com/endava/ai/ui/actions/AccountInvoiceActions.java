package com.endava.ai.ui.actions;

import com.endava.ai.ui.core.DriverManager;
import com.endava.ai.ui.factory.CustomerDataFactory;
import com.endava.ai.ui.model.AccountInvoiceContext;
import com.endava.ai.ui.model.AccountInvoiceDetailsContext;
import com.endava.ai.ui.model.CustomerData;
import com.endava.ai.ui.pages.AccountPage;
import com.endava.ai.ui.pages.CheckoutPage;
import com.endava.ai.ui.pages.LoginPage;
import com.endava.ai.ui.pages.ProductPage;
import com.endava.ai.ui.service.RegistrationService;
import com.endava.ai.ui.utils.UIActions;
import com.endava.ai.ui.utils.WaitUtils;
import com.endava.ai.ui.validation.RegistrationValidation;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AccountInvoiceActions {
    private static final Pattern INVOICE_PATTERN = Pattern.compile("INV-\\d+");
    private static final String PAYMENT_METHOD = "Cash on Delivery";

    private final RegistrationService registration = new RegistrationService();
    private final RegistrationValidation registrationValidation = new RegistrationValidation();

    public AccountInvoiceContext createRegisteredInvoiceAndOpenInvoices() {
        UIActions.clearSession();
        CustomerData customer = registerCustomerWithRetry();
        login(customer);
        String productName = addAvailableProductToCart();
        String cartTotal = proceedFromCart();
        proceedFromAuthenticatedSignInStep(customer);
        fillBillingAddress(customer);
        payAndConfirm();
        String invoiceNumber = readInvoiceNumber();
        openInvoices();
        return new AccountInvoiceContext(customer, productName, cartTotal, PAYMENT_METHOD, invoiceNumber);
    }

    public AccountInvoiceContext createRegisteredInvoiceReopenInvoices() {
        AccountInvoiceContext context = createRegisteredInvoiceAndOpenInvoices();
        openAccount();
        openInvoices();
        return context;
    }

    public AccountInvoiceContext createRegisteredInvoiceReloginAndOpenInvoices() {
        AccountInvoiceContext context = createRegisteredInvoiceAndOpenInvoices();
        signOut();
        login(context.getCustomer());
        openInvoices();
        return context;
    }

    public AccountInvoiceDetailsContext createRegisteredInvoiceAndOpenInvoiceDetails() {
        AccountInvoiceContext context = createRegisteredInvoiceAndOpenInvoices();
        return openInvoiceDetails(context);
    }

    public AccountInvoiceDetailsContext createRegisteredInvoiceReopenInvoiceDetails() {
        AccountInvoiceContext context = createRegisteredInvoiceAndOpenInvoices();
        openAccount();
        openInvoices();
        return openInvoiceDetails(context);
    }

    public AccountInvoiceDetailsContext createRegisteredInvoiceReloginAndOpenInvoiceDetails() {
        AccountInvoiceContext context = createRegisteredInvoiceAndOpenInvoices();
        signOut();
        login(context.getCustomer());
        openInvoices();
        return openInvoiceDetails(context);
    }

    public AccountInvoiceDetailsContext openInvoiceDetails(AccountInvoiceContext context) {
        String detailsLink = AccountPage.invoiceDetailsLink(context.getInvoiceNumber());
        String invoiceListBody = currentAccountBody();
        UIActions.click(detailsLink, "Invoice details for " + context.getInvoiceNumber());
        WaitUtils.pause(Duration.ofSeconds(2));
        WaitUtils.waitForCondition(
                "invoice details surface contains purchased product",
                () -> currentAccountBody().contains(context.getProductName()),
                "Expected invoice details to contain purchased product: " + context.getProductName()
        );
        WaitUtils.waitForCondition(
                "invoice details surface contains products table",
                () -> currentAccountBody().contains("Products") && currentAccountBody().contains("Quantity"),
                "Expected invoice details to expose products table."
        );
        return new AccountInvoiceDetailsContext(
                context,
                invoiceListBody,
                currentAccountBody(),
                DriverManager.getEngine().getCurrentUrl()
        );
    }

    public void openInvoices() {
        UIActions.navigateToRelative(AccountPage.INVOICES_PATH);
        WaitUtils.waitForVisible(AccountPage.INVOICES_TITLE);
        WaitUtils.waitForCondition(
                "account invoice row is visible",
                () -> safeVisible(AccountPage.INVOICE_ROWS),
                "Expected at least one account invoice row."
        );
    }

    public void openAccount() {
        UIActions.navigateToRelative("/account");
        WaitUtils.waitForVisible(AccountPage.INVOICES_NAV);
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

    public void login(CustomerData customer) {
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

    private String addAvailableProductToCart() {
        UIActions.navigateToRelative("/");
        WaitUtils.waitForVisible(ProductPage.FIRST_CATALOG_PRODUCT);
        int productCount = UIActions.getTexts(ProductPage.FIRST_CATALOG_PRODUCT, "Catalog product cards").size();
        for (int index = 1; index <= productCount; index++) {
            String cardText = UIActions.getTexts(ProductPage.FIRST_CATALOG_PRODUCT, "Catalog product cards").get(index - 1);
            if (cardText.toLowerCase().contains("out of stock")) {
                continue;
            }
            UIActions.clickVisible(ProductPage.FIRST_CATALOG_PRODUCT, "Available catalog product", index);
            WaitUtils.waitForVisible(ProductPage.PRODUCT_NAME);
            String productName = UIActions.getText(ProductPage.PRODUCT_NAME, "Product detail name");
            UIActions.click(ProductPage.ADD_TO_CART, "Add product to cart");
            WaitUtils.waitForVisible(ProductPage.CART_NAV);
            return productName;
        }
        throw new AssertionError("No in-stock product card was available for registered invoice checkout.");
    }

    private String proceedFromCart() {
        UIActions.click(ProductPage.CART_NAV, "Cart checkout link");
        WaitUtils.waitForVisible(CheckoutPage.PROCEED_FROM_CART);
        String cartBody = currentCheckoutBody();
        UIActions.click(CheckoutPage.PROCEED_FROM_CART, "Proceed from cart");
        return totalFrom(cartBody);
    }

    private void proceedFromAuthenticatedSignInStep(CustomerData customer) {
        WaitUtils.waitForCondition(
                "authenticated checkout sign-in step is visible",
                () -> currentCheckoutBody().contains("already logged in") || safeVisible(LoginPage.EMAIL),
                "Expected checkout sign-in step before billing."
        );
        if (safeVisible(LoginPage.EMAIL)) {
            UIActions.type(LoginPage.EMAIL, "Checkout login email", customer.getEmail());
            UIActions.type(LoginPage.PASSWORD, "Checkout login password", customer.getPassword());
            UIActions.click(LoginPage.LOGIN_BUTTON, "Checkout login");
        }
        WaitUtils.waitForVisible(CheckoutPage.PROCEED_FROM_SIGN_IN);
        UIActions.click(CheckoutPage.PROCEED_FROM_SIGN_IN, "Proceed from authenticated sign-in");
    }

    private void fillBillingAddress(CustomerData customer) {
        WaitUtils.waitForVisible(CheckoutPage.COUNTRY);
        UIActions.select(CheckoutPage.COUNTRY, "Billing country", customer.getCountry());
        UIActions.type(CheckoutPage.POSTAL_CODE, "Billing postal code", customer.getPostalCode());
        UIActions.type(CheckoutPage.HOUSE_NUMBER, "Billing house number", customer.getHouseNumber());
        UIActions.type(CheckoutPage.STREET, "Billing street", customer.getStreet());
        UIActions.type(CheckoutPage.CITY, "Billing city", customer.getCity());
        UIActions.type(CheckoutPage.STATE, "Billing state", customer.getState());
        UIActions.click(CheckoutPage.PROCEED_FROM_BILLING, "Proceed from billing address");
        Boolean paymentVisible = WaitUtils.waitUntil(
                () -> safeVisible(CheckoutPage.PAYMENT_METHOD),
                Boolean.TRUE::equals,
                Duration.ofSeconds(8),
                Duration.ofMillis(500)
        );
        if (!Boolean.TRUE.equals(paymentVisible) && safeVisible(CheckoutPage.PROCEED_FROM_BILLING)) {
            UIActions.click(CheckoutPage.PROCEED_FROM_BILLING, "Retry proceed from billing address");
        }
    }

    private void payAndConfirm() {
        WaitUtils.waitForVisible(CheckoutPage.PAYMENT_METHOD);
        UIActions.select(CheckoutPage.PAYMENT_METHOD, "Payment method", PAYMENT_METHOD);
        UIActions.click(CheckoutPage.FINISH, "Confirm payment method");
        WaitUtils.waitForCondition(
                "payment success message is visible",
                () -> currentCheckoutBody().contains("Payment was successful"),
                "Expected payment success message before final confirmation."
        );
        UIActions.click(CheckoutPage.FINISH, "Confirm payment");
        Boolean invoiceVisible = WaitUtils.waitUntil(
                () -> INVOICE_PATTERN.matcher(currentCheckoutBody()).find(),
                Boolean.TRUE::equals,
                Duration.ofSeconds(30),
                Duration.ofMillis(500)
        );
        if (!Boolean.TRUE.equals(invoiceVisible)) {
            throw new AssertionError("Expected invoice confirmation with INV number after payment.");
        }
    }

    private String readInvoiceNumber() {
        Matcher matcher = INVOICE_PATTERN.matcher(currentCheckoutBody());
        if (!matcher.find()) {
            throw new AssertionError("Invoice number was not visible in confirmation body.");
        }
        return matcher.group();
    }

    private String currentCheckoutBody() {
        return DriverManager.getEngine().getText(CheckoutPage.CHECKOUT_BODY);
    }

    private String currentAccountBody() {
        return DriverManager.getEngine().getText(AccountPage.INVOICE_DETAILS_BODY);
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

    private String totalFrom(String body) {
        Matcher matcher = Pattern.compile("\\$\\d+(?:\\.\\d{2})?").matcher(body);
        String last = "";
        while (matcher.find()) {
            last = matcher.group();
        }
        return last;
    }
}
