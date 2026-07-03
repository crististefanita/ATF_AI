package com.endava.ai.ui.support.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class AccountInvoiceContext {
    private final CustomerData customer;
    private final String productName;
    private final String cartTotal;
    private final String paymentMethod;
    private final String invoiceNumber;
    private final String invoiceId;
}
