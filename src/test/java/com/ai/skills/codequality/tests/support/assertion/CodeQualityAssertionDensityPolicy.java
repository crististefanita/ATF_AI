package com.ai.skills.codequality.tests.support.assertion;

import lombok.Value;

@Value
public class CodeQualityAssertionDensityPolicy {
    int minimumCuePercent;
    int maxLowDensityClasses;
    int maxZeroCueClasses;
}
