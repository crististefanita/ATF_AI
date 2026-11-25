package com.ai.ui.support.validation;

import com.ai.core.reporting.StepLogger;
import com.ai.ui.core.runtime.DriverManager;
import com.ai.ui.support.model.CustomerData;
import com.ai.ui.support.pages.AccountPage;
import com.ai.ui.support.pages.ProfilePage;
import com.ai.ui.support.actions.UIActions;
import com.ai.ui.support.wait.WaitUtils;
import org.testng.Assert;

public final class ProfileAccountValidation {
    public void assertProfileIdentity(CustomerData data) {
        assertValue(ProfilePage.FIRST_NAME, "profile first name", data.getFirstName());
        assertValue(ProfilePage.LAST_NAME, "profile last name", data.getLastName());
        assertValue(ProfilePage.EMAIL, "profile email", data.getEmail());
    }

    public void assertProfileContact(CustomerData data) {
        assertValue(ProfilePage.PHONE, "profile phone", data.getPhone());
        assertValue(ProfilePage.STREET, "profile street", data.getStreet());
    }

    public void assertProfileAddress(CustomerData data) {
        assertValue(ProfilePage.POSTAL_CODE, "profile postal code", data.getPostalCode());
        assertValue(ProfilePage.CITY, "profile city", data.getCity());
        assertValue(ProfilePage.STATE, "profile state", data.getState());
        assertValue(ProfilePage.COUNTRY, "profile country", data.getCountry());
    }

    public void assertProfileTitle() {
        assertTextContains(ProfilePage.PAGE_TITLE, "profile page title", "Profile");
    }

    public void assertAccountTitle() {
        assertTextContains(AccountPage.PAGE_TITLE, "account page title", "My account");
    }

    public void assertNavMenuNamesCustomer(CustomerData data) {
        assertTextContains(AccountPage.NAV_MENU, "authenticated nav menu", data.getFirstName());
        assertTextContains(AccountPage.NAV_MENU, "authenticated nav menu", data.getLastName());
    }

    public void assertPasswordControlsVisible() {
        assertPresent(ProfilePage.CURRENT_PASSWORD, "current password input");
        assertPresent(ProfilePage.NEW_PASSWORD, "new password input");
        assertPresent(ProfilePage.NEW_PASSWORD_CONFIRM, "confirm password input");
        assertPresent(ProfilePage.CHANGE_PASSWORD, "change password button");
    }

    public void assertTotpControlsVisible() {
        assertTextNotBlank(ProfilePage.TOTP_SECRET, "TOTP secret");
        assertPresent(ProfilePage.TOTP_CODE, "TOTP code input");
        assertPresent(ProfilePage.VERIFY_TOTP, "verify TOTP button");
    }

    public void assertUpdateProfileAvailable() {
        assertPresent(ProfilePage.UPDATE_PROFILE, "update profile button");
    }

    public void assertPageTitleContains(String expected) {
        assertTextContains(AccountPage.PAGE_TITLE, "account area page title", expected);
    }

    private void assertValue(String locator, String description, String expected) {
        StepLogger.startStep("Validate " + description);
        try {
            WaitUtils.waitForInputValue(locator, description + " equals expected value", expected::equals);
            String actual = UIActions.getValue(locator, description);
            StepLogger.logDetail("expected=" + expected);
            StepLogger.logDetail("actual=" + actual);
            Assert.assertEquals(actual, expected, "Unexpected " + description);
            StepLogger.pass("Validated " + description);
        } catch (RuntimeException | AssertionError error) {
            StepLogger.fail("Validation failed for " + description, error);
            throw error;
        }
    }

    private void assertTextContains(String locator, String description, String expected) {
        StepLogger.startStep("Validate " + description);
        try {
            WaitUtils.waitForCondition(
                    description + " contains " + expected,
                    () -> DriverManager.getEngine().getText(locator).contains(expected),
                    "Expected " + description + " to contain " + expected
            );
            String text = UIActions.getText(locator, description);
            StepLogger.logDetail("expected fragment=" + expected);
            StepLogger.logDetail("actual text=" + text);
            Assert.assertTrue(text.contains(expected), "Expected " + description + " to contain " + expected + ", but was: " + text);
            StepLogger.pass("Validated " + description);
        } catch (RuntimeException | AssertionError error) {
            StepLogger.fail("Validation failed for " + description, error);
            throw error;
        }
    }

    private void assertTextNotBlank(String locator, String description) {
        StepLogger.startStep("Validate " + description);
        try {
            WaitUtils.waitForCondition(
                    description + " is not blank",
                    () -> !DriverManager.getEngine().getText(locator).trim().isEmpty(),
                    "Expected non-empty " + description
            );
            String text = UIActions.getText(locator, description);
            StepLogger.logDetail("actual text=" + text);
            Assert.assertFalse(text.trim().isEmpty(), "Expected non-empty " + description);
            StepLogger.pass("Validated " + description);
        } catch (RuntimeException | AssertionError error) {
            StepLogger.fail("Validation failed for " + description, error);
            throw error;
        }
    }

    private void assertPresent(String locator, String description) {
        StepLogger.startStep("Validate visible " + description);
        try {
            String value = UIActions.getAttribute(locator, description, "data-test");
            StepLogger.logDetail("data-test=" + value);
            Assert.assertNotNull(value, "Expected visible " + description);
            StepLogger.pass("Validated visible " + description);
        } catch (RuntimeException | AssertionError error) {
            StepLogger.fail("Validation failed for " + description, error);
            throw error;
        }
    }
}
