package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.GuestCheckoutComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.validation.CheckoutValidation;
import org.testng.annotations.Test;

public class CheckoutRun02CartReviewTests extends BaseTestUI {
    private final GuestCheckoutComponent checkout = new GuestCheckoutComponent();
    private final CheckoutValidation verify = new CheckoutValidation();
    @Test
    public void checkoutCartContainsPliersLine() {
        checkout.reachCartWithProduct();
        verify.assertCartLineContainsPliers();
    }

    @Test
    public void checkoutCartKeepsSingleQuantity() {
        checkout.reachCartWithProduct();
        verify.assertCartQuantityIsOne();
    }

    @Test
    public void checkoutCartTotalContainsProductPrice() {
        checkout.reachCartWithProduct();
        verify.assertCartTotalContainsProductPrice();
    }

    @Test
    public void checkoutCartCanProceedToSignInStep() {
        checkout.reachCartWithProduct();
        verify.assertCartCanProceedToCheckout();
    }

    @Test
    public void checkoutCartUrlIsStable() {
        checkout.reachCartWithProduct();
        verify.assertCheckoutUrlIsOpen();
    }
}

