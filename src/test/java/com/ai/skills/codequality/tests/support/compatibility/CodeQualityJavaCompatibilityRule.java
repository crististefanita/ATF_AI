package com.ai.skills.codequality.tests.support.compatibility;

import lombok.Value;

@Value
public class CodeQualityJavaCompatibilityRule {
    String id;
    String forbiddenApi;
    String repair;
}
