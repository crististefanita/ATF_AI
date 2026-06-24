package com.endava.ai.mcp.tests.support;

import com.endava.ai.mcp.McpPackageValidationResult;
import org.testng.Assert;

public final class McpViolationAssert {
    private McpViolationAssert() {
    }

    public static void requireCode(McpPackageValidationResult result, String code) {
        Assert.assertTrue(hasCode(result, code), result.violations().toString());
    }

    public static void requireCodes(McpPackageValidationResult result, String... codes) {
        for (String code : codes) {
            requireCode(result, code);
        }
    }

    public static boolean hasCode(McpPackageValidationResult result, String code) {
        return result.violations().stream().anyMatch(violation -> violation.code().equals(code));
    }
}
