package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.ProfileAccountComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.model.CustomerData;
import com.endava.ai.ui.validation.ProfileAccountValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AccountAuthenticatedAreaTests extends BaseTestUI {
    private final ProfileAccountComponent account = new ProfileAccountComponent();
    private final ProfileAccountValidation verify = new ProfileAccountValidation();
    private CustomerData customer;

    @BeforeClass(alwaysRun = true)
    public void createCustomerProfile() {
        customer = account.registerLoginAndOpenProfile();
    }

    @Test
    public void accountOverviewIsVisibleAfterLogin() {
        account.openAccount();
        verify.assertAccountTitle();
    }

    @Test
    public void accountProfileAreaIsReachable() {
        account.openProfile();
        verify.assertProfileTitle();
    }

    @Test
    public void accountFavoritesAreaIsReachable() {
        account.openFavorites();
        verify.assertPageTitleContains("Favorites");
    }

    @Test
    public void accountInvoicesAreaIsReachable() {
        account.openInvoices();
        verify.assertPageTitleContains("Invoices");
    }

    @Test
    public void accountMessagesAreaIsReachable() {
        account.openMessages();
        verify.assertPageTitleContains("Messages");
    }
}
