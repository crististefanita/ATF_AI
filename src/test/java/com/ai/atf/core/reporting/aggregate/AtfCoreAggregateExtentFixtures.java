package com.ai.atf.core.reporting.aggregate;

import com.ai.atf.core.reporting.model.AtfCoreAggregateCase;
import com.ai.core.reporting.aggregate.AggregateStatus;

public final class AtfCoreAggregateExtentFixtures {
    private AtfCoreAggregateExtentFixtures() {
    }

    public static Object aggregateCase(String methodName, long startMillis) throws Exception {
        return aggregateCase(methodName, startMillis, "");
    }

    public static Object aggregateCase(String methodName, long startMillis, String parameters) throws Exception {
        return aggregateCase(aggregateFixture("AggregateStandardShellFixture"), methodName, startMillis, parameters);
    }

    public static Object aggregateCase(String className, String methodName, long startMillis, String parameters)
            throws Exception {
        return new AtfCoreAggregateCase(
                "ATF Core Package Sweep",
                AtfCoreAggregateArtifacts.ATF_TEST_PACKAGE,
                className,
                methodName,
                parameters,
                "EXTENT_CONTRACT",
                "ATF core aggregate Extent contract",
                "Validate aggregate Extent standard shell.",
                "REPORTING_ADAPTER",
                "AggregateExtentReportWriter",
                "Aggregate Extent report uses the shared Extent shell.",
                "Generated HTML contains standard runtime audit sections.",
                "Keep aggregate Extent generation routed through the shared writer and finalizer.",
                AggregateStatus.PASSED,
                startMillis,
                startMillis + 5,
                "",
                ""
        );
    }

    public static String aggregateFixture(String simpleName) {
        return AtfCoreAggregateExtentContractTest.class.getPackageName() + "." + simpleName;
    }
}
