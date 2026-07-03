package com.endava.ai.skills.mcp.rules.source.kpi;

import lombok.RequiredArgsConstructor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
public final class SourceEvolutionKpiRow {
    private final SourceEvolutionKpiCatalog.KpiSpec spec;
    private final int expected;
    private final Integer declared;
    private final String status;
    private final String firstBlocker;

    public static SourceEvolutionKpiRow of(SourceEvolutionKpiCatalog.KpiSpec spec,
                                           int expected,
                                           Integer declared,
                                           String status,
                                           String firstBlocker) {
        return new SourceEvolutionKpiRow(spec, expected, declared, status, firstBlocker);
    }

    public String declaredText() {
        return declared == null ? "missing" : String.valueOf(declared);
    }
}
