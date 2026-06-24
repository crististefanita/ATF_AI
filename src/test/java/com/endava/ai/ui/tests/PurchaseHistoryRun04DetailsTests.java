package com.endava.ai.ui.tests;

import com.endava.ai.ui.actions.AccountInvoiceActions;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.AccountInvoiceDetailsContext;
import com.endava.ai.ui.validation.AccountPurchaseHistoryValidation;
import org.testng.annotations.Test;

public class PurchaseHistoryRun04DetailsTests extends BaseTestUI {
    private final AccountInvoiceActions actions = new AccountInvoiceActions();
    private final AccountPurchaseHistoryValidation validation = new AccountPurchaseHistoryValidation();

    @Test
    public void purchase_history_detail_route_is_account_scoped() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowStaysInAccountInvoices(context);
    }

    @Test
    public void purchase_history_detail_route_preserves_row_identity() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowCanBeReopened(context);
    }

    @Test
    public void purchase_history_detail_route_preserves_product() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowKeepsPurchasedProduct(context);
    }

    @Test
    public void purchase_history_detail_route_preserves_payment_method() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowExposesPaymentSurface(context);
    }

    @Test
    public void purchase_history_detail_route_preserves_total() {
        AccountInvoiceDetailsContext context = actions.createRegisteredInvoiceAndOpenInvoiceDetails();
        validation.assertPurchaseHistoryRowKeepsCheckoutTotal(context);
    }
}
