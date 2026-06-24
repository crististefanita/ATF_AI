package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.GuestCheckoutComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.validation.CheckoutValidation;
import org.testng.annotations.Test;

public class CheckoutRun05PaymentClosureTests extends BaseTestUI {
    private final GuestCheckoutComponent checkout = new GuestCheckoutComponent();
    private final CheckoutValidation verify = new CheckoutValidation();
    @Test
    public void paymentStepListsCashOnDeliveryOption() {
        checkout.reachPayment();
        verify.assertPaymentMethodsContainCashOnDelivery();
    }

    @Test
    public void selectingCashOnDeliveryEnablesConfirm() {
        checkout.reachPayment();
        checkout.selectCashOnDelivery();
        verify.assertPaymentSelectionEnablesConfirm();
    }

    @Test
    public void confirmingCashOnDeliveryShowsPaymentSuccess() {
        checkout.reachPaymentSuccess();
        verify.assertPaymentSuccessVisible();
    }

    @Test
    public void paymentSuccessRemainsInsideCheckoutStepper() {
        checkout.reachPaymentSuccess();
        verify.assertPaymentSuccessStillShowsCheckoutStepper();
    }

    @Test
    public void paymentSuccessDoesNotExposeReceiptOrderOrInvoiceIdentity() {
        checkout.reachPaymentSuccess();
        verify.assertPaymentSuccessDoesNotExposeReceiptId();
    }
}

