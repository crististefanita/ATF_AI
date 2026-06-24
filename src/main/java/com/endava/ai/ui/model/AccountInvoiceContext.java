package com.endava.ai.ui.model;

public final class AccountInvoiceContext {
    private final CustomerData customer;
    private final String productName;
    private final String cartTotal;
    private final String paymentMethod;
    private final String invoiceNumber;

    public AccountInvoiceContext(CustomerData customer,
                                 String productName,
                                 String cartTotal,
                                 String paymentMethod,
                                 String invoiceNumber) {
        this.customer = customer;
        this.productName = productName;
        this.cartTotal = cartTotal;
        this.paymentMethod = paymentMethod;
        this.invoiceNumber = invoiceNumber;
    }

    public CustomerData getCustomer() {
        return customer;
    }

    public String getProductName() {
        return productName;
    }

    public String getCartTotal() {
        return cartTotal;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }
}
