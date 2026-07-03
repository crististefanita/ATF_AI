package com.ai.skills.atlas.validation;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasGeneratedOutputValidator;
import com.ai.skills.atlas.contract.AtlasContractArtifacts;
import com.ai.core.reporting.FrameworkContractReportLabels;
import com.ai.core.reporting.StepLogger;

import java.io.IOException;

final class AtlasCandidateRuleMatrixPublisher {
    private AtlasCandidateRuleMatrixPublisher() {
    }

    static void publish(AtlasGeneratedOutputValidator validator) throws IOException {
        StepLogger.startStep("Candidate rule matrix");
        AtlasContext context = AtlasContext.fromWorkingDirectory();
        logExpectedContract(context);
        AtlasRuleMatrix matrix = logObservedResult(context, validator);
        writeMatrix(matrix);
        logColdVerdict();
        StepLogger.pass("Candidate rule matrix published");
    }

    private static void logExpectedContract(AtlasContext context) {
        StepLogger.startStep(FrameworkContractReportLabels.EXPECTED_CONTRACT);
        StepLogger.logDetail(FrameworkContractReportLabels.EXPECTED_CONTRACT);
        StepLogger.logDetail("- HTML owner: " + AtlasContractArtifacts.HTML_OWNER);
        StepLogger.logDetail("- Atlas profile: atlas base + atlas candidate");
        StepLogger.logDetail("- Required artifacts: " + AtlasContractArtifacts.ATLAS_HTML
                + ", " + AtlasContractArtifacts.SHARED_CSS);
        StepLogger.logDetail("- Declared atlas artifact: " + context.atlasHtml());
        StepLogger.logDetail("- " + FrameworkContractReportLabels.EXPECTED_INVARIANT
                + ": base rules prove the atlas is valid; candidate rules prove it is teacher-grade.");
        StepLogger.pass(FrameworkContractReportLabels.EXPECTED_CONTRACT_RECORDED);
    }

    private static AtlasRuleMatrix logObservedResult(
            AtlasContext context,
            AtlasGeneratedOutputValidator validator
    ) throws IOException {
        StepLogger.startStep(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT);
        StepLogger.logDetail(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT);
        StepLogger.logDetail("- Package: " + context.projectRoot());
        StepLogger.logDetail("- Atlas: " + context.atlasHtml());
        StepLogger.logDetail("- Source package: " + context.sourceRoot());
        StepLogger.logDetail("- Requirements: " + context.requirementsHtml());
        AtlasRuleMatrix matrix = AtlasRuleMatrix.build(context, validator);
        matrix.logSummary();
        StepLogger.pass(FrameworkContractReportLabels.OBSERVED_VALIDATOR_RESULT_RECORDED);
        return matrix;
    }

    private static void writeMatrix(AtlasRuleMatrix matrix) throws IOException {
        StepLogger.startStep("Rule matrix");
        matrix.logRows();
        matrix.writeArtifacts();
        StepLogger.pass("Rule matrix recorded");
    }

    private static void logColdVerdict() {
        StepLogger.startStep(FrameworkContractReportLabels.COLD_VERDICT);
        StepLogger.logDetail(FrameworkContractReportLabels.COLD_VERDICT);
        StepLogger.logDetail("- PASS here only means the matrix was published.");
        StepLogger.logDetail("- A candidate rule may fail intentionally until the generated atlas is repaired.");
        StepLogger.pass(FrameworkContractReportLabels.COLD_VERDICT_RECORDED);
    }
}
