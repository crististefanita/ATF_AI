package com.endava.ai.skills.codequality.tests.support.method;

import lombok.Value;

@Value
public class CodeQualityMethodSizePolicy {
    int warningLines;
    int maxLines;
    int maxWarningMethods;
    int maxOverMaxMethods;
}
