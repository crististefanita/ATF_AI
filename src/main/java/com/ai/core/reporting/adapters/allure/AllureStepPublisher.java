package com.ai.core.reporting.adapters.allure;

import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Status;

public final class AllureStepPublisher {

    private final AllureReporter reporter;
    private final AllureStepContext stepContext;
    private final AllureStepBuffer stepBuffer;

    public AllureStepPublisher(
            AllureReporter reporter,
            AllureStepContext stepContext,
            AllureStepBuffer stepBuffer
    ) {
        this.reporter = reporter;
        this.stepContext = stepContext;
        this.stepBuffer = stepBuffer;
    }

    public void startStep(String title) {
        if (!reporter.hasCurrentTestCaseOrStep()) {
            stepBuffer.start(title);
            return;
        }

        flushBufferedStepsIfPossible();
        stepContext.startStep(title);
    }

    public void addParameter(Parameter parameter) {
        if (stepBuffer.hasActiveStep()) {
            stepBuffer.addParam(parameter);
            return;
        }
        stepContext.addParam(parameter);
    }

    public void passCurrentStep() {
        if (stepBuffer.hasActiveStep()) {
            stepBuffer.passActiveStep();
            return;
        }
        stepContext.finishStep(Status.PASSED);
    }

    public void failCurrentStep(Parameter errorParameter) {
        if (stepBuffer.hasActiveStep()) {
            stepBuffer.failActiveStep(errorParameter);
            return;
        }

        if (errorParameter != null) {
            stepContext.addParam(errorParameter);
        }
        stepContext.finishStep(Status.FAILED);
    }

    public void flushBufferedStepsIfPossible() {
        if (reporter.hasCurrentTestCaseOrStep()) {
            stepBuffer.flushInto(stepContext);
        }
    }

    public void clear() {
        stepContext.clear();
        stepBuffer.clear();
    }
}
