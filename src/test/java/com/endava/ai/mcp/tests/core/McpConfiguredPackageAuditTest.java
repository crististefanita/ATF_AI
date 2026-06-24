package com.endava.ai.mcp.tests.core;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
public class McpConfiguredPackageAuditTest {
    @Test
    public void validatesConfiguredOrLatestGeneratedPackage() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertCompletePackageHasNoBlockers();
    }
}
