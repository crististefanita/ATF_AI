package com.endava.ai.atf.core.reporting.aggregate.descriptor;

import com.endava.ai.atf.core.reporting.aggregate.AtfCoreAggregateArtifacts;
import com.endava.ai.atf.core.reporting.aggregate.AtfCoreAggregateRunCommands;
import com.endava.ai.atf.core.reporting.config.AtfCoreAggregateEngine;
import com.endava.ai.core.reporting.aggregate.AggregateReportDescriptor;

public final class AtfCoreAggregateReportDescriptor {
    private AtfCoreAggregateReportDescriptor() {
    }

    public static AggregateReportDescriptor create() {
        return AggregateReportDescriptor.builder()
                .testTitle("ATF Core Aggregate Summary")
                .description("One audit surface for all ATF core test cases in "
                        + AtfCoreAggregateArtifacts.ATF_TEST_PACKAGE + ".")
                .scopeLabel(AtfCoreAggregateArtifacts.testNgSweepLabel())
                .fragmentationReason(
                        "Extent runtime reports can be fragmented by adapter-isolation contract tests; "
                                + "this report is generated from TestNG suite results."
                )
                .auditShape(
                        "Every test node publishes TestNG result plus available @AtfContract / "
                                + "@AtfContractSuite metadata."
                )
                .addRunCommand(AtfCoreAggregateRunCommands.aggregateEngine(AtfCoreAggregateEngine.EXTENT))
                .addRunCommand(AtfCoreAggregateRunCommands.aggregateEngine(AtfCoreAggregateEngine.ALLURE))
                .addRunCommand(AtfCoreAggregateRunCommands.aggregateEngine(AtfCoreAggregateEngine.BOTH))
                .addRunCommand(AtfCoreAggregateRunCommands.allureProfile())
                .build();
    }
}
