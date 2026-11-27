package com.ai.ui.core.engine.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Proxy;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

/** Pure capture-contract observation; no driver or application is launched. */
public class SeleniumObservationContractTest {
    @Test
    public void capturePreservesTheDriverPayloadAndItsOriginalFailure() throws Exception {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(2, 1,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        image.setRGB(1, 0, 0xff445566);
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        Assert.assertTrue(javax.imageio.ImageIO.write(image, "png", output));
        String expected = Base64.getEncoder().encodeToString(output.toByteArray());
        AtomicReference<WebDriverException> failure = new AtomicReference<>();
        WebDriver driver = (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{WebDriver.class, TakesScreenshot.class}, (proxy, method, args) -> {
                    Assert.assertEquals(method.getName(), "getScreenshotAs");
                    Assert.assertSame(args[0], OutputType.BASE64);
                    if (failure.get() != null) throw failure.get();
                    return expected;
                });
        SeleniumScreenshotCapture capture = new SeleniumScreenshotCapture(driver);
        Assert.assertEquals(capture.captureAsBase64(), expected, "The driver payload must not be replaced");
        Assert.assertEquals(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(
                Base64.getDecoder().decode(capture.captureAsBase64()))).getWidth(), 2);
        WebDriverException original = new WebDriverException("Capture unavailable");
        failure.set(original);
        Assert.assertSame(Assert.expectThrows(WebDriverException.class, capture::captureAsBase64), original);
    }
}
