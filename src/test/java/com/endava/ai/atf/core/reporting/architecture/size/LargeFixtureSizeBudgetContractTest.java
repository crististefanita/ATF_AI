package com.endava.ai.atf.core.reporting.architecture.size;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlSizeBudgetSpec;
import com.endava.ai.atf.core.reporting.architecture.support.ArchitectureSourceScanner;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.LARGE_FIXTURE_SIZE_BUDGET_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.LARGE_FIXTURE_SIZE_BUDGET_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class LargeFixtureSizeBudgetContractTest {

    @Test
    public void supportFixturesStayWithinBudgetOrKnownLegacyCap() throws IOException {
        Map<String, Integer> legacyCaps = ArchitectureSourceScanner.legacyLargeTestCaps();
        AtfCoreYamlSizeBudgetSpec supportRoot = supportRootBudget();
        List<String> violations = new ArrayList<>();

        List<Path> supportFiles = java.nio.file.Files.walk(supportRoot.asPath())
                .filter(LargeFixtureSizeBudgetContractTest::isFixtureOrSupportSource)
                .collect(Collectors.toList());

        for (Path file : supportFiles) {
            String normalized = AtfSourcePaths.unixPath(file);
            long lines = ArchitectureSourceScanner.lineCount(file);
            Integer legacyCap = legacyCaps.get(normalized);
            if (legacyCap != null) {
                if (lines > legacyCap) {
                    violations.add(normalized + " grew to " + lines + " lines above legacy cap " + legacyCap);
                }
            } else if (lines > supportRoot.max()) {
                violations.add(normalized + " has " + lines + " lines, max " + supportRoot.max());
            }
        }

        Assert.assertTrue(violations.isEmpty(),
                "Support fixtures must be split into focused helpers before growing further: " + violations);
    }

    private static AtfCoreYamlSizeBudgetSpec supportRootBudget() {
        List<AtfCoreYamlSizeBudgetSpec> budgets = AtfCoreYamlContractCatalog.sizeBudgets().stream()
                .filter(budget -> "support-root".equals(budget.kind()))
                .collect(Collectors.toUnmodifiableList());
        Assert.assertEquals(budgets.size(), 1, "Expected exactly one support-root size budget.");
        return budgets.get(0);
    }

    private static boolean isFixtureOrSupportSource(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.endsWith("Support.java")
                || fileName.endsWith("Fixture.java")
                || fileName.endsWith("Fixtures.java");
    }
}
