package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.GuestCheckoutComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.validation.CheckoutValidation;
import org.testng.annotations.Test;

public class CheckoutRun01CartEntryTests extends BaseTestUI {
    private final GuestCheckoutComponent checkout = new GuestCheckoutComponent();
    private final CheckoutValidation verify = new CheckoutValidation();
    @Test
    public void productDetailIdentifiesPliersBeforeCheckout() {
        checkout.clearAndOpenProduct();
        verify.assertProductDetailOwnsPliers();
    }

    @Test
    public void productDetailShowsCheckoutPrice() {
        checkout.clearAndOpenProduct();
        verify.assertProductPriceVisible();
    }

    @Test
    public void addingPliersRaisesCartCounterToOne() {
        checkout.clearAndOpenProduct();
        checkout.addPliersToCart();
        verify.assertCartCounterIsOne();
    }

    @Test
    public void addingPliersShowsCartConfirmation() {
        checkout.clearAndOpenProduct();
        checkout.addPliersToCart();
        verify.assertAddToCartSuccessMessage();
    }

    @Test
    public void cartNavigationTargetsCheckout() {
        checkout.clearAndOpenProduct();
        checkout.addPliersToCart();
        verify.assertCartLinkTargetsCheckout();
    }
}

