package com.ai.skills.codequality.tests.support;

import lombok.Value;

@Value
public class CodeQualitySourceSize {
    String path;
    int lines;
}
