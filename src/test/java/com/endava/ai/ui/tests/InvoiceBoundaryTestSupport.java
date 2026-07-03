package com.endava.ai.ui.tests;

import com.endava.ai.ui.support.component.AccountInvoiceBoundaryComponent;
import com.endava.ai.ui.core.runtime.BaseTestUI;
import com.endava.ai.ui.support.validation.AccountInvoiceBoundaryValidation;

abstract class InvoiceBoundaryTestSupport extends BaseTestUI {
    protected final AccountInvoiceBoundaryComponent invoice = new AccountInvoiceBoundaryComponent();
    protected final AccountInvoiceBoundaryValidation verify = new AccountInvoiceBoundaryValidation();
}
