package com.endava.ai.ui.validation;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.ui.model.AccountInvoiceDetailsContext;
import org.testng.Assert;

public final class AccountInvoiceDetailsValidation {

    public void assertDetailsOpenedFromCreatedInvoiceRow(AccountInvoiceDetailsContext context) {
        StepLogger.startStep("Validate invoice details open from created invoice row");
        StepLogger.logDetail("invoiceNumber=" + context.getInvoice().getInvoiceNumber());
        StepLogger.logDetail("detailsUrl=" + context.getDetailsUrl());
        StepLogger.logDetail("invoiceListBody=" + context.getInvoiceListBody());
        StepLogger.logDetail("detailsBody=" + context.getDetailsBody());
        Assert.assertTrue(context.getInvoiceListBody().contains(context.getInvoice().getInvoiceNumber()),
                "Expected source invoice list to contain created invoice number.");
        Assert.assertTrue(context.getDetailsUrl().contains("/account/invoices/"),
                "Expected details route to open below account invoices.");
        Assert.assertTrue(context.getDetailsBody().contains("Products"),
                "Expected invoice details to expose products section.");
        StepLogger.pass("Invoice details open from the created account invoice row");
    }

    public void assertDetailsContainPurchasedLineItem(AccountInvoiceDetailsContext context) {
        StepLogger.startStep("Validate invoice details contain purchased line item");
        StepLogger.logDetail("productName=" + context.getInvoice().getProductName());
        StepLogger.logDetail("detailsBody=" + context.getDetailsBody());
        Assert.assertTrue(context.getDetailsBody().contains(context.getInvoice().getProductName()),
                "Expected invoice details to contain purchased product line item.");
        StepLogger.pass("Invoice details preserve the purchased product line item");
    }

    public void assertDetailsPreserveCheckoutTotal(AccountInvoiceDetailsContext context) {
        StepLogger.startStep("Validate invoice details preserve checkout total");
        StepLogger.logDetail("cartTotal=" + context.getInvoice().getCartTotal());
        StepLogger.logDetail("detailsBody=" + context.getDetailsBody());
        Assert.assertTrue(context.getDetailsBody().contains(context.getInvoice().getCartTotal()),
                "Expected invoice details to preserve checkout total.");
        StepLogger.pass("Invoice details preserve checkout total");
    }

    public void assertDetailsExposePaymentAndBillingSections(AccountInvoiceDetailsContext context) {
        StepLogger.startStep("Validate invoice details expose payment and billing sections");
        String body = context.getDetailsBody();
        StepLogger.logDetail("paymentMethod=" + context.getInvoice().getPaymentMethod());
        StepLogger.logDetail("detailsBody=" + body);
        Assert.assertTrue(body.contains("Billing Address"),
                "Expected invoice details to expose billing address section.");
        Assert.assertTrue(body.contains("Payment Information"),
                "Expected invoice details to expose payment information section.");
        Assert.assertTrue(body.contains("Payment Method"),
                "Expected invoice details to expose payment method label.");
        StepLogger.pass("Invoice details expose billing and payment sections");
    }

    public void assertDetailsAreOpenedFromAccountSurface(AccountInvoiceDetailsContext context) {
        StepLogger.startStep("Validate invoice details are opened from account surface");
        StepLogger.logDetail("detailsUrl=" + context.getDetailsUrl());
        StepLogger.logDetail("detailsBody=" + context.getDetailsBody());
        Assert.assertTrue(context.getDetailsUrl().contains("/account/invoices"),
                "Expected invoice details URL to remain under account invoices.");
        Assert.assertTrue(context.getDetailsBody().contains("Download PDF"),
                "Expected invoice details body to expose PDF invoice action.");
        Assert.assertTrue(context.getDetailsBody().contains("Quantity"),
                "Expected invoice details body to expose line item quantity.");
        StepLogger.pass("Invoice details are account-owned, not only checkout confirmation");
    }
}
