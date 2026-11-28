package com.ai.atf.core.reporting.screenshot;

import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.TestStatus;
import com.ai.ui.core.engine.UIEngine;
import com.ai.ui.core.runtime.DriverManager;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.TestResult;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class FinalScreenshotContractSupport {

    private FinalScreenshotContractSupport() {
    }

    static void setEngineForCurrentThread(UIEngine engine) {
        try {
            Field field = DriverManager.class.getDeclaredField("ENGINE");
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            ThreadLocal<UIEngine> engines = (ThreadLocal<UIEngine>) field.get(null);

            if (engine == null) {
                engines.remove();
            } else {
                engines.set(engine);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to set UI engine for tests", e);
        }
    }

    static ITestResult successResult(Class<?> owner) {
        TestResult result = TestResult.newEmptyTestResult();
        result.setStatus(ITestResult.SUCCESS);
        result.setMethod(fakeMethod(owner));
        return result;
    }

    static ITestResult failureResult(Throwable t, Class<?> owner) {
        TestResult result = TestResult.newEmptyTestResult();
        result.setStatus(ITestResult.FAILURE);
        result.setThrowable(t);
        result.setMethod(fakeMethod(owner));
        return result;
    }

    static org.testng.IInvokedMethod testInvocation() {
        org.testng.IInvokedMethod invoked = mock(org.testng.IInvokedMethod.class);
        when(invoked.isTestMethod()).thenReturn(true);
        return invoked;
    }

    private static ITestNGMethod fakeMethod(Class<?> owner) {
        ITestNGMethod method = mock(ITestNGMethod.class);
        when(method.getMethodName()).thenReturn("fakeUiTest");
        when(method.getDescription()).thenReturn("fake ui description");
        when(method.getRealClass()).thenReturn(owner);
        return method;
    }
}

final class ScreenshotObserver implements ReportLogger {

    private final List<String> screenshotTitles = new CopyOnWriteArrayList<>();

    List<String> screenshotTitles() {
        return new ArrayList<>(screenshotTitles);
    }

    @Override public void startTest(String testName, String description) {}
    @Override public void endTest(TestStatus status) {}
    @Override public void startStep(String stepTitle) {}
    @Override public void logDetail(String detail) {}
    @Override public void pass(String message) {}
    @Override public void fail(String message, String stacktraceAsText) {}

    @Override
    public void attachScreenshotBase64(String base64, String title) {
        screenshotTitles.add(title);
    }

    @Override public void logCodeBlock(String content) {}
    @Override public void flush() {}
}

final class ObserverMirrorLogger implements ReportLogger {

    private final ReportLogger active;
    private final ReportLogger observer;

    ObserverMirrorLogger(ReportLogger active, ReportLogger observer) {
        this.active = active;
        this.observer = observer;
    }

    @Override public void startTest(String testName, String description) {
        active.startTest(testName, description);
        observer.startTest(testName, description);
    }

    @Override public void startTest(String className, String testName, String description) {
        active.startTest(className, testName, description);
        observer.startTest(className, testName, description);
    }

    @Override public void ensureTestStarted(String testName, String description) {
        active.ensureTestStarted(testName, description);
        observer.ensureTestStarted(testName, description);
    }

    @Override public void ensureTestStarted(String className, String testName, String description) {
        active.ensureTestStarted(className, testName, description);
        observer.ensureTestStarted(className, testName, description);
    }

    @Override public void endTest(TestStatus status) {
        if (status != TestStatus.FAIL) {
            active.endTest(status);
        }
        observer.endTest(status);
    }

    @Override public void startStep(String stepTitle) {
        active.startStep(stepTitle);
        observer.startStep(stepTitle);
    }

    @Override public void logDetail(String detail) {
        active.logDetail(detail);
        observer.logDetail(detail);
    }

    @Override public void pass(String message) {
        active.pass(message);
        observer.pass(message);
    }

    @Override public void fail(String message, String stacktraceAsText) {
        active.logDetail("Controlled failure observed by screenshot contract observer: " + message);
        observer.fail(message, stacktraceAsText);
    }

    @Override public void attachScreenshotBase64(String base64, String title) {
        observer.attachScreenshotBase64(base64, title);
    }

    @Override public void logCodeBlock(String content) {
        active.logCodeBlock(content);
        observer.logCodeBlock(content);
    }

    @Override public void flush() {
        active.flush();
        observer.flush();
    }
}

final class FakeUiEngine implements UIEngine {

    private final String screenshot;
    private final AtomicInteger captureCalls = new AtomicInteger();

    FakeUiEngine(String screenshot) {
        this.screenshot = screenshot;
    }

    int captureCalls() {
        return captureCalls.get();
    }

    @Override public boolean supportsAutoWait() { return true; }
    @Override public void open(String url) {}
    @Override public void click(String cssSelector) {}
    @Override public void type(String cssSelector, String text) {}
    @Override public void select(String cssSelector, String valueOrText) {}
    @Override public String getText(String cssSelector) { return ""; }
    @Override public String getValue(String cssSelector) { return ""; }
    @Override public boolean isVisible(String cssSelector) { return true; }
    @Override public void waitForVisible(String cssSelector, int seconds) {}
    @Override public void waitForUrlContains(String fragment, int seconds) {}
    @Override public String getCurrentUrl() { return "about:blank"; }
    @Override public void clearSession() {}

    @Override
    public String captureScreenshotAsBase64() {
        captureCalls.incrementAndGet();
        return Base64.getEncoder().encodeToString(screenshot.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    @Override public void quit() {}
    @Override public void setWindowSize(int width, int height) {}
}
