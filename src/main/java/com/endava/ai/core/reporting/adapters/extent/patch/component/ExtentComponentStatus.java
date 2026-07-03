package com.endava.ai.core.reporting.adapters.extent.patch.component;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
class ExtentComponentStatus {
    int classPassed;
    int classFailed;
    int classSkipped;
    int testPassed;
    int testFailed;
    int testSkipped;

    boolean available() {
        return classTotal() > 0 || testTotal() > 0;
    }

    int classTotal() {
        return classPassed + classFailed + classSkipped;
    }

    int testTotal() {
        return testPassed + testFailed + testSkipped;
    }
}
