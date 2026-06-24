package com.endava.ai.mcp.tests.latest;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
public class McpLatestPackageLinkContractTest {
    @Test
    public void everyLocalHrefInFinalReportResolves() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertEveryLocalHrefInFinalReportResolves();
    }

    @Test
    public void savedLinkAuditReportsNoMissingLinks() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertSavedLinkAuditReportsNoMissingLinks();
    }
}
