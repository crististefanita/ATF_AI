package com.endava.ai.skills.codequality.tests.support.dependency;

import lombok.Value;

@Value
public class CodeQualityDependencyBoundary {
    String id;
    String root;
    String forbiddenPackage;
    String repair;
}
