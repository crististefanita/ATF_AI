package com.ai.skills.mcp.rules.pretraining;


import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@McpContract(id = "pretraining.intake.quality",
        category = McpRuleCategory.PRETRAINING,
        htmlReq = "pretraining-reference.html",
        artifact = "review/round-pretraining-brief.md")
public final class PretrainingIntakeRule implements McpValidationRule {
    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path intake = context.packageRoot().resolve(McpArtifactPaths.ROUND_PRETRAINING_BRIEF);
        if (!Files.isRegularFile(intake)) {
            return;
        }
        String text = context.files().read(intake, result).toLowerCase(Locale.ROOT);
        McpRuleSupport.requireText(text, intake, result, "pretraining-intake.sources",
                "Pretraining intake should list sources read", "source", "sources", "read", "recit");
        McpRuleSupport.requireText(text, intake, result, "pretraining-intake.extract",
                "Pretraining intake should state what it extracts", "extract", "extrag", "learn", "lesson");
        McpRuleSupport.requireText(text, intake, result, "pretraining-intake.no-inherit",
                "Pretraining intake should state what is not inherited/copied", "do not inherit", "nu mosten", "not inherit", "do not copy", "nu cop");
        McpRuleSupport.requireText(text, intake, result, "pretraining-intake.frontier",
                "Pretraining intake should name the chosen frontier or pressure", "frontier", "pressure", "anchor", "business");
        McpRuleSupport.requireText(text, intake, result, "pretraining-intake.artifact",
                "Pretraining intake should name the saved artifact", "artifact", "artefact", "saved", "salvat");
        McpRuleSupport.requireText(text, intake, result, "pretraining-intake.winning-frontier",
                "Pretraining intake should name the selected winning frontier", "winning frontier", "frontier now", "target frontier");
        McpRuleSupport.requireText(text, intake, result, "pretraining-intake.why-it-wins",
                "Pretraining intake should explain why the selected frontier wins", "why it wins", "de ce castiga", "why wins");
        McpRuleSupport.requireText(text, intake, result, "pretraining-intake.decision",
                "Pretraining intake should publish the cold launch/stop decision", "decision", "decizie", "launch", "stop", "blocked");
    }
}
