package com.endava.ai.skills.mcp.provenance;

import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

final class McpExtentManualProofDetector {
    List<McpPackageViolation> verify(Path extentReport) {
        try {
            String lower = Files.readString(extentReport).toLowerCase(Locale.ROOT);
            List<String> injectedTokens = ContractCatalog.extentProvenanceTokens("manual-proof-injection-token").stream()
                    .filter(lower::contains)
                    .collect(Collectors.toList());
            if (injectedTokens.isEmpty()) {
                return List.of();
            }
            List<String> runtimeAuditTokens = ContractCatalog.extentProvenanceTokens("framework-runtime-audit-token").stream()
                    .filter(lower::contains)
                    .collect(Collectors.toList());
            return List.of(McpPackageViolation.blocker("run.extent.manual-proof-injection",
                    "Extent report contains manual final-report/package vocabulary that belongs outside execution evidence: "
                            + injectedTokens
                            + ". Framework runtime audit tokens are classified separately and allowed when Maven-native: "
                            + runtimeAuditTokens,
                    extentReport));
        } catch (IOException exception) {
            return List.of(McpPackageViolation.blocker("run.extent.unreadable",
                    "Cannot read Extent report: " + exception.getMessage(), extentReport));
        }
    }
}
