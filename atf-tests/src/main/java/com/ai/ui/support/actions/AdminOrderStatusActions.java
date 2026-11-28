package com.ai.ui.support.actions;

import com.ai.ui.core.runtime.DriverManager;
import com.ai.ui.support.model.AccountInvoiceDetailsContext;
import com.ai.ui.support.model.AdminOrderStatusContext;
import com.ai.ui.support.pages.AccountPage;
import com.ai.ui.support.pages.AdminOrderPage;
import com.ai.ui.support.actions.UIActions;
import com.ai.ui.support.model.CustomerData;
import com.ai.ui.support.pages.LoginPage;
import com.ai.ui.support.wait.WaitUtils;

import java.time.Duration;

public final class AdminOrderStatusActions {
    private static final String ADMIN_EMAIL = "admin@practicesoftwaretesting.com";
    private static final String ADMIN_PASSWORD = "welcome01";
    private static final int MAX_ADMIN_ORDER_PAGES_TO_SCAN = 40;
    private static final Duration LIVE_ORDER_TIMEOUT = Duration.ofSeconds(60);
    private static final Duration LIVE_ORDER_POLL = Duration.ofSeconds(3);
    private static final Duration SEARCH_RESULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration SEARCH_RESULT_POLL = Duration.ofMillis(500);

    private final AccountInvoiceActions invoiceActions = new AccountInvoiceActions();

    public AdminOrderStatusContext createInvoiceThenChangeStatusAsAdmin(String targetStatus) {
        AccountInvoiceDetailsContext invoice = invoiceActions.createRegisteredInvoiceAndOpenInvoiceDetails();
        CustomerData customer = invoice.getInvoice().getCustomer();
        String invoiceNumber = invoice.getInvoice().getInvoiceNumber();

        UIActions.clearSession();
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        openOrdersListContaining(invoiceNumber);
        String adminListBefore = currentListEvidence(invoiceNumber);

        clickOrderEdit(invoiceNumber, "Edit admin order " + invoiceNumber);
        WaitUtils.waitForCondition(
                "admin edit status control is ready",
                () -> DriverManager.getEngine().getCurrentUrl().contains("/admin/orders/edit/")
                        && safeVisible(AdminOrderPage.ORDER_STATUS),
                "Expected admin edit page status control after clicking order edit.",
                Duration.ofSeconds(45)
        );
        WaitUtils.waitForCondition(
                "admin edit page exposes status control for selected invoice",
                () -> DriverManager.getEngine().getCurrentUrl().contains("/admin/orders/edit/")
                        && currentBody().contains("Status"),
                "Expected admin edit page to expose order status control."
        );
        String adminEditBefore = currentBody();
        String adminEditUrl = DriverManager.getEngine().getCurrentUrl();

        if (!normalizedStatus(targetStatus).equals(normalizedStatus(safeValue(AdminOrderPage.ORDER_STATUS)))) {
            UIActions.select(AdminOrderPage.ORDER_STATUS, "Admin order status", targetStatus);
            UIActions.click(AdminOrderPage.UPDATE_STATUS, "Update admin order status");
        }
        String expectedStatusValue = safeValue(AdminOrderPage.ORDER_STATUS);
        String expectedStatusLabel = safeText(AdminOrderPage.SELECTED_ORDER_STATUS).trim();
        if (expectedStatusValue.isBlank() || expectedStatusLabel.isBlank()) {
            throw new AssertionError("Expected a selected order status and its visible label after selection.");
        }
        WaitUtils.pause(Duration.ofMillis(600));
        UIActions.openRelativeWithoutUrlWait(java.net.URI.create(adminEditUrl).getRawPath());
        WaitUtils.waitForCondition(
                "reopened admin order keeps selected status",
                () -> expectedStatusValue.equals(safeValue(AdminOrderPage.ORDER_STATUS)),
                "Expected persisted status " + targetStatus + " after reopening the order."
        );
        String adminEditStatusAfter = safeValue(AdminOrderPage.ORDER_STATUS);
        String adminEditAfter = currentBody();

        waitForOrdersListCondition(
                invoiceNumber,
                "admin orders list keeps invoice and updated status",
                () -> safeText(AdminOrderPage.orderRow(invoiceNumber)).contains(expectedStatusLabel),
                "Expected admin orders list to show " + invoiceNumber + " with " + targetStatus
        );
        String adminListAfter = currentListEvidence(invoiceNumber);

        UIActions.clearSession();
        login(ADMIN_EMAIL, ADMIN_PASSWORD);
        waitForOrdersListCondition(
                invoiceNumber,
                "new admin session keeps order row and updated status",
                () -> safeText(AdminOrderPage.orderRow(invoiceNumber)).contains(expectedStatusLabel),
                "Expected new admin session orders list to show " + invoiceNumber + " with " + targetStatus
        );
        String adminReloginListAfter = currentListEvidence(invoiceNumber);

        UIActions.clearSession();
        login(customer.getEmail(), customer.getPassword());
        AccountInvoiceDetailsContext reopened = invoiceActions.openInvoiceDetails(invoice.getInvoice());
        String customerListAfter = reopened.getInvoiceListBody();

        return new AdminOrderStatusContext(
                invoice,
                expectedStatusValue,
                adminListBefore,
                adminEditBefore,
                adminEditAfter,
                adminListAfter,
                adminReloginListAfter,
                customerListAfter,
                reopened.getDetailsBody(),
                adminEditUrl,
                reopened.getDetailsUrl(),
                adminEditStatusAfter,
                expectedStatusLabel
        );
    }

