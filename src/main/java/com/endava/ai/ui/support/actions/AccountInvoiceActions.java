package com.endava.ai.ui.support.actions;

import com.endava.ai.ui.core.engine.UiNetworkResponse;
import com.endava.ai.ui.core.runtime.DriverManager;
import com.endava.ai.ui.support.model.AccountInvoiceContext;
import com.endava.ai.ui.support.model.AccountInvoiceDetailsContext;
import com.endava.ai.ui.support.pages.AccountPage;
import com.endava.ai.ui.support.actions.UIActions;
import com.endava.ai.ui.support.factory.CustomerDataFactory;
import com.endava.ai.ui.support.model.CustomerData;
import com.endava.ai.ui.support.pages.CheckoutPage;
import com.endava.ai.ui.support.pages.LoginPage;
import com.endava.ai.ui.support.pages.ProductPage;
import com.endava.ai.ui.support.service.RegistrationService;
import com.endava.ai.ui.support.validation.RegistrationValidation;
import com.endava.ai.ui.support.wait.WaitUtils;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AccountInvoiceActions {
    private static final Pattern INVOICE_PATTERN = Pattern.compile("INV-\\d+");
    private static final String PAYMENT_METHOD = "Cash on Delivery";
    private static final String CHECKOUT_COUNTRY = "US";
    private static final String CHECKOUT_POSTAL_CODE = "33101";
    private static final String CHECKOUT_HOUSE_NUMBER = "12";
    private static final String CHECKOUT_STREET = "Art Point";
    private static final String CHECKOUT_CITY = "Fionaville";
    private static final String CHECKOUT_STATE = "West Virginia";
    private static final String PREFERRED_CHECKOUT_PRODUCT = "Pliers";
    private static final String PAYMENT_CHECK_ENDPOINT = "/payment/check";
    private static final String INVOICES_ENDPOINT = "/invoices";
    private static final String HTTP_POST = "POST";
    private static final Pattern INVOICE_ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final int INVOICE_CREATION_MAX_ATTEMPTS = 3;
    private static final Duration INVOICE_CREATION_RETRY_DELAY = Duration.ofSeconds(2);

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
        UiNetworkResponse invoiceCreation = payAndConfirm();
        String invoiceNumber = invoiceNumberFrom(invoiceCreation.getBodyPreview());
        if (invoiceNumber.isBlank()) {
            invoiceNumber = readInvoiceNumber();
        }
        return new AccountInvoiceContext(customer, productName, cartTotal, PAYMENT_METHOD, invoiceNumber,
                invoiceIdFrom(invoiceCreation.getBodyPreview()));
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
        return openInvoiceDetails(context);
    }

    public AccountInvoiceDetailsContext createRegisteredInvoiceReloginAndOpenInvoiceDetails() {
        AccountInvoiceContext context = createRegisteredInvoiceAndOpenInvoices();
        signOut();
        login(context.getCustomer());
        return openInvoiceDetails(context);
    }

    public AccountInvoiceDetailsContext openInvoiceDetails(AccountInvoiceContext context) {
        String invoiceListBody;
        if (context.getInvoiceId() != null && !context.getInvoiceId().isBlank()) {
            UIActions.navigateToRelative(AccountPage.invoiceDetailsPath(context.getInvoiceId()));
            invoiceListBody = "directInvoice=" + context.getInvoiceNumber() + "\nsource=invoice-create-response";
        } else {
            String detailsLink = AccountPage.invoiceDetailsLink(context.getInvoiceNumber());
            invoiceListBody = currentAccountBody();
            UIActions.click(detailsLink, "Invoice details for " + context.getInvoiceNumber());
        }
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
        Boolean visible = WaitUtils.waitUntil(
                () -> {
                    UIActions.navigateToRelative(AccountPage.INVOICES_PATH);
                    return DriverManager.getEngine().getCurrentUrl().contains(AccountPage.INVOICES_PATH)
                            && currentAccountBody().contains("Invoice Number")
                            && safeVisible(AccountPage.INVOICE_ROWS);
                },
                Boolean.TRUE::equals,
                Duration.ofSeconds(45),
                Duration.ofSeconds(3)
        );
        if (!Boolean.TRUE.equals(visible)) {
            throw new AssertionError("Expected at least one account invoice row.");
        }
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
        WaitUtils.waitForVisible(ProductPage.PRODUCT_CARD);
        var productCards = UIActions.getTexts(ProductPage.PRODUCT_CARD, "Catalog product cards");
        int preferredIndex = preferredAvailableProductIndex(productCards);
        if (preferredIndex > 0) {
            return openProductAndAddToCart(preferredIndex);
        }
        int productCount = productCards.size();
        for (int index = 1; index <= productCount; index++) {
            String cardText = productCards.get(index - 1);
            if (cardText.toLowerCase().contains("out of stock")) {
                continue;
            }
            return openProductAndAddToCart(index);
        }
        throw new AssertionError("No in-stock product card was available for registered invoice checkout.");
    }

    private int preferredAvailableProductIndex(java.util.List<String> productCards) {
        for (int index = 0; index < productCards.size(); index++) {
            String cardText = productCards.get(index);
            if (cardText.toLowerCase().contains("out of stock")) {
                continue;
            }
            if (PREFERRED_CHECKOUT_PRODUCT.equals(firstLine(cardText))) {
                return index + 1;
            }
        }
        return -1;
    }

    private String openProductAndAddToCart(int oneBasedIndex) {
        UIActions.clickVisible(ProductPage.PRODUCT_CARD, "Available catalog product", oneBasedIndex);
        WaitUtils.waitForCondition(
                "product details page is ready",
                () -> safeVisible(ProductPage.PRODUCT_NAME),
                "Expected product details page to expose product name after selecting catalog card.",
                java.time.Duration.ofSeconds(45)
        );
        String productName = UIActions.getText(ProductPage.PRODUCT_NAME, "Product detail name");
        UIActions.click(ProductPage.ADD_TO_CART, "Add product to cart");
        WaitUtils.waitForVisible(ProductPage.CART_LINK);
        return productName;
    }

    private static String firstLine(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .findFirst()
                .orElse("");
    }

    private String proceedFromCart() {
        UIActions.click(ProductPage.CART_LINK, "Cart checkout link");
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
        WaitUtils.waitForVisible(CheckoutPage.PROCEED_FROM_ACCOUNT);
        UIActions.click(CheckoutPage.PROCEED_FROM_ACCOUNT, "Proceed from authenticated sign-in");
    }

    private void fillBillingAddress(CustomerData customer) {
        WaitUtils.waitForVisible(CheckoutPage.COUNTRY);
        UIActions.select(CheckoutPage.COUNTRY, "Billing country", CHECKOUT_COUNTRY);
        typeFresh(CheckoutPage.POSTAL_CODE, "Billing postal code", CHECKOUT_POSTAL_CODE);
        typeFresh(CheckoutPage.HOUSE_NUMBER, "Billing house number", CHECKOUT_HOUSE_NUMBER);
        typeFresh(CheckoutPage.STREET, "Billing street", CHECKOUT_STREET);
        typeFresh(CheckoutPage.CITY, "Billing city", CHECKOUT_CITY);
        typeFresh(CheckoutPage.STATE, "Billing state", CHECKOUT_STATE);
        UIActions.select(CheckoutPage.COUNTRY, "Billing country", CHECKOUT_COUNTRY);
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

    private UiNetworkResponse payAndConfirm() {
        WaitUtils.waitForVisible(CheckoutPage.PAYMENT_METHOD);
        UIActions.select(CheckoutPage.PAYMENT_METHOD, "Payment method", PAYMENT_METHOD);
        UiNetworkResponse paymentCheck = UIActions.waitForResponse(
                "payment method validation",
                PAYMENT_CHECK_ENDPOINT,
                HTTP_POST,
                () -> UIActions.click(CheckoutPage.FINISH, "Confirm payment method")
        );
        assertSuccessfulNetworkResponse(paymentCheck, "payment method validation");
        WaitUtils.waitForCondition(
                "payment success message is visible",
                () -> currentCheckoutBody().contains("Payment was successful"),
                "Expected payment success message before final confirmation."
        );
        UiNetworkResponse invoiceCreation = submitInvoiceCreationWithRetry();
        waitForFinalPaymentSubmission();
        return invoiceCreation;
    }

    private UiNetworkResponse submitInvoiceCreationWithRetry() {
        UiNetworkResponse lastResponse = null;
        for (int attempt = 1; attempt <= INVOICE_CREATION_MAX_ATTEMPTS; attempt++) {
            String attemptLabel = attempt == 1 ? "" : " retry " + attempt;
            lastResponse = UIActions.waitForResponse(
                    "invoice creation" + attemptLabel,
                    INVOICES_ENDPOINT,
                    HTTP_POST,
                    () -> UIActions.click(CheckoutPage.FINISH, "Confirm payment" + attemptLabel)
            );
            if (lastResponse.isSuccess()) {
                return lastResponse;
            }
            if (!isRetryableServerFailure(lastResponse) || attempt == INVOICE_CREATION_MAX_ATTEMPTS) {
                break;
            }
            WaitUtils.pause(INVOICE_CREATION_RETRY_DELAY);
            if (!safeVisible(CheckoutPage.FINISH)) {
                break;
            }
        }
        assertSuccessfulNetworkResponse(lastResponse, "invoice creation");
        return lastResponse;
    }

    private boolean isRetryableServerFailure(UiNetworkResponse response) {
        return response != null && response.getStatus() >= 500 && response.getStatus() < 600;
    }

    private void waitForFinalPaymentSubmission() {
        Boolean invoiceVisible = WaitUtils.waitUntil(
                () -> INVOICE_PATTERN.matcher(currentCheckoutBody()).find()
                        || DriverManager.getEngine().getCurrentUrl().contains(AccountPage.INVOICES_PATH),
                Boolean.TRUE::equals,
                Duration.ofSeconds(15),
                Duration.ofMillis(500)
        );
        if (!Boolean.TRUE.equals(invoiceVisible)) {
            throw new AssertionError("Expected final payment confirmation to expose invoice evidence. Current URL: "
                    + DriverManager.getEngine().getCurrentUrl() + "; body: " + currentCheckoutBody());
        }
    }

    private void assertSuccessfulNetworkResponse(UiNetworkResponse response, String description) {
        if (!response.isSuccess()) {
            throw new AssertionError("Expected successful " + description + " response, but got HTTP "
                    + response.getStatus() + " from " + response.getUrl()
                    + "; body: " + response.getBodyPreview());
        }
    }

    private void typeFresh(String locator, String description, String value) {
        UIActions.pressKey(locator, description + " select all", "Control+A");
        UIActions.pressKey(locator, description + " clear", "Backspace");
        UIActions.type(locator, description, value);
    }

    private String readInvoiceNumber() {
        String body = currentCheckoutBody();
        Matcher matcher = INVOICE_PATTERN.matcher(body);
        if (matcher.find()) {
            return matcher.group();
        }
        openInvoices();
        String invoiceNumber = invoiceNumberFrom(currentAccountBody());
        if (invoiceNumber.isBlank()) {
            throw new AssertionError("Invoice number was not visible in confirmation or invoice account body. Current URL: "
                    + DriverManager.getEngine().getCurrentUrl() + "; body: " + currentAccountBody());
        }
        return invoiceNumber;
    }

    private String invoiceNumberFrom(String body) {
        Matcher matcher = INVOICE_PATTERN.matcher(body == null ? "" : body);
        return matcher.find() ? matcher.group() : "";
    }

    private String invoiceIdFrom(String body) {
        Matcher matcher = INVOICE_ID_PATTERN.matcher(body == null ? "" : body);
        return matcher.find() ? matcher.group(1) : "";
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

