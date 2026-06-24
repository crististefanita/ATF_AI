package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.AccountInvoiceBoundaryComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.validation.AccountInvoiceBoundaryValidation;
import org.testng.annotations.Test;

public class InvoiceBoundaryRun04NavigationTests extends BaseTestUI {
    private final AccountInvoiceBoundaryComponent invoice = new AccountInvoiceBoundaryComponent();
    private final AccountInvoiceBoundaryValidation verify = new AccountInvoiceBoundaryValidation();

    @Test
    public void accountMenuExposesMyInvoicesNavigation() {
        invoice.registerLoginAndOpenAccountMenu();
        verify.assertMyInvoicesNavigationIsAvailable();
    }

    @Test
    public void myInvoicesNavigationTargetsAccountInvoicesRoute() {
        invoice.registerLoginAndOpenAccount();
        verify.assertMyInvoicesNavigationTargetsInvoices();
    }

    @Test
    public void accountInvoicesRouteOpensInvoicesShell() {
        invoice.registerLoginAndOpenAccount();
        invoice.openInvoices();
        verify.assertInvoicesPageTitleVisible();
    }

    @Test
    public void accountInvoicesShellStartsAsEmptyInvoicesArea() {
        invoice.registerLoginAndOpenAccount();
        invoice.openInvoices();
        verify.assertInvoicesPageHasNoInvoiceRow();
    }

    @Test
    public void accountInvoicesShellStartsWithoutInvoiceRow() {
        invoice.registerLoginAndOpenAccount();
        invoice.openInvoices();
        verify.assertInvoicesPageHasNoInvoiceRow();
    }
}