    private static String normalizedStatus(String status) {
        return status == null ? "" : status.toLowerCase(java.util.Locale.ROOT).replaceAll("[-_\\s]", "");
    }

    private void openOrdersListContaining(String invoiceNumber) {
        waitForOrdersListCondition(
                invoiceNumber,
                "admin orders list contains new customer invoice row",
                () -> currentBody().contains(invoiceNumber),
                "Expected admin orders list to contain invoice " + invoiceNumber
        );
    }

    private void waitForOrdersListCondition(String invoiceNumber,
                                            String description,
                                            java.util.function.BooleanSupplier condition,
                                            String failureMessage) {
        Boolean matched = WaitUtils.waitUntil(
                () -> openOrdersPageContaining(invoiceNumber) && condition.getAsBoolean(),
                Boolean.TRUE::equals,
                LIVE_ORDER_TIMEOUT,
                LIVE_ORDER_POLL
        );
        boolean finallyMatched = condition.getAsBoolean();
        if (!Boolean.TRUE.equals(matched) && !finallyMatched) {
            throw new AssertionError(failureMessage + ". Current URL: "
                    + DriverManager.getEngine().getCurrentUrl() + "; body: " + currentBody());
        }
    }

    private boolean openOrdersPageContaining(String invoiceNumber) {
        UIActions.navigateToRelative(AdminOrderPage.ORDERS_PATH);
        if (tryFilterOrdersByInvoice(invoiceNumber) && adminOrderEditLinkReady(invoiceNumber)) {
            return true;
        }
        for (int page = 1; page <= MAX_ADMIN_ORDER_PAGES_TO_SCAN; page++) {
            String body = currentBody();
            if (body.contains(invoiceNumber) && adminOrderEditLinkReady(invoiceNumber)) {
                return true;
            }
            if (!safeVisible(AdminOrderPage.NEXT_PAGE)) {
                return false;
            }
            UIActions.click(AdminOrderPage.NEXT_PAGE, "Next admin orders page");
            WaitUtils.pause(Duration.ofMillis(500));
        }
        return currentBody().contains(invoiceNumber) && adminOrderEditLinkReady(invoiceNumber);
    }

    private boolean tryFilterOrdersByInvoice(String invoiceNumber) {
        if (!safeVisible(AdminOrderPage.ORDER_SEARCH_QUERY)) {
            return false;
        }
        UIActions.pressKey(AdminOrderPage.ORDER_SEARCH_QUERY, "Admin invoice search select all", "Control+A");
        UIActions.pressKey(AdminOrderPage.ORDER_SEARCH_QUERY, "Admin invoice search clear", "Backspace");
        UIActions.type(AdminOrderPage.ORDER_SEARCH_QUERY, "Admin invoice search", invoiceNumber);
        UIActions.click(AdminOrderPage.ORDER_SEARCH_SUBMIT, "Search admin order by invoice");
        Boolean found = WaitUtils.waitUntil(
                () -> adminOrderEditLinkReady(invoiceNumber),
                Boolean.TRUE::equals,
                SEARCH_RESULT_TIMEOUT,
                SEARCH_RESULT_POLL
        );
        return Boolean.TRUE.equals(found);
    }

