package com.ai.skills.mcp.rules.packageaudit.closeout;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpPackageContext;

final class McpFrozenEvidenceConsistency {
    private McpFrozenEvidenceConsistency() {
    }

    static void validate(McpPackageContext context,
                         McpPackageValidationResult result,
                         String gateText) {
        McpCloseoutEvidenceCounts.validate(context, result, gateText);
        McpCloseoutReportDisclosure.validateSummary(context, result, gateText);
        McpSkippedEvidencePolicy.validate(context, result, gateText);
    }
}

