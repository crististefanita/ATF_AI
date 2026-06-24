package com.endava.ai.ui.tests;

import com.endava.ai.ui.actions.AccountInvoiceActions;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.AccountInvoiceDetailsContext;
import com.endava.ai.ui.validation.AccountPurchaseHistoryValidation;
import org.testng.annotations.Test;

public class PurchaseHistoryRun05AuditTests extends BaseTestUI {
    private final AccountInvoiceActions actions = new AccountInvoiceActions();
    private final AccountPurchaseHistoryValidation validation = new AccountPurchaseHistoryValidation();

    @Test
    public void purchase_history_audit_reopens_row_after_account_navigation() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReopenInvoiceDetails();
        validation.assertPurchaseHistoryRowCanBeReopened(context);
    }

    @Test
    public void purchase_history_audit_reopens_product_after_account_navigation() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReopenInvoiceDetails();
        validation.assertPurchaseHistoryRowKeepsPurchasedProduct(context);
    }

    @Test
    public void purchase_history_audit_survives_relogin_identity() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowSurvivesRelogin(context);
    }

    @Test
    public void purchase_history_audit_survives_relogin_payment_method() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowExposesPaymentSurface(context);
    }

    @Test
    public void purchase_history_audit_survives_relogin_total() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceReloginAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowKeepsCheckoutTotal(context);
    }
}
