package com.ai.skills.codequality.tests.support.assertion;

import lombok.Value;

@Value
public class CodeQualityAssertionDensity {
    String source;
    int testMethods;
    int assertionCues;

    public int cuePercent() {
        return testMethods == 0 ? 0 : assertionCues * 100 / testMethods;
    }

    public boolean isZeroCue() {
        return assertionCues == 0;
    }

    public boolean isBelow(CodeQualityAssertionDensityPolicy policy) {
        return cuePercent() < policy.getMinimumCuePercent();
    }
}
