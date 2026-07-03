package com.ai.ui.support.pages;

public final class AccountPage {
    private AccountPage() {
    }

    public static final String PAGE_TITLE = "[data-test='page-title']";
    public static final String NAV_MENU = "[data-test='nav-menu']";
    public static final String PROFILE_NAV = "a[data-test='nav-profile']";
    public static final String INVOICES_NAV = "a[data-test='nav-invoices']";
    public static final String SIGN_OUT = "a[data-test='nav-sign-out']";
    public static final String INVOICES_PATH = "/account/invoices";
    public static final String INVOICES_TITLE = "text=Invoices";
    public static final String INVOICE_ROWS = "tbody tr";
    public static final String INVOICE_DETAILS_BODY = "body";
    public static final String ACCOUNT_CARD = "app-overview";
    public static final String FAVORITES_LINK = "a[data-test='nav-favorites'], a[href*='favorites']";
    public static final String PROFILE_LINK = "a[data-test='nav-profile'], a[href*='profile']";
    public static final String INVOICES_LINK = "a[data-test='nav-invoices'], a[href*='invoices']";
    public static final String MESSAGES_LINK = "a[data-test='nav-messages'], a[href*='messages']";
    public static final String NAV_MY_INVOICES = "[data-test='nav-my-invoices']";
    public static final String NAV_SIGN_OUT = "[data-test='nav-sign-out']";
    public static final String BODY = "body";
    public static final String INVOICE_TABLE = "table";

    public static String invoiceDetailsPath(String invoiceId) {
        return INVOICES_PATH + "/" + invoiceId;
    }

    public static String invoiceDetailsLink(String invoiceNumber) {
        return "tbody tr:has-text('" + invoiceNumber + "') a:has-text('Details'), "
                + "tbody tr:has-text('" + invoiceNumber + "') button:has-text('Details')";
    }
}
