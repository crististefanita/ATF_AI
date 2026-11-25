package com.ai.atf.ui.component;

import com.ai.atf.ui.component.support.StubUiEngine;
import com.ai.atf.ui.component.support.UiEngineThreadLocalTestSupport;
import com.ai.core.reporting.StepLogger;
import com.ai.ui.support.actions.AccountInvoiceActions;
import com.ai.ui.support.model.AccountInvoiceContext;
import com.ai.ui.support.model.AccountInvoiceDetailsContext;
import com.ai.ui.support.model.AdminOrderStatusContext;
import com.ai.ui.support.pages.AccountPage;
import com.ai.ui.support.validation.AdminOrderStatusValidation;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class InvoiceEvidenceContractTest {
    private String previousTimeout;
    private final InvoiceEngine engine = new InvoiceEngine();

    @BeforeMethod
    public void installEngine() {
        previousTimeout = System.getProperty("ui.wait.timeout.seconds");
        System.setProperty("ui.wait.timeout.seconds", "0");
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);
    }

    @AfterMethod(alwaysRun = true)
    public void restoreEngine() {
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(null);
        if (previousTimeout == null) System.clearProperty("ui.wait.timeout.seconds");
        else System.setProperty("ui.wait.timeout.seconds", previousTimeout);
        StepLogger.clear();
    }

    @Test
    public void listEvidenceComesFromObservedInvoiceRow() {
        engine.rowText = "INV-123  Pliers  19.95  COMPLETED";
        String observed = new AccountInvoiceActions().openInvoiceListAndReadRow("INV-123");
        Assert.assertEquals(observed, engine.rowText);
        Assert.assertTrue(engine.url.endsWith(AccountPage.INVOICES_PATH));
    }

    @Test
    public void knownCreationIdCannotReplaceMissingCustomerListEvidence() {
        engine.rowText = "";
        AccountInvoiceContext invoice = new AccountInvoiceContext(
                null, "Pliers", "19.95", "Cash on Delivery", "INV-123", "known-id");
        Assert.expectThrows(AssertionError.class,
                () -> new AccountInvoiceActions().openInvoiceDetails(invoice));
        Assert.assertFalse(engine.url.contains("known-id"),
                "Details navigation must not bypass the missing customer invoice row.");
    }

    @Test
    public void unrelatedInvoiceDoesNotSatisfyCustomerListEvidence() {
        engine.rowText = "INV-999  Pliers  19.95  COMPLETED";
        Assert.expectThrows(AssertionError.class,
                () -> new AccountInvoiceActions().openInvoiceListAndReadRow("INV-123"));
    }

    @Test
    public void invoiceNumberPrefixCannotProveAnotherInvoiceExists() {
        engine.rowText = "INV-1234  Pliers  19.95  COMPLETED";
        Assert.expectThrows(AssertionError.class,
                () -> new AccountInvoiceActions().openInvoiceListAndReadRow("INV-123"));
    }

    @Test
    public void persistedStatusMustMatchSelectedValueAfterReopen() {
        AdminOrderStatusContext context = statusContext("NEW", "INV-123 COMPLETED");
        Assert.expectThrows(AssertionError.class,
                () -> new AdminOrderStatusValidation().assertAdminStatusTransitionIsSaved(context));
    }

    @Test
    public void statusOnUnrelatedOrderCannotProveTransition() {
        AdminOrderStatusContext context = statusContext("COMPLETED", "INV-999 COMPLETED");
        Assert.expectThrows(AssertionError.class,
                () -> new AdminOrderStatusValidation().assertAdminStatusTransitionIsSaved(context));
    }

    @Test
    public void matchingReopenedStatusAndOrderRowProveTransition() {
        new AdminOrderStatusValidation().assertAdminStatusTransitionIsSaved(
                statusContext("COMPLETED", "INV-123 COMPLETED"));
    }

    @Test
    public void statusLabelAndPersistedValueRemainSeparateEvidence() {
        AdminOrderStatusContext baseline = statusContext("COMPLETED", "INV-123 COMPLETED");
        AdminOrderStatusContext context = new AdminOrderStatusContext(
                baseline.getInvoiceDetails(), "awaiting-fulfillment", baseline.getAdminListBodyBefore(),
                baseline.getAdminEditBodyBefore(), "Awaiting Fulfillment", "INV-123 Awaiting Fulfillment",
                "INV-123 Awaiting Fulfillment", baseline.getCustomerListBodyAfter(),
                baseline.getCustomerDetailsBodyAfter(), baseline.getAdminEditUrl(), baseline.getCustomerDetailsUrl(),
                "awaiting-fulfillment", "Awaiting Fulfillment");
        new AdminOrderStatusValidation().assertAdminStatusTransitionIsSaved(context);
        new AdminOrderStatusValidation().assertAdminReloginKeepsUpdatedStatus(context);
    }

    private AdminOrderStatusContext statusContext(String selectedStatus, String row) {
        AccountInvoiceContext invoice = new AccountInvoiceContext(
                null, "Pliers", "19.95", "Cash on Delivery", "INV-123", "known-id");
        AccountInvoiceDetailsContext details = new AccountInvoiceDetailsContext(
                invoice, "INV-123 19.95", "Pliers 19.95", "/account/invoices/known-id");
        return new AdminOrderStatusContext(details, "COMPLETED", "INV-123 NEW",
                "Status NEW COMPLETED", "Status NEW COMPLETED", row, row,
                details.getInvoiceListBody(), details.getDetailsBody(),
                "/admin/orders/edit/known-id", details.getDetailsUrl(), selectedStatus, "COMPLETED");
    }

    private static final class InvoiceEngine extends StubUiEngine {
        private String url = "";
        private String rowText = "";

        @Override public void open(String value) { url = value; }
        @Override public String getCurrentUrl() { return url; }
        @Override public boolean isVisible(String selector) { return !rowText.isBlank(); }
        @Override public String getText(String selector) { return rowText; }
    }
}
