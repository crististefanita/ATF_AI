package com.endava.ai.mcp.tests.latest;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
public class McpLatestPackageArchitectureContractTest {
    @Test
    public void generatedPackageDoesNotMutateProtectedFrameworkLayers() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertGeneratedPackageDoesNotMutateProtectedFrameworkLayers();
    }

    @Test
    public void diffTouchedUiTestsRespectLivingArchitecture() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertDiffTouchedUiTestsRespectLivingArchitecture();
    }
}
