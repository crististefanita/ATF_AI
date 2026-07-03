package com.endava.ai.atf.core.support.profiles;

import com.endava.ai.core.support.contract.AtfContractArea;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractProfile;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfContractSteps;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlContractAnnotationSpec;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CONTRACT_PROFILE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CONTRACT_PROFILE_CLASS)
@Listeners(TestListener.class)
public class AtfContractProfileContractTest {

    private static final Set<String> SUPPORTED_ENGINES = Set.of(
            ReportingConfigKeys.ENGINE_EXTENT,
            ReportingConfigKeys.ENGINE_ALLURE
    );
    private static final Set<String> SUPPORTED_ANNOTATION_KINDS = Set.of("profile", "area");

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CONTRACT_PROFILE_BOUNDARIES)
    public void atfContractProfilesDeclareCoherentExecutionBoundaries() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "atfContractProfilesDeclareCoherentExecutionBoundaries"
        );
        AtfContractSteps.expected(contract);

        for (AtfContractProfile profile : AtfContractProfile.values()) {
            Assert.assertTrue(
                    SUPPORTED_ENGINES.contains(profile.recommendedEngine()),
                    "Unsupported reporting engine for " + profile + ": " + profile.recommendedEngine()
            );
            Assert.assertFalse(
                    profile.isPureFramework() && profile.isLiveBoundary(),
                    profile + " cannot be both pure framework and live boundary"
            );
        }

        Assert.assertTrue(AtfContractProfile.ALLURE_CONTRACT.isAllureOnly());
        Assert.assertTrue(AtfContractProfile.EXTENT_CONTRACT.isExtentRecommended());
        Assert.assertTrue(AtfContractProfile.LIVE_API_SMOKE_CONTRACT.isLiveBoundary());
        Assert.assertTrue(AtfContractProfile.UI_PRODUCT_SMOKE_CONTRACT.isLiveBoundary());
        Assert.assertFalse(AtfContractProfile.LIVE_API_SMOKE_CONTRACT.isPureFramework());
        Assert.assertFalse(AtfContractProfile.UI_PRODUCT_SMOKE_CONTRACT.isPureFramework());

        AtfContractSteps.observed(
                "profiles=" + Arrays.stream(AtfContractProfile.values())
                        .map(profile -> profile.name()
                                + "[engine=" + profile.recommendedEngine()
                                + ",pure=" + profile.isPureFramework()
                                + ",live=" + profile.isLiveBoundary()
                                + "]")
                        .collect(Collectors.joining(", "))
        );
        AtfContractSteps.verdict(
                "ATF contract profiles now encode routing boundaries instead of relying on duplicated string lists.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CONTRACT_PROFILE_YAML_TAXONOMY)
    public void atfContractAnnotationTaxonomyIsYamlBacked() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "atfContractAnnotationTaxonomyIsYamlBacked"
        );
        AtfContractSteps.expected(contract);

        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateContractAnnotationIds().isEmpty(),
                "ATF contract annotation YAML ids must be unique.");
        Assert.assertTrue(AtfCoreYamlContractCatalog.contractAnnotationSpecs().stream()
                        .map(AtfCoreYamlContractAnnotationSpec::kind)
                        .allMatch(SUPPORTED_ANNOTATION_KINDS::contains),
                "ATF contract annotation YAML must only use supported kinds: " + SUPPORTED_ANNOTATION_KINDS);

        Set<String> yamlProfiles = AtfCoreYamlContractCatalog.contractAnnotationProfiles().stream()
                .map(AtfCoreYamlContractAnnotationSpec::name)
                .collect(Collectors.toUnmodifiableSet());
        Set<String> enumProfiles = Arrays.stream(AtfContractProfile.values())
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());
        Assert.assertEquals(yamlProfiles, enumProfiles,
                "contract-annotations.yml must describe every AtfContractProfile enum exactly once.");

        Set<String> yamlAreas = AtfCoreYamlContractCatalog.contractAnnotationAreas().stream()
                .map(AtfCoreYamlContractAnnotationSpec::name)
                .collect(Collectors.toUnmodifiableSet());
        Set<String> enumAreas = Arrays.stream(AtfContractArea.values())
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());
        Assert.assertEquals(yamlAreas, enumAreas,
                "contract-annotations.yml must describe every AtfContractArea enum exactly once.");

        for (AtfContractProfile profile : AtfContractProfile.values()) {
            AtfCoreYamlContractAnnotationSpec spec =
                    AtfCoreYamlContractCatalog.contractAnnotationProfile(profile.name());
            Assert.assertEquals(profile.recommendedEngine(), spec.recommendedEngine());
            Assert.assertEquals(profile.isPureFramework(), spec.pureFramework());
            Assert.assertEquals(profile.isLiveBoundary(), spec.liveBoundary());
            Assert.assertFalse(spec.owner().isBlank(), "Missing annotation owner for " + spec.id());
            Assert.assertFalse(spec.purpose().isBlank(), "Missing annotation purpose for " + spec.id());
        }

        AtfContractSteps.observed(
                "yaml.profiles=" + yamlProfiles.size()
                        + " | yaml.areas=" + yamlAreas.size()
                        + " | annotation.ids.duplicates=0"
        );
        AtfContractSteps.verdict(
                "ATF contract annotation taxonomy is now declarative and guarded by enum-to-YAML parity.",
                contract.repairHint()
        );
    }
}
