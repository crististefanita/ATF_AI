package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.AccountInvoiceBoundaryComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.validation.AccountInvoiceBoundaryValidation;
import org.testng.annotations.Test;

public class InvoiceBoundaryRun02InvoiceShellTests extends BaseTestUI {
    private final AccountInvoiceBoundaryComponent invoice = new AccountInvoiceBoundaryComponent();
    private final AccountInvoiceBoundaryValidation verify = new AccountInvoiceBoundaryValidation();

    @Test
    public void invoicesPageOpensAfterAuthenticatedPayment() {
        invoice.completeAuthenticatedCheckoutAndOpenInvoices();
        verify.assertInvoicesPageTitleVisible();
    }

    @Test
    public void invoicesShellHasNoTableHeadersAfterPayment() {
        invoice.completeAuthenticatedCheckoutAndOpenInvoices();
        verify.assertInvoicesShellHasNoTableHeaders();
    }

    @Test
    public void invoicesPageHasNoInvoiceNumberRowAfterPayment() {
        invoice.completeAuthenticatedCheckoutAndOpenInvoices();
        verify.assertInvoicesPageHasNoInvoiceRow();
    }

    @Test
    public void invoicesPageHasNoBillingAddressRowAfterPayment() {
        invoice.completeAuthenticatedCheckoutAndOpenInvoices();
        verify.assertInvoicesPageDoesNotExposeBillingAddressRow(invoice.getCustomer());
    }

    @Test
    public void invoicesPageHasNoOpenInvoiceActionAfterPayment() {
        invoice.completeAuthenticatedCheckoutAndOpenInvoices();
        verify.assertInvoicesPageDoesNotExposeOpenInvoiceAction();
    }
}
