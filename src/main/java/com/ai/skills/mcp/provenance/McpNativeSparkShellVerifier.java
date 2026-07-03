package com.ai.skills.mcp.provenance;

import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

final class McpNativeSparkShellVerifier {
    List<McpPackageViolation> verify(Path extentReport) {
        try {
            String lower = Files.readString(extentReport).toLowerCase(Locale.ROOT);
            List<String> nativeShellTokens = ContractCatalog.extentProvenanceTokens("native-spark-shell-token");
            int minimumNativeShellTokens = ContractCatalog.extentProvenanceThreshold("native-spark-shell-threshold", 4);
            List<String> presentTokens = nativeShellTokens.stream()
                    .filter(lower::contains)
                    .collect(Collectors.toList());
            if (presentTokens.size() >= minimumNativeShellTokens) {
                return List.of();
            }
            return List.of(McpPackageViolation.blocker("run.extent.native-spark-shell.missing",
                    "Extent report does not preserve enough native Spark shell structure. Present tokens: "
                            + presentTokens + "; expected at least " + minimumNativeShellTokens
                            + " from " + nativeShellTokens,
                    extentReport));
        } catch (IOException exception) {
            return List.of(McpPackageViolation.blocker("run.extent.unreadable",
                    "Cannot read Extent report: " + exception.getMessage(), extentReport));
        }
    }
}
