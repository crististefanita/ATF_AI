package com.ai.atf.ui.component;

import com.ai.atf.ui.component.support.StubUiEngine;
import com.ai.atf.ui.component.support.UiEngineThreadLocalTestSupport;
import com.ai.core.reporting.StepLogger;
import com.ai.ui.support.pages.RegisterPage;
import com.ai.ui.support.service.RegistrationService;
import com.ai.ui.support.model.CustomerData;
import com.ai.ui.support.model.RegistrationData;
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
        String email;
        String password;
        if (customer) {
            CustomerData data = service.registerValidCustomer();
            email = data.getEmail();
            password = data.getPassword();
        } else {
            RegistrationData data = service.registerValidUser();
            email = data.email();
            password = data.password();
            RegistrationData changed = data.withEmail("changed@example.com").withPostal("20002");
            Assert.assertEquals(data.email(), email, "Changing a copy must preserve the original identity");
            Assert.assertEquals(data.postal(), "10001");
            Assert.assertEquals(changed.email(), "changed@example.com");
            Assert.assertEquals(changed.postal(), "20002");
            Assert.assertEquals(changed.firstName(), "John");
            Assert.assertEquals(changed.password(), password);
        }
        Assert.assertEquals(engine.submissions, 1);
        Assert.assertEquals(engine.sessionClears, 0);
        Assert.assertEquals(engine.getText(RegisterPage.ANY_ERROR), "Registration rejected");
        Assert.assertTrue(email.matches("user_[a-z0-9]{10}@example\\.com"),
                "Repair hint: generated shape is checked; global uniqueness is not established");
        Assert.assertTrue(password.matches("Aa1@[A-Za-z0-9]{8}"),
                "Repair hint: retain the required password categories and length without logging its value");
        java.util.Map<String, String> expected = new java.util.HashMap<>();
        expected.put(RegisterPage.FIRST_NAME, "John");
        expected.put(RegisterPage.LAST_NAME, "Doe");
        expected.put(RegisterPage.DOB, "1990-01-01");
        expected.put(RegisterPage.STREET, "Main Street 1");
        expected.put(RegisterPage.POSTAL, "10001");
        expected.put(RegisterPage.HOUSE_NUMBER, "5");
        expected.put(RegisterPage.CITY, "Bucharest");
        expected.put(RegisterPage.STATE, "B");
        expected.put(RegisterPage.COUNTRY, "RO");
        expected.put(RegisterPage.PHONE, "0712345678");
        expected.put(RegisterPage.EMAIL, email);
        expected.put(RegisterPage.PASSWORD, password);
        Assert.assertEquals(engine.values.keySet(), expected.keySet(),
                "Repair hint: both factory routes must submit the complete maintained flat resource fixture");
        for (String selector : expected.keySet()) {
            Assert.assertTrue(java.util.Objects.equals(engine.values.get(selector), expected.get(selector)),
                    "Repair hint: the submitted field differs from its expected value at " + selector);
        }
    }

    private static final class RejectedRegistrationEngine extends StubUiEngine {
        private int submissions;
        private int sessionClears;
        private final java.util.Map<String, String> values = new java.util.HashMap<>();
        @Override public void type(String selector, String value) { values.put(selector, value); }
        @Override public void select(String selector, String value) { values.put(selector, value); }
        @Override public String getValue(String selector) { return values.getOrDefault(selector, ""); }
        @Override public void click(String selector) {
            if (RegisterPage.REGISTER_BUTTON.equals(selector)) submissions++;
        }
        @Override public void clearSession() { sessionClears++; }
        @Override public String getCurrentUrl() { return "/auth/register"; }
        @Override public String getText(String selector) { return "Registration rejected"; }
    }
}
