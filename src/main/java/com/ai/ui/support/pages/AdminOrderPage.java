package com.ai.ui.support.pages;

public final class AdminOrderPage {
    private AdminOrderPage() {
    }

    public static final String ORDERS_PATH = "/admin/orders";
    public static final String ORDERS_BODY = "body";
    public static final String ORDER_ROWS = "tbody tr";
    public static final String INVOICE_NUMBER = "input[data-test='invoice-number']";
    public static final String ORDER_STATUS = "select[data-test='order-status']";
    public static final String UPDATE_STATUS = "button[data-test='update-status-submit']";
    public static final String INVOICE_TOTAL = "input[data-test='invoice-total']";
    public static final String PAYMENT_METHOD = "input[data-test='payment-method']";
    public static final String BACK = "a[data-test='back']";
    public static final String ORDER_SEARCH_QUERY = "input[data-test='order-search-query']";
    public static final String ORDER_SEARCH_SUBMIT = "button[data-test='order-search-submit']";
    public static final String NEXT_PAGE = "a:has-text('\u00BB'), a[aria-label='Next'], .pagination-next a";

    public static String orderRow(String invoiceNumber) {
        return "tbody tr:has-text('" + invoiceNumber + "')";
    }

    public static String editLink(String invoiceNumber) {
        return orderRow(invoiceNumber) + " a:has-text('Edit')";
    }
}
