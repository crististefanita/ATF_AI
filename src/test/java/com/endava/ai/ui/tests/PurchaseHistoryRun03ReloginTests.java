package com.endava.ai.ui.tests;

import com.endava.ai.ui.actions.AccountInvoiceActions;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.AccountInvoiceDetailsContext;
import com.endava.ai.ui.validation.AccountPurchaseHistoryValidation;
import org.testng.annotations.Test;

public class PurchaseHistoryRun03ReloginTests extends BaseTestUI {
    private final AccountInvoiceActions actions = new AccountInvoiceActions();
    private final AccountPurchaseHistoryValidation validation = new AccountPurchaseHistoryValidation();

    @Test
    public void relogin_purchase_history_row_keeps_invoice_identity() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowSurvivesRelogin(context);
    }

    @Test
    public void relogin_purchase_history_details_keep_product() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowKeepsPurchasedProduct(context);
    }

    @Test
    public void relogin_purchase_history_details_keep_payment_method() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowExposesPaymentSurface(context);
    }

    @Test
    public void relogin_purchase_history_details_keep_total() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowKeepsCheckoutTotal(context);
    }

    @Test
    public void relogin_purchase_history_stays_account_owned() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowStaysInAccountInvoices(context);
    }
}
