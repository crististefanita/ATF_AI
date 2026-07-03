package com.ai.atf.core.support.inventory;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.StepLogger;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ai.atf.core.support.inventory.AtfCoreTestInventoryScanner.catalogProfileMatrix;
import static com.ai.atf.core.support.inventory.AtfCoreTestInventoryScanner.catalogProfiles;
import static com.ai.atf.core.support.inventory.AtfCoreTestInventoryScanner.profileMatrix;
import static com.ai.atf.core.support.inventory.AtfCoreTestInventoryScanner.suiteProfiles;
import static com.ai.atf.core.support.inventory.AtfCoreTestInventoryScanner.testSources;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_TEST_INVENTORY)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_TEST_INVENTORY_CLASS)
@Listeners(TestListener.class)
public class AtfCoreTestInventoryExtentAuditTest {

    private static final Path ATF_TEST_ROOT = AtfSourcePaths.TEST_ATF_ROOT;
    private static final String PRODUCT_FLOW_TEST = "ui/RegistrationTests.java";

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.ATF_TEST_INVENTORY_EXTENT_AUDIT)
    public void atfCoreTestInventoryIsVisibleInExtentReport() throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "atfCoreTestInventoryIsVisibleInExtentReport"
        );
        AtfContractSteps.expected(contract);

        List<AtfCoreTestSource> sources = testSources(ATF_TEST_ROOT);
        List<AtfCoreTestSource> executable = sources.stream()
                .filter(AtfCoreTestSource::hasTests)
                .collect(Collectors.toList());
        List<AtfCoreTestSource> contracts = executable.stream()
                .filter(AtfCoreTestSource::hasAtfContract)
                .collect(Collectors.toList());
        List<AtfCoreTestSource> suiteBackedContracts = contracts.stream()
                .filter(AtfCoreTestSource::hasAtfContractSuite)
                .collect(Collectors.toList());
        List<AtfCoreTestSource> listenerBackedContracts = contracts.stream()
                .filter(AtfCoreTestSource::isReportableThroughTestListener)
                .collect(Collectors.toList());
        List<AtfCoreTestSource> selfAuditingContracts = contracts.stream()
                .filter(AtfCoreTestSource::isSelfAuditingExtentContract)
                .collect(Collectors.toList());
        List<AtfCoreTestSource> extentAuditableContracts = contracts.stream()
                .filter(AtfCoreTestSource::isExtentAuditableContract)
                .collect(Collectors.toList());
        List<AtfCoreTestSource> pureFrameworkContracts = suiteBackedContracts.stream()
                .filter(AtfCoreTestSource::isPureFrameworkContract)
                .collect(Collectors.toList());
        List<AtfCoreTestSource> liveBoundaryContracts = suiteBackedContracts.stream()
                .filter(AtfCoreTestSource::isLiveBoundaryContract)
                .collect(Collectors.toList());
        List<AtfCoreTestSource> remainingNonContracts = executable.stream()
                .filter(source -> !source.hasAtfContract())
                .collect(Collectors.toList());
        List<String> unexpectedNonContracts = remainingNonContracts.stream()
                .filter(source -> !source.isIntentionalNonContractExecutable())
                .map(AtfCoreTestSource::summary)
                .collect(Collectors.toList());

        StepLogger.startStep("ATF-core test inventory matrix");
        StepLogger.logDetail("Java test files: " + sources.size());
        StepLogger.logDetail("Executable @Test methods: " + executable.stream().mapToInt(AtfCoreTestSource::testCount).sum());
        StepLogger.logDetail("@AtfContract suites: " + contracts.size());
        StepLogger.logDetail("@AtfContractSuite metadata coverage: " + suiteBackedContracts.size() + " / " + contracts.size());
        StepLogger.logDetail("Listener-backed contract suites: " + listenerBackedContracts.size());
        StepLogger.logDetail("Self-auditing Extent contract suites: " + selfAuditingContracts.size());
        StepLogger.logDetail("Extent-auditable contract suites: " + extentAuditableContracts.size());
        StepLogger.pass("ATF-core inventory totals recorded");

        StepLogger.startStep("ATF capability profile matrix");
        profileMatrix(suiteBackedContracts).forEach((profile, rows) -> {
            StepLogger.logDetail(profile + " -> " + rows.size() + " suite(s)");
            rows.stream()
                    .sorted(Comparator.comparing(AtfCoreTestSource::relativePath))
                    .forEach(source -> StepLogger.logDetail("  - " + source.relativePath() + " | tests=" + source.testCount()));
        });
        StepLogger.pass("ATF capability profile matrix recorded");

        StepLogger.startStep("ATF capability catalog baseline");
        catalogProfileMatrix().forEach((profile, count) ->
                StepLogger.logDetail(profile + " -> " + count + " capability spec(s)"));
        StepLogger.pass("ATF capability catalog baseline recorded");

        StepLogger.startStep("Pure framework contract suites");
        pureFrameworkContracts.stream()
                .sorted(Comparator.comparing(AtfCoreTestSource::relativePath))
                .forEach(source -> StepLogger.logDetail(source.summary()));
        StepLogger.pass("Pure framework contracts are separated from live boundaries");

        StepLogger.startStep("Live UI/API boundary suites");
        liveBoundaryContracts.stream()
                .sorted(Comparator.comparing(AtfCoreTestSource::relativePath))
                .forEach(source -> StepLogger.logDetail(source.summary()));
        StepLogger.pass("Live boundary suites are explicit and not counted as pure core");

        StepLogger.startStep("Extent-auditable contract suites");
        extentAuditableContracts.stream()
                .sorted(Comparator.comparing(AtfCoreTestSource::relativePath))
                .forEach(source -> StepLogger.logDetail(source.summary()));
        StepLogger.pass("Contract-backed suites are listed");

        StepLogger.startStep("Non-auditable contract suites");
        contracts.stream()
                .filter(source -> !source.isExtentAuditableContract())
                .sorted(Comparator.comparing(AtfCoreTestSource::relativePath))
                .forEach(source -> StepLogger.logDetail(source.summary()));
        StepLogger.pass("Non-auditable contract suite inventory recorded");

        StepLogger.startStep("Self-auditing Extent contract suites");
        selfAuditingContracts.stream()
                .sorted(Comparator.comparing(AtfCoreTestSource::relativePath))
                .forEach(source -> StepLogger.logDetail(source.summary()
                        + " | audit=generated Extent HTML opened in browser"));
        StepLogger.pass("Self-auditing Extent suites are explicit");

        StepLogger.startStep("Intentional standalone non-contract executables");
        remainingNonContracts.stream()
                .sorted(Comparator.comparing(AtfCoreTestSource::relativePath))
                .forEach(source -> StepLogger.logDetail(source.summary()));
        StepLogger.pass("Only controlled fixtures or standalone narrative contracts are allowed outside ATF contract metadata");

        StepLogger.startStep("Known product/live-flow boundary");
        sources.stream()
                .filter(source -> source.relativePath().equals(PRODUCT_FLOW_TEST))
                .forEach(source -> StepLogger.logDetail(source.summary() + " | boundary=product flow, not pure ATF-core contract"));
        StepLogger.pass("Product-flow boundary recorded");

        Assert.assertFalse(executable.isEmpty(), "ATF inventory must discover executable tests");
        Assert.assertEquals(
                contracts.size(),
                suiteBackedContracts.size(),
                "Every @AtfContract suite must declare @AtfContractSuite capability metadata"
        );
        Assert.assertEquals(
                contracts.size(),
                extentAuditableContracts.size(),
                "Every @AtfContract suite must be Extent-auditable either through TestListener or a self-auditing Extent browser contract"
        );
        Assert.assertTrue(
                suiteBackedContracts.stream()
                        .filter(source -> source.relativePath().startsWith("api/"))
                        .allMatch(source -> "LIVE_API_SMOKE_CONTRACT".equals(source.suiteProfile())),
                "API live suites under com.ai.atf/api must be marked as LIVE_API_SMOKE_CONTRACT, not pure ATF-core"
        );
        Assert.assertTrue(
                suiteBackedContracts.stream()
                        .filter(source -> source.relativePath().equals(PRODUCT_FLOW_TEST))
                        .allMatch(source -> "UI_PRODUCT_SMOKE_CONTRACT".equals(source.suiteProfile())),
                "RegistrationTests must remain a UI_PRODUCT_SMOKE_CONTRACT boundary, not pure ATF-core"
        );
        Assert.assertTrue(
                suiteBackedContracts.stream()
                        .map(AtfCoreTestSource::profileMetadata)
                        .allMatch(Optional::isPresent),
                "Every @AtfContractSuite profile must map to AtfContractProfile metadata"
        );
        Assert.assertTrue(
                catalogProfiles().containsAll(suiteProfiles(suiteBackedContracts)),
                "Every suite profile used by ATF-core tests must be represented in AtfCapabilityCatalog"
        );
        Assert.assertTrue(
                suiteBackedContracts.stream()
                        .filter(AtfCoreTestSource::isAllureRecommended)
                        .allMatch(AtfCoreTestSource::hasAllureOnlyGroup),
                "Allure contract suites must be tagged with allure-only so Extent sweeps can exclude them"
        );
        Assert.assertTrue(
                selfAuditingContracts.stream()
                        .allMatch(AtfCoreTestSource::hasExtentOnlyGroup),
                "Self-auditing Extent suites must be tagged with extent-only so Allure sweeps can exclude browser/HTML-only contracts"
        );
        Assert.assertTrue(
                unexpectedNonContracts.isEmpty(),
                "Only controlled failure fixtures or standalone narrative contracts may remain non-contract executable sources: "
                        + unexpectedNonContracts
        );

        AtfContractSteps.observed(
                "inventory.files=" + sources.size()
                        + " | executable.methods=" + executable.stream().mapToInt(AtfCoreTestSource::testCount).sum()
                        + " | contract.suites=" + contracts.size()
                        + " | contract.suite.metadata=" + suiteBackedContracts.size()
                        + " | listener.backed.contracts=" + listenerBackedContracts.size()
                        + " | self.auditing.extent.contracts=" + selfAuditingContracts.size()
                        + " | pure.framework.contracts=" + pureFrameworkContracts.size()
                        + " | live.boundary.contracts=" + liveBoundaryContracts.size()
        );
        AtfContractSteps.verdict(
                "The generated report now separates pure ATF-core contracts from live UI/API smoke boundaries.",
                contract.repairHint()
        );
    }

}
