package com.ai.skills.mcp.rules.run;

import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.context.McpRunArtifacts;
import com.ai.skills.mcp.fs.HashService;
import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@McpContract(id = "run.window.independence",
        category = McpRuleCategory.RUN,
        htmlReq = "orchestrate-iterative-runs.html",
        artifact = "runs/run_##/review/thin-no-index.diff")
public final class RunIndependenceRule implements McpValidationRule {
    private final HashService hashService = new HashService();

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        List<Path> runDirectories = RunRuleSupport.runDirectories(context, result);
        requireRunDiffsAreNotAllIdentical(runDirectories, result);
        requireRunIndependence(runDirectories, result);
        requireRunDiffsMentionOwnedBusinessObject(context, runDirectories, result);
    }

    private void requireRunDiffsAreNotAllIdentical(List<Path> runDirectories, McpPackageValidationResult result) {
        Set<String> hashes = new HashSet<>();
        for (Path runDirectory : runDirectories) {
            Path diff = runDirectory.resolve(McpRunArtifacts.DIFF);
            if (Files.isRegularFile(diff)) {
                hashes.add(hashService.sha256(diff));
            }
        }
        if (runDirectories.size() > 1 && hashes.size() <= 1) {
            result.add(McpPackageViolation.blocker("run-diff.duplicate-all",
                    "All per-run thin-no-index.diff files are identical; run history is not cold-distinct",
                    runDirectories.get(0).getParent()));
        }
    }

    private void requireRunIndependence(List<Path> runDirectories, McpPackageValidationResult result) {
        if (runDirectories.size() <= 1) {
            return;
        }
        requireNotAllSameHash(runDirectories, McpRunArtifacts.SUMMARY, "run-independence.summary-duplicate-all",
                "All run SUMMARY.md files are identical; run narrative is not independently auditable", result);
        requireNotAllSameHash(runDirectories, McpRunArtifacts.STATE, "run-independence.state-duplicate-all",
                "All run-state.json files are identical; run state does not prove independent decisions", result);
        requireNotAllSameHash(runDirectories, McpRunArtifacts.BUSINESS_UNDERSTANDING,
                "run-independence.feedback-duplicate-all",
                "All run business-understanding.md files are identical; feedback does not prove iteration novelty",
                result);
    }

    private void requireNotAllSameHash(List<Path> runDirectories,
                                       String relativePath,
                                       String code,
                                       String message,
                                       McpPackageValidationResult result) {
        Set<String> hashes = new HashSet<>();
        int present = 0;
        for (Path runDirectory : runDirectories) {
            Path artifact = runDirectory.resolve(relativePath);
            if (Files.isRegularFile(artifact)) {
                present++;
                hashes.add(hashService.sha256(artifact));
            }
        }
        if (present > 1 && hashes.size() <= 1) {
            result.add(McpPackageViolation.blocker(code, message, runDirectories.get(0).getParent()));
        }
    }

    private void requireRunDiffsMentionOwnedBusinessObject(McpPackageContext context,
                                                           List<Path> runDirectories,
                                                           McpPackageValidationResult result) {
        for (Path runDirectory : runDirectories) {
            Path state = runDirectory.resolve(McpRunArtifacts.STATE);
            Path diff = runDirectory.resolve(McpRunArtifacts.DIFF);
            if (!Files.isRegularFile(state) || !Files.isRegularFile(diff)) {
                continue;
            }
            String businessObject = McpRuleSupport.extractStringValue(context.files().read(state, result), "business_object");
            if (businessObject == null || businessObject.isBlank()) {
                continue;
            }
            String normalizedDiff = context.files().read(diff, result).toLowerCase(Locale.ROOT);
            long hits = Arrays.stream(businessObject.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                    .filter(word -> word.length() >= RunRuleSupport.MIN_MEANINGFUL_BUSINESS_WORD_LENGTH)
                    .filter(normalizedDiff::contains)
                    .count();
            if (hits == 0) {
                result.add(McpPackageViolation.warning("run-diff.business-object-not-visible",
                        "Run diff does not visibly mention the run business object: " + businessObject,
                        diff));
            }
        }
    }
}
