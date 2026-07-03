package com.endava.ai.skills.codequality.tests.support;

import lombok.Value;

@Value
public class CodeQualityYamlSizePolicy {
    long warningBytes;
    long maxBytes;
    int maxWarningFiles;
    int maxOverMaxFiles;
}
