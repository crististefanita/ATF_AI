package com.ai.atf.api.lifecycle;

import com.ai.api.core.BaseTestAPI;
import com.ai.core.support.contract.ids.AtfContractSteps;
import io.restassured.response.Response;

abstract class ApiLifecycleContractSupport extends BaseTestAPI {

    protected final void observedLifecycle(String hookPath, Integer userId, Response... responses) {
        StringBuilder details = new StringBuilder("hook.path=").append(hookPath)
                .append(" | user.id=").append(userId);
        for (int i = 0; i < responses.length; i++) {
            Response response = responses[i];
            if (response != null) {
                details.append(" | response.").append(i + 1).append(".status=")
                        .append(response.getStatusCode());
            }
        }
        AtfContractSteps.observed(details.toString());
    }
}
