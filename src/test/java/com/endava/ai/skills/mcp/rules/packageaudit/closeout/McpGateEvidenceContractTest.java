package com.endava.ai.skills.mcp.rules.packageaudit.closeout;

import com.endava.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import org.testng.Assert;
import org.testng.annotations.Test;

public class McpGateEvidenceContractTest {
    @Test
    public void gateEvidenceClassifiesPackageCloseoutPassWithExplicitRoot() {
        McpGateEvidence evidence = McpGateEvidence.fromText(
                "Status: PASS\n"
                        + "Command: " + packageCloseoutCommandWithRoot("2026-06-29_07.50.05") + "\n"
                        + "passed=90 failures=0 errors=0",
                "2026-06-29_07.50.05");

        Assert.assertTrue(evidence.claimsPass());
        Assert.assertTrue(evidence.finalCloseoutMarker());
        Assert.assertTrue(evidence.publishableMcpMarker());
        Assert.assertFalse(evidence.subsetOnlyMarker());
        Assert.assertTrue(evidence.explicitPackageRoot());
    }

    @Test
    public void gateEvidenceRejectsLatestPackageSubsetAsPublishableCloseout() {
        McpGateEvidence evidence = McpGateEvidence.fromText(
                "result: PASS\n"
                        + "Command: mvn -q -Dtest=McpLatestPackageExtentEvidenceContractTest test\n"
                        + "package_root=2026-06-29_07.50.05",
                "2026-06-29_07.50.05");

        Assert.assertTrue(evidence.claimsPass());
        Assert.assertFalse(evidence.publishableMcpMarker());
        Assert.assertTrue(evidence.subsetOnlyMarker());
        Assert.assertTrue(evidence.explicitPackageRoot());
    }

    @Test
    public void gateEvidenceAcceptsHistoricalCompleteMcpGeneratedValidatorMarker() {
        McpGateEvidence evidence = McpGateEvidence.fromText(
                "No MCP blocker remains after complete MCP McpGeneratedPackageValidatorTest package_root=run_01",
                "run_01");

        Assert.assertTrue(evidence.claimsPass());
        Assert.assertTrue(evidence.publishableMcpMarker());
        Assert.assertFalse(evidence.finalCloseoutMarker());
        Assert.assertTrue(evidence.explicitPackageRoot());
    }

    @Test
    public void closeoutCommandProfileCentralizesFinalAndDiagnosticProfileMarkers() {
        Assert.assertEquals(
                McpCloseoutCommandProfile.fromCommand(McpExecutionProfile.PACKAGE_CLOSEOUT.mavenCommand()),
                McpCloseoutCommandProfile.PACKAGE_CLOSEOUT
        );
        Assert.assertEquals(
                McpCloseoutCommandProfile.fromCommand(McpExecutionProfile.PACKAGE_AUDIT.mavenCommand()),
                McpCloseoutCommandProfile.PACKAGE_AUDIT
        );
        Assert.assertEquals(
                McpCloseoutCommandProfile.fromCommand(McpExecutionProfile.FULL.mavenCommand()),
                McpCloseoutCommandProfile.FULL_SWEEP
        );
        Assert.assertEquals(
                McpCloseoutCommandProfile.fromCommand(McpExecutionProfile.CANDIDATE.mavenCommand()),
                McpCloseoutCommandProfile.CANDIDATE
        );
        Assert.assertTrue(McpCloseoutCommandProfile.hasFinalCloseoutEvidence("MCP package close-out profile"));
        Assert.assertTrue(McpCloseoutCommandProfile.hasNonCloseoutPrimaryEvidence("MCP Full Gate"));
        Assert.assertTrue(McpCloseoutCommandProfile.hasNonCloseoutPrimaryEvidence("McpCandidateContractTest"));
        Assert.assertFalse(McpCloseoutCommandProfile.hasNonCloseoutPrimaryEvidence("MCP package close-out profile"));
    }

    private static String packageCloseoutCommandWithRoot(String packageRoot) {
        return McpExecutionProfile.PACKAGE_CLOSEOUT.mavenCommand()
                .replace(" test", " -Dmcp.package.root=" + packageRoot + " test");
    }
}

