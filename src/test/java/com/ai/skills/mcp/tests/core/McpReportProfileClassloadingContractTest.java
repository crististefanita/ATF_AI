package com.ai.skills.mcp.tests.core;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-report-profile-classloading-contract-test")
public class McpReportProfileClassloadingContractTest {
    private static final String[] REPORT_PROFILE_CLASSES = {
            "com.ai.skills.mcp.validation.profile.McpReportSections",
            "com.ai.skills.mcp.validation.profile.McpReportOpeningProfile",
            "com.ai.skills.mcp.validation.profile.McpReportTemplateProfile",
            "com.ai.skills.mcp.validation.report.McpReportAssertions",
            "com.ai.skills.mcp.validation.report.McpReportTemplateAssertions",
            "com.ai.skills.mcp.validation.report.McpReportBrowserAssertions",
            "com.ai.skills.mcp.validation.report.McpReportEvidenceAssertions",
            "com.ai.skills.mcp.validation.report.McpReportLinkAssertions",
            "com.ai.skills.mcp.validation.report.evidence.McpReportProofEvidenceAssertions",
            "com.ai.skills.mcp.validation.report.evidence.McpReportFrozenMcpEvidenceAssertions",
            "com.ai.skills.mcp.validation.report.evidence.McpReportRunEvidenceAssertions",
            "com.ai.skills.mcp.validation.report.evidence.McpReportGovernanceEvidenceAssertions",
            "com.ai.skills.mcp.validation.report.link.McpReportLinkProbe",
            "com.ai.skills.mcp.validation.run.extent.evidence.McpFrozenExtentReportEvidence",
            "com.ai.skills.mcp.validation.run.extent.evidence.McpFrozenGateResultEvidence",
            "com.ai.skills.mcp.validation.run.extent.html.McpHtmlTagScanner",
            "com.ai.skills.mcp.validation.run.extent.html.McpHtmlTagSnapshot",
            "com.ai.skills.mcp.validation.run.extent.html.McpScreenshotVisibility",
            "com.ai.skills.mcp.validation.run.extent.proof.McpExtentProofClass",
            "com.ai.skills.mcp.validation.run.extent.proof.McpExtentProofClassPublication"
    };

    @Test
    @McpCheckRef("mcp.report-profile.classloading.self")
    public void reportProfileClassesLoadInSelfSuite() throws ClassNotFoundException {
        for (String className : REPORT_PROFILE_CLASSES) {
            Class<?> loaded = load(className);
            Assert.assertEquals(loaded.getName(), className, "Unexpected report profile class loaded");
        }
    }

    private static Class<?> load(String className) throws ClassNotFoundException {
        ClassLoader stableLoader = McpReportProfileClassloadingContractTest.class.getClassLoader();
        try {
            return Class.forName(className, false, stableLoader);
        } catch (ClassNotFoundException stableLoaderMiss) {
            return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
        }
    }
}
