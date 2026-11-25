package com.ai.atf.ui.component;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.TestStatus;
import com.ai.atf.ui.component.support.StubUiEngine;
import com.ai.atf.ui.component.support.UiEngineThreadLocalTestSupport;
import com.ai.ui.support.pages.LoginPage;
import com.ai.ui.support.pages.RegisterPage;
import com.ai.ui.support.validation.RegistrationValidation;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.REGISTRATION_VALIDATION_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.REGISTRATION_VALIDATION_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class RegistrationValidationContractTest {

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        StepLogger.clear();
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(null);
    }

    @Test
    public void redirected_to_login_contract_waits_for_url_and_login_button() {
        RecordingLogger audit = recordValidation();
        SequencedTextUiEngine engine = new SequencedTextUiEngine()
                .withCurrentUrl("https://practicesoftwaretesting.com/auth/login");
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);

        new RegistrationValidation().assertRedirectedToLogin();

        Assert.assertTrue(
                engine.getVisibleCallCount(LoginPage.LOGIN_BUTTON) >= 1,
                "Expected validation to inspect the login surface"
        );
        Assert.assertTrue(
                engine.getVisibleCallCount(RegisterPage.FIRST_NAME) >= 1,
                "Expected validation to distinguish login from register surface"
        );
        assertPassedAudit(audit, "registration redirects to login", "login-surface", "login-surface");
        Assert.assertTrue(audit.details.contains("currentUrl=https://practicesoftwaretesting.com/auth/login"));
    }

    @Test
    public void any_error_validation_retries_until_error_text_is_present() {
        RecordingLogger audit = recordValidation();
        SequencedTextUiEngine engine = new SequencedTextUiEngine()
                .withTextSequence(RegisterPage.ANY_ERROR, "", "", "Email already exists");
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);

        new RegistrationValidation().assertAnyErrorVisibleOrPresent();

        Assert.assertTrue(
                engine.getTextCallCount(RegisterPage.ANY_ERROR) >= 3,
                "Expected validation to poll for the generic registration error"
        );
        assertPassedAudit(audit, "registration error is present", "non-empty registration error", "Email already exists");
        Assert.assertTrue(audit.details.contains("locator=" + RegisterPage.ANY_ERROR));
    }

    @Test
    public void password_error_validation_waits_for_expected_message_fragment() {
        RecordingLogger audit = recordValidation();
        SequencedTextUiEngine engine = new SequencedTextUiEngine()
                .withTextSequence(RegisterPage.PASSWORD_ERROR, "", "Password must be longer than 6 characters");
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);

        new RegistrationValidation().assertPasswordErrorContains("longer than 6");

        Assert.assertTrue(
                engine.getTextCallCount(RegisterPage.PASSWORD_ERROR) >= 2,
                "Expected validation to retry until the password message contained the expected fragment"
        );
        assertPassedAudit(audit, "Password validation message", "longer than 6", "Password must be longer than 6 characters");
    }

    @Test
    public void phone_error_validation_surfaces_clear_assertion_when_expected_text_never_appears() {
        RecordingLogger audit = recordValidation();
        SequencedTextUiEngine engine = new SequencedTextUiEngine()
                .withTextSequence(RegisterPage.PHONE_ERROR, "", "");
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);

        try {
            AssertionError error = Assert.expectThrows(
                    AssertionError.class,
                    () -> new RegistrationValidation().assertPhoneErrorContains("Phone number is invalid", Duration.ofSeconds(1))
            );

            Assert.assertTrue(
                    error.getMessage().contains("Phone validation message"),
                    "Expected failure message to identify the phone validation contract"
            );
            Assert.assertTrue(
                    engine.getTextCallCount(RegisterPage.PHONE_ERROR) < 10,
                    "Expected negative validation contract to use a short timeout, not the global UI wait"
            );
            assertFailedAudit(audit, "Phone validation message");
            Assert.assertTrue(audit.details.contains("expected=Phone number is invalid"));
            Assert.assertTrue(audit.details.contains("actual="), "A timeout must retain the last empty observation.");
            Assert.assertTrue(audit.failures.get(0).contains(error.getMessage()), "Failure evidence must retain the original assertion.");
        } finally {
            StepLogger.clear();
        }
    }

    @Test
    public void registration_form_url_validation_distinguishes_register_from_login() {
        SequencedTextUiEngine engine = new SequencedTextUiEngine();
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);
        RecordingLogger valid = recordValidation();
        new RegistrationValidation().assertStillOnRegister();
        assertPassedAudit(valid, "registration form remains open", "URL contains /auth/register",
                "https://practicesoftwaretesting.com/auth/register");

        RecordingLogger invalid = recordValidation();
        engine.withCurrentUrl("https://practicesoftwaretesting.com/auth/login");
        try {
            AssertionError error = Assert.expectThrows(AssertionError.class,
                    () -> new RegistrationValidation().assertStillOnRegister());
            Assert.assertTrue(error.getMessage().contains("Expected to remain on /auth/register"));
            assertFailedAudit(invalid, "registration form remains open");
            Assert.assertTrue(invalid.details.contains("actual=https://practicesoftwaretesting.com/auth/login"));
        } finally {
            StepLogger.clear();
        }
    }

    @Test
    public void registration_error_surface_has_a_failed_validation_and_no_success_verdict() {
        RecordingLogger audit = recordValidation();
        SequencedTextUiEngine engine = new SequencedTextUiEngine()
                .withTextSequence(RegisterPage.ANY_ERROR, "Email already exists");
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);
        try {
            AssertionError error = Assert.expectThrows(AssertionError.class,
                    () -> new RegistrationValidation().assertRedirectedToLogin());
            Assert.assertTrue(error.getMessage().contains("register error surface"));
            assertFailedAudit(audit, "registration redirects to login");
            Assert.assertTrue(audit.details.contains("expected=login-surface"));
            Assert.assertTrue(audit.details.contains("actual=register-error"));
            Assert.assertTrue(audit.details.contains("registrationError=Email already exists"));
        } finally {
            StepLogger.clear();
        }
    }

    @Test
    public void unrelated_nonempty_error_cannot_be_reported_as_duplicate_email_acceptance() {
        RecordingLogger audit = recordValidation();
        SequencedTextUiEngine engine = new SequencedTextUiEngine()
                .withTextSequence(RegisterPage.ANY_ERROR, "Password is invalid");
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);
        try {
            AssertionError error = Assert.expectThrows(AssertionError.class,
                    () -> new RegistrationValidation().assertDuplicateEmailRejected());
            Assert.assertTrue(error.getMessage().contains("duplicate email rejection"));
            assertFailedAudit(audit, "duplicate email is rejected");
            Assert.assertTrue(audit.events.contains("pass:Validated registration error is present"),
                    "A successful precondition must remain distinct from the failed business oracle.");
            Assert.assertTrue(audit.details.contains("actual=Password is invalid"));
        } finally {
            StepLogger.clear();
        }
    }

    @Test
    public void validation_preserves_the_original_engine_assertion_and_records_no_false_pass() {
        RecordingLogger audit = recordValidation();
        AssertionError original = new AssertionError("Independent engine observation failed");
        SequencedTextUiEngine engine = new SequencedTextUiEngine().withTextFailure(original);
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);
        try {
            AssertionError observed = Assert.expectThrows(AssertionError.class,
                    () -> new RegistrationValidation().assertPhoneErrorContains("digits", Duration.ofMillis(1)));
            Assert.assertSame(observed, original, "Audit decoration must not replace the failing observation.");
            assertFailedAudit(audit, "Phone validation message");
            Assert.assertTrue(audit.details.contains("actual=<not observed>"));
            Assert.assertTrue(audit.failures.get(0).contains(original.getMessage()));
        } finally {
            StepLogger.clear();
        }
    }

    private static RecordingLogger recordValidation() {
        StepLogger.clear();
        RecordingLogger audit = new RecordingLogger();
        StepLogger.setDelegate(audit);
        return audit;
    }

    private static void assertPassedAudit(RecordingLogger audit, String description, String expected, String actual) {
        Assert.assertTrue(audit.events.contains("step:Validate " + description));
        Assert.assertTrue(audit.details.contains("expected=" + expected));
        Assert.assertTrue(audit.details.contains("actual=" + actual));
        Assert.assertEquals(audit.events.stream().filter(event -> event.equals("pass:Validated " + description)).count(), 1L);
        Assert.assertTrue(audit.failures.isEmpty());
        Assert.assertFalse(StepLogger.testHasFailed());
        Assert.expectThrows(IllegalStateException.class, () -> StepLogger.logDetail("outside completed validation"));
    }

    private static void assertFailedAudit(RecordingLogger audit, String description) {
        Assert.assertTrue(audit.events.contains("step:Validate " + description));
        Assert.assertTrue(audit.events.contains("fail:Validation failed for " + description));
        Assert.assertFalse(audit.events.contains("pass:Validated " + description));
        Assert.assertTrue(StepLogger.testHasFailed());
        Assert.expectThrows(IllegalStateException.class, () -> StepLogger.logDetail("outside failed validation"));
    }

    private static final class RecordingLogger implements ReportLogger {
        private final List<String> events = new ArrayList<>();
        private final List<String> details = new ArrayList<>();
        private final List<String> failures = new ArrayList<>();

        @Override public void startTest(String name, String description) { }
        @Override public void endTest(TestStatus status) { }
        @Override public void startStep(String title) { events.add("step:" + title); }
        @Override public void logDetail(String detail) { details.add(detail); }
        @Override public void pass(String message) { events.add("pass:" + message); }
        @Override public void fail(String message, String stacktrace) {
            events.add("fail:" + message);
            failures.add(stacktrace);
        }
        @Override public void attachScreenshotBase64(String image, String title) { }
        @Override public void logCodeBlock(String code) { }
        @Override public void flush() { }
    }

    private static final class SequencedTextUiEngine extends StubUiEngine {
        private final Map<String, Deque<String>> textSequences = new HashMap<>();
        private final Map<String, Integer> textCallCounts = new HashMap<>();
        private final Map<String, Integer> visibleCallCounts = new HashMap<>();
        private String currentUrl = "https://practicesoftwaretesting.com/auth/register";
        private String waitedUrlFragment;
        private int waitedUrlSeconds;
        private String waitedVisibleSelector;
        private int waitedVisibleSeconds;
        private AssertionError textFailure;

        private SequencedTextUiEngine withTextFailure(AssertionError failure) {
            textFailure = failure;
            return this;
        }

        private SequencedTextUiEngine withCurrentUrl(String currentUrl) {
            this.currentUrl = currentUrl;
            return this;
        }

        private SequencedTextUiEngine withTextSequence(String locator, String... values) {
            Deque<String> sequence = new ArrayDeque<>();
            for (String value : values) {
                sequence.addLast(value);
            }
            textSequences.put(locator, sequence);
            return this;
        }

        private int getTextCallCount(String locator) {
            return textCallCounts.getOrDefault(locator, 0);
        }

        private int getVisibleCallCount(String locator) {
            return visibleCallCounts.getOrDefault(locator, 0);
        }

        @Override
        public String getText(String cssSelector) {
            if (textFailure != null) throw textFailure;
            textCallCounts.merge(cssSelector, 1, Integer::sum);
            Deque<String> sequence = textSequences.get(cssSelector);
            if (sequence == null || sequence.isEmpty()) {
                return "";
            }
            if (sequence.size() > 1) {
                return sequence.removeFirst();
            }
            return sequence.peekFirst();
        }

        @Override
        public boolean isVisible(String cssSelector) {
            visibleCallCounts.merge(cssSelector, 1, Integer::sum);
            if (LoginPage.LOGIN_BUTTON.equals(cssSelector)) {
                return currentUrl.contains("/auth/login");
            }
            if (RegisterPage.FIRST_NAME.equals(cssSelector)) {
                return currentUrl.contains("/auth/register");
            }
            return true;
        }

        @Override
        public void waitForVisible(String cssSelector, int seconds) {
            waitedVisibleSelector = cssSelector;
            waitedVisibleSeconds = seconds;
        }

        @Override
        public void waitForUrlContains(String fragment, int seconds) {
            waitedUrlFragment = fragment;
            waitedUrlSeconds = seconds;
        }

        @Override
        public String getCurrentUrl() {
            return currentUrl;
        }

    }
}
