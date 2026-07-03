package com.endava.ai.ui.support.validation;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.ui.support.model.AdminOrderStatusContext;
import org.testng.Assert;

public final class AdminOrderStatusValidation {
    public void assertAdminListFindsCustomerInvoice(AdminOrderStatusContext context) {
        StepLogger.startStep("Validate admin list finds customer invoice row");
        StepLogger.logDetail("invoiceNumber=" + context.getInvoiceNumber());
        StepLogger.logDetail("adminListBodyBefore=" + context.getAdminListBodyBefore());
        Assert.assertTrue(context.getAdminListBodyBefore().contains(context.getInvoiceNumber()),
                "Expected admin orders list to contain the customer invoice number.");
        StepLogger.pass("Admin list finds customer invoice row");
    }

    public void assertAdminEditOwnsInvoiceIdentity(AdminOrderStatusContext context) {
        StepLogger.startStep("Validate admin edit page owns invoice identity");
        StepLogger.logDetail("adminEditUrl=" + context.getAdminEditUrl());
        StepLogger.logDetail("adminEditBodyBefore=" + context.getAdminEditBodyBefore());
        Assert.assertTrue(context.getAdminEditUrl().contains("/admin/orders/edit/"),
                "Expected admin edit route for the selected order.");
        Assert.assertTrue(context.getAdminListBodyBefore().contains(context.getInvoiceNumber()),
                "Expected source admin list to contain the invoice number before opening edit route.");
        Assert.assertTrue(context.getAdminEditBodyBefore().contains("Status"),
                "Expected admin edit page to expose order status.");
        StepLogger.pass("Admin edit page owns invoice identity and status control");
    }

    public void assertAdminStatusTransitionIsSaved(AdminOrderStatusContext context) {
        StepLogger.startStep("Validate admin status transition is saved");
        StepLogger.logDetail("targetStatus=" + context.getTargetStatus());
        StepLogger.logDetail("adminEditBodyAfter=" + context.getAdminEditBodyAfter());
        StepLogger.logDetail("adminListBodyAfter=" + context.getAdminListBodyAfter());
        Assert.assertTrue(context.getAdminEditBodyAfter().contains(context.getTargetStatus())
                        || context.getAdminListBodyAfter().contains(context.getTargetStatus()),
                "Expected admin evidence to contain the updated order status.");
        StepLogger.pass("Admin status transition is saved");
    }

    public void assertAdminReloginKeepsUpdatedStatus(AdminOrderStatusContext context) {
        StepLogger.startStep("Validate admin relogin keeps updated order status");
        StepLogger.logDetail("targetStatus=" + context.getTargetStatus());
        StepLogger.logDetail("adminReloginListBodyAfter=" + context.getAdminReloginListBodyAfter());
        Assert.assertTrue(context.getAdminReloginListBodyAfter().contains(context.getInvoiceNumber()),
                "Expected admin relogin orders list to contain the invoice number.");
        Assert.assertTrue(context.getAdminReloginListBodyAfter().contains(context.getTargetStatus()),
                "Expected admin relogin orders list to contain the updated status.");
        StepLogger.pass("Admin relogin keeps updated order status");
    }

    public void assertCustomerListKeepsInvoiceAfterAdminTransition(AdminOrderStatusContext context) {
        StepLogger.startStep("Validate customer invoice list keeps invoice after admin status transition");
        StepLogger.logDetail("customerEmail=" + context.getCustomerEmail());
        StepLogger.logDetail("customerListBodyAfter=" + context.getCustomerListBodyAfter());
        Assert.assertTrue(context.getCustomerListBodyAfter().contains(context.getInvoiceNumber()),
                "Expected customer invoices list to contain the invoice number after relogin.");
        StepLogger.pass("Customer invoice list keeps invoice after admin transition");
    }

    public void assertCustomerDetailsStillOwnBusinessRecord(AdminOrderStatusContext context) {
        StepLogger.startStep("Validate customer details still own product and total after status transition");
        StepLogger.logDetail("productName=" + context.getProductName());
        StepLogger.logDetail("cartTotal=" + context.getCartTotal());
        StepLogger.logDetail("customerDetailsUrl=" + context.getCustomerDetailsUrl());
        StepLogger.logDetail("customerDetailsBodyAfter=" + context.getCustomerDetailsBodyAfter());
        Assert.assertTrue(context.getCustomerDetailsUrl().contains("/account/invoices/"),
                "Expected customer details route after admin status transition.");
        Assert.assertTrue(context.getCustomerDetailsBodyAfter().contains(context.getProductName()),
                "Expected customer invoice details to keep the purchased product.");
        Assert.assertTrue(context.getCustomerDetailsBodyAfter().contains(context.getCartTotal()),
                "Expected customer invoice details to keep the checkout total.");
        StepLogger.pass("Customer details keep the business record after status transition");
    }
}
