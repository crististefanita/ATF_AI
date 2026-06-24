package com.endava.ai.ui.model;

public final class ContactMessageContext {
    private final CustomerData customer;
    private final String subjectLabel;
    private final String subjectSlug;
    private final String message;
    private final String listBody;
    private final String detailsBody;
    private final String detailsUrl;

    public ContactMessageContext(CustomerData customer,
                                 String subjectLabel,
                                 String subjectSlug,
                                 String message,
                                 String listBody,
                                 String detailsBody,
                                 String detailsUrl) {
        this.customer = customer;
        this.subjectLabel = subjectLabel;
        this.subjectSlug = subjectSlug;
        this.message = message;
        this.listBody = listBody;
        this.detailsBody = detailsBody;
        this.detailsUrl = detailsUrl;
    }

    public CustomerData getCustomer() {
        return customer;
    }

    public String getSubjectLabel() {
        return subjectLabel;
    }

    public String getSubjectSlug() {
        return subjectSlug;
    }

    public String getMessage() {
        return message;
    }

    public String getMessagePreview() {
        return message.substring(0, Math.min(32, message.length()));
    }

    public String getListBody() {
        return listBody;
    }

    public String getDetailsBody() {
        return detailsBody;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }
}
