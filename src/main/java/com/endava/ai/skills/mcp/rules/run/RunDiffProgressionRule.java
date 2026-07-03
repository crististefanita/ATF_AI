package com.endava.ai.skills.mcp.rules.run;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.context.McpRunArtifacts;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.fs.DiffFileExtractor;
import com.endava.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@McpContract(id = "run.diff.progression",
        category = McpRuleCategory.RUN,
        htmlReq = "orchestrate-iterative-runs.html + execute-and-understand-run.html + reporting.html",
        artifact = "review/root-source-thin-no-index.diff + runs/run_##/review/thin-no-index.diff")
public final class RunDiffProgressionRule implements McpValidationRule {
    private final DiffFileExtractor diffFileExtractor = new DiffFileExtractor();

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path rootDiff = context.paths().rootDiff();
        if (!Files.isRegularFile(rootDiff) || !Files.isDirectory(context.paths().runsRoot())) {
            return;
        }

        Set<String> rootSourceFiles = sourceFiles(context.files().read(rootDiff, result));
        if (rootSourceFiles.isEmpty()) {
            result.add(McpPackageViolation.blocker("root-diff.progression.no-source-files",
                    "Root diff must expose the compiled source/test mutation, not only notes or artifact churn",
                    rootDiff));
            return;
        }

        Set<String> runSourceUnion = new LinkedHashSet<>();
        Set<String> runSourceSignatures = new LinkedHashSet<>();
        int runDiffsWithSource = 0;
        for (Path runDirectory : RunRuleSupport.runDirectories(context, result)) {
            Path runDiff = runDirectory.resolve(McpRunArtifacts.DIFF);
            if (!Files.isRegularFile(runDiff)) {
                continue;
            }
            Set<String> runSourceFiles = sourceFiles(context.files().read(runDiff, result));
            if (runSourceFiles.isEmpty()) {
                result.add(McpPackageViolation.blocker("run-diff.progression.no-source-files",
                        "Run diff must expose at least one changed src/** file so the run can be reopened as real progress: "
                                + runDirectory.getFileName(),
                        runDiff));
                continue;
            }
            runDiffsWithSource++;
            runSourceUnion.addAll(runSourceFiles);
            runSourceSignatures.add(String.join("|", runSourceFiles));
        }

        if (runDiffsWithSource == 0) {
            result.add(McpPackageViolation.blocker("run-diff.progression.no-run-source-deltas",
                    "No run diff exposes source/test deltas; progress cannot be audited from baseline to final",
                    context.paths().runsRoot()));
            return;
        }

        if (runDiffsWithSource > 1 && runSourceSignatures.size() <= 1) {
            result.add(McpPackageViolation.blocker("run-diff.progression.flat-source-set",
                    "All parseable run diffs touch the same source file set; publish finer per-run deltas or collapse duplicate runs",
                    context.paths().runsRoot()));
        }

        List<String> missingFromRoot = runSourceUnion.stream()
                .filter(file -> !rootSourceFiles.contains(file))
                .collect(Collectors.toList());
        if (!missingFromRoot.isEmpty()) {
            result.add(McpPackageViolation.blocker("run-diff.progression.not-compiled-into-root",
                    "Root diff does not include run-level source deltas: " + String.join(", ", missingFromRoot),
                    rootDiff));
        }
    }

    private Set<String> sourceFiles(String diff) {
        return diffFileExtractor.referencedFiles(diff).stream()
                .map(path -> path.replace('\\', '/'))
                .filter(path -> path.startsWith("src/"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
