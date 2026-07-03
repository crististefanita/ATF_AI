package com.endava.ai.skills.mcp.context;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.file.Path;
import java.util.List;

@Getter
@Accessors(fluent = true)
public final class McpClaimEvidence {
    private final String claimText;
    private final String normalizedVerb;
    private final String businessObject;
    private final String runId;
    private final Path runState;
    private final Path runDiff;
    private final List<Path> sourceFiles;
    private final List<Path> machineProofs;
    private final Path extentProof;
    private final String proofMappingRow;
    private final String status;
    private final String actionCorpus;
    private final String proofCorpus;

    public McpClaimEvidence(String claimText,
                            String normalizedVerb,
                            String businessObject,
                            String runId,
                            Path runState,
                            Path runDiff,
                            List<Path> sourceFiles,
                            List<Path> machineProofs,
                            Path extentProof,
                            String proofMappingRow,
                            String status,
                            String actionCorpus,
                            String proofCorpus) {
        this.claimText = claimText;
        this.normalizedVerb = normalizedVerb;
        this.businessObject = businessObject;
        this.runId = runId;
        this.runState = runState;
        this.runDiff = runDiff;
        this.sourceFiles = List.copyOf(sourceFiles);
        this.machineProofs = List.copyOf(machineProofs);
        this.extentProof = extentProof;
        this.proofMappingRow = proofMappingRow;
        this.status = status;
        this.actionCorpus = actionCorpus == null ? "" : actionCorpus;
        this.proofCorpus = proofCorpus == null ? "" : proofCorpus;
    }

    public String chainSummary() {
        return "claim -> source -> diff -> run-state -> execution proof -> proof mapping -> verdict"
                + " | run=" + runId
                + " | verb=" + normalizedVerb
                + " | sources=" + sourceFiles.size()
                + " | machineProofs=" + machineProofs.size()
                + " | proofRow=" + present(proofMappingRow)
                + " | status=" + present(status);
    }

    public String missingEvidenceHint() {
        StringBuilder missing = new StringBuilder();
        appendMissing(missing, "source file", sourceFiles.isEmpty());
        appendMissing(missing, "machine proof", machineProofs.isEmpty());
        appendMissing(missing, "Extent proof", extentProof == null);
        appendMissing(missing, "Proof Mapping row", proofMappingRow == null || proofMappingRow.isBlank());
        appendMissing(missing, "run status/verdict", status == null || status.isBlank());
        return missing.length() == 0 ? "none" : missing.toString();
    }

    private String present(String value) {
        return value == null || value.isBlank() ? "missing" : "present";
    }

    private void appendMissing(StringBuilder target, String label, boolean missing) {
        if (!missing) {
            return;
        }
        if (target.length() > 0) {
            target.append(", ");
        }
        target.append(label);
    }
}
