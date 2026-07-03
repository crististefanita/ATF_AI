package com.ai.atf.core.support.contract.capability;

import com.ai.core.support.contract.capability.AtfCapabilityCatalog;
import com.ai.core.support.contract.capability.AtfCapabilitySeverity;
import com.ai.core.support.contract.capability.AtfCapabilitySpec;
import com.ai.core.support.contract.capability.AtfCoreOwnershipMap;
import com.ai.core.support.contract.capability.AtfCoreReportingArchitectureMap;
import com.ai.core.support.contract.AtfContractProfile;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.StepLogger;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CAPABILITY_CATALOG)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CAPABILITY_CATALOG_CLASS)
@Listeners(TestListener.class)
public class AtfCapabilityCatalogContractTest {
    private static final Path OWNERSHIP_MATRIX = AtfSourcePaths.TARGET_ROOT.resolve("atf-core-ownership-matrix.md");
    private static final Path CORE_README = AtfSourcePaths.MAIN_CORE_ROOT.resolve(AtfSourcePaths.README_FILE_NAME);

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CAPABILITY_CATALOG_PROFILE_COVERAGE)
    public void atfCapabilityCatalogCoversExecutionProfiles() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "atfCapabilityCatalogCoversExecutionProfiles"
        );
        AtfContractSteps.expected(contract);

        List<AtfCapabilitySpec> specs = AtfCapabilityCatalog.all();
        Assert.assertFalse(specs.isEmpty(), "ATF capability catalog must not be empty");
        assertUniqueIds(specs);
        assertAllProfilesCovered(specs);
        assertCriticalCapabilitiesAreBlockers(specs);
        assertLiveBoundaryCapabilitiesAreExplicit(specs);
        assertReportExpectationsAreExplicit(specs);
        assertSuiteAndEvidenceAreExplicit(specs);
        assertAllureCapabilitiesAreEvidenceContracts(specs);

        AtfContractSteps.observed("catalogEntries=" + specs.size()
                + ", profiles=" + profileSummary(specs));
        publishCapabilityMatrix(specs);
        AtfContractSteps.verdict(
                "ATF-core now has a readable capability catalog instead of relying only on scattered test class names.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CAPABILITY_CATALOG_REPORTING_MAP)
    public void reportingArchitectureMapExplainsExtentOwnershipLanes() {
        Assert.assertTrue(
                Files.exists(testResource(AtfCoreReportingArchitectureMap.RESOURCE)),
                "Reporting architecture map must be a YAML resource: " + AtfCoreReportingArchitectureMap.RESOURCE
        );

        List<AtfCoreReportingArchitectureMap.Zone> zones = AtfCoreReportingArchitectureMap.zones();
        Set<String> zoneIds = zones.stream()
                .map(AtfCoreReportingArchitectureMap.Zone::id)
                .collect(Collectors.toSet());

        Assert.assertTrue(zoneIds.containsAll(Set.of(
                "lifecycle",
                "step-console",
                "runtime-audit",
                "extent-patch-pipeline",
                "extent-visual-shell",
                "screenshot-evidence",
                "aggregate-report"
        )), "Reporting map must cover the core reporting/Extent lanes: " + zoneIds);

        for (AtfCoreReportingArchitectureMap.Zone zone : zones) {
            AtfCapabilityCatalog.require(zone.capabilityId());
            Assert.assertTrue(Files.exists(zone.sourceRoot()),
                    "Reporting map source root must exist for zone " + zone.id() + ": " + zone.sourceRoot());
            assertProjectFileExists(zone.suiteXml(), "Reporting map suite XML", zone.id());
            Assert.assertTrue(Files.exists(contractTestSourcePath(zone.contractTest())),
                    "Reporting map contract test must exist for zone " + zone.id() + ": " + zone.contractTest());
            Assert.assertFalse(zone.owner().isBlank(), "Zone owner must be explicit: " + zone.id());
            Assert.assertFalse(zone.input().isBlank(), "Zone input must be explicit: " + zone.id());
            Assert.assertFalse(zone.output().isBlank(), "Zone output must be explicit: " + zone.id());
            Assert.assertFalse(zone.responsibility().isBlank(), "Zone responsibility must be explicit: " + zone.id());
        }

        StepLogger.startStep("ATF core reporting architecture map");
        for (AtfCoreReportingArchitectureMap.Zone zone : zones) {
            StepLogger.logDetail(zone.id()
                    + " | " + zone.capabilityId()
                    + " | " + zone.owner()
                    + " | " + zone.sourceRoot()
                    + " | input=" + zone.input()
                    + " | output=" + zone.output()
                    + " | contract=" + zone.contractTest()
                    + " | suite=" + zone.suiteXml()
                    + " | " + zone.responsibility());
        }
        StepLogger.pass("ATF core reporting architecture map recorded");
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CAPABILITY_CATALOG_OWNERSHIP_MAP)
    public void coreOwnershipMapExplainsRepairTargetsAndRuntimeBoundaries() throws IOException {
        Assert.assertTrue(
                Files.exists(testResource(AtfCoreOwnershipMap.RESOURCE)),
                "Core ownership map must be a YAML resource: " + AtfCoreOwnershipMap.RESOURCE
        );

        List<AtfCoreOwnershipMap.Zone> zones = AtfCoreOwnershipMap.zones();
        Set<String> zoneIds = zones.stream()
                .map(AtfCoreOwnershipMap.Zone::id)
                .collect(Collectors.toSet());

        Assert.assertTrue(zoneIds.containsAll(Set.of(
                "profile-taxonomy",
                "capability-catalog",
                "mcp-boundary",
                "lifecycle",
                "step-console",
                "thread-local-lifecycle",
                "runtime-audit",
                "extent-patch-pipeline",
                "extent-visual-shell",
                "screenshot-evidence",
                "api-core",
                "ui-engine",
                "ui-wait-policy",
                "aggregate-report"
        )), "Core ownership map must cover the reusable ATF runtime lanes: " + zoneIds);

        for (AtfCoreOwnershipMap.Zone zone : zones) {
            AtfCapabilityCatalog.require(zone.capabilityId());
            Assert.assertTrue(Files.exists(zone.sourceRoot()),
                    "Ownership source root must exist for zone " + zone.id() + ": " + zone.sourceRoot());
            Assert.assertTrue(Files.exists(zone.repairTarget()),
                    "Ownership repair target must exist for zone " + zone.id() + ": " + zone.repairTarget());
            assertProjectFileExists(zone.suiteXml(), "Ownership suite XML", zone.id());
            Assert.assertTrue(Files.exists(contractTestSourcePath(zone.contractTest())),
                    "Ownership contract test must exist for zone " + zone.id() + ": " + zone.contractTest());
            Assert.assertFalse(zone.owner().isBlank(), "Zone owner must be explicit: " + zone.id());
            Assert.assertFalse(zone.input().isBlank(), "Zone input must be explicit: " + zone.id());
            Assert.assertFalse(zone.output().isBlank(), "Zone output must be explicit: " + zone.id());
            Assert.assertFalse(zone.responsibility().isBlank(), "Zone responsibility must be explicit: " + zone.id());
        }

        Path matrix = OWNERSHIP_MATRIX;
        Files.createDirectories(matrix.getParent());
        Files.writeString(matrix, AtfCoreOwnershipMap.toMarkdown(), StandardCharsets.UTF_8);

        StepLogger.startStep("ATF core ownership matrix");
        StepLogger.logDetail("Matrix artifact: " + matrix);
        for (AtfCoreOwnershipMap.Zone zone : zones) {
            StepLogger.logDetail(zone.id()
                    + " | capability=" + zone.capabilityId()
                    + " | owner=" + zone.owner()
                    + " | source=" + zone.sourceRoot()
                    + " | repairTarget=" + zone.repairTarget()
                    + " | contract=" + zone.contractTest()
                    + " | suite=" + zone.suiteXml());
        }
        StepLogger.pass("ATF core ownership matrix recorded");
    }

    @Test
    public void coreLibraryReadmeExplainsSkillBoundariesAndVerification() throws IOException {
        Assert.assertTrue(Files.isRegularFile(CORE_README),
                "ATF Core needs a package-level README that explains the reusable library boundary.");

        String readme = Files.readString(CORE_README);
        for (String section : List.of("## Purpose", "## Entry Points", "## Change Here When",
                "## Do Not", "## Verification")) {
            Assert.assertTrue(readme.contains(section), "Missing Core README section: " + section);
        }
        Assert.assertTrue(readme.contains("reusable ATF runtime library"),
                "Core README must describe Core as a reusable library, not a validator skill.");
        Assert.assertTrue(readme.contains("Do not add MCP") && readme.contains("Do not add Atlas"),
                "Core README must make MCP/Atlas ownership boundaries explicit.");
    }

    private static void assertUniqueIds(List<AtfCapabilitySpec> specs) {
        Set<String> ids = specs.stream()
                .map(AtfCapabilitySpec::id)
                .collect(Collectors.toSet());
        Assert.assertEquals(ids.size(), specs.size(), "ATF capability ids must be unique");
    }

    private static void assertAllProfilesCovered(List<AtfCapabilitySpec> specs) {
        Set<AtfContractProfile> coveredProfiles = specs.stream()
                .map(AtfCapabilitySpec::profile)
                .collect(Collectors.toSet());
        Assert.assertTrue(
                coveredProfiles.containsAll(EnumSet.allOf(AtfContractProfile.class)),
                "Every ATF contract profile must be represented in the capability catalog. Covered="
                        + coveredProfiles
        );
    }

    private static void assertCriticalCapabilitiesAreBlockers(List<AtfCapabilitySpec> specs) {
        Set<String> blockerIds = specs.stream()
                .filter(spec -> spec.severity() == AtfCapabilitySeverity.BLOCKER)
                .map(AtfCapabilitySpec::id)
                .collect(Collectors.toSet());
        Assert.assertTrue(blockerIds.contains("core.profile.taxonomy"));
        Assert.assertTrue(blockerIds.contains("core.contract.inventory"));
        Assert.assertTrue(blockerIds.contains("reporting.extent.aggregate"));
        Assert.assertTrue(blockerIds.contains("reporting.extent.spark.presentation"));
        Assert.assertTrue(blockerIds.contains("reporting.extent.failure-placement"));
    }

    private static void assertLiveBoundaryCapabilitiesAreExplicit(List<AtfCapabilitySpec> specs) {
        List<AtfCapabilitySpec> liveBoundarySpecs = specs.stream()
                .filter(spec -> spec.profile().isLiveBoundary())
                .collect(Collectors.toList());
        Assert.assertFalse(liveBoundarySpecs.isEmpty(), "Live boundary profiles must be explicit in the catalog");
        for (AtfCapabilitySpec spec : liveBoundarySpecs) {
            Assert.assertNotEquals(
                    spec.severity(),
                    AtfCapabilitySeverity.BLOCKER,
                    "Live smoke boundary should not become a blocker for pure ATF-core health: " + spec.id()
            );
        }
    }

    private static void assertReportExpectationsAreExplicit(List<AtfCapabilitySpec> specs) {
        for (AtfCapabilitySpec spec : specs) {
            Assert.assertFalse(
                    spec.reportExpectation().isBlank(),
                    "Every ATF capability must state what evidence/report shape proves it: " + spec.id()
            );
        }
    }

    private static void assertSuiteAndEvidenceAreExplicit(List<AtfCapabilitySpec> specs) {
        for (AtfCapabilitySpec spec : specs) {
            Assert.assertFalse(spec.suiteXml().isBlank(),
                    "Every ATF capability must name its canonical suite XML: " + spec.id());
            assertProjectFileExists(spec.suiteXml(), "Capability suite XML", spec.id());
            Assert.assertFalse(spec.primaryEvidence().isBlank(),
                    "Every ATF capability must name the primary evidence artifact/surface: " + spec.id());
        }
    }

    private static void assertAllureCapabilitiesAreEvidenceContracts(List<AtfCapabilitySpec> specs) {
        List<AtfCapabilitySpec> allureSpecs = specs.stream()
                .filter(spec -> spec.profile().isAllureOnly())
                .collect(Collectors.toList());
        Assert.assertFalse(allureSpecs.isEmpty(), "Allure capabilities must be explicit");
        for (AtfCapabilitySpec spec : allureSpecs) {
            String expectation = spec.reportExpectation().toLowerCase();
            Assert.assertTrue(
                    expectation.contains("evidence") || expectation.contains("results"),
                    "Allure capability must be described as evidence/results contract: " + spec.id()
            );
            Assert.assertTrue(
                    expectation.contains("no extent visual-layout parity"),
                    "Allure must not be treated as a visual clone of Extent: " + spec.id()
            );
        }
    }

    private static Path contractTestSourcePath(String className) {
        return AtfSourcePaths.testJava(className);
    }

    private static Path testResource(String resource) {
        return AtfSourcePaths.projectPath(AtfSourcePaths.TEST_RESOURCES_ROOT.resolve(resource));
    }

    private static void assertProjectFileExists(String relativePath, String label, String ownerId) {
        Assert.assertTrue(Files.exists(AtfSourcePaths.projectPath(relativePath)),
                label + " must exist for " + ownerId + ": " + relativePath);
    }

    private static String profileSummary(List<AtfCapabilitySpec> specs) {
        Map<AtfContractProfile, Long> countsByProfile = specs.stream()
                .collect(Collectors.groupingBy(
                        AtfCapabilitySpec::profile,
                        () -> new java.util.EnumMap<>(AtfContractProfile.class),
                        Collectors.counting()
                ));
        return countsByProfile.entrySet().stream()
                .map(entry -> entry.getKey().name() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    private static void publishCapabilityMatrix(List<AtfCapabilitySpec> specs) {
        StepLogger.startStep("ATF capability catalog matrix");
        for (AtfCapabilitySpec spec : specs) {
            StepLogger.logDetail(spec.id()
                    + " | " + spec.profile()
                    + " | " + spec.area()
                    + " | " + spec.severity()
                    + " | " + spec.owner()
                    + " | suite=" + spec.suiteXml()
                    + " | evidence=" + spec.primaryEvidence()
                    + " | " + spec.reportExpectation());
        }
        StepLogger.pass("ATF capability catalog matrix recorded");
    }
}
