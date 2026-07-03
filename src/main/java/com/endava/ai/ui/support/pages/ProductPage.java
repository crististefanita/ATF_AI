package com.endava.ai.ui.support.pages;

public final class ProductPage {
    private ProductPage() {
    }

    public static final String PRODUCT_CARD = "a[data-test^='product-']";
    public static final String PRODUCT_NAME = "[data-test='product-name']";
    public static final String PRODUCT_PRICE = "[data-test='product-price']";
    public static final String QUANTITY = "[data-test='quantity']";
    public static final String ADD_TO_CART = "[data-test='add-to-cart']";
    public static final String ADD_TO_FAVORITES = "[data-test='add-to-favorites']";
    public static final String ADD_TO_COMPARE = "[data-test='add-to-compare']";
    public static final String CART_LINK = "[data-test='nav-cart']";
    public static final String CART_TOAST = ".toast-body, .alert, body";
}
