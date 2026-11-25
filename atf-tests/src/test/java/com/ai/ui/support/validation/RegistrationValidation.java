package com.ai.ui.support.validation;

import com.ai.ui.core.runtime.DriverManager;
import com.ai.core.reporting.StepLogger;
import com.ai.ui.support.pages.LoginPage;
import com.ai.ui.support.pages.RegisterPage;
import com.ai.ui.support.actions.UIActions;
import com.ai.ui.support.wait.WaitUtils;
import org.testng.Assert;

import java.time.Duration;

/**
 * Validations → assertions only.
 */
public final class RegistrationValidation {
    public void assertRedirectedToLogin() {
        auditValidation("registration redirects to login", () -> {
            StepLogger.logDetail("expected=login-surface");
            StepLogger.logDetail("locator=" + LoginPage.LOGIN_BUTTON);
            StepLogger.logDetail("locator=" + RegisterPage.FIRST_NAME);
            String[] lastError = {"<not observed>"};
            String outcome = WaitUtils.waitUntil(
                    () -> {
                        String url = DriverManager.getEngine().getCurrentUrl();
                        String body = safeText("body");
                        boolean loginVisible = safeVisible(LoginPage.LOGIN_BUTTON);
                        boolean registerStillVisible = safeVisible(RegisterPage.FIRST_NAME);
                        String errorText = safeText(RegisterPage.ANY_ERROR);
                        lastError[0] = String.valueOf(errorText);

                        if (loginVisible && !registerStillVisible
                                && (url.contains("/auth/login")
                                || body.contains("Sign in with Google")
                                || body.contains("Email address *"))) {
                            return "login-surface";
                        }

                        if (registerStillVisible
                                && url.contains("/auth/register")
                                && errorText != null
                                && !errorText.trim().isEmpty()) {
                            return "register-error";
                        }

                        return "pending";
                    },
                    value -> !"pending".equals(value),
                    Duration.ofSeconds(20),
                    Duration.ofMillis(250)
            );

            String url = DriverManager.getEngine().getCurrentUrl();
            StepLogger.logDetail("actual=" + outcome);
            StepLogger.logDetail("currentUrl=" + url);
            StepLogger.logDetail("registrationError=" + lastError[0]);

            if (!"login-surface".equals(outcome)) {
                throw new AssertionError(
                        "Expected registration success to resolve to login surface, but browser ended on a register error surface. Current URL: "
                                + url
                );
            }

            Assert.assertTrue(
                    url.contains("/auth/login") || DriverManager.getEngine().isVisible(LoginPage.LOGIN_BUTTON),
                    "Expected redirect to login surface, but was: " + url
            );
        });
    }

    public void assertStillOnRegister() {
        auditValidation("registration form remains open", () -> {
            String url = DriverManager.getEngine().getCurrentUrl();
            StepLogger.logDetail("expected=URL contains /auth/register");
            StepLogger.logDetail("actual=" + url);
            Assert.assertTrue(url.contains("/auth/register"), "Expected to remain on /auth/register, but was: " + url);
        });
    }

    public void assertAnyErrorVisibleOrPresent() {
        auditValidation("registration error is present", () -> {
            StepLogger.logDetail("locator=" + RegisterPage.ANY_ERROR);
            StepLogger.logDetail("expected=non-empty registration error");
            String[] lastObserved = {"<not observed>"};
            try {
                WaitUtils.waitForCondition(
                        "registration error is visible",
                        () -> {
                            String text = DriverManager.getEngine().getText(RegisterPage.ANY_ERROR);
                            lastObserved[0] = String.valueOf(text);
                            return text != null && !text.trim().isEmpty();
                        },
                        "Expected some validation error to be present, but none found."
                );
            } finally {
                StepLogger.logDetail("actual=" + lastObserved[0]);
            }
            String text = UIActions.getText(RegisterPage.ANY_ERROR, "Any error message container");
            StepLogger.logDetail("actual=" + text);
            Assert.assertTrue(text != null && !text.trim().isEmpty(), "Expected some validation error to be present, but none found.");
        });
    }

    public void assertErrorContains(String expectedText) {
        assertTextContains(RegisterPage.ANY_ERROR, "Any error message container", expectedText);
    }

    public void assertDuplicateEmailRejected() {
        auditValidation("duplicate email is rejected", () -> {
            StepLogger.logDetail("locator=" + RegisterPage.ANY_ERROR);
            StepLogger.logDetail("expected=email rejection mentioning exist, already, used or registered");
            assertAnyErrorVisibleOrPresent();
            String text = UIActions.getText(RegisterPage.ANY_ERROR, "Any error message container");
            StepLogger.logDetail("actual=" + text);
            String normalized = text == null ? "" : text.toLowerCase();
            Assert.assertTrue(
                    normalized.contains("email")
                            && (normalized.contains("exist")
                            || normalized.contains("already")
                            || normalized.contains("used")
                            || normalized.contains("registered")),
                    "Expected duplicate email rejection message, but was: " + text
            );
        });
    }

    public void assertRegistrationFormContains(String expectedText) {
        assertErrorContains(expectedText);
    }

    public void assertPasswordErrorContains(String expectedText) {
        assertTextContains(RegisterPage.PASSWORD_ERROR, "Password validation message", expectedText);
    }

    public void assertPhoneErrorContains(String expectedText) {
        assertPhoneErrorContains(expectedText, Duration.ofSeconds(20));
    }

    public void assertPhoneErrorContains(String expectedText, Duration timeout) {
        assertTextContains(RegisterPage.PHONE_ERROR, "Phone validation message", expectedText, timeout);
    }

    private void assertTextContains(String locator, String description, String expectedText) {
        assertTextContains(locator, description, expectedText, Duration.ofSeconds(20));
    }

    private void assertTextContains(String locator, String description, String expectedText, Duration timeout) {
        auditValidation(description, () -> {
            StepLogger.logDetail("locator=" + locator);
            StepLogger.logDetail("expected=" + expectedText);
            String[] lastObserved = {"<not observed>"};
            try {
                WaitUtils.waitForCondition(
                        description + " contains " + expectedText,
                        () -> {
                            String observed = DriverManager.getEngine().getText(locator);
                            lastObserved[0] = String.valueOf(observed);
                            return observed.contains(expectedText);
                        },
                        "Expected " + description + " to contain '" + expectedText + "'",
                        timeout
                );
            } finally {
                StepLogger.logDetail("actual=" + lastObserved[0]);
            }
            String text = UIActions.getText(locator, description);
            StepLogger.logDetail("actual=" + text);
            Assert.assertTrue(
                    text.contains(expectedText),
                    "Expected " + description + " to contain '" + expectedText + "', but was: " + text
            );
        });
    }

    private void auditValidation(String description, Runnable validation) {
        StepLogger.startStep("Validate " + description);
        try {
            validation.run();
            StepLogger.pass("Validated " + description);
        } catch (RuntimeException | AssertionError failure) {
            StepLogger.fail("Validation failed for " + description, failure);
            throw failure;
        }
    }

    private String safeText(String locator) {
        try {
            return DriverManager.getEngine().getText(locator);
        } catch (Exception ignored) {
            return "";
        }
    }

    private boolean safeVisible(String locator) {
        try {
            return DriverManager.getEngine().isVisible(locator);
        } catch (Exception ignored) {
            return false;
        }
    }
}
