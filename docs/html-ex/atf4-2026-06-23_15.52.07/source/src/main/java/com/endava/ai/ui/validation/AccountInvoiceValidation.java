package com.endava.ai.ui.validation;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.ui.model.AccountInvoiceContext;
import com.endava.ai.ui.pages.AccountPage;
import com.endava.ai.ui.utils.UIActions;
import org.testng.Assert;

public final class AccountInvoiceValidation {

    public void assertInvoiceRowContainsCreatedInvoice(AccountInvoiceContext context) {
        StepLogger.startStep("Validate account invoices row contains created invoice");
        String body = invoicesBody();
        StepLogger.logDetail("invoiceNumber=" + context.getInvoiceNumber());
        StepLogger.logDetail("invoicesBody=" + body);
        Assert.assertTrue(body.contains(context.getInvoiceNumber()),
                "Expected account invoices page to list created invoice: " + context.getInvoiceNumber());
        StepLogger.pass("Created invoice is listed in account invoices");
    }

    public void assertInvoiceRowPreservesCartTotal(AccountInvoiceContext context) {
        StepLogger.startStep("Validate account invoices row preserves cart total");
        String body = invoicesBody();
        StepLogger.logDetail("cartTotal=" + context.getCartTotal());
        StepLogger.logDetail("invoicesBody=" + body);
        Assert.assertTrue(body.contains(context.getCartTotal()),
                "Expected account invoice row to contain cart total: " + context.getCartTotal());
        StepLogger.pass("Account invoice row keeps checkout total");
    }

    public void assertInvoiceBelongsToLoggedCustomer(AccountInvoiceContext context) {
        StepLogger.startStep("Validate invoice row is owned by logged customer surface");
        String body = invoicesBody();
        StepLogger.logDetail("customerEmail=" + context.getCustomer().getEmail());
        StepLogger.logDetail("customerName=" + context.getCustomer().getFirstName() + " " + context.getCustomer().getLastName());
        Assert.assertTrue(body.contains("Invoices"), "Expected account invoices page title.");
        Assert.assertTrue(body.contains(context.getInvoiceNumber()), "Expected invoice number on account page.");
        StepLogger.pass("Invoice row is reachable from the registered account");
    }

    public void assertInvoiceRowHasTableShape(AccountInvoiceContext context) {
        StepLogger.startStep("Validate account invoice table shape");
        String body = invoicesBody();
        StepLogger.logDetail("invoiceNumber=" + context.getInvoiceNumber());
        Assert.assertTrue(body.contains("Invoice Number"), "Expected invoice number column.");
        Assert.assertTrue(body.contains("Billing Address"), "Expected billing address column.");
        Assert.assertTrue(body.contains("Invoice Date"), "Expected invoice date column.");
        Assert.assertTrue(body.contains("Total"), "Expected total column.");
        Assert.assertTrue(body.contains("Details"), "Expected details affordance for invoice row.");
        StepLogger.pass("Account invoices table exposes the durable row contract");
    }

    public void assertInvoiceRowSurvivesAccountReopen(AccountInvoiceContext context) {
        StepLogger.startStep("Validate invoice row survives account reopen");
        String body = invoicesBody();
        StepLogger.logDetail("invoiceNumber=" + context.getInvoiceNumber());
        Assert.assertTrue(body.contains(context.getInvoiceNumber()),
                "Expected invoice row after account navigation reopen.");
        StepLogger.pass("Invoice row survives account reopen");
    }

    private String invoicesBody() {
        return UIActions.getText(AccountPage.INVOICES_BODY, "Account invoices body");
    }
}
