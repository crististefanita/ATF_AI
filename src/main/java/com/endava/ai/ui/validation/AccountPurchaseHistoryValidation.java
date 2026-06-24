package com.endava.ai.ui.validation;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.ui.model.AccountInvoiceContext;
import com.endava.ai.ui.model.AccountInvoiceDetailsContext;
import org.testng.Assert;

public final class AccountPurchaseHistoryValidation {

    public void assertPurchaseHistoryRowHasInvoiceIdentity(AccountInvoiceContext context) {
        StepLogger.startStep("Validate purchase history row has invoice identity");
        StepLogger.logDetail("invoiceNumber=" + context.getInvoiceNumber());
        Assert.assertTrue(context.getInvoiceNumber().matches("INV-\\d+"),
                "Expected purchase history identity to be an invoice number.");
        StepLogger.pass("Purchase history row exposes invoice identity");
    }

    public void assertPurchaseHistoryRowKeepsPurchasedProduct(AccountInvoiceDetailsContext context) {
        StepLogger.startStep("Validate purchase history row opens purchased product");
        StepLogger.logDetail("productName=" + context.getInvoice().getProductName());
        StepLogger.logDetail("detailsBody=" + context.getDetailsBody());
        Assert.assertTrue(context.getDetailsBody().contains(context.getInvoice().getProductName()),
                "Expected purchase history details to keep purchased product.");
        StepLogger.pass("Purchase history details preserve the purchased product");
    }

    public void assertPurchaseHistoryRowExposesPaymentSurface(AccountInvoiceDetailsContext context) {
        StepLogger.startStep("Validate purchase history row exposes payment surface");
        StepLogger.logDetail("paymentMethod=" + context.getInvoice().getPaymentMethod());
        StepLogger.logDetail("detailsBody=" + context.getDetailsBody());
        Assert.assertTrue(context.getDetailsBody().contains("Payment Information"),
                "Expected purchase history details to expose payment information section.");
        Assert.assertTrue(context.getDetailsBody().contains("Payment Method"),
                "Expected purchase history details to expose payment method label.");
        StepLogger.pass("Purchase history details expose payment surface");
    }

    public void assertPurchaseHistoryRowKeepsCheckoutTotal(AccountInvoiceDetailsContext context) {
        StepLogger.startStep("Validate purchase history row keeps checkout total");
        StepLogger.logDetail("cartTotal=" + context.getInvoice().getCartTotal());
        StepLogger.logDetail("detailsBody=" + context.getDetailsBody());
        Assert.assertTrue(context.getDetailsBody().contains(context.getInvoice().getCartTotal()),
                "Expected purchase history details to preserve checkout total.");
        StepLogger.pass("Purchase history details preserve checkout total");
    }

    public void assertPurchaseHistoryRowStaysInAccountInvoices(AccountInvoiceDetailsContext context) {
        StepLogger.startStep("Validate purchase history row stays under account invoices");
        StepLogger.logDetail("detailsUrl=" + context.getDetailsUrl());
        Assert.assertTrue(context.getDetailsUrl().contains("/account/invoices/"),
                "Expected purchase history details route to stay under account invoices.");
        StepLogger.pass("Purchase history row is account-owned");
    }

    public void assertPurchaseHistoryRowCanBeReopened(AccountInvoiceDetailsContext context) {
        StepLogger.startStep("Validate purchase history row can be reopened");
        StepLogger.logDetail("invoiceListBody=" + context.getInvoiceListBody());
        Assert.assertTrue(context.getInvoiceListBody().contains(context.getInvoice().getInvoiceNumber()),
                "Expected reopened purchase history list to contain the created invoice row.");
        StepLogger.pass("Purchase history row is visible after account reopen");
    }

    public void assertPurchaseHistoryRowSurvivesRelogin(AccountInvoiceDetailsContext context) {
        StepLogger.startStep("Validate purchase history row survives relogin");
        StepLogger.logDetail("invoiceNumber=" + context.getInvoice().getInvoiceNumber());
        StepLogger.logDetail("invoiceListBody=" + context.getInvoiceListBody());
        StepLogger.logDetail("detailsUrl=" + context.getDetailsUrl());
        StepLogger.logDetail("detailsBody=" + context.getDetailsBody());
        Assert.assertTrue(context.getInvoiceListBody().contains(context.getInvoice().getInvoiceNumber()),
                "Expected relogin purchase history list to contain the created invoice row.");
        Assert.assertTrue(context.getDetailsUrl().contains("/account/invoices/"),
                "Expected relogin purchase history details to stay under account invoices.");
        StepLogger.pass("Purchase history row survives relogin");
    }
}
