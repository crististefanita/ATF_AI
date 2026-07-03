package com.endava.ai.atf.core.support.contract.core.yaml;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlAdvisorSpec;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlReportingArchitectureSpec;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Listeners(TestListener.class)
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.YAML_MANIFEST)
@AtfContractRef(AtfContractNarrativeIds.Contract.YAML_MANIFEST_CATALOG)
public class AtfCoreYamlGovernanceManifestTest {
    private static final Set<String> ALLOWED_ADVISOR_SEVERITIES = Set.of("INFO", "WARN");

    @Test
    public void reportingArchitectureManifestExplainsPublicInternalAuditAndRepairLanes()
            throws ClassNotFoundException {
        Assert.assertFalse(AtfCoreYamlContractCatalog.reportingArchitecture().isEmpty());
        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateReportingArchitectureIds().isEmpty());

        Path root = AtfSourcePaths.workspaceRoot();
        Set<String> ids = AtfCoreYamlContractCatalog.reportingArchitecture().stream()
                .map(AtfCoreYamlReportingArchitectureSpec::id)
                .collect(Collectors.toUnmodifiableSet());
        Assert.assertTrue(ids.containsAll(Set.of(
                "core.reporting.public-api",
                "core.reporting.listener-lifecycle",
                "core.reporting.extent-adapter",
                "core.reporting.spark-sections",
                "core.reporting.runtime-audit",
                "core.reporting.aggregate",
                "core.reporting.diagnostics-console"
        )), "reporting-architecture.yml must cover the main reporting lanes: " + ids);

        for (AtfCoreYamlReportingArchitectureSpec spec : AtfCoreYamlContractCatalog.reportingArchitecture()) {
            Assert.assertTrue(spec.id().startsWith("core.reporting."),
                    "Unexpected reporting architecture id: " + spec.id());
            Assert.assertTrue(Files.exists(root.resolve(spec.sourceRootPath())),
                    "Missing reporting architecture source root for " + spec.id() + ": " + spec.sourceRoot());
            Assert.assertTrue(Files.exists(root.resolve(spec.repairTargetPath())),
                    "Missing reporting architecture repair target for " + spec.id() + ": " + spec.repairTarget());
            Assert.assertTrue(Files.isRegularFile(root.resolve(spec.suiteXml())),
                    "Missing reporting architecture suite for " + spec.id() + ": " + spec.suiteXml());
            Assert.assertFalse(spec.owner().isBlank(), "Missing reporting architecture owner for " + spec.id());
            Assert.assertFalse(spec.publicApi().isBlank(), "Missing public API map for " + spec.id());
            Assert.assertFalse(spec.internalBoundary().isBlank(), "Missing internal boundary map for " + spec.id());
            Assert.assertFalse(spec.auditSurface().isBlank(), "Missing audit surface for " + spec.id());
            Assert.assertFalse(spec.responsibility().isBlank(), "Missing responsibility for " + spec.id());
            loadContractClass(spec.contractTest());
        }
    }

    @Test
    public void coreAdvisorManifestIsNonBlockingAndPointsAtCoreRepairTargets() {
        Assert.assertFalse(AtfCoreYamlContractCatalog.coreAdvisorContracts().isEmpty());
        Assert.assertTrue(AtfCoreYamlContractCatalog.duplicateCoreAdvisorIds().isEmpty());

        Path root = AtfSourcePaths.workspaceRoot();
        Set<String> ids = AtfCoreYamlContractCatalog.coreAdvisorContracts().stream()
                .map(AtfCoreYamlAdvisorSpec::id)
                .collect(Collectors.toUnmodifiableSet());
        Assert.assertTrue(ids.contains("core.advisor.mcp-or-atlas-rule-in-core"));
        Assert.assertTrue(ids.contains("core.advisor.reporting-html-patch-risk"));
        for (AtfCoreYamlAdvisorSpec spec : AtfCoreYamlContractCatalog.coreAdvisorContracts()) {
            Assert.assertTrue(spec.id().startsWith("core.advisor."), "Unexpected core advisor id: " + spec.id());
            Assert.assertTrue(ALLOWED_ADVISOR_SEVERITIES.contains(spec.severity()),
                    "Core advisor severity must stay INFO/WARN for " + spec.id());
            Assert.assertFalse(spec.blocking(),
                    "Core advisor entries are guidance only; executable contracts own blockers: " + spec.id());
            Assert.assertFalse(spec.signal().isBlank(), "Missing advisor signal for " + spec.id());
            Assert.assertFalse(spec.recommendation().isBlank(), "Missing advisor recommendation for " + spec.id());
            Assert.assertFalse(spec.reportAnchor().isBlank(), "Missing advisor report anchor for " + spec.id());
            Assert.assertTrue(Files.exists(root.resolve(spec.repairTargetPath())),
                    "Core advisor repair target must exist for " + spec.id() + ": " + spec.repairTarget());
            Assert.assertTrue(Files.isRegularFile(root.resolve(spec.recommendedProfile())),
                    "Core advisor recommended profile must exist for " + spec.id() + ": " + spec.recommendedProfile());
        }
    }

    @Test
    public void yamlContractCatalogExposesIndexedLookupsForGovernanceContracts() {
        Assert.assertEquals(
                AtfCoreYamlContractCatalog
                        .requireReportingArchitecture("core.reporting.extent-adapter")
                        .repairTarget(),
                AtfSourcePaths.unixPath(AtfSourcePaths.MAIN_CORE_ROOT.resolve("reporting/adapters/extent"))
        );
        Assert.assertEquals(
                AtfCoreYamlContractCatalog.requireCoreAdvisor("core.advisor.reporting-html-patch-risk").severity(),
                "WARN"
        );
    }

    private static Class<?> loadContractClass(String className) throws ClassNotFoundException {
        try {
            return Class.forName(className, false, AtfCoreYamlGovernanceManifestTest.class.getClassLoader());
        } catch (ClassNotFoundException firstFailure) {
            try (URLClassLoader outputLoader = new URLClassLoader(compiledOutputUrls(),
                    AtfCoreYamlGovernanceManifestTest.class.getClassLoader())) {
                return Class.forName(className, false, outputLoader);
            } catch (java.io.IOException outputFailure) {
                ClassNotFoundException enriched = new ClassNotFoundException(
                        "Could not load ATF YAML governance validator from test output: " + className,
                        firstFailure);
                enriched.addSuppressed(outputFailure);
                throw enriched;
            }
        }
    }

    private static URL[] compiledOutputUrls() {
        List<URL> urls = new ArrayList<>();
        addIfDirectory(urls, AtfSourcePaths.TEST_CLASSES_ROOT);
        addIfDirectory(urls, AtfSourcePaths.MAIN_CLASSES_ROOT);
        return urls.toArray(URL[]::new);
    }

    private static void addIfDirectory(List<URL> urls, Path path) {
        if (!Files.isDirectory(path)) {
            return;
        }
        try {
            urls.add(path.toUri().toURL());
        } catch (MalformedURLException exception) {
            throw new IllegalStateException("Could not add compiled output to contract classpath: " + path, exception);
        }
    }
}
