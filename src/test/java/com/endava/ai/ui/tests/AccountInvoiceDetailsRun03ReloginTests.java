package com.endava.ai.ui.tests;

import com.endava.ai.ui.actions.AccountInvoiceActions;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.AccountInvoiceDetailsContext;
import com.endava.ai.ui.validation.AccountInvoiceDetailsValidation;
import org.testng.annotations.Test;

public class AccountInvoiceDetailsRun03ReloginTests extends BaseTestUI {
    private final AccountInvoiceActions actions = new AccountInvoiceActions();
    private final AccountInvoiceDetailsValidation validation = new AccountInvoiceDetailsValidation();

    @Test
    public void relogin_invoice_details_keep_invoice_number() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertDetailsOpenedFromCreatedInvoiceRow(context);
    }

    @Test
    public void relogin_invoice_details_keep_line_item() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertDetailsContainPurchasedLineItem(context);
    }

    @Test
    public void relogin_invoice_details_keep_total() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertDetailsPreserveCheckoutTotal(context);
    }

    @Test
    public void relogin_invoice_details_expose_payment_and_billing_sections() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertDetailsExposePaymentAndBillingSections(context);
    }

    @Test
    public void relogin_invoice_details_stay_under_account() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertDetailsAreOpenedFromAccountSurface(context);
    }
}
