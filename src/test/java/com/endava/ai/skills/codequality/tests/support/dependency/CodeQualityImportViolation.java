package com.endava.ai.skills.codequality.tests.support.dependency;

import lombok.Value;

@Value
public class CodeQualityImportViolation {
    String boundaryId;
    String source;
    int line;
    String forbiddenImport;
}
