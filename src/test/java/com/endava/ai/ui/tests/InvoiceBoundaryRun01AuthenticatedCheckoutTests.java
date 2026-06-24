package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.AccountInvoiceBoundaryComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.validation.AccountInvoiceBoundaryValidation;
import org.testng.annotations.Test;

public class InvoiceBoundaryRun01AuthenticatedCheckoutTests extends BaseTestUI {
    private final AccountInvoiceBoundaryComponent invoice = new AccountInvoiceBoundaryComponent();
    private final AccountInvoiceBoundaryValidation verify = new AccountInvoiceBoundaryValidation();

    @Test
    public void authenticatedCheckoutRecognizesRegisteredCustomer() {
        invoice.completeAuthenticatedCheckoutAndRemainOnSuccess();
        verify.assertAuthenticatedCheckoutRecognizesCustomer(invoice.getCustomer());
    }

    @Test
    public void authenticatedCheckoutBypassesGuestIdentityForm() {
        invoice.completeAuthenticatedCheckoutAndRemainOnSuccess();
        verify.assertAuthenticatedCheckoutBypassesGuestIdentityForm();
    }

    @Test
    public void authenticatedCheckoutReachesBillingStep() {
        invoice.completeAuthenticatedCheckoutAndRemainOnSuccess();
        verify.assertAuthenticatedBillingStepVisible();
    }

    @Test
    public void authenticatedCashOnDeliveryShowsPaymentSuccess() {
        invoice.completeAuthenticatedCheckoutAndRemainOnSuccess();
        verify.assertPaymentSuccessVisible();
    }

    @Test
    public void authenticatedPaymentSuccessDoesNotExposeInvoiceNumber() {
        invoice.completeAuthenticatedCheckoutAndRemainOnSuccess();
        verify.assertPaymentSuccessDoesNotExposeInvoiceNumber();
    }
}
