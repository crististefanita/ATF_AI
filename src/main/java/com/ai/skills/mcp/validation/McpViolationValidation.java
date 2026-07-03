package com.ai.skills.mcp.validation;

import com.ai.skills.mcp.McpPackageValidationResult;

public final class McpViolationValidation {
    private McpViolationValidation() {
    }

    public static void requireCode(McpPackageValidationResult result, String code) {
        if (!hasCode(result, code)) {
            throw new AssertionError(result.violations().toString());
        }
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
