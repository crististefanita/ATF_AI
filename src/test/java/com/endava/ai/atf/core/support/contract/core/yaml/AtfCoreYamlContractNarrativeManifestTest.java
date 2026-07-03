package com.endava.ai.atf.core.support.contract.core.yaml;

import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.ids.AtfContractSteps;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlContractAnnotationSpec;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlContractNarrativeSpec;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.listener.internal.contract.AtfContractReportNarrator;
import com.endava.ai.core.reporting.StepLogger;
import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.YAML_NARRATIVE)
@AtfContractRef(AtfContractNarrativeIds.Contract.YAML_NARRATIVE_CATALOG)
@Listeners(TestListener.class)
public class AtfCoreYamlContractNarrativeManifestTest {
    private static final Set<String> ALLOWED_CONTRACT_NARRATIVE_KINDS = Set.of(
            "contract",
            "suite",
            "expectedFailure"
    );

    @Test
    public void contractNarrativeManifestHasValidSchemaAndLookupBoundaries() {
        Assert.assertFalse(AtfCoreYamlContractCatalog.contractNarrativeSpecs().isEmpty());
        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateContractNarrativeIds().isEmpty());

        Assert.assertFalse(AtfCoreYamlContractCatalog.contractNarrativeContracts().isEmpty(),
                "contract-narratives.yml must declare method/class contract rows.");
        Assert.assertFalse(AtfCoreYamlContractCatalog.contractNarrativeSuites().isEmpty(),
                "contract-narratives.yml must declare suite rows.");
        Assert.assertFalse(AtfCoreYamlContractCatalog.contractNarrativeExpectedFailures().isEmpty(),
                "contract-narratives.yml must declare expected-failure rows.");

        Set<String> profileNames = AtfCoreYamlContractCatalog.contractAnnotationProfiles().stream()
                .map(AtfCoreYamlContractAnnotationSpec::name)
                .collect(Collectors.toUnmodifiableSet());
        Set<String> areaNames = AtfCoreYamlContractCatalog.contractAnnotationAreas().stream()
                .map(AtfCoreYamlContractAnnotationSpec::name)
                .collect(Collectors.toUnmodifiableSet());

        for (AtfCoreYamlContractNarrativeSpec spec : AtfCoreYamlContractCatalog.contractNarrativeSpecs()) {
            Assert.assertTrue(ALLOWED_CONTRACT_NARRATIVE_KINDS.contains(spec.kind()),
                    "Invalid contract narrative kind for " + spec.id() + ": " + spec.kind());

            if (spec.isContract()) {
                Assert.assertTrue(spec.id().startsWith("core.contract-narrative."),
                        "Unexpected contract narrative id: " + spec.id());
                Assert.assertTrue(areaNames.contains(spec.contractArea()),
                        "Contract narrative area must map to AtfContractArea metadata for " + spec.id());
                Assert.assertFalse(spec.contractOwner().isBlank(), "Missing owner for " + spec.id());
                Assert.assertFalse(spec.contractExpected().isBlank(), "Missing expected invariant for " + spec.id());
                Assert.assertFalse(spec.contractObserved().isBlank(), "Missing observed result for " + spec.id());
                Assert.assertFalse(spec.contractRepairHint().isBlank(), "Missing repair hint for " + spec.id());
            }

            if (spec.isSuite()) {
                Assert.assertTrue(spec.id().startsWith("core.contract-narrative."),
                        "Unexpected suite narrative id: " + spec.id());
                Assert.assertTrue(profileNames.contains(spec.suiteProfile()),
                        "Suite narrative profile must map to AtfContractProfile metadata for " + spec.id());
                Assert.assertTrue(areaNames.contains(spec.suiteArea()),
                        "Suite narrative area must map to AtfContractArea metadata for " + spec.id());
                Assert.assertFalse(spec.suiteCapability().isBlank(), "Missing capability for " + spec.id());
                Assert.assertFalse(spec.suitePurpose().isBlank(), "Missing purpose for " + spec.id());
            }

            if (spec.isExpectedFailure()) {
                Assert.assertTrue(spec.id().startsWith("core.expected-failure."),
                        "Unexpected expected-failure narrative id: " + spec.id());
                Assert.assertFalse(spec.expectedFailureReason().isBlank(),
                        "Missing expected-failure reason for " + spec.id());
                Assert.assertFalse(spec.expectedFailureSignal().isBlank(),
                        "Missing expected-failure signal for " + spec.id());
            }
        }

