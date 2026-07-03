package com.endava.ai.core.reporting.adapters.extent;

import com.aventstack.extentreports.ExtentTest;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ExtentTestContext {

    private final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();
    private final ThreadLocal<ExtentTest> currentClass = new ThreadLocal<>();
    private final Map<String, ExtentTest> classNodes = new LinkedHashMap<>();
    private final ExtentReporter reporter;

    public ExtentTestContext(ExtentReporter reporter) {
        this.reporter = reporter;
    }

    public ExtentTest get() {
        return currentTest.get();
    }

    public void start(String name, String description) {
        if (currentTest.get() == null) {
            ExtentTest test = reporter.createTest(name, description);
            ExtentNodeTiming.start(test);
            currentTest.set(test);
        }
    }

    public synchronized void start(String className, String name, String description) {
        if (currentTest.get() != null) {
            return;
        }

        if (className == null || className.isBlank()) {
            start(name, description);
            return;
        }

        ExtentTest classNode = classNodes.computeIfAbsent(className, nameKey -> {
            ExtentTest node = reporter.createClassNode(nameKey);
            ExtentNodeTiming.start(node);
            return node;
        });
        currentClass.set(classNode);
        ExtentTest testNode = classNode.createNode(
                name != null ? name : "Test",
                description != null ? description : ""
        );
        ExtentNodeTiming.start(testNode);
        reporter.assignTestMetadata(testNode, className);
        currentTest.set(testNode);
    }

    public void end() {
        ExtentNodeTiming.finish(currentTest.get());
        ExtentNodeTiming.finish(currentClass.get());
        currentTest.remove();
        currentClass.remove();
    }
}
