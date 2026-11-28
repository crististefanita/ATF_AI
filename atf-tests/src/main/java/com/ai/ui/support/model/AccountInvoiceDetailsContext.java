package com.ai.ui.support.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class AccountInvoiceDetailsContext {
    private final AccountInvoiceContext invoice;
    private final String invoiceListBody;
    private final String detailsBody;
    private final String detailsUrl;
}
