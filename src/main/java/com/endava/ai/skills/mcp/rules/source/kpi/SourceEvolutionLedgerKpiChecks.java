package com.endava.ai.skills.mcp.rules.source.kpi;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.rules.source.inventory.SourceEvolutionInventory;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

final class SourceEvolutionLedgerKpiChecks {
    private SourceEvolutionLedgerKpiChecks() {
    }

    static void requireLedgerValues(String ledgerText,
                                    Path ledger,
                                    SourceEvolutionInventory inventory,
                                    McpPackageValidationResult result) {
        requireRootKpis(ledgerText, ledger, inventory, result);
        requireRunKpis(ledgerText, ledger, inventory, result);
        requireNoProtectedRootLie(ledgerText, ledger, inventory, result);
    }

    private static void requireRootKpis(String ledgerText,
                                        Path ledger,
                                        SourceEvolutionInventory inventory,
                                        McpPackageValidationResult result) {
        Map<String, Integer> expected = new LinkedHashMap<>();
        for (SourceEvolutionKpiCatalog.KpiSpec spec : SourceEvolutionKpiCatalog.rootKpis()) {
            expected.put(spec.id(), inventory.expectedValue(spec.id()));
        }

        for (Map.Entry<String, Integer> entry : expected.entrySet()) {
            requireNumericValue(ledgerText, ledger, entry.getKey(), entry.getValue(), result);
        }
    }

    private static void requireRunKpis(String ledgerText,
                                       Path ledger,
                                       SourceEvolutionInventory inventory,
                                       McpPackageValidationResult result) {
        for (Map.Entry<String, Integer> run : inventory.runFileCounts().entrySet()) {
            requireNumericValue(ledgerText, ledger, run.getKey(), run.getValue(), result);
        }
    }

    private static void requireNoProtectedRootLie(String ledgerText,
                                                  Path ledger,
                                                  SourceEvolutionInventory inventory,
                                                  McpPackageValidationResult result) {
        Integer declared = SourceEvolutionKpiReader.readNumericValue(ledgerText, "protected_root_dirty_count");
        if (declared != null && declared == 0 && inventory.hasProtectedRootFiles()) {
            result.add(McpPackageViolation.blocker("source-evolution.kpi.protected-root-drift",
                    "Source evolution ledger declares protected_root_dirty_count=0, but root diff references "
                            + inventory.protectedRootFiles() + " protected source file(s)",
                    ledger));
        }
    }

    private static void requireNumericValue(String ledgerText,
                                            Path ledger,
                                            String key,
                                            int expected,
                                            McpPackageValidationResult result) {
        Integer actual = SourceEvolutionKpiReader.readNumericValue(ledgerText, key);
        if (actual == null) {
            result.add(McpPackageViolation.blocker("source-evolution.kpi.numeric-missing",
                    "Source evolution ledger must publish numeric KPI " + key + "=" + expected,
                    ledger));
            return;
        }
        if (actual != expected) {
            result.add(McpPackageViolation.blocker("source-evolution.kpi.value-mismatch",
                    "Source evolution ledger KPI " + key + " declares " + actual
                            + " but expected " + expected + " from package diffs",
                    ledger));
        }
    }
}
