package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.AccountInvoiceBoundaryComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.validation.AccountInvoiceBoundaryValidation;
import org.testng.annotations.Test;

public class InvoiceBoundaryRun05PaymentToInvoiceBoundaryTests extends BaseTestUI {
    private final AccountInvoiceBoundaryComponent invoice = new AccountInvoiceBoundaryComponent();
    private final AccountInvoiceBoundaryValidation verify = new AccountInvoiceBoundaryValidation();

    @Test
    public void paymentSuccessIsVisibleBeforeInvoiceBoundaryCheck() {
        invoice.completeAuthenticatedCheckoutAndRemainOnSuccess();
        verify.assertPaymentSuccessVisible();
    }

    @Test
    public void paymentSuccessDoesNotCarryInvoiceNumber() {
        invoice.completeAuthenticatedCheckoutAndRemainOnSuccess();
        verify.assertPaymentSuccessDoesNotExposeInvoiceNumber();
    }

    @Test
    public void invoiceBoundarySeparatesSuccessMessageFromInvoicesPage() {
        invoice.completeAuthenticatedCheckoutAndOpenInvoices();
        verify.assertInvoiceBoundarySeparatesPaymentSuccessFromAccountRecord();
    }

    @Test
    public void paidCheckoutTotalDoesNotMaterializeAsInvoiceRow() {
        invoice.completeAuthenticatedCheckoutAndOpenInvoices();
        verify.assertInvoicesPageHasNoInvoiceRow();
    }

    @Test
    public void paymentBillingAddressDoesNotMaterializeAsInvoiceRow() {
        invoice.completeAuthenticatedCheckoutAndOpenInvoices();
        verify.assertInvoicesPageDoesNotExposeBillingAddressRow(invoice.getCustomer());
    }
}
