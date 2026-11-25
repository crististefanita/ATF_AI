package com.ai.ui.core.engine.selenium;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;

final class SeleniumSessionCleaner {
    private static final String CLEAR_SESSION_SCRIPT =
            "window.localStorage.clear(); window.sessionStorage.clear();";

    private final WebDriver driver;

    SeleniumSessionCleaner(WebDriver driver) {
        this.driver = driver;
    }

    void clearSession() {
        driver.manage().deleteAllCookies();
        try {
            ((JavascriptExecutor) driver).executeScript(CLEAR_SESSION_SCRIPT);
        } catch (WebDriverException ignored) {
        }
    }
}
