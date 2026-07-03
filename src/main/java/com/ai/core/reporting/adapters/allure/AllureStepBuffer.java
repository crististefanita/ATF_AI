package com.ai.core.reporting.adapters.allure;

import io.qameta.allure.model.Parameter;
import io.qameta.allure.model.Status;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

public final class AllureStepBuffer {

    private final ThreadLocal<BufferedStep> activeStep = new ThreadLocal<>();
    private final ThreadLocal<List<BufferedStep>> completedSteps =
            ThreadLocal.withInitial(ArrayList::new);

    public void start(String title) {
        activeStep.set(new BufferedStep(title));
    }

    public boolean hasActiveStep() {
        return activeStep.get() != null;
    }

    public void addParam(Parameter parameter) {
        BufferedStep step = activeStep.get();
        if (step != null && parameter != null) {
            step.params.add(parameter);
        }
    }

    public void passActiveStep() {
        completeActiveStep(Status.PASSED);
    }

    public void failActiveStep(Parameter errorParameter) {
        addParam(errorParameter);
        completeActiveStep(Status.FAILED);
    }

    public void flushInto(AllureStepContext stepContext) {
        List<BufferedStep> bufferedSteps = completedSteps.get();
        if (bufferedSteps.isEmpty()) {
            return;
        }

        for (BufferedStep buffered : bufferedSteps) {
            stepContext.startStep(buffered.title);
            for (Parameter param : buffered.params) {
                stepContext.addParam(param);
            }
            stepContext.finishStep(buffered.status);
        }
        bufferedSteps.clear();
    }

    public void clear() {
        activeStep.remove();
        completedSteps.remove();
    }

    private void completeActiveStep(Status status) {
        BufferedStep step = activeStep.get();
        if (step == null) {
            return;
        }
        step.status = status;
        completedSteps.get().add(step);
        activeStep.remove();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class BufferedStep {
        private final String title;
        private final List<Parameter> params = new ArrayList<>();
        private Status status = Status.PASSED;
    }
}
