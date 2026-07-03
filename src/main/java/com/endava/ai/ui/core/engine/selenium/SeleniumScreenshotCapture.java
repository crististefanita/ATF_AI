package com.endava.ai.ui.core.engine.selenium;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

final class SeleniumScreenshotCapture {
    private final WebDriver driver;

    SeleniumScreenshotCapture(WebDriver driver) {
        this.driver = driver;
    }

    String captureAsBase64() {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }
}
