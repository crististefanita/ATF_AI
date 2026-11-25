package com.ai.ui.support.pages;

public final class ContactPage {
    private ContactPage() {
    }

    public static final String CONTACT_PATH = "/contact";
    public static final String SUBJECT = "[data-test='subject']";
    public static final String MESSAGE = "[data-test='message']";
    public static final String SUBMIT = "[data-test='contact-submit']";
    public static final String BODY = "body";
    public static final String PAGE_HEADING = "h3";
    public static final String FIRST_NAME = "input[data-test='first-name']";
    public static final String LAST_NAME = "input[data-test='last-name']";
    public static final String EMAIL = "input[data-test='email']";
    public static final String SUBJECT_OPTIONS = "select[data-test='subject'] option";
    public static final String ATTACHMENT = "input[data-test='attachment']";
    public static final String SEND_BUTTON = SUBMIT;
    public static final String PAGE_BODY = BODY;
}
