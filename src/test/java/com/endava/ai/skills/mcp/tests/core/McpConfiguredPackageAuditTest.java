package com.endava.ai.skills.mcp.tests.core;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.tests.candidate.McpCandidateContractTest;
import com.endava.ai.skills.mcp.tests.core.calibration.McpRunCalibrationContractTest;
import com.endava.ai.skills.mcp.tests.core.calibration.McpRunCountabilityRuleContractTest;
import com.endava.ai.skills.mcp.tests.core.profiles.McpTestNgProfileContractTest;
import com.endava.ai.skills.mcp.tests.core.shape.McpPackageShapeContractTest;
import com.endava.ai.skills.mcp.tests.latest.McpBusinessEvidenceContractTest;
import com.endava.ai.skills.mcp.tests.latest.McpLatestPackageArchitectureContractTest;
import com.endava.ai.skills.mcp.tests.latest.McpLatestPackageExtentEvidenceContractTest;
import com.endava.ai.skills.mcp.tests.latest.McpLatestPackageLinkContractTest;
import com.endava.ai.skills.mcp.tests.latest.McpLatestPackageRunQualityContractTest;
import com.endava.ai.skills.mcp.tests.latest.McpRuntimeAndPackageContractTest;
import com.endava.ai.skills.mcp.tests.latest.report.McpLatestPackageBusinessGraphPresentationContractTest;
import com.endava.ai.skills.mcp.tests.latest.report.McpLatestPackageHtmlReportContractTest;
import com.endava.ai.skills.mcp.tests.latest.report.McpLatestPackageReportPresentationContractTest;
import com.endava.ai.skills.mcp.tests.latest.report.McpLatestPackageScoreAndOpeningContractTest;
import com.endava.ai.skills.mcp.tests.negative.McpBusinessNegativeContractTest;
import com.endava.ai.skills.mcp.tests.negative.McpCandidateNegativeContractTest;
import com.endava.ai.skills.mcp.tests.negative.McpGeneratedPackageValidatorTest;
import com.endava.ai.skills.mcp.tests.negative.McpReportNegativeContractTest;
import com.endava.ai.skills.mcp.tests.negative.McpRuntimeNegativeContractTest;
import com.endava.ai.skills.mcp.tests.negative.McpScoreNegativeContractTest;
import com.endava.ai.skills.mcp.tests.negative.McpSourceNegativeContractTest;
import com.endava.ai.skills.mcp.tests.negative.run.McpRunDiffNegativeContractTest;
import com.endava.ai.skills.mcp.tests.negative.run.McpRunExtentNegativeContractTest;
import com.endava.ai.skills.mcp.tests.negative.run.McpRunExtentProvenanceNegativeContractTest;
import com.endava.ai.skills.mcp.tests.negative.run.McpRunLearningNegativeContractTest;
import com.endava.ai.skills.mcp.tests.negative.run.McpRunStateNegativeContractTest;
import com.endava.ai.skills.mcp.tests.support.McpPackageAuditAssumptions;
import com.endava.ai.skills.mcp.validation.McpContractMetadataValidation;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-configured-package-audit-test")
public class McpConfiguredPackageAuditTest {
    @Test
    @McpCheckRef("configured-package.no-blockers")
    public void validatesConfiguredOrLatestGeneratedPackage() throws IOException {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
        McpPackageAuditValidation.latestPackage().artifacts().assertCompletePackageHasNoNonRegenerableBlockers();
    }

    @Test
    @McpCheckRef("mcp-suite.check-metadata.coverage")
    public void latestAndCandidateSuitesExposeRepresentativeMcpChecks() {
        McpContractMetadataValidation.assertSuitesExposeRepresentativeMcpChecks("major contract",
                McpRuntimeAndPackageContractTest.class,
                McpBusinessEvidenceContractTest.class,
                McpLatestPackageArchitectureContractTest.class,
                McpLatestPackageBusinessGraphPresentationContractTest.class,
                McpLatestPackageExtentEvidenceContractTest.class,
                McpLatestPackageHtmlReportContractTest.class,
                McpLatestPackageLinkContractTest.class,
                McpLatestPackageReportPresentationContractTest.class,
                McpLatestPackageRunQualityContractTest.class,
                McpLatestPackageScoreAndOpeningContractTest.class,
                McpCandidateContractTest.class,
                McpSemanticPackageValidatorTest.class,
                McpBusinessNegativeContractTest.class,
                McpCandidateNegativeContractTest.class,
                McpGeneratedPackageValidatorTest.class,
                McpReportNegativeContractTest.class,
                McpRunStateNegativeContractTest.class,
                McpRunDiffNegativeContractTest.class,
                McpRunLearningNegativeContractTest.class,
                McpRunExtentNegativeContractTest.class,
                McpRunExtentProvenanceNegativeContractTest.class,
                McpRuntimeNegativeContractTest.class,
                McpScoreNegativeContractTest.class,
                McpSourceNegativeContractTest.class,
                McpRunCalibrationContractTest.class,
                McpRunCountabilityRuleContractTest.class,
                McpExtentScreenshotBrowserVisibilityContractTest.class,
                McpProjectLocatorContractTest.class,
                McpPackageShapeContractTest.class,
                McpReportProfileClassloadingContractTest.class,
                McpTestNgProfileContractTest.class
        );
    }
}
