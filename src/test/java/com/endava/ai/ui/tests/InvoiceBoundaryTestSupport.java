package com.endava.ai.ui.tests;

import com.endava.ai.ui.component.AccountInvoiceBoundaryComponent;
import com.endava.ai.ui.core.BaseTestUI;
import com.endava.ai.ui.validation.AccountInvoiceBoundaryValidation;

abstract class InvoiceBoundaryTestSupport extends BaseTestUI {
    protected final AccountInvoiceBoundaryComponent invoice = new AccountInvoiceBoundaryComponent();
    protected final AccountInvoiceBoundaryValidation verify = new AccountInvoiceBoundaryValidation();
}
