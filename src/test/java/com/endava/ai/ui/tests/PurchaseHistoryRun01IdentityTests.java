package com.endava.ai.ui.tests;

import com.endava.ai.ui.actions.AccountInvoiceActions;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.AccountInvoiceContext;
import com.endava.ai.ui.model.AccountInvoiceDetailsContext;
import com.endava.ai.ui.validation.AccountPurchaseHistoryValidation;
import org.testng.annotations.Test;

public class PurchaseHistoryRun01IdentityTests extends BaseTestUI {
    private final AccountInvoiceActions actions = new AccountInvoiceActions();
    private final AccountPurchaseHistoryValidation validation = new AccountPurchaseHistoryValidation();

    @Test
    public void registered_purchase_history_row_has_invoice_identity() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowHasInvoiceIdentity(context.getInvoice());
    }

    @Test
    public void registered_purchase_history_row_opens_from_invoice_list() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowStaysInAccountInvoices(context);
    }

    @Test
    public void registered_purchase_history_details_preserve_product() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowKeepsPurchasedProduct(context);
    }

    @Test
    public void registered_purchase_history_details_preserve_payment_method() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowExposesPaymentSurface(context);
    }

    @Test
    public void registered_purchase_history_details_preserve_checkout_total() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowKeepsCheckoutTotal(context);
    }
}
