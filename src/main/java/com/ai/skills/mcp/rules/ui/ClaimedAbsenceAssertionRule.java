package com.ai.skills.mcp.rules.ui;


import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.context.McpProjectLayout;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

@McpContract(id = "ui.claimed-absence.requires-assertion",
        category = McpRuleCategory.UI_WRAPPER,
        htmlReq = "ui-business-frontier-adapter.html",
        artifact = "run proof + report")
public final class ClaimedAbsenceAssertionRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        String packageTruth = context.files().readIfRegular(context.paths().packageState(), result)
                + "\n" + context.files().readIfRegular(context.packageRoot().resolve(McpArtifactPaths.QUALITY_ACCOUNTING_VERDICT), result)
                + "\n" + context.files().readIfRegular(context.paths().report(), result);
        String normalizedTruth = packageTruth.toLowerCase(Locale.ROOT);
        boolean claimsAbsence = Pattern.compile("(?is)\\b(no|missing|absent|not visible|not present|not found|unavailable)\\b.{0,80}\\b(id|row|record|object|entity|item|field|value|state|message|history|result|status|screen|element|flow)\\b")
                .matcher(normalizedTruth)
                .find();
        if (!claimsAbsence) {
            return;
        }

        String evidence = context.files().readIfRegular(context.paths().rootDiff(), result)
                + "\n" + McpRuleSupport.readProjectSource(context, McpProjectLayout.TEST_JAVA_ROOT)
                + "\n" + McpRuleSupport.readProjectSource(context, McpProjectLayout.MAIN_JAVA_ROOT);
        boolean hasAbsenceAssertion = Arrays.stream(evidence.split("\\R"))
                .map(line -> line.toLowerCase(Locale.ROOT))
                .anyMatch(line -> McpRuleSupport.containsAny(line, "assertfalse", "assert true", "asserttrue", "not visible", "not present",
                        "not found", "absent", "missing", " no ", "unavailable",
                        "does not contain", "doesn't contain"));
        if (!hasAbsenceAssertion) {
            result.add(McpPackageViolation.blocker("claimed-absence.assertion-missing",
                    "Package claims an absence, but root diff/source does not show line-level assertion/proof for that absence",
                    context.paths().rootDiff()));
        }
    }
}
