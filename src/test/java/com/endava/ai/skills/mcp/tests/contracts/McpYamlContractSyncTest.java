package com.endava.ai.skills.mcp.tests.contracts;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalogSync;
import com.endava.ai.skills.mcp.contracts.model.KpiSpec;
import com.endava.ai.skills.mcp.contracts.policy.McpReportHyperlinkSpec;
import com.endava.ai.skills.mcp.contracts.policy.McpReportTemplateSpec;
import com.endava.ai.skills.mcp.contracts.policy.McpScoreCapSpec;
import com.endava.ai.skills.mcp.contracts.policy.McpScoreTruthSyncSpec;
import com.endava.ai.skills.mcp.contracts.policy.McpTestNgProfileSpec;
import com.endava.ai.skills.mcp.rules.source.ProtectedSourceSurface;
import com.endava.ai.skills.mcp.rules.source.kpi.SourceEvolutionKpiCatalog;
import com.endava.ai.skills.mcp.validation.profile.McpReportOpeningProfile;
import com.endava.ai.skills.mcp.validation.profile.McpScoreOverviewProfile;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-yaml-contract-sync-test")
public class McpYamlContractSyncTest {
    @Test
    @McpCheckRef("mcp.yaml-contract.kpi-sync.executable-validator")
    public void nonMetadataYamlKpisDeclareExecutableValidator() throws ClassNotFoundException {
        List<KpiSpec> specs = ContractCatalog.sourceEvolutionKpis();
        Assert.assertTrue(ContractCatalogSync.specsWithoutExecutableValidator(specs).isEmpty(),
                "YAML KPIs without validator or metadataOnly=true: "
                        + ContractCatalogSync.specsWithoutExecutableValidator(specs));

        for (KpiSpec spec : specs) {
            if (!spec.metadataOnly()) {
                loadContractClass(spec.validator());
            }
        }
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.kpi-sync.catalog-facade")
    public void javaKpiCatalogFacadeMatchesYamlCatalog() {
        Set<String> yamlBlockers = ContractCatalog.sourceEvolutionKpis().stream()
                .filter(spec -> "BLOCKER".equals(spec.severity()))
                .map(KpiSpec::id)
                .collect(Collectors.toSet());
        Set<String> javaBlockers = SourceEvolutionKpiCatalog.rootKpis().stream()
                .map(SourceEvolutionKpiCatalog.KpiSpec::id)
                .collect(Collectors.toSet());
        Set<String> yamlDiagnostics = ContractCatalog.sourceEvolutionKpis().stream()
                .filter(spec -> "INFO".equals(spec.severity()))
                .map(KpiSpec::id)
                .collect(Collectors.toSet());
        Set<String> javaDiagnostics = SourceEvolutionKpiCatalog.diagnosticKpis().stream()
                .map(SourceEvolutionKpiCatalog.KpiSpec::id)
                .collect(Collectors.toSet());

        Assert.assertEquals(javaBlockers, yamlBlockers, "Java blocker KPI facade must match YAML.");
        Assert.assertEquals(javaDiagnostics, yamlDiagnostics, "Java diagnostic KPI facade must match YAML.");
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.profile-sync.testng")
    public void testNgProfileYamlMatchesSuiteXmlFiles() throws IOException {
        for (McpTestNgProfileSpec profile : ContractCatalog.testNgProfiles()) {
            Path suite = Path.of(profile.suiteXml());
            Assert.assertTrue(Files.isRegularFile(suite), "Missing TestNG suite for profile " + profile.id() + ": " + suite);
            String xml = Files.readString(suite);

            for (String expectedClass : profile.expectedClasses()) {
                Assert.assertTrue(xml.contains("class name=\"" + expectedClass + "\""),
                        profile.id() + " must include expected class " + expectedClass);
            }
            for (String requiredClass : profile.requiredClasses()) {
                Assert.assertTrue(xml.contains("class name=\"" + requiredClass + "\""),
                        profile.id() + " must include required class " + requiredClass);
            }
            for (String fragment : profile.requiredClassFragments()) {
                Assert.assertTrue(xml.contains(fragment),
                        profile.id() + " must include class fragment " + fragment);
            }
            for (String expectedPackage : profile.expectedPackages()) {
                Assert.assertTrue(xml.contains("package name=\"" + expectedPackage + "\""),
                        profile.id() + " must include expected package " + expectedPackage);
            }
            for (String excludedGroup : profile.excludedGroups()) {
                Assert.assertTrue(xml.contains("exclude name=\"" + excludedGroup + "\""),
                        profile.id() + " must exclude TestNG group " + excludedGroup);
            }
            for (String forbiddenClass : profile.forbiddenClasses()) {
                Assert.assertFalse(xml.contains("class name=\"" + forbiddenClass + "\""),
                        profile.id() + " must not include forbidden class " + forbiddenClass);
            }
            for (String fragment : profile.forbiddenClassFragments()) {
                Assert.assertFalse(xml.contains(fragment),
                        profile.id() + " must not include forbidden class fragment " + fragment);
            }
        }

        Assert.assertEquals(
                Files.readString(Path.of(ContractCatalog.requireTestNgProfile("mcp-package-closeout").suiteXml()))
                        .replace("MCP Package Close-Out", "MCP Package Audit")
                        .replace("Final publishability evidence: same checks as package-audit, but this frozen profile is the canonical close-out artifact source.",
                                "Pre-closeout diagnostic: same publishability checks as close-out, but not the final frozen evidence unless rerun as package-closeout.")
                        .replace("mcp-package-closeout", "mcp-package-audit"),
                Files.readString(Path.of(ContractCatalog.requireTestNgProfile("mcp-package-audit").suiteXml())),
                "package-closeout must stay class-for-class equivalent to package-audit."
        );
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.protected-roots-sync")
    public void protectedRootYamlMatchesExecutableSurface() {
        Set<String> yamlPaths = ContractCatalog.protectedRootContracts().stream()
                .map(entry -> normalize(entry.path()))
                .collect(Collectors.toSet());
        Set<String> javaPaths = ProtectedSourceSurface.entries().stream()
                .map(ProtectedSourceSurface.Entry::prefix)
                .map(McpYamlContractSyncTest::normalize)
                .collect(Collectors.toSet());

        Assert.assertEquals(yamlPaths, javaPaths,
                "Declarative protected roots must match executable protected source surface.");
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.policy-sync.validators")
    public void policyYamlValidatorClassesAreLoadable() throws ClassNotFoundException {
        for (String validator : policyValidatorClassNames()) {
            loadContractClass(validator);
        }
        for (McpScoreTruthSyncSpec spec : ContractCatalog.scoreTruthSyncContracts()) {
            loadContractClass(spec.validator());
        }
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.score-cap-sync")
    public void scoreCapYamlCoversKnownCloseoutBlockers() {
        Set<String> ids = ContractCatalog.scoreCapContracts().stream()
                .map(McpScoreCapSpec::id)
                .collect(Collectors.toSet());

        Assert.assertTrue(ids.contains("mcp.score-cap.missing-closeout-artifact"));
        Assert.assertTrue(ids.contains("mcp.score-cap.wrong-closeout-profile"));
        Assert.assertTrue(ids.contains("mcp.score-cap.skipped-closeout"));
        Assert.assertTrue(ids.contains("mcp.score-cap.protected-root-drift"));
        Assert.assertTrue(ids.contains("mcp.score-cap.stale-final-links"));

        Set<String> truthSyncIds = ContractCatalog.scoreTruthSyncContracts().stream()
                .map(McpScoreTruthSyncSpec::id)
                .collect(Collectors.toSet());
        Assert.assertTrue(truthSyncIds.contains("mcp.score-truth.package-state-final-score"));
        Assert.assertTrue(truthSyncIds.contains("mcp.score-truth.latest-mcp-links"));
        Assert.assertTrue(truthSyncIds.contains("mcp.score-truth.kpi-matrix-cap-rationale"));
        Assert.assertTrue(truthSyncIds.contains("mcp.score-truth.closeout-profile-command"));
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.report-template-sync")
    public void reportTemplateYamlBacksJavaReportProfiles() {
        Assert.assertEquals(McpReportOpeningProfile.canonicalScoreCards(),
                ContractCatalog.reportTemplateTokens("score-card-label"));
        Assert.assertEquals(McpReportOpeningProfile.contractArtifactLabels(),
                ContractCatalog.reportTemplateTokens("contract-artifact-label"));
        Assert.assertEquals(McpReportOpeningProfile.executiveContextEvidence(),
                ContractCatalog.reportTemplateTokens("executive-context-evidence"));
        Assert.assertEquals(McpReportOpeningProfile.runtimeConstantsAndCounters(),
                ContractCatalog.reportTemplateTokens("runtime-token"));
        Assert.assertEquals(McpReportOpeningProfile.forbiddenCalibrationTokens(),
                ContractCatalog.reportTemplateTokens("forbidden-calibration-token"));

        List<String> blockers = McpScoreOverviewProfile.canonicalBlockers("");
        Set<String> yamlMessages = ContractCatalog.reportTemplateContracts().stream()
                .filter(entry -> "score-overview-any".equals(entry.kind()))
                .map(McpReportTemplateSpec::message)
                .collect(Collectors.toSet());
        for (String message : yamlMessages) {
            Assert.assertTrue(blockers.stream().anyMatch(blocker -> blocker.startsWith(message)),
                    "Score overview Java profile must surface YAML message: " + message);
        }
    }

    private static List<String> policyValidatorClassNames() {
        ArrayList<String> validators = new ArrayList<>();
        ContractCatalog.reportHyperlinkContracts().stream()
                .map(McpReportHyperlinkSpec::validator)
                .forEach(validators::add);
        ContractCatalog.scoreCapContracts().stream()
                .map(McpScoreCapSpec::validator)
                .forEach(validators::add);
        ContractCatalog.htmlExCorpusContracts().stream()
                .map(spec -> spec.validator().trim())
                .filter(validator -> !validator.isEmpty())
                .forEach(validators::add);
        return List.copyOf(validators);
    }

    private static String normalize(String value) {
        return value.replace('\\', '/').trim();
    }

    private static Class<?> loadContractClass(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className, false, McpYamlContractSyncTest.class.getClassLoader());
        } catch (ClassNotFoundException firstFailure) {
            try {
                return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException secondFailure) {
                try (URLClassLoader outputLoader = new URLClassLoader(compiledOutputUrls(),
                        McpYamlContractSyncTest.class.getClassLoader())) {
                    return Class.forName(className, false, outputLoader);
                } catch (java.io.IOException outputFailure) {
                    ClassNotFoundException enriched = new ClassNotFoundException(
                            "Could not load MCP YAML contract validator from test output: " + className,
                            secondFailure);
                    enriched.addSuppressed(firstFailure);
                    enriched.addSuppressed(outputFailure);
                    throw enriched;
                }
            }
        }
    }

    private static URL[] compiledOutputUrls() {
        List<URL> urls = new ArrayList<>();
        addIfDirectory(urls, Path.of("target/test-classes"));
        addIfDirectory(urls, Path.of("target/classes"));
        return urls.toArray(URL[]::new);
    }

    private static void addIfDirectory(List<URL> urls, Path path) {
        if (!Files.isDirectory(path)) {
            return;
        }
        try {
            urls.add(path.toUri().toURL());
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("Could not add compiled output to MCP contract classpath: " + path,
                    exception);
        }
    }
}
