package com.endava.ai.api.core.validation;

import com.endava.ai.core.reporting.StepLogger;

final class ResponseValidationStep {
    private ResponseValidationStep() {
    }

    static void run(String stepName, Runnable assertion) {
        StepLogger.startStep(stepName);
        try {
            assertion.run();
            StepLogger.pass("Validated");
        } catch (AssertionError e) {
            StepLogger.fail(e.getMessage(), e);
            throw e;
        }
    }
}
