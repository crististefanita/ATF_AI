package com.ai.core.reporting.listener.internal.lifecycle;

import java.util.Objects;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class LifecycleCommand {
    @Getter
    private final String name;
    private final Runnable action;

    private LifecycleCommand(String name, Runnable action) {
        this.name = Objects.requireNonNull(name, "name");
        this.action = Objects.requireNonNull(action, "action");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Lifecycle command name must not be blank");
        }
    }

    public static LifecycleCommand named(String name, Runnable action) {
        return new LifecycleCommand(name, action);
    }

    public void execute() {
        action.run();
    }
}
