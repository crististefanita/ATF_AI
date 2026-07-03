package com.endava.ai.api.core.client;

import java.time.Duration;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class ApiRetryDecision {
    private final RetryOutcome outcome;
    private final Duration delay;
    private final String reason;

    private ApiRetryDecision(RetryOutcome outcome, Duration delay, String reason) {
        this.outcome = outcome;
        this.delay = delay == null ? Duration.ZERO : delay;
        this.reason = reason == null ? "" : reason;
    }

    public static ApiRetryDecision retry(Duration delay, String reason) {
        return new ApiRetryDecision(RetryOutcome.RETRY, delay, reason);
    }

    public static ApiRetryDecision complete(String reason) {
        return new ApiRetryDecision(RetryOutcome.COMPLETE, Duration.ZERO, reason);
    }

    public boolean retry() {
        return outcome == RetryOutcome.RETRY;
    }
}
