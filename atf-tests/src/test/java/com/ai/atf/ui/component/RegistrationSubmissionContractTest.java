package com.ai.atf.ui.component;

import com.ai.atf.ui.component.support.StubUiEngine;
import com.ai.atf.ui.component.support.UiEngineThreadLocalTestSupport;
import com.ai.core.reporting.StepLogger;
import com.ai.ui.support.pages.RegisterPage;
import com.ai.ui.support.service.RegistrationService;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class RegistrationSubmissionContractTest {
    @AfterMethod(alwaysRun = true)
    public void restoreEngine() {
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(null);
        StepLogger.clear();
    }

    @Test
    public void positiveUserSubmissionDoesNotHideFirstFormErrorWithAnotherIdentity() {
        assertOneSubmission(false);
    }

    @Test
    public void positiveCustomerSubmissionDoesNotHideFirstFormErrorWithAnotherIdentity() {
        assertOneSubmission(true);
    }

    private void assertOneSubmission(boolean customer) {
        RejectedRegistrationEngine engine = new RejectedRegistrationEngine();
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);
        RegistrationService service = new RegistrationService();
        if (customer) service.registerValidCustomer();
        else service.registerValidUser();
        Assert.assertEquals(engine.submissions, 1);
        Assert.assertEquals(engine.sessionClears, 0);
        Assert.assertEquals(engine.getText(RegisterPage.ANY_ERROR), "Registration rejected");
    }

    private static final class RejectedRegistrationEngine extends StubUiEngine {
        private int submissions;
        private int sessionClears;
        private final java.util.Map<String, String> values = new java.util.HashMap<>();
        @Override public void type(String selector, String value) { values.put(selector, value); }
        @Override public String getValue(String selector) { return values.getOrDefault(selector, ""); }
        @Override public void click(String selector) {
            if (RegisterPage.REGISTER_BUTTON.equals(selector)) submissions++;
        }
        @Override public void clearSession() { sessionClears++; }
        @Override public String getCurrentUrl() { return "/auth/register"; }
        @Override public String getText(String selector) { return "Registration rejected"; }
    }
}
