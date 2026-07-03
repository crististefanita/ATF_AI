package com.endava.ai.ui.support.validation;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.ui.core.runtime.DriverManager;
import com.endava.ai.ui.support.model.CustomerData;
import com.endava.ai.ui.support.pages.AccountPage;
import com.endava.ai.ui.support.pages.CheckoutPage;
import com.endava.ai.ui.support.actions.UIActions;
import com.endava.ai.ui.support.wait.WaitUtils;
import org.testng.Assert;

public final class AccountInvoiceBoundaryValidation {
    private void logValidation(String description) {
        StepLogger.startStep("Validate " + description);
        StepLogger.logDetail("Account invoice boundary validation keeps assertions in validation layer.");
    }

    public void assertAuthenticatedCheckoutRecognizesCustomer(CustomerData customer) {
        logValidation("authenticated checkout recognizes customer");
        String body = UIActions.getText(CheckoutPage.BODY, "authenticated checkout body");
        Assert.assertTrue(body.contains(customer.getFirstName()) && body.contains(customer.getLastName()),
                "Expected authenticated checkout to expose the registered customer identity, but was: " + body);
    }

    public void assertAuthenticatedCheckoutBypassesGuestIdentityForm() {
        String body = UIActions.getText(CheckoutPage.BODY, "authenticated checkout sign-in body");
        Assert.assertFalse(body.contains("Continuing as guest"), "Authenticated checkout should not use guest identity.");
        Assert.assertFalse(body.contains("guest-email"), "Authenticated checkout should not expose guest email entry as business proof.");
    }

    public void assertAuthenticatedBillingStepVisible() {
        logValidation("authenticated billing step represented in checkout stepper");
        String body = UIActions.getText(CheckoutPage.BODY, "authenticated billing body");
        Assert.assertTrue(body.contains("BILLING ADDRESS") && body.contains("PAYMENT"),
                "Expected authenticated checkout stepper to include Billing Address and Payment, but was: " + body);
    }

    public void assertPaymentSuccessVisible() {
        logValidation("authenticated payment success visible");
        String body = UIActions.getText(CheckoutPage.BODY, "authenticated payment success body");
        Assert.assertTrue(body.contains("Payment was successful"), "Expected payment success message, but was: " + body);
    }

    public void assertPaymentSuccessDoesNotExposeInvoiceNumber() {
        String body = UIActions.getText(CheckoutPage.BODY, "authenticated payment success body");
        Assert.assertFalse(body.matches("(?is).*\\binvoice\\s*(number|#|id)\\b.*"),
                "Payment success screen unexpectedly exposed invoice identity: " + body);
    }

    public void assertInvoicesPageTitleVisible() {
        logValidation("invoices page title visible");
        String title = UIActions.getText(AccountPage.PAGE_TITLE, "account invoices title");
        Assert.assertEquals(title.trim(), "Invoices", "Expected account invoices page title.");
    }

    public void assertInvoicesTableHeadersVisible() {
        String body = UIActions.getText(AccountPage.BODY, "account invoices body");
        Assert.assertTrue(body.contains("Invoice Number"), "Expected Invoice Number header.");
        Assert.assertTrue(body.contains("Billing Address"), "Expected Billing Address header.");
        Assert.assertTrue(body.contains("Invoice Date"), "Expected Invoice Date header.");
        Assert.assertTrue(body.contains("Total"), "Expected Total header.");
    }

    public void assertInvoicesShellHasNoTableHeaders() {
        String body = UIActions.getText(AccountPage.BODY, "account invoices body");
        Assert.assertTrue(body.contains("Invoices"), "Expected Invoices shell to be reachable.");
        Assert.assertFalse(body.contains("Invoice Number"), "Expected no invoice table header on empty shell.");
        Assert.assertFalse(body.contains("Billing Address"), "Expected no billing-address table header on empty shell.");
        Assert.assertFalse(body.contains("Invoice Date"), "Expected no invoice-date table header on empty shell.");
        Assert.assertFalse(body.matches("(?is).*\\bTotal\\b.*\\$12\\.01.*"), "Expected no total-bearing invoice table row.");
    }

    public void assertInvoicesPageHasNoInvoiceRow() {
        logValidation("invoice row remains absent after authenticated checkout");
        String body = UIActions.getText(AccountPage.BODY, "account invoices body");
        Assert.assertFalse(body.matches("(?is).*INV[-_ ]?\\d+.*"),
                "Expected no visible invoice number row, but body was: " + body);
        Assert.assertFalse(body.matches("(?is).*\\$12\\.01\\s*(View|Open|Download|Details)?.*"),
                "Expected no invoice row carrying the checkout total, but body was: " + body);
    }

    public void assertInvoicesPageDoesNotExposeBillingAddressRow(CustomerData customer) {
        String body = UIActions.getText(AccountPage.BODY, "account invoices body");
        Assert.assertFalse(body.contains(customer.getStreet()) && body.contains(customer.getCity()),
                "Expected no invoice row with billing address, but body was: " + body);
    }

    public void assertInvoicesPageDoesNotExposeOpenInvoiceAction() {
        String body = UIActions.getText(AccountPage.BODY, "account invoices body");
        Assert.assertFalse(body.matches("(?is).*(View|Open|Download|Details)\\s+Invoice.*"),
                "Expected no invoice action because no row is visible, but body was: " + body);
    }

    public void assertMyInvoicesNavigationIsAvailable() {
        logValidation("my invoices navigation available");
        Assert.assertTrue(DriverManager.getEngine().isVisible(AccountPage.NAV_MY_INVOICES),
                "Expected account menu to expose My invoices navigation.");
    }

    public void assertMyInvoicesNavigationTargetsInvoices() {
        String href = UIActions.getAttribute(AccountPage.NAV_MY_INVOICES, "my invoices link", "href");
        Assert.assertTrue(href.endsWith("/account/invoices"), "Expected My invoices link to target /account/invoices, but was: " + href);
    }

    public void assertReloginKeepsCustomerMenuIdentity(CustomerData customer) {
        String body = UIActions.getText(AccountPage.BODY, "account body after relogin");
        Assert.assertTrue(body.contains(customer.getFirstName()) || body.contains(customer.getLastName()),
                "Expected account shell to keep customer identity after relogin, but was: " + body);
    }

    public void assertInvoiceBoundarySeparatesPaymentSuccessFromAccountRecord() {
        String body = UIActions.getText(AccountPage.BODY, "account invoices body");
        Assert.assertTrue(body.contains("Invoices"), "Expected invoices shell to be reachable.");
        Assert.assertFalse(body.contains("Payment was successful"),
                "Expected account invoice page to be separate from checkout success state.");
    }
}
