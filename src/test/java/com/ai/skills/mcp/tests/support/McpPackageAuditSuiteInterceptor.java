package com.ai.skills.mcp.tests.support;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.List;
import java.util.stream.Collectors;

public final class McpPackageAuditSuiteInterceptor implements IMethodInterceptor {
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        if (McpPackageAuditAssumptions.hasExplicitPackageAudit()
                && !McpPackageAuditAssumptions.hasExternalPackageAuditWithoutSourceRoot()) {
            return methods;
        }
        return methods.stream()
                .filter(method -> method.getMethod()
                .getTestClass()
                .getName()
                .endsWith(".McpPackageAuditPreflightTest"))
                .collect(Collectors.toList());
    }
}
