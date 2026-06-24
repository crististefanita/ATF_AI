package com.endava.ai.mcp.tests.latest;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
public class McpLatestPackageHtmlReportContractTest {
    @Test
    public void finalReportContainsCanonicalBusinessSections() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportContainsCanonicalBusinessSections();
    }

    @Test
    public void finalReportPublishesRuntimeConstantsAndCounters() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportPublishesRuntimeConstantsAndCounters();
    }

    @Test
    public void finalReportLinksEveryRunEvidenceFamily() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportLinksEveryRunEvidenceFamily();
    }

    @Test
    public void finalReportDoesNotLeakExternalCalibrationSources() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportDoesNotLeakExternalCalibrationSources();
    }
}
