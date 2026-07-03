package com.ai.core.reporting.attachment;

import java.util.ArrayList;
import java.util.List;

public final class FinalStateAttachmentRegistry {

    private static final List<FinalStateAttachmentHandler> HANDLERS = new ArrayList<>();

    private FinalStateAttachmentRegistry() {
    }

    public static synchronized void register(FinalStateAttachmentHandler h) {
        if (h != null) {
            HANDLERS.removeIf(existing -> existing == h || existing.getClass().equals(h.getClass()));
            HANDLERS.add(h);
        }
    }

    public static void onTestSuccess() {
        for (FinalStateAttachmentHandler handler : snapshot()) {
            handler.onTestSuccess();
        }
    }

    public static synchronized void clearForTests() {
        HANDLERS.clear();
    }

    private static synchronized List<FinalStateAttachmentHandler> snapshot() {
        return List.copyOf(HANDLERS);
    }
}
