package com.ai.core.reporting.listener.internal.lifecycle;

import java.util.ArrayList;
import java.util.List;

public final class LifecycleCommandRunner {
    private LifecycleCommandRunner() {
    }

    public static List<String> executeAll(List<LifecycleCommand> commands) {
        List<String> executed = new ArrayList<>();
        for (LifecycleCommand command : commands) {
            command.execute();
            executed.add(command.name());
        }
        return List.copyOf(executed);
    }
}