    private boolean adminOrderEditLinkReady(String invoiceNumber) {
        try {
            return currentBody().contains(invoiceNumber)
                    && DriverManager.getEngine().isVisible(AdminOrderPage.orderRow(invoiceNumber))
                    && DriverManager.getEngine().isVisible(AdminOrderPage.editLink(invoiceNumber));
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private void clickOrderEdit(String invoiceNumber, String description) {
        AssertionError lastFailure = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                WaitUtils.waitForCondition(
                        "admin order edit link is ready",
                        () -> currentBody().contains(invoiceNumber) && adminOrderEditLinkReady(invoiceNumber),
                        "Expected admin order edit link for invoice " + invoiceNumber + "."
                );
                UIActions.click(AdminOrderPage.editLink(invoiceNumber), description);
                Boolean editPageReady = WaitUtils.waitUntil(
                        () -> DriverManager.getEngine().getCurrentUrl().contains("/admin/orders/edit/")
                                && safeVisible(AdminOrderPage.ORDER_STATUS),
                        Boolean.TRUE::equals,
                        Duration.ofSeconds(10),
                        Duration.ofMillis(500)
                );
                if (Boolean.TRUE.equals(editPageReady)) {
                    return;
                }
                lastFailure = new AssertionError("Expected admin edit page status control after clicking order edit. "
                        + "Current URL: " + DriverManager.getEngine().getCurrentUrl() + "; body: " + currentBody());
            } catch (AssertionError failure) {
                lastFailure = failure;
            }
            WaitUtils.pause(Duration.ofMillis(500));
            openOrdersPageContaining(invoiceNumber);
        }
        throw lastFailure;
    }

    private String currentListEvidence(String invoiceNumber) {
        return safeText(AdminOrderPage.orderRow(invoiceNumber));
    }

    private void login(String email, String password) {
        AssertionError lastFailure = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                UIActions.openRelativeWithoutUrlWait("/auth/login");
                WaitUtils.waitForVisible(LoginPage.EMAIL);
                UIActions.type(LoginPage.EMAIL, "Login email", email);
                UIActions.type(LoginPage.PASSWORD, "Login password", password);
                UIActions.click(LoginPage.LOGIN_BUTTON, "Login");
                WaitUtils.waitForCondition(
                        "authenticated navigation is visible",
                        () -> safeVisible(AccountPage.NAV_MENU),
                        "Expected authenticated account menu after login."
                );
                if (!ADMIN_EMAIL.equals(email)) {
                    return;
                }
                UIActions.navigateToRelative(AdminOrderPage.ORDERS_PATH);
                WaitUtils.waitForCondition(
                        "authenticated admin orders surface is visible",
                        this::adminOrdersSurfaceIsAuthenticated,
                        "Expected authenticated admin orders surface after login.",
                        Duration.ofSeconds(25)
                );
                return;
            } catch (AssertionError failure) {
                lastFailure = failure;
            }
            UIActions.clearSession();
        }
        throw lastFailure;
    }

    private boolean adminOrdersSurfaceIsAuthenticated() {
        String body = currentBody();
        String currentUrl = DriverManager.getEngine().getCurrentUrl();
        return currentUrl.contains(AdminOrderPage.ORDERS_PATH)
                && !body.contains("Sign in")
                && (body.contains("Invoice") || body.contains("Order") || safeVisible(AdminOrderPage.ORDER_SEARCH_QUERY));
    }

    private boolean safeVisible(String locator) {
        try {
            return DriverManager.getEngine().isVisible(locator);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private String safeValue(String locator) {
        try {
            return DriverManager.getEngine().getValue(locator);
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    private String safeText(String locator) {
        try {
            return DriverManager.getEngine().getText(locator);
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    private String currentBody() {
        try {
            return DriverManager.getEngine().getText(AdminOrderPage.ORDERS_BODY);
        } catch (RuntimeException ignored) {
            return "";
        }
    }
}

