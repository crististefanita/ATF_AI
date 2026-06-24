package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.GuestCheckoutComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.validation.CheckoutValidation;
import org.testng.annotations.Test;

public class CheckoutRun03GuestIdentityTests extends BaseTestUI {
    private final GuestCheckoutComponent checkout = new GuestCheckoutComponent();
    private final CheckoutValidation verify = new CheckoutValidation();
    @Test
    public void guestIdentityShowsAcceptedCustomerBanner() {
        checkout.reachGuestIdentityAccepted();
        verify.assertGuestIdentityAccepted(checkout.getGuestEmail());
    }

    @Test
    public void guestIdentityKeepsGeneratedEmailVisible() {
        checkout.reachGuestIdentityAccepted();
        verify.assertGuestEmailValue(checkout.getGuestEmail());
    }

    @Test
    public void guestIdentityExposesProceedToBilling() {
        checkout.reachGuestIdentityAccepted();
        verify.assertGuestProceedButtonVisible();
    }

    @Test
    public void guestIdentityKeepsCheckoutStepperContext() {
        checkout.reachGuestIdentityAccepted();
        verify.assertGuestCheckoutKeepsCartStepHistory();
    }

    @Test
    public void guestIdentityCanEnterBillingAddressStep() {
        checkout.reachBillingAddress();
        verify.assertBillingAddressStepVisible();
    }
}

