package com.ai.core.support.contract.ids;

public final class AtfContractNarrationState {
    private static final ThreadLocal<State> CURRENT = ThreadLocal.withInitial(State::new);

    private AtfContractNarrationState() {
    }

    public static void reset() {
        CURRENT.set(new State());
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static void markAutoExpectedPublished() {
        CURRENT.get().autoExpectedPublished = true;
    }

    public static boolean autoExpectedPublished() {
        return CURRENT.get().autoExpectedPublished;
    }

    public static void markManualObservedPublished() {
        CURRENT.get().manualObservedPublished = true;
    }

    public static boolean manualObservedPublished() {
        return CURRENT.get().manualObservedPublished;
    }

    public static void markManualVerdictPublished() {
        CURRENT.get().manualVerdictPublished = true;
    }

    public static boolean manualVerdictPublished() {
        return CURRENT.get().manualVerdictPublished;
    }

    private static final class State {
        private boolean autoExpectedPublished;
        private boolean manualObservedPublished;
        private boolean manualVerdictPublished;
    }
}
