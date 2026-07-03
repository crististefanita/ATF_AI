package com.ai.skills.codequality.tests.support.method;

import lombok.Value;

@Value
public class CodeQualityMethodSize {
    String source;
    String method;
    int startLine;
    int lines;
}
