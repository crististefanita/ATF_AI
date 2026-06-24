package com.endava.ai.ui.validation;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.ui.core.DriverManager;
import com.endava.ai.ui.pages.CheckoutPage;
import com.endava.ai.ui.pages.ProductPage;
import com.endava.ai.ui.utils.UIActions;
import com.endava.ai.ui.utils.WaitUtils;
import org.testng.Assert;

public final class CheckoutValidation {
    private void logValidation(String description) {
        StepLogger.startStep("Validate " + description);
        StepLogger.logDetail("Checkout validation emits expected/actual assertion detail through TestNG messages.");
    }

    public void assertProductDetailOwnsPliers() {
        logValidation("product detail owns Pliers");
        String productName = UIActions.getText(ProductPage.PRODUCT_NAME, "product detail name");
        Assert.assertEquals(productName, "Pliers", "Expected product detail to identify the Pliers item.");
    }

    public void assertProductPriceVisible() {
        String productPage = UIActions.getText(CheckoutPage.BODY, "product detail body");
        Assert.assertTrue(productPage.contains("$12.01"), "Expected product page to show $12.01, but was: " + productPage);
    }

    public void assertCartCounterIsOne() {
        String count = UIActions.getText(ProductPage.CART_LINK, "cart counter");
        Assert.assertEquals(count.trim(), "1", "Expected cart counter to show exactly one item.");
    }

    public void assertAddToCartSuccessMessage() {
        String body = UIActions.getText(ProductPage.CART_TOAST, "product page cart confirmation");
        Assert.assertTrue(body.contains("Product added to shopping cart."),
                "Expected add-to-cart success message, but body was: " + body);
    }

    public void assertCartLinkTargetsCheckout() {
        String href = UIActions.getAttribute(ProductPage.CART_LINK, "cart link", "href");
        Assert.assertTrue(href.endsWith("/checkout"), "Expected cart link to target /checkout, but was: " + href);
    }

    public void assertCartLineContainsPliers() {
        logValidation("cart line contains Pliers");
        String cart = UIActions.getText(CheckoutPage.CART_BODY, "cart body");
        Assert.assertTrue(cart.contains("Pliers"), "Expected cart line to contain Pliers, but was: " + cart);
    }

    public void assertCartQuantityIsOne() {
        String quantity = UIActions.getValue(CheckoutPage.PRODUCT_QUANTITY, "cart product quantity");
        Assert.assertEquals(quantity, "1", "Expected checkout cart quantity to remain one.");
    }

    public void assertCartTotalContainsProductPrice() {
        String cart = UIActions.getText(CheckoutPage.CART_BODY, "cart body");
        Assert.assertTrue(cart.contains("$12.01"), "Expected cart total to include $12.01, but was: " + cart);
    }

    public void assertCartCanProceedToCheckout() {
        Assert.assertTrue(DriverManager.getEngine().isEnabled(CheckoutPage.PROCEED_FROM_CART),
                "Expected cart Proceed to checkout button to be enabled.");
    }

    public void assertCheckoutUrlIsOpen() {
        Assert.assertTrue(DriverManager.getEngine().getCurrentUrl().contains("/checkout"),
                "Expected browser to be on /checkout, but was: " + DriverManager.getEngine().getCurrentUrl());
    }

    public void assertGuestIdentityAccepted(String email) {
        logValidation("guest identity accepted");
        String body = UIActions.getText(CheckoutPage.BODY, "checkout body after guest identity");
        Assert.assertTrue(body.contains("Continuing as guest: Fresh Checkout (" + email + ")"),
                "Expected guest identity banner with generated email, but was: " + body);
    }

    public void assertGuestProceedButtonVisible() {
        WaitUtils.waitForVisible(CheckoutPage.PROCEED_FROM_GUEST);
        Assert.assertTrue(DriverManager.getEngine().isVisible(CheckoutPage.PROCEED_FROM_GUEST),
                "Expected guest proceed button to be visible.");
    }

