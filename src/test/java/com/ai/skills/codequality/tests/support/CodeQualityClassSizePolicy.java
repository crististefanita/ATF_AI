package com.ai.skills.codequality.tests.support;

import lombok.Value;

@Value
public class CodeQualityClassSizePolicy {
    int warningLines;
    int maxLines;
    int maxWarningFiles;
    int maxOverMaxFiles;
}