        Assert.assertEquals(
                AtfCoreYamlContractCatalog
                        .requireContractNarrativeContract(
                                AtfContractNarrativeIds.Contract.REPORTING_INVENTORY_METHOD)
                        .contractOwner(),
                "ATF-core Extent test inventory"
        );
        Assert.assertEquals(
                AtfCoreYamlContractCatalog
                        .requireContractNarrativeSuite(AtfContractNarrativeIds.Suite.REPORTING_INVENTORY)
                        .suiteProfile(),
                "CORE_CONTRACT"
        );
        Assert.expectThrows(
                IllegalArgumentException.class,
                () -> AtfCoreYamlContractCatalog
                        .requireContractNarrativeContract(AtfContractNarrativeIds.Suite.REPORTING_INVENTORY)
        );
        Assert.assertEquals(
                AtfCoreYamlContractCatalog
                        .requireContractNarrativeExpectedFailure(
                                AtfContractNarrativeIds.ExpectedFailure.API_DELETED_RESOURCE_NOT_READABLE)
                        .expectedFailureSignal(),
                "Unexpected HTTP status"
        );
    }

    @Test
    public void contractNarrativeIdConstantsStaySynchronizedWithYamlCatalog() {
        Set<String> yamlIds = AtfCoreYamlContractCatalog.contractNarrativeSpecs().stream()
                .map(AtfCoreYamlContractNarrativeSpec::id)
                .collect(Collectors.toSet());
        Set<String> constantIds = Stream.of(
                        AtfContractNarrativeIds.Suite.class,
                        AtfContractNarrativeIds.Contract.class,
                        AtfContractNarrativeIds.ExpectedFailure.class)
                .flatMap(type -> Arrays.stream(type.getFields()))
                .filter(field -> field.getType().equals(String.class))
                .filter(AtfCoreYamlContractNarrativeManifestTest::isPublicStaticFinal)
                .map(AtfCoreYamlContractNarrativeManifestTest::constantValue)
                .collect(Collectors.toSet());

        Assert.assertEquals(constantIds, yamlIds,
                "AtfContractNarrativeIds must stay synchronized with contract-narratives.yml.");
    }

    @Test
    public void atfContractNarratorAcceptsYamlBackedRefs() throws NoSuchMethodException {
        ITestResult result = mock(ITestResult.class);
        ITestNGMethod testNgMethod = mock(ITestNGMethod.class);
        Method javaMethod = YamlBackedContractFixture.class.getDeclaredMethod("yamlBackedContract");

        when(result.getMethod()).thenReturn(testNgMethod);
        when(testNgMethod.getConstructorOrMethod()).thenReturn(new ConstructorOrMethod(javaMethod));
        when(testNgMethod.getRealClass()).thenReturn(YamlBackedContractFixture.class);

        try {
            AtfContractReportNarrator.logExpected(result);
            AtfContractReportNarrator.logObserved(result);
            AtfContractReportNarrator.logVerdict(result, true, "YAML-backed ATF contract narrative resolved.");
            Assert.assertFalse(StepLogger.testHasFailed(),
                    "YAML-backed ATF contract refs must resolve without marking the report as failed.");
        } finally {
            StepLogger.clear();
        }
    }

    @Test
    public void atfContractStepsResolveYamlBackedContractDetails() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                YamlBackedContractFixture.class,
                "yamlBackedContract"
        );

        Assert.assertEquals(contract.owner(), "ATF-core Extent test inventory");
        Assert.assertTrue(contract.expected().contains("ATF test sources are visible"));
        Assert.assertTrue(contract.observed().contains("records totals"));
        Assert.assertTrue(contract.repairHint().contains("Use precise profiles"));

        AtfContractSteps.SuiteDetails suite = AtfContractSteps.suiteDetailsFor(YamlBackedContractFixture.class);
        Assert.assertEquals(suite.profile(), "CORE_CONTRACT");
        Assert.assertEquals(suite.area(), "REPORTING_LIFECYCLE");
        Assert.assertEquals(suite.capability(), "ATF contract narrative coverage");
        Assert.assertTrue(suite.reportHint().contains("contract metadata"));
    }

    private static boolean isPublicStaticFinal(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
    }

    private static String constantValue(Field field) {
        try {
            return (String) field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read ATF contract narrative id constant " + field.getName(), e);
        }
    }

    @AtfContractSuiteRef(AtfContractNarrativeIds.Suite.REPORTING_INVENTORY)
    private static final class YamlBackedContractFixture {
        @AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_INVENTORY_METHOD)
        private void yamlBackedContract() {
        }
    }
}
