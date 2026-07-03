package com.ai.atf.core.reporting.aggregate.command;

import com.ai.atf.core.reporting.aggregate.AtfCoreAggregateRunCommands;
import com.ai.atf.core.reporting.config.AtfCoreAggregateEngine;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_AGGREGATE_EXTENT_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class AtfCoreAggregateRunCommandContractTest {
    @Test
    public void aggregate_core_run_commands_skip_allure_testng_spi_listener() {
        String skipProperty = AtfCoreAggregateRunCommands.skipAllureSpiListenerProperty();
        String resetTestFilter = AtfCoreAggregateRunCommands.resetSurefireTestFilterProperty();

        assertCoreAggregateCommandIsIsolated(AtfCoreAggregateEngine.EXTENT, skipProperty, resetTestFilter);
        assertCoreAggregateCommandIsIsolated(AtfCoreAggregateEngine.ALLURE, skipProperty, resetTestFilter);
        assertCoreAggregateCommandIsIsolated(AtfCoreAggregateEngine.BOTH, skipProperty, resetTestFilter);
        Assert.assertFalse(
                AtfCoreAggregateRunCommands.allureProfile().contains(skipProperty),
                "The dedicated testng-atf-allure.xml evidence profile must remain free to run with Allure's listener."
        );
        Assert.assertTrue(
                AtfCoreAggregateRunCommands.allureProfile().contains(resetTestFilter),
                "The dedicated testng-atf-allure.xml evidence profile must also reset -Dtest so it cannot inherit "
                        + "a stale Surefire subset from a previous focused run."
        );
    }

    private static void assertCoreAggregateCommandIsIsolated(
            AtfCoreAggregateEngine engine,
            String skipProperty,
            String resetTestFilter
    ) {
        String command = AtfCoreAggregateRunCommands.aggregateEngine(engine);
        Assert.assertTrue(
                command.contains(skipProperty),
                "Core aggregate command for " + engine
                        + " must skip the Allure TestNG SPI listener; aggregate Allure evidence is written by "
                        + "AtfCoreAggregateReportListener, not by auto-attaching AllureTestNg to the core sweep."
        );
        Assert.assertTrue(
                command.contains(resetTestFilter),
                "Core aggregate command for " + engine
                        + " must reset -Dtest so stale Surefire method/class filters cannot turn the full sweep "
                        + "into a misleading green subset."
        );
    }
}
