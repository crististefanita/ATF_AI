package com.endava.ai.skills.mcp.tests.latest;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.tests.support.McpPackageAuditAssumptions;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-latest-package-run-quality-contract-test")
public class McpLatestPackageRunQualityContractTest {
    @BeforeClass
    public void requirePackageAuditTarget() {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
    }

    @Test
    @McpCheckRef("run.window.artifacts.exist")
    public void runWindowAndRunArtifactsExist() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertRunWindowAndRunArtifactsExist();
    }

    @Test
    @McpCheckRef("run.state.business-verdict")
    public void runStatesExposeTestsAndBusinessVerdict() throws IOException {
        McpPackageAuditValidation.latestPackage().artifacts().assertRunStatesExposeTestsAndBusinessVerdict();
    }

    @Test
    @McpCheckRef("run.xml.target.no-failures")
    public void eachRunXmlMatchesRuntimeTargetAndHasNoFailures() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertEachRunXmlMatchesRuntimeTargetAndHasNoFailures();
    }

    @Test
    @McpCheckRef("run.state.target-counts")
    public void eachRunStatePublishesTargetTestsAndBusinessStatus() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertEachRunStatePublishesTargetTestsAndBusinessStatus();
    }

    @Test
    @McpCheckRef("run.score-support.delta-quality")
    public void eachRunScoreSupportMeetsRuntimeDeltaQualityWhenPublished() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertEachRunScoreSupportMeetsRuntimeDeltaQualityWhenPublished();
    }

    @Test
    @McpCheckRef("run.diff.feedback.reopenable")
    public void eachRunDiffAndFeedbackAreSubstantiveEnoughToReopen() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertEachRunDiffAndFeedbackAreSubstantiveEnoughToReopen();
    }

    @Test
    @McpCheckRef("run.diff.history.distinct")
    public void runDiffHistoryIsColdDistinct() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertRunDiffHistoryIsColdDistinct();
    }

    @Test
    @McpCheckRef("run.diff.progression.compiled")
    public void runDiffTimelineCompilesIntoRootDiff() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertRunDiffTimelineCompilesIntoRootDiff();
    }

    @Test
    @McpCheckRef("run.independence.auditable")
    public void runIndependenceIsAuditable() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertRunIndependenceIsAuditable();
    }

    @Test
    @McpCheckRef("run.xml.java-method.countability")
    public void runXmlMapsToRealJavaTestMethods() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertRunXmlMapsToRealJavaTestMethods();
    }
}
