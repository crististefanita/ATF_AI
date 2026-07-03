package com.endava.ai.core.reporting.aggregate.model;

import com.endava.ai.core.reporting.aggregate.AggregateStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class AggregateRuntimeCase {
    private final String className;
    private final String methodName;
    private final AggregateStatus status;
    private final long startMillis;

    public static AggregateRuntimeCase of(String className, String methodName, AggregateStatus status, long startMillis) {
        return new AggregateRuntimeCase(
                className,
                methodName,
                status,
                startMillis
        );
    }

    public String fullName() {
        return className + "#" + methodName;
    }

    public String line() {
        return status.name() + " | " + fullName();
    }
}
