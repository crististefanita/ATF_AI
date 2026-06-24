package com.endava.ai.ui.tests;

import com.endava.ai.ui.actions.AccountInvoiceActions;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.AccountInvoiceDetailsContext;
import com.endava.ai.ui.validation.AccountInvoiceDetailsValidation;
import org.testng.annotations.Test;

public class AccountInvoiceDetailsOwnershipTest extends BaseTestUI {
    // Audit object: registered account invoice details page with purchased line item.
    private final AccountInvoiceActions actions = new AccountInvoiceActions();
    private final AccountInvoiceDetailsValidation validation = new AccountInvoiceDetailsValidation();

    @Test
    public void account_owned_details_include_invoice_number_after_reopen() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReopenInvoiceDetails();
        validation.assertDetailsOpenedFromCreatedInvoiceRow(context);
    }

    @Test
    public void account_owned_details_include_line_item_after_relogin() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertDetailsContainPurchasedLineItem(context);
    }

    @Test
    public void account_owned_details_include_total_after_reopen() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReopenInvoiceDetails();
        validation.assertDetailsPreserveCheckoutTotal(context);
    }

    @Test
    public void account_owned_details_expose_billing_and_payment_after_relogin() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertDetailsExposePaymentAndBillingSections(context);
    }

    @Test
    public void account_owned_details_keep_account_route_after_relogin() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertDetailsAreOpenedFromAccountSurface(context);
    }
}
