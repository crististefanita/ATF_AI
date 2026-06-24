package com.endava.ai.ui.tests;

import com.endava.ai.ui.actions.AccountInvoiceActions;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.AccountInvoiceDetailsContext;
import com.endava.ai.ui.validation.AccountInvoiceDetailsValidation;
import org.testng.annotations.Test;

public class AccountInvoiceDetailsLineItemTest extends BaseTestUI {
    // Audit object: registered account invoice details page with purchased line item.
    private final AccountInvoiceActions actions = new AccountInvoiceActions();
    private final AccountInvoiceDetailsValidation validation = new AccountInvoiceDetailsValidation();

    @Test
    public void line_item_details_bind_invoice_number_to_product() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertDetailsOpenedFromCreatedInvoiceRow(context);
        validation.assertDetailsContainPurchasedLineItem(context);
    }

    @Test
    public void line_item_details_bind_product_to_total() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertDetailsContainPurchasedLineItem(context);
        validation.assertDetailsPreserveCheckoutTotal(context);
    }

    @Test
    public void line_item_details_bind_total_to_payment_and_billing_sections() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertDetailsPreserveCheckoutTotal(context);
        validation.assertDetailsExposePaymentAndBillingSections(context);
    }

    @Test
    public void line_item_details_are_not_checkout_confirmation_only() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertDetailsAreOpenedFromAccountSurface(context);
    }

    @Test
    public void line_item_details_preserve_full_invoice_chain() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertDetailsOpenedFromCreatedInvoiceRow(context);
        validation.assertDetailsContainPurchasedLineItem(context);
        validation.assertDetailsPreserveCheckoutTotal(context);
    }
}
