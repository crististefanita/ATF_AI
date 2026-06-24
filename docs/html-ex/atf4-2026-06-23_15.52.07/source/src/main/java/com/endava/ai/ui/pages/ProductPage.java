package com.endava.ai.ui.pages;

public final class ProductPage {
    private ProductPage() {
    }

    public static final String FIRST_CATALOG_PRODUCT = "a[data-test^='product-']";
    public static final String PRODUCT_NAME = "[data-test='product-name']";
    public static final String UNIT_PRICE = "[data-test='unit-price']";
    public static final String PRODUCT_DESCRIPTION = "[data-test='product-description']";
    public static final String ADD_TO_CART = "[data-test='add-to-cart']";
    public static final String ADD_TO_FAVORITES = "[data-test='add-to-favorites']";
    public static final String CART_NAV = "a[data-test='nav-cart']";
}
