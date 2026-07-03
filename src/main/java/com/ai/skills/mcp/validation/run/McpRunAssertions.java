package com.ai.skills.mcp.validation.run;

import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import java.io.IOException;

public final class McpRunAssertions {
    private final McpRunExtentAssertions extent;
    private final McpRunStateAssertions state;
    private final McpRunDiffAssertions diff;
    private final McpRunArchitectureAssertions architecture;

    public McpRunAssertions(McpPackageAuditValidation audit) {
        this.extent = new McpRunExtentAssertions(audit);
        this.state = new McpRunStateAssertions(audit);
        this.diff = new McpRunDiffAssertions(audit);
        this.architecture = new McpRunArchitectureAssertions(audit);
    }

    public void assertEveryRunHasExtentReport() throws IOException {
        extent.assertEveryRunHasExtentReport();
    }

    public void assertExtentReportsDoNotUseExternalScreenshotFolderReferences() throws IOException {
        extent.assertExtentReportsDoNotUseExternalScreenshotFolderReferences();
    }

    public void assertExtentReportsEmbedScreenshotsWhenUiProofClaimsScreenshots() throws IOException {
        extent.assertExtentReportsEmbedScreenshotsWhenUiProofClaimsScreenshots();
    }

    public void assertPerRunExtentReportsAreGeneratedAndSubstantive() throws IOException {
        extent.assertPerRunExtentReportsAreGeneratedAndSubstantive();
    }

    public void assertPerRunExtentReportsAreSingleFileProofsWithoutSparkAssets() throws IOException {
        extent.assertPerRunExtentReportsAreSingleFileProofsWithoutSparkAssets();
    }

    public void assertPerRunExtentReportsHaveMavenProvenanceAndHashChain() throws IOException {
        extent.assertPerRunExtentReportsHaveMavenProvenanceAndHashChain();
    }

    public void assertExtentReportsHaveNoManualProofInjection() throws IOException {
        extent.assertExtentReportsHaveNoManualProofInjection();
    }

    public void assertPerRunExtentReportsHaveNativeTestListenerProvenance() throws IOException {
        extent.assertPerRunExtentReportsHaveNativeTestListenerProvenance();
    }

    public void assertFrozenMcpExtentReportContainsReadableContractDetails() throws IOException {
        extent.assertFrozenMcpExtentReportContainsReadableContractDetails();
    }

    public void assertFrozenMcpGateResultMatchesCurrentValidatorState() throws IOException {
        extent.assertFrozenMcpGateResultMatchesCurrentValidatorState();
    }

    public void assertExtentProofClassIsPublishedAndReflectedInScoring() throws IOException {
        extent.assertExtentProofClassIsPublishedAndReflectedInScoring();
    }

    public void assertEachRunXmlMatchesRuntimeTargetAndHasNoFailures() throws IOException {
        state.assertEachRunXmlMatchesRuntimeTargetAndHasNoFailures();
    }

    public void assertEachRunStatePublishesTargetTestsAndBusinessStatus() throws IOException {
        state.assertEachRunStatePublishesTargetTestsAndBusinessStatus();
    }

    public void assertEachRunScoreSupportMeetsRuntimeDeltaQualityWhenPublished() throws IOException {
        state.assertEachRunScoreSupportMeetsRuntimeDeltaQualityWhenPublished();
    }

    public void assertRunXmlMapsToRealJavaTestMethods() throws IOException {
        state.assertRunXmlMapsToRealJavaTestMethods();
    }

    public void assertEachRunDiffAndFeedbackAreSubstantiveEnoughToReopen() throws IOException {
        diff.assertEachRunDiffAndFeedbackAreSubstantiveEnoughToReopen();
    }

    public void assertRunDiffHistoryIsColdDistinct() {
        diff.assertRunDiffHistoryIsColdDistinct();
    }

    public void assertRunDiffTimelineCompilesIntoRootDiff() {
        diff.assertRunDiffTimelineCompilesIntoRootDiff();
    }

    public void assertRunIndependenceIsAuditable() {
        diff.assertRunIndependenceIsAuditable();
    }

    public void assertDiffTouchedUiTestsRespectLivingArchitecture() throws IOException {
        architecture.assertDiffTouchedUiTestsRespectLivingArchitecture();
    }
}
