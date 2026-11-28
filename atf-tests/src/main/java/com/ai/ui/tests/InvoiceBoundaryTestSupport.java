package com.ai.ui.tests;

import com.ai.ui.support.component.AccountInvoiceBoundaryComponent;
import com.ai.ui.core.runtime.BaseTestUI;
import com.ai.ui.support.validation.AccountInvoiceBoundaryValidation;

abstract class InvoiceBoundaryTestSupport extends BaseTestUI {
    protected final AccountInvoiceBoundaryComponent invoice = new AccountInvoiceBoundaryComponent();
    protected final AccountInvoiceBoundaryValidation verify = new AccountInvoiceBoundaryValidation();
}
