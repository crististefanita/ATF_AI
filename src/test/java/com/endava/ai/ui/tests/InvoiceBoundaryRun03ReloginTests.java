package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.AccountInvoiceBoundaryComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.validation.AccountInvoiceBoundaryValidation;
import org.testng.annotations.Test;

public class InvoiceBoundaryRun03ReloginTests extends BaseTestUI {
    private final AccountInvoiceBoundaryComponent invoice = new AccountInvoiceBoundaryComponent();
    private final AccountInvoiceBoundaryValidation verify = new AccountInvoiceBoundaryValidation();

    @Test
    public void reloginCanReopenInvoicesShellAfterPayment() {
        invoice.registerLoginAndOpenAccount();
        invoice.openInvoices();
        invoice.reopenInvoicesAfterRelogin();
        verify.assertInvoicesPageTitleVisible();
    }

    @Test
    public void reloginKeepsAccountInvoicesNavigationAvailable() {
        invoice.registerLoginAndOpenAccount();
        invoice.openInvoices();
        invoice.reopenInvoicesAfterRelogin();
        invoice.openAccountMenu();
        verify.assertMyInvoicesNavigationIsAvailable();
    }

    @Test
    public void reloginInvoicesPageKeepsEmptyShellOnly() {
        invoice.registerLoginAndOpenAccount();
        invoice.openInvoices();
        invoice.reopenInvoicesAfterRelogin();
        verify.assertInvoicesShellHasNoTableHeaders();
    }

    @Test
    public void reloginStillHasNoInvoiceRowForPayment() {
        invoice.registerLoginAndOpenAccount();
        invoice.openInvoices();
        invoice.reopenInvoicesAfterRelogin();
        verify.assertInvoicesPageHasNoInvoiceRow();
    }

    @Test
    public void reloginStillHasNoBillingAddressInvoiceRow() {
        invoice.registerLoginAndOpenAccount();
        invoice.openInvoices();
        invoice.reopenInvoicesAfterRelogin();
        verify.assertInvoicesPageDoesNotExposeBillingAddressRow(invoice.getCustomer());
    }
}
