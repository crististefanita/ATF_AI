package com.endava.ai.ui.pages;

public final class AccountPage {
    private AccountPage() {
    }

    public static final String PROFILE_NAV = "a[data-test='nav-profile']";
    public static final String FAVORITES_NAV = "a[data-test='nav-favorites']";
    public static final String INVOICES_NAV = "a[data-test='nav-invoices']";
    public static final String NAV_MENU = "button[data-test='nav-menu']";
    public static final String MY_FAVORITES_NAV = "a[data-test='nav-my-favorites']";
    public static final String MY_INVOICES_NAV = "a[data-test='nav-my-invoices']";
    public static final String SIGN_OUT = "a[data-test='nav-sign-out']";
    public static final String INVOICES_PATH = "/account/invoices";
    public static final String INVOICES_TITLE = "text=Invoices";
    public static final String INVOICES_BODY = "body";
    public static final String INVOICE_ROWS = "tbody tr";
    public static final String INVOICE_DETAILS_BODY = "body";

    public static String invoiceDetailsLink(String invoiceNumber) {
        return "tbody tr:has-text('" + invoiceNumber + "') a:has-text('Details'), "
                + "tbody tr:has-text('" + invoiceNumber + "') button:has-text('Details')";
    }

    public static final String FIRST_NAME = "input[data-test='first-name']";
    public static final String LAST_NAME = "input[data-test='last-name']";
    public static final String EMAIL = "input[data-test='email']";
    public static final String PHONE = "input[data-test='phone']";
    public static final String STREET = "input[data-test='street']";
    public static final String POSTAL_CODE = "input[data-test='postal_code']";
    public static final String CITY = "input[data-test='city']";
    public static final String STATE = "input[data-test='state']";
    public static final String COUNTRY = "input[data-test='country'], select[data-test='country']";

    public static final String CURRENT_PASSWORD = "input[data-test='current-password']";
    public static final String NEW_PASSWORD = "input[data-test='new-password']";
    public static final String NEW_PASSWORD_CONFIRM = "input[data-test='new-password-confirm']";
    public static final String CHANGE_PASSWORD_BUTTON = "button[data-test='change-password-submit']";
}
