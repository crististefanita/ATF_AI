package com.ai.ui.core.engine.playwright;

import com.ai.ui.core.engine.UiNetworkResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.PlaywrightException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Proxy;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/** SDK-boundary components; no browser, server or application is started. */
public class PlaywrightObservationContractTest {
    @Test
    public void responseProjectionRetainsStatusAndBoundsPreviewsWithoutInventingSuccess() {
        AtomicReference<RuntimeException> bodyFailure = new AtomicReference<>();
        AtomicInteger triggers = new AtomicInteger();
        Request request = (Request) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{Request.class}, (proxy, method, args) -> {
                    if ("method".equals(method.getName())) return "post";
                    if ("postData".equals(method.getName())) return "  request\n  detail  ";
                    throw new AssertionError("Unexpected request operation: " + method.getName());
                });
        Response response = response("https://contract.invalid/observed", request, bodyFailure);
        Page page = (Page) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Page.class},
                (proxy, method, args) -> {
                    Assert.assertEquals(method.getName(), "waitForResponse");
                    @SuppressWarnings("unchecked") Predicate<Response> predicate = (Predicate<Response>) args[0];
                    Assert.assertFalse(predicate.test(response("https://contract.invalid/unrelated", request, bodyFailure)));
                    Request wrongMethod = (Request) Proxy.newProxyInstance(getClass().getClassLoader(),
                            new Class<?>[]{Request.class}, (candidate, operation, arguments) -> {
                                Assert.assertEquals(operation.getName(), "method");
                                return "GET";
                            });
                    Assert.assertFalse(predicate.test(response("https://contract.invalid/observed", wrongMethod, bodyFailure)));
                    Assert.assertTrue(predicate.test(response));
                    ((Runnable) args[1]).run();
                    return response;
                });
        PlaywrightNetworkWaiter waiter = new PlaywrightNetworkWaiter(page);
        UiNetworkResponse observed = waiter.waitForResponse("/observed", "POST", triggers::incrementAndGet);
        Assert.assertEquals(triggers.get(), 1);
        Assert.assertEquals(observed.getUrl(), "https://contract.invalid/observed");
        Assert.assertEquals(observed.getMethod(), "POST");
        Assert.assertEquals(observed.getStatus(), 503);
        Assert.assertFalse(observed.isSuccess(), "An observed server error is not a successful response");
        Assert.assertEquals(observed.getRequestPreview(), "request detail");
        Assert.assertEquals(observed.getBodyPreview(), "x".repeat(2000) + "...");
        bodyFailure.set(new PlaywrightException("Body unavailable"));
        Assert.assertEquals(waiter.waitForResponse("/observed", "POST", () -> {}).getBodyPreview(), "",
                "The documented SDK body-read failure leaves an empty preview");
        IllegalStateException original = new IllegalStateException("Unexpected body failure");
        bodyFailure.set(original);
        Assert.assertSame(Assert.expectThrows(IllegalStateException.class,
                () -> waiter.waitForResponse("/observed", "POST", () -> {})), original,
                "Other failures must not be converted into a successful empty response");
    }

    @Test
    public void fullPageCapturePreservesImageBytesAndOriginalFailure() throws Exception {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(2, 1,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0xff112233);
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        Assert.assertTrue(javax.imageio.ImageIO.write(image, "png", output));
        byte[] expected = output.toByteArray();
        AtomicReference<RuntimeException> failure = new AtomicReference<>();
        Page page = (Page) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Page.class},
                (proxy, method, args) -> {
                    Assert.assertEquals(method.getName(), "screenshot");
                    Assert.assertEquals(((Page.ScreenshotOptions) args[0]).fullPage, Boolean.TRUE);
                    if (failure.get() != null) throw failure.get();
                    return expected;
                });
        PlaywrightScreenshotCapture capture = new PlaywrightScreenshotCapture(page);
        byte[] actual = Base64.getDecoder().decode(capture.captureFullPageAsBase64());
        Assert.assertEquals(actual, expected, "The adapter must preserve the captured image bytes");
        Assert.assertEquals(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(actual)).getWidth(), 2);
        PlaywrightException original = new PlaywrightException("Capture unavailable");
        failure.set(original);
        Assert.assertSame(Assert.expectThrows(PlaywrightException.class, capture::captureFullPageAsBase64), original);
    }

    private Response response(String url, Request request, AtomicReference<RuntimeException> failure) {
        return (Response) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Response.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "url": return url;
                        case "request": return request;
                        case "status": return 503;
                        case "text":
                            if (failure.get() != null) throw failure.get();
                            return "  " + "x".repeat(2001) + "  ";
                        default: throw new AssertionError("Unexpected response operation: " + method.getName());
                    }
                });
    }
}
