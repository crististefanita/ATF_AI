package com.ai.core.reporting.utils;

public final class ConsoleLogger {
    private static final ThreadLocal<ConsoleSink> TEST_SINK = new ThreadLocal<>();

    private ConsoleLogger() {
    }

    public static void console(String msg) {
        sink().publish(msg == null ? "" : msg);
    }

    public static void setSinkForTests(ConsoleSink testSink) {
        if (testSink == null) {
            TEST_SINK.remove();
            return;
        }
        TEST_SINK.set(testSink);
    }

    public static void resetSinkForTests() {
        TEST_SINK.remove();
    }

    @FunctionalInterface
    public interface ConsoleSink {
        void publish(String message);
    }

    private static void publishToSystemOut(String message) {
        System.out.println(message);
    }

    private static ConsoleSink sink() {
        ConsoleSink testSink = TEST_SINK.get();
        return testSink == null ? ConsoleLogger::publishToSystemOut : testSink;
    }
}
