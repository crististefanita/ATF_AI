package com.endava.ai.ui.component;

import com.endava.ai.ui.pages.CheckoutPage;
import com.endava.ai.ui.pages.ProductPage;
import com.endava.ai.ui.utils.DataGenerator;
import com.endava.ai.ui.utils.UIActions;
import com.endava.ai.ui.utils.WaitUtils;

import java.time.Duration;

public final class GuestCheckoutComponent {
    private String guestEmail;

    public String getGuestEmail() {
        return guestEmail;
    }

    public void clearAndOpenProduct() {
        UIActions.clearSession();
        UIActions.navigateToRelative("/");
        WaitUtils.waitForVisible(ProductPage.PRODUCT_CARD);
        UIActions.clickVisible(ProductPage.PRODUCT_CARD, "available Pliers product card", 2);
        WaitUtils.waitForVisible(ProductPage.ADD_TO_CART);
    }

    public void addPliersToCart() {
        UIActions.click(ProductPage.ADD_TO_CART, "Add Pliers to cart");
        WaitUtils.waitForCondition(
                "cart counter becomes visible",
                () -> "1".equals(UIActions.getText(ProductPage.CART_LINK, "cart counter").trim()),
                "Expected cart counter to show one item."
        );
    }

    public void openCart() {
        UIActions.openRelativeWithoutUrlWait("/checkout");
        WaitUtils.waitForVisible(CheckoutPage.PROCEED_FROM_CART);
    }

    public void proceedToGuestSignIn() {
        UIActions.click(CheckoutPage.PROCEED_FROM_CART, "Proceed from cart");
        UIActions.click(CheckoutPage.GUEST_TAB, "Guest checkout tab");
        WaitUtils.waitForVisible(CheckoutPage.GUEST_EMAIL);
    }

    public void continueAsGuest() {
        guestEmail = DataGenerator.uniqueEmail();
        UIActions.type(CheckoutPage.GUEST_EMAIL, "Guest email", guestEmail);
        UIActions.type(CheckoutPage.GUEST_FIRST_NAME, "Guest first name", "Fresh");
        UIActions.type(CheckoutPage.GUEST_LAST_NAME, "Guest last name", "Checkout");
        UIActions.click(CheckoutPage.GUEST_SUBMIT, "Continue as guest");
        WaitUtils.waitForVisible(CheckoutPage.PROCEED_FROM_GUEST);
    }

    public void proceedToBilling() {
        UIActions.click(CheckoutPage.PROCEED_FROM_GUEST, "Proceed from guest identity");
        WaitUtils.waitForVisible(CheckoutPage.COUNTRY);
    }

    public void fillBillingAddress() {
        UIActions.select(CheckoutPage.COUNTRY, "Billing country", "Romania");
        UIActions.type(CheckoutPage.POSTAL_CODE, "Billing postal code", "010101");
        UIActions.type(CheckoutPage.HOUSE_NUMBER, "Billing house number", "42");
        UIActions.type(CheckoutPage.STREET, "Billing street", "Audit Street");
        UIActions.type(CheckoutPage.CITY, "Billing city", "Bucharest");
        UIActions.type(CheckoutPage.STATE, "Billing state", "Bucharest");
    }

    public void proceedToPayment() {
        UIActions.click(CheckoutPage.PROCEED_FROM_BILLING, "Proceed from billing address");
        WaitUtils.waitForVisible(CheckoutPage.PAYMENT_METHOD);
    }

    public void selectCashOnDelivery() {
        UIActions.select(CheckoutPage.PAYMENT_METHOD, "Payment method", "cash-on-delivery");
        WaitUtils.pause(Duration.ofMillis(300));
    }

    public void confirmPayment() {
        UIActions.click(CheckoutPage.FINISH, "Confirm payment");
        WaitUtils.waitForCondition(
                "payment success message appears",
                () -> UIActions.getText(CheckoutPage.BODY, "checkout body").contains("Payment was successful"),
                "Expected checkout to show payment success."
        );
    }

    public void reachCartWithProduct() {
        clearAndOpenProduct();
        addPliersToCart();
        openCart();
    }

    public void reachGuestIdentityAccepted() {
        reachCartWithProduct();
        proceedToGuestSignIn();
        continueAsGuest();
    }

    public void reachBillingAddress() {
        reachGuestIdentityAccepted();
        proceedToBilling();
    }

    public void reachPayment() {
        reachBillingAddress();
        fillBillingAddress();
        proceedToPayment();
    }

    public void reachPaymentSuccess() {
        reachPayment();
        selectCashOnDelivery();
        confirmPayment();
    }
}
