package com.ai.skills.codequality.tests.support.compatibility;

import lombok.Value;

@Value
public class CodeQualityJavaCompatibilityViolation {
    String ruleId;
    String source;
    int line;
    String snippet;
}
