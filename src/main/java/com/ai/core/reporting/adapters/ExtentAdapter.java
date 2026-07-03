package com.ai.core.reporting.adapters;

import com.aventstack.extentreports.ExtentTest;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.TestStatus;
import com.ai.core.reporting.adapters.extent.ExtentNodeTiming;
import com.ai.core.reporting.adapters.extent.ExtentReporter;
import com.ai.core.reporting.adapters.extent.ExtentTestContext;
import com.ai.core.support.html.HtmlText;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

public final class ExtentAdapter implements ReportLogger {
    private static final ExtentAdapter INSTANCE = new ExtentAdapter();

    private ExtentReporter reporter;
    private ExtentTestContext testContext;
    private final ThreadLocal<Deque<ExtentTest>> stepStack =
            ThreadLocal.withInitial(ArrayDeque::new);

    private ExtentAdapter() {
    }

    public static ExtentAdapter getInstance() {
        return INSTANCE;
    }

    public synchronized void resetForTests() {
        clearSteps();
        reporter = null;
        testContext = null;
    }

    @Override
    public void ensureTestStarted(String testName, String description) {
        context().start(testName, description);
    }
    @Override
    public void ensureTestStarted(String className, String testName, String description) {
        context().start(className, testName, description);
    }
    @Override
    public void startTest(String testName, String description) {
        clearSteps();
        context().end();
        ensureTestStarted(testName, description);
    }
    @Override
    public void startTest(String className, String testName, String description) {
        clearSteps();
        context().end();
        ensureTestStarted(className, testName, description);
    }
    @Override
    public void endTest(TestStatus status) {
        ExtentTest test = context().get();
        if (test != null && status == TestStatus.FAIL) {
            test.getModel().setStatus(com.aventstack.extentreports.Status.FAIL);
        }
        if (test != null && status == TestStatus.SKIP) {
            test.skip("Test skipped");
            test.getModel().setStatus(com.aventstack.extentreports.Status.SKIP);
        }
        clearSteps();
        context().end();
    }
    @Override
    public void startStep(String title) {
        ExtentTest test = context().get();
        if (test == null) return;
        ExtentTest parent = stepStack.get().peekLast();
        ExtentTest node = (parent != null ? parent : test).createNode(escapeHtml(title));
        ExtentNodeTiming.start(node);
        stepStack.get().addLast(node);
    }
    @Override
    public void logDetail(String detail) {
        ExtentTest node = getActiveNode();
        if (node == null) return;
        node.info(escapeHtml(detail));
    }
    @Override
    public void pass(String message) {
        ExtentTest test = context().get();
        if (test == null) return;
        if (message != null && !message.isEmpty()) {
            ExtentTest node = getActiveNode();
            if (node != null) {
                node.pass(escapeHtml(message));
            }
        }
        popStepIfNeeded();
    }
    @Override
    public void fail(String message, String stacktraceAsText) {
        ExtentTest test = context().get();
        if (test == null) return;
        ExtentTest node = getActiveNode();
        if (node != null) {
            reporter().fail(node, message, stacktraceAsText);
        }
        popStepIfNeeded();
    }
    @Override
    public void attachScreenshotBase64(String base64, String title) {
        ExtentTest test = context().get();
        if (test == null) return;
        reporter().attachScreenshot(test, base64, title);
    }
    @Override
    public void logCodeBlock(String content) {
        ExtentTest test = context().get();
        if (test == null || content == null || content.isBlank()) return;
        ExtentTest node = getActiveNode();
        if (node != null) {
            reporter().logCodeBlock(node, content);
        }
    }
    @Override
    public void flush() {
        reporter().flush();
    }

    public Path currentReportPath() {
        return reporter().reportPath();
    }

    public boolean hasReportContent() {
        return reporter != null && reporter.hasReportContent();
    }

    private ExtentTest getActiveNode() {
        Deque<ExtentTest> stack = stepStack.get();
        ExtentTest node = stack.peekLast();
        return node != null ? node : context().get();
    }
    private void popStepIfNeeded() {
        Deque<ExtentTest> stack = stepStack.get();
        if (!stack.isEmpty()) {
            ExtentNodeTiming.finish(stack.pollLast());
        }
    }
    private void clearSteps() {
        Deque<ExtentTest> stack = stepStack.get();
        while (!stack.isEmpty()) {
            ExtentNodeTiming.finish(stack.pollLast());
        }
        stack.clear();
        stepStack.remove();
    }
    private String escapeHtml(String value) {
        return HtmlText.html(value);
    }
    private ExtentTestContext context() {
        ensureRuntimeReporter();
        return testContext;
    }
    private ExtentReporter reporter() {
        ensureRuntimeReporter();
        return reporter;
    }
    private synchronized void ensureRuntimeReporter() {
        if (reporter == null) {
            reporter = new ExtentReporter();
            testContext = new ExtentTestContext(reporter);
        }
    }
}
