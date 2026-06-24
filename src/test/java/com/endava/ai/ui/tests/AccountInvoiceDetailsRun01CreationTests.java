package com.endava.ai.ui.tests;

import com.endava.ai.ui.actions.AccountInvoiceActions;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.AccountInvoiceDetailsContext;
import com.endava.ai.ui.validation.AccountInvoiceDetailsValidation;
import org.testng.annotations.Test;

public class AccountInvoiceDetailsRun01CreationTests extends BaseTestUI {
    private final AccountInvoiceActions actions = new AccountInvoiceActions();
    private final AccountInvoiceDetailsValidation validation = new AccountInvoiceDetailsValidation();

    @Test
    public void registered_customer_opens_invoice_details_from_account() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertDetailsOpenedFromCreatedInvoiceRow(context);
    }

    @Test
    public void invoice_details_show_purchased_line_item() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertDetailsContainPurchasedLineItem(context);
    }

    @Test
    public void invoice_details_keep_checkout_total() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertDetailsPreserveCheckoutTotal(context);
    }

    @Test
    public void created_invoice_detail_exposes_payment_and_billing_sections() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertDetailsExposePaymentAndBillingSections(context);
    }

    @Test
    public void invoice_details_remain_account_owned_surface() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertDetailsAreOpenedFromAccountSurface(context);
    }
}
