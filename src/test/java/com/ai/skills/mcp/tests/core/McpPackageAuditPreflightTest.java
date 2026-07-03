package com.ai.skills.mcp.tests.core;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.step.StepScope;
import com.ai.skills.mcp.context.McpProjectLocator;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.tests.support.McpPackageAuditAssumptions;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-package-audit-preflight-test")
public class McpPackageAuditPreflightTest {
    @Test
    @McpCheckRef("mcp.package-audit.preflight.input-contract")
    public void reportsPackageAuditInputContractOnce(ITestContext context) {
        try (StepScope scope = StepLogger.step("MCP package-audit preflight")) {
            if (McpPackageAuditAssumptions.hasExternalPackageAuditWithoutSourceRoot()) {
                String message = McpPackageAuditAssumptions.sourceRootRequiredMessage();
                StepLogger.logDetail(message);
                scope.fail("External package audit requires explicit audited source root", null);
                Assert.fail(message);
            }
            if (McpPackageAuditAssumptions.hasExplicitPackageAudit()) {
                assertExplicitPackageAuditScheduleIsComplete(context);
                scope.pass("MCP package-audit target: " + System.getProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY));
                return;
            }
            StepLogger.logDetail(McpPackageAuditAssumptions.skipMessage());
            scope.pass("Package-audit heavy checks were not scheduled because package root is missing");
        }
    }

    private static void assertExplicitPackageAuditScheduleIsComplete(ITestContext context) {
        if (McpPackageAuditAssumptions.hasCompleteExplicitPackageAuditSchedule(context)) {
            StepLogger.logDetail("Scheduled MCP package-audit methods: " + context.getAllTestMethods().length);
            StepLogger.logDetail("Scheduled MCP package-audit classes: "
                    + McpPackageAuditAssumptions.scheduledClassNames(context));
            return;
        }

        String message = "Explicit MCP package audit must schedule the full publishability suite, not only preflight."
                + System.lineSeparator()
                + "Scheduled methods: " + context.getAllTestMethods().length
                + " / minimum " + McpPackageAuditAssumptions.MINIMUM_EXPLICIT_PACKAGE_AUDIT_METHODS
                + System.lineSeparator()
                + "Scheduled classes: " + McpPackageAuditAssumptions.scheduledClassNames(context)
                + System.lineSeparator()
                + "Missing required classes: " + McpPackageAuditAssumptions.missingExplicitAuditClasses(context);
        StepLogger.logDetail(message);
        Assert.fail(message);
    }
}
