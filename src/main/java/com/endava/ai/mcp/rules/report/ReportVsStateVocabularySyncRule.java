package com.endava.ai.mcp.rules.report;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@com.endava.ai.mcp.contract.McpContract(id = "report.state.vocabulary-sync",
        category = com.endava.ai.mcp.contract.McpRuleCategory.REPORT,
        htmlReq = "reporting.html",
        artifact = "analysis/langgraph-business-understanding.html")
public final class ReportVsStateVocabularySyncRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        String publicText = String.join("\n",
                context.files().readIfRegular(context.paths().report(), result),
                context.files().readIfRegular(context.paths().readme(), result),
                context.files().readIfRegular(context.paths().packageCloseOut(), result)
        ).toLowerCase(Locale.ROOT);
        requirePublished("business_object", state, publicText, context.paths().report(), result);
        requirePublished("primary_next_business_object", state, publicText, context.paths().packageCloseOut(), result);
        requirePublished("new_frontier", state, publicText, context.paths().readme(), result);
    }

    private void requirePublished(String key, String state, String publicText, Path source,
                                  McpPackageValidationResult result) {
        String value = McpRuleSupport.extractStringValue(state, key);
        if (value == null || value.length() < 8) {
            return;
        }
        if (!publicText.contains(value.toLowerCase(Locale.ROOT))) {
            result.add(McpPackageViolation.blocker("report-state.vocabulary-drift",
                    "Public artifacts must publish package-state value for " + key + ": " + value,
                    source));
        }
    }
}
