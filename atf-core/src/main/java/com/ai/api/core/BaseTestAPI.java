package com.ai.api.core;

import com.ai.api.core.client.ApiClient;
import com.ai.core.support.config.ConfigManager;
import com.ai.core.reporting.listener.TestListener;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

@Listeners(TestListener.class)
public abstract class BaseTestAPI {

    @BeforeClass(alwaysRun = true)
    public void baseSetup() {
        ApiClient.init();
        requireTokenForWrites();
    }

    protected void requireTokenForWrites() {
        String token = ConfigManager.get("auth.token", "");
        if (token.isBlank()) {
            throw new SkipException("auth.token is not set; GoRest write operations require a token");
        }
    }
}
