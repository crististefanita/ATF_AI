package com.endava.ai.ui.pages;

public final class CheckoutPage {
    private CheckoutPage() {
    }

    public static final String PATH = "/checkout";
    public static final String CART_ROW = "tbody tr";
    public static final String CART_TOTAL = "tbody tr:last-child td";
    public static final String PROCEED_FROM_CART = "[data-test='proceed-1']";

    public static final String GUEST_TAB = "text=Continue as Guest";
    public static final String GUEST_EMAIL = "[data-test='guest-email']";
    public static final String GUEST_FIRST_NAME = "[data-test='guest-first-name']";
    public static final String GUEST_LAST_NAME = "[data-test='guest-last-name']";
    public static final String GUEST_SUBMIT = "[data-test='guest-submit']";
    public static final String PROCEED_FROM_GUEST = "[data-test='proceed-2-guest']";
    public static final String PROCEED_FROM_SIGN_IN = "[data-test='proceed-2']";

    public static final String COUNTRY = "[data-test='country']";
    public static final String POSTAL_CODE = "[data-test='postal_code']";
    public static final String HOUSE_NUMBER = "[data-test='house_number']";
    public static final String STREET = "[data-test='street']";
    public static final String CITY = "[data-test='city']";
    public static final String STATE = "[data-test='state']";
    public static final String PROCEED_FROM_BILLING = "[data-test='proceed-3']";

    public static final String PAYMENT_METHOD = "[data-test='payment-method']";
    public static final String FINISH = "[data-test='finish']";
    public static final String CHECKOUT_BODY = "body";
}
