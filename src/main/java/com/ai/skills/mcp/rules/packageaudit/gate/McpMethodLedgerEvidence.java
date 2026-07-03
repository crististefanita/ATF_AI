package com.ai.skills.mcp.rules.packageaudit.gate;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.rules.McpRuleSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

public final class McpMethodLedgerEvidence {
    private McpMethodLedgerEvidence() {
    }

    public static void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path ledger = context.packageRoot().resolve(McpArtifactPaths.MCP_METHOD_LEDGER);
        if (!Files.isRegularFile(ledger)) {
            return;
        }
        try {
            String text = Files.readString(ledger);
            if (!Pattern.compile("(?i)tests?\\s*(run)?\\s*[:=]\\s*[1-9]\\d*").matcher(text).find()) {
                result.add(McpPackageViolation.blocker("mcp.method-ledger.incomplete",
                        "Full MCP method ledger must publish a non-zero total test count", ledger));
            }
            if (Pattern.compile("(?i)(failures|errors)\\s*[:=]\\s*[1-9]\\d*\\b").matcher(text).find()) {
                result.add(McpPackageViolation.blocker("mcp.method-ledger.incomplete",
                        "Full MCP method ledger cannot publish non-zero failures/errors for final close-out", ledger));
            }
            if (!Pattern.compile("(?i)failures\\s*[:=]\\s*0\\b").matcher(text).find()
                    || !Pattern.compile("(?i)errors\\s*[:=]\\s*0\\b").matcher(text).find()) {
                result.add(McpPackageViolation.blocker("mcp.method-ledger.incomplete",
                        "Full MCP method ledger must publish zero failures/errors for final close-out", ledger));
            }
            if (!McpRuleSupport.containsAny(text.toLowerCase(Locale.ROOT), "mcp", "method", "passed", "skipped")) {
                result.add(McpPackageViolation.blocker("mcp.method-ledger.incomplete",
                        "Full MCP method ledger must expose MCP method-level passed/skipped accounting", ledger));
            }
            if (Pattern.compile("(?s)\\$\\{?\\w+}?|\\$class|\\$method").matcher(text).find()) {
                result.add(McpPackageViolation.blocker("mcp.method-ledger.placeholder",
                        "Full MCP method ledger must not contain unresolved placeholders such as $class or $method", ledger));
            }
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("mcp.method-ledger.unreadable",
                    "Cannot read full MCP method ledger: " + exception.getMessage(), ledger));
        }
    }
}
