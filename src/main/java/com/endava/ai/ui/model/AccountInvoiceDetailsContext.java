package com.endava.ai.ui.model;

public final class AccountInvoiceDetailsContext {
    private final AccountInvoiceContext invoice;
    private final String invoiceListBody;
    private final String detailsBody;
    private final String detailsUrl;

    public AccountInvoiceDetailsContext(AccountInvoiceContext invoice,
                                        String invoiceListBody,
                                        String detailsBody,
                                        String detailsUrl) {
        this.invoice = invoice;
        this.invoiceListBody = invoiceListBody;
        this.detailsBody = detailsBody;
        this.detailsUrl = detailsUrl;
    }

    public AccountInvoiceContext getInvoice() {
        return invoice;
    }

    public String getInvoiceListBody() {
        return invoiceListBody;
    }

    public String getDetailsBody() {
        return detailsBody;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }
}
