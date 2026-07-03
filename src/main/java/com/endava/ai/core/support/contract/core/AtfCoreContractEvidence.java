package com.endava.ai.core.support.contract.core;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class AtfCoreContractEvidence {
    private final String contractId;
    private final AtfCoreEvidenceKind kind;
    private final String evidence;

    public AtfCoreContractEvidence(AtfCoreCheck check) {
        this.contractId = check.contract();
        this.kind = check.evidenceKind();
        this.evidence = check.evidence();
    }
}
