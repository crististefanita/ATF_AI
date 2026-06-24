package com.endava.ai.ui.tests;

import com.endava.ai.ui.actions.AccountInvoiceActions;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.AccountInvoiceDetailsContext;
import com.endava.ai.ui.validation.AccountInvoiceDetailsValidation;
import org.testng.annotations.Test;

public class AccountInvoiceDetailsRun05AuditTrailTests extends BaseTestUI {
    private final AccountInvoiceActions actions = new AccountInvoiceActions();
    private final AccountInvoiceDetailsValidation validation = new AccountInvoiceDetailsValidation();

    @Test
    public void audit_trail_links_checkout_invoice_to_account_details() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReopenInvoiceDetails();
        validation.assertDetailsOpenedFromCreatedInvoiceRow(context);
        validation.assertDetailsAreOpenedFromAccountSurface(context);
    }

    @Test
    public void audit_trail_links_relogin_invoice_to_product_line() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertDetailsContainPurchasedLineItem(context);
        validation.assertDetailsAreOpenedFromAccountSurface(context);
    }

    @Test
    public void audit_trail_links_relogin_invoice_to_total() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertDetailsPreserveCheckoutTotal(context);
        validation.assertDetailsOpenedFromCreatedInvoiceRow(context);
    }

    @Test
    public void audit_trail_links_invoice_to_payment_and_billing() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReopenInvoiceDetails();
        validation.assertDetailsExposePaymentAndBillingSections(context);
        validation.assertDetailsPreserveCheckoutTotal(context);
    }

    @Test
    public void audit_trail_keeps_full_account_invoice_story() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertDetailsOpenedFromCreatedInvoiceRow(context);
        validation.assertDetailsContainPurchasedLineItem(context);
        validation.assertDetailsExposePaymentAndBillingSections(context);
    }
}
