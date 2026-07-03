package com.ai.core.support.contract.core;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class AtfCoreContractSpec {
    private final String id;
    private final AtfCoreArea area;
    private final String owner;
    private final String invariant;
    private final String repairHint;
    private final AtfCoreSeverity severity;

    public AtfCoreContractSpec(AtfCoreContract contract) {
        this.id = contract.id();
        this.area = contract.area();
        this.owner = contract.owner();
        this.invariant = contract.invariant();
        this.repairHint = contract.repairHint();
        this.severity = contract.severity();
    }
}
