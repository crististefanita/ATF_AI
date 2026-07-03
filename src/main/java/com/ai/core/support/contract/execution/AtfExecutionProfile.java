package com.ai.core.support.contract.execution;

import com.ai.core.support.config.TestExecutionConfigKeys;

public interface AtfExecutionProfile {
    String id();

    String suiteXml();

    default String suiteFileName() {
        int slash = suiteXml().lastIndexOf('/');
        return slash >= 0 ? suiteXml().substring(slash + 1) : suiteXml();
    }

    default String mavenCommand() {
        return "mvn -q -D" + TestExecutionConfigKeys.SUREFIRE_SUITE_XML_FILES + "=" + suiteXml() + " test";
    }
}
