package com.ai.atf.core.reporting.aggregate.command;

import com.ai.atf.core.reporting.aggregate.AtfCoreAggregateRunCommands;
import com.ai.atf.core.reporting.config.AtfCoreAggregateEngine;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfSourcePaths;
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
    public void aggregate_extent_commands_reset_filters_and_reject_retired_engines() {
        String command = AtfCoreAggregateRunCommands.aggregateEngine(AtfCoreAggregateEngine.EXTENT);
        Assert.assertTrue(command.startsWith("mvn -pl atf-tests -am test"));
        Assert.assertTrue(command.contains("-Datf.core.aggregate.report.enabled=true"));
        Assert.assertTrue(command.contains(AtfCoreAggregateRunCommands.resetSurefireTestFilterProperty()),
                "The complete Extent sweep must reset stale Surefire class/method filters.");
        Assert.assertTrue(command.contains("-Datf.core.aggregate.report.engine=extent"));
        Assert.assertEquals(AtfCoreAggregateEngine.parse(null), AtfCoreAggregateEngine.EXTENT);
        Assert.assertEquals(AtfCoreAggregateEngine.parse("off"), AtfCoreAggregateEngine.NONE);
        Assert.assertEquals(com.ai.core.reporting.internal.ReportingEngine.from(" ExTeNt "),
                com.ai.core.reporting.internal.ReportingEngine.EXTENT);
        for (String missing : new String[]{null, "", "  "}) {
            Assert.expectThrows(IllegalArgumentException.class,
                    () -> com.ai.core.reporting.internal.ReportingEngine.from(missing));
        }
        for (String retired : new String[]{"allure", "both", "unknown"}) {
            Assert.expectThrows(IllegalArgumentException.class, () -> AtfCoreAggregateEngine.parse(retired));
            IllegalArgumentException invalid = Assert.expectThrows(IllegalArgumentException.class,
                    () -> com.ai.core.reporting.internal.ReportingEngine.from(retired));
            Assert.assertTrue(invalid.getMessage().contains("reporting.engine"));
            Assert.assertTrue(invalid.getMessage().contains(retired));
        }
    }

    @Test
    public void advertisedSuiteCommandsResolveToMaintainedFiles() {
        Assert.assertTrue(java.nio.file.Files.isRegularFile(AtfSourcePaths.projectPath("atf-tests").resolve(
                AtfCoreAggregateRunCommands.CORE_SUITE_XML)));
    }
}
