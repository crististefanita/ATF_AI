package com.endava.ai.atf.ui.component;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.atf.ui.component.support.StubUiEngine;
import com.endava.ai.atf.ui.component.support.UiEngineThreadLocalTestSupport;
import com.endava.ai.ui.support.pages.LoginPage;
import com.endava.ai.ui.support.pages.RegisterPage;
import com.endava.ai.ui.support.validation.RegistrationValidation;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.time.Duration;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.REGISTRATION_VALIDATION_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.REGISTRATION_VALIDATION_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class RegistrationValidationContractTest {

    @AfterMethod
    public void tearDown() {
        StepLogger.clear();
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(null);
    }

    @Test
    public void redirected_to_login_contract_waits_for_url_and_login_button() {
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
    }

    @Test
    public void any_error_validation_retries_until_error_text_is_present() {
        SequencedTextUiEngine engine = new SequencedTextUiEngine()
                .withTextSequence(RegisterPage.ANY_ERROR, "", "", "Email already exists");
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);

        new RegistrationValidation().assertAnyErrorVisibleOrPresent();

        Assert.assertTrue(
                engine.getTextCallCount(RegisterPage.ANY_ERROR) >= 3,
                "Expected validation to poll for the generic registration error"
        );
    }

    @Test
    public void password_error_validation_waits_for_expected_message_fragment() {
        SequencedTextUiEngine engine = new SequencedTextUiEngine()
                .withTextSequence(RegisterPage.PASSWORD_ERROR, "", "Password must be longer than 6 characters");
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);

        new RegistrationValidation().assertPasswordErrorContains("longer than 6");

        Assert.assertTrue(
                engine.getTextCallCount(RegisterPage.PASSWORD_ERROR) >= 2,
                "Expected validation to retry until the password message contained the expected fragment"
        );
    }

    @Test
    public void phone_error_validation_surfaces_clear_assertion_when_expected_text_never_appears() {
        SequencedTextUiEngine engine = new SequencedTextUiEngine()
                .withTextSequence(RegisterPage.PHONE_ERROR, "", "");
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);

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
