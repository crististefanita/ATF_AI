package com.ai.ui.support.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class AdminOrderStatusContext {
    private final AccountInvoiceDetailsContext invoiceDetails;
    private final String targetStatus;
    private final String adminListBodyBefore;
    private final String adminEditBodyBefore;
    private final String adminEditBodyAfter;
    private final String adminListBodyAfter;
    private final String adminReloginListBodyAfter;
    private final String customerListBodyAfter;
    private final String customerDetailsBodyAfter;
    private final String adminEditUrl;
    private final String customerDetailsUrl;

    public String getInvoiceNumber() {
        return invoiceDetails.getInvoice().getInvoiceNumber();
    }

    public String getCustomerEmail() {
        return invoiceDetails.getInvoice().getCustomer().getEmail();
    }

    public String getProductName() {
        return invoiceDetails.getInvoice().getProductName();
    }

    public String getCartTotal() {
        return invoiceDetails.getInvoice().getCartTotal();
    }

}
