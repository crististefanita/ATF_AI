package com.ai.skills.mcp.rules.source.inventory;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.context.McpRunArtifacts;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Per-run source delta accounting.
 */
@Getter
@Accessors(fluent = true)
public final class RunDeltaInventory {
    private final Map<String, Integer> fileCounts;
    private final int totalRunsWithDiff;
    private final int runsWithDistinctSourceDelta;

    private RunDeltaInventory(Map<String, Integer> fileCounts,
                              int totalRunsWithDiff,
                              int runsWithDistinctSourceDelta) {
        this.fileCounts = new LinkedHashMap<>(fileCounts);
        this.totalRunsWithDiff = totalRunsWithDiff;
        this.runsWithDistinctSourceDelta = runsWithDistinctSourceDelta;
    }

    public static RunDeltaInventory load(McpPackageContext context, McpPackageValidationResult result) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Set<Set<String>> distinctSourceSets = new java.util.LinkedHashSet<>();
        for (Path runDirectory : context.files().runDirectories(context.paths().runsRoot(), result)) {
            Path runDiff = runDirectory.resolve(McpRunArtifacts.DIFF);
            if (!Files.isRegularFile(runDiff)) {
                continue;
            }
            String run = runDirectory.getFileName().toString();
            Set<String> sources = SourceDiffInventory.sourceFiles(context.files().read(runDiff, result));
            counts.put(run + "_files_count", sources.size());
            if (!sources.isEmpty()) {
                distinctSourceSets.add(new java.util.LinkedHashSet<>(sources));
            }
        }
        return new RunDeltaInventory(counts, counts.size(), distinctSourceSets.size());
    }
}
