package com.ai.skills.mcp.tests.latest;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.tests.support.McpPackageAuditAssumptions;
import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-latest-package-link-contract-test")
public class McpLatestPackageLinkContractTest {
    @BeforeClass
    public void requirePackageAuditTarget() {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
    }

    @Test
    @McpCheckRef("report.links.local-hrefs-resolve")
    public void finalReportLocalHyperlinksResolve() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertFinalReportLocalHyperlinksResolve();
    }

    @Test
    public void everyLocalHrefInFinalReportResolves() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertEveryLocalHrefInFinalReportResolves();
    }

    @Test
    public void savedLinkAuditReportsNoMissingLinks() throws IOException {
        McpPackageAuditValidation.latestPackage().report().assertSavedLinkAuditReportsNoMissingLinks();
    }
}
