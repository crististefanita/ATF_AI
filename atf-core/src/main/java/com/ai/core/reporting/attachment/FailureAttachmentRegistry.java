package com.ai.core.reporting.attachment;

import com.ai.core.reporting.diagnostic.NonBlockingDiagnostic;

import java.util.ArrayList;
import java.util.List;

public final class FailureAttachmentRegistry {

    private static final List<FailureAttachmentHandler> HANDLERS = new ArrayList<>();
    private static final ThreadLocal<Throwable> FAILURE = new ThreadLocal<>();

    private FailureAttachmentRegistry() {
    }

    public static synchronized void register(FailureAttachmentHandler h) {
        if (h != null) {
            HANDLERS.removeIf(existing -> existing == h || existing.getClass().equals(h.getClass()));
            HANDLERS.add(h);
        }
    }

    public static void onTestStart() {
        FAILURE.remove();
        for (FailureAttachmentHandler handler : snapshot()) {
            invoke(handler, FailureAttachmentHandler::onTestStart, "failure attachment test-start hook");
        }
    }

    public static void onTestFailure(Throwable t) {
        FAILURE.set(t);
        for (FailureAttachmentHandler handler : snapshot()) {
            invoke(handler, FailureAttachmentHandler::onTestFailure, "failure attachment capture hook");
        }
    }

    public static Throwable getFailure() {
        return FAILURE.get();
    }

    public static synchronized void clearForTests() {
        FAILURE.remove();
        HANDLERS.clear();
    }

    private static synchronized List<FailureAttachmentHandler> snapshot() {
        return List.copyOf(HANDLERS);
    }

    private static void invoke(FailureAttachmentHandler handler, HandlerCall call, String operation) {
        try {
            call.run(handler);
        } catch (Throwable failure) {
            NonBlockingDiagnostic.warn(operation + " [" + handler.getClass().getName() + "]", failure);
        }
    }

    @FunctionalInterface
    private interface HandlerCall {
        void run(FailureAttachmentHandler handler);
    }
}
