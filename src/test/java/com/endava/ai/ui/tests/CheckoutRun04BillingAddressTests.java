package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.GuestCheckoutComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.validation.CheckoutValidation;
import org.testng.annotations.Test;

public class CheckoutRun04BillingAddressTests extends BaseTestUI {
    private final GuestCheckoutComponent checkout = new GuestCheckoutComponent();
    private final CheckoutValidation verify = new CheckoutValidation();
    @Test
    public void billingAddressShowsRequiredAddressFields() {
        checkout.reachBillingAddress();
        verify.assertBillingFieldsVisible();
    }

    @Test
    public void billingAddressShowsProceedControl() {
        checkout.reachBillingAddress();
        verify.assertBillingProceedInitiallyDisabledOrBecomesEnabledAfterAddress();
    }

    @Test
    public void completedBillingAddressEnablesPaymentProgression() {
        checkout.reachBillingAddress();
        checkout.fillBillingAddress();
        verify.assertFilledAddressEnablesProceed();
    }

    @Test
    public void billingAddressCanOpenPaymentStep() {
        checkout.reachPayment();
        verify.assertPaymentStepVisible();
    }

    @Test
    public void billingAddressKeepsCheckoutUrl() {
        checkout.reachBillingAddress();
        verify.assertCheckoutUrlIsOpen();
    }
}

