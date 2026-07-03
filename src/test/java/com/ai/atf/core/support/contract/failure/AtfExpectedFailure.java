package com.ai.atf.core.support.contract.failure;

import com.ai.core.support.contract.ids.AtfContractReportLabels;
import com.ai.core.reporting.StepLogger;
import org.testng.Assert;

public final class AtfExpectedFailure {

    private AtfExpectedFailure() {
    }

    public static <T extends Throwable> T expect(String contractName,
                                                 Class<T> expectedType,
                                                 ThrowingRunnable action,
                                                 String expectedMessageFragment) {
        StepLogger.startStep(AtfContractReportLabels.EXPECTED_NEGATIVE_CONTRACT);
        StepLogger.logDetail("Contract: " + contractName);
        StepLogger.logDetail("Expected exception: " + expectedType.getSimpleName());
        StepLogger.logDetail("Expected signal: " + expectedMessageFragment);
        StepLogger.pass("Expected controlled failure documented");

        T thrown = Assert.expectThrows(expectedType, () ->
                StepLogger.withExpectedFailureLogging(() -> runUnchecked(action))
        );

        StepLogger.startStep(AtfContractReportLabels.OBSERVED_CONTROLLED_FAILURE);
        StepLogger.logDetail("Observed exception: " + thrown.getClass().getSimpleName());
        StepLogger.logDetail("Observed message: " + thrown.getMessage());
        Assert.assertTrue(
                thrown.getMessage() != null && thrown.getMessage().contains(expectedMessageFragment),
                "Expected controlled failure message to contain: " + expectedMessageFragment
        );
        StepLogger.pass("Controlled failure matched the expected signal");

        StepLogger.startStep(AtfContractReportLabels.COLD_NEGATIVE_VERDICT);
        StepLogger.logDetail("PASS means the expected negative signal was produced and audited.");
        StepLogger.logDetail("It does not mean the underlying negative business action succeeded.");
        StepLogger.pass("Controlled negative failure converted into a green contract result");

        return thrown;
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    private static void runUnchecked(ThrowingRunnable action) {
        try {
            action.run();
        } catch (Throwable e) {
            sneakyThrow(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable throwable) throws E {
        throw (E) throwable;
    }
}
