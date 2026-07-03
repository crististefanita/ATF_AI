package com.ai.core.reporting.diagnostic;

import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.utils.ConsoleLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class NonBlockingDiagnostic {

    private static final List<NonBlockingWarning> WARNINGS = Collections.synchronizedList(new ArrayList<>());
    private static volatile Sink sink = StandardSink.INSTANCE;

    private NonBlockingDiagnostic() {
    }

    public static void warn(String operation, Throwable failure) {
        NonBlockingWarning warning = NonBlockingWarning.from(operation, failure);
        WARNINGS.add(warning);
        sink.publish(warning);
    }

    public static void warn(String operation, String message) {
        NonBlockingWarning warning = NonBlockingWarning.from(operation, message);
        WARNINGS.add(warning);
        sink.publish(warning);
    }

    public static void warn(
            String operation,
            Throwable failure,
            String component,
            String artifact,
            String repairHint
    ) {
        NonBlockingWarning warning = NonBlockingWarning.from(operation, failure)
                .withComponent(component)
                .withArtifact(artifact)
                .withRepairHint(repairHint);
        WARNINGS.add(warning);
        sink.publish(warning);
    }

    public static List<String> warnings() {
        synchronized (WARNINGS) {
            return WARNINGS.stream()
                    .map(NonBlockingWarning::message)
                    .collect(Collectors.toList());
        }
    }

    public static List<NonBlockingWarning> warningEvents() {
        synchronized (WARNINGS) {
            return List.copyOf(WARNINGS);
        }
    }

    public static void setSinkForTests(Sink testSink) {
        sink = testSink == null ? StandardSink.INSTANCE : testSink;
    }

    public static void resetSinkForTests() {
        sink = StandardSink.INSTANCE;
    }

    public static void clear() {
        WARNINGS.clear();
    }

    public interface Sink {
        void publish(NonBlockingWarning warning);
    }

    private enum StandardSink implements Sink {
        INSTANCE;

        @Override
        public void publish(NonBlockingWarning warning) {
            ReportLogger logger = ReportingManager.tryGetLogger();
            if (logger != null) {
                logger.logDetail("ATF warning: " + warning.message());
            }
            ConsoleLogger.console("  WARN: ATF warning: " + warning.message());
        }
    }
}
