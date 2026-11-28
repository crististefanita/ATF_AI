package com.ai.api.core.client;

import com.ai.api.core.utils.ApiSleeper;

public final class ApiRetrySleeper {
    public void sleep(ApiRetryDecision decision) {
        ApiSleeper.sleep(decision.delay(), "Interrupted while waiting for API rate limit reset");
    }
}