    public void assertGuestCheckoutKeepsCartStepHistory() {
        String body = UIActions.getText(CheckoutPage.BODY, "checkout stepper");
        Assert.assertTrue(body.contains("CART") && body.contains("SIGN IN"),
                "Expected checkout stepper to retain Cart and Sign in steps, but was: " + body);
    }

    public void assertGuestEmailValue(String email) {
        String body = UIActions.getText(CheckoutPage.BODY, "guest checkout body");
        Assert.assertTrue(body.contains(email), "Expected guest email to be visible in accepted identity banner.");
    }

    public void assertBillingAddressStepVisible() {
        WaitUtils.waitForVisible(CheckoutPage.COUNTRY);
        String body = UIActions.getText(CheckoutPage.BODY, "billing address body");
        Assert.assertTrue(body.contains("Billing Address"), "Expected Billing Address step, but was: " + body);
    }

    public void assertBillingFieldsVisible() {
        logValidation("billing fields visible");
        Assert.assertTrue(DriverManager.getEngine().isVisible(CheckoutPage.COUNTRY), "Expected country field visible.");
        Assert.assertTrue(DriverManager.getEngine().isVisible(CheckoutPage.POSTAL_CODE), "Expected postal code field visible.");
        Assert.assertTrue(DriverManager.getEngine().isVisible(CheckoutPage.STREET), "Expected street field visible.");
        Assert.assertTrue(DriverManager.getEngine().isVisible(CheckoutPage.CITY), "Expected city field visible.");
        Assert.assertTrue(DriverManager.getEngine().isVisible(CheckoutPage.STATE), "Expected state field visible.");
    }

    public void assertBillingProceedInitiallyDisabledOrBecomesEnabledAfterAddress() {
        Assert.assertTrue(DriverManager.getEngine().isVisible(CheckoutPage.PROCEED_FROM_BILLING),
                "Expected billing proceed button to be visible.");
    }

    public void assertFilledAddressEnablesProceed() {
        WaitUtils.waitForCondition(
                "billing proceed button enabled",
                () -> DriverManager.getEngine().isEnabled(CheckoutPage.PROCEED_FROM_BILLING),
                "Expected billing proceed button to become enabled after address fields are filled."
        );
        Assert.assertTrue(DriverManager.getEngine().isEnabled(CheckoutPage.PROCEED_FROM_BILLING),
                "Expected billing proceed button to be enabled after address fields are filled.");
    }

    public void assertPaymentStepVisible() {
        String body = UIActions.getText(CheckoutPage.BODY, "payment body");
        Assert.assertTrue(body.contains("Payment"), "Expected Payment step, but was: " + body);
    }

    public void assertPaymentMethodsContainCashOnDelivery() {
        String options = UIActions.getText(CheckoutPage.PAYMENT_METHOD, "payment method options");
        Assert.assertTrue(options.contains("Cash on Delivery"),
                "Expected Cash on Delivery payment option, but options were: " + options);
    }

    public void assertPaymentSelectionEnablesConfirm() {
        WaitUtils.waitForCondition(
                "finish button enabled after payment method",
                () -> DriverManager.getEngine().isEnabled(CheckoutPage.FINISH),
                "Expected Confirm button to be enabled after selecting a payment method."
        );
    }

    public void assertPaymentSuccessVisible() {
        logValidation("payment success visible");
        String body = UIActions.getText(CheckoutPage.BODY, "payment success body");
        Assert.assertTrue(body.contains("Payment was successful"),
                "Expected payment success message, but was: " + body);
    }

    public void assertPaymentSuccessDoesNotExposeReceiptId() {
        String body = UIActions.getText(CheckoutPage.BODY, "payment success body");
        Assert.assertFalse(body.matches("(?is).*\\b(order|invoice|receipt)\\s*(id|number|#)\\b.*"),
                "Payment success screen unexpectedly exposed receipt/order/invoice identity: " + body);
    }

    public void assertPaymentSuccessStillShowsCheckoutStepper() {
        String body = UIActions.getText(CheckoutPage.BODY, "payment success body");
        Assert.assertTrue(body.contains("CART") && body.contains("PAYMENT"),
                "Expected payment success to remain inside checkout stepper, but was: " + body);
    }
}

