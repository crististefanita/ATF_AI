package com.endava.ai.core.reporting.listener.internal.lifecycle;

import com.endava.ai.core.reporting.listener.internal.contract.AtfContractReportNarrator;
import com.endava.ai.core.support.contract.ids.AtfContractNarrationState;
import org.testng.ITestResult;

public final class AtfContractNarrationHandler {
    public void beforeTestInvocation(ITestResult result) {
        AtfContractNarrationState.reset();
        AtfContractReportNarrator.logExpected(result);
    }

    public void afterTestInvocation(ITestResult result) {
        if (result == null) {
            return;
        }
        try {
            if (result.getStatus() == ITestResult.SKIP) {
                if (!AtfContractNarrationState.manualVerdictPublished()) {
                    AtfContractReportNarrator.logVerdict(
                            result,
                            false,
                            "The framework contract was skipped before completion."
                    );
                }
                return;
            }
            if (!AtfContractNarrationState.manualObservedPublished()) {
                AtfContractReportNarrator.logObserved(result);
            }
            if (!AtfContractNarrationState.manualVerdictPublished()) {
                AtfContractReportNarrator.logVerdict(
                        result,
                        result.getStatus() == ITestResult.SUCCESS,
                        result.getStatus() == ITestResult.SUCCESS
                                ? "The observed framework behavior satisfied the declared ATF contract."
                                : "The framework contract did not complete successfully."
                );
            }
        } finally {
            AtfContractNarrationState.clear();
        }
    }
}
