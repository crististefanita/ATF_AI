package com.ai.atf.core.reporting.architecture;

import com.ai.atf.core.reporting.architecture.support.ArchitectureSourceScanner;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.graph.BusinessGraphLayoutAdapter;
import com.ai.core.reporting.htmlcontract.validation.HtmlReportShellValidator;
import com.ai.skills.reporting.ReportSkillSurface;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.REPORTING_PACKAGE_SHAPE)
@AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_CLASS)
@Listeners(TestListener.class)
public class ReportingSkillSurfaceContractTest {
    private static final String SKILL_ARCHITECTURE = "atf-core/contracts/skills/skill-architecture.yml";

    @Test
    public void reportingSkillSurfaceMatchesArchitectureContract() {
        Map<String, String> reporting = SimpleYamlContractLoader.load(SKILL_ARCHITECTURE).stream()
                .filter(entry -> "skill.reporting".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing skill.reporting architecture row"));

        Assert.assertEquals(ReportSkillSurface.IMPLEMENTATION_PACKAGE, "com.ai.core.reporting");
        Assert.assertEquals(ReportSkillSurface.SANITY_SUITE, reporting.get("sanitySuite"));
        Assert.assertEquals(ReportSkillSurface.DEEP_SUITE, reporting.get("deepSuite"));
        Assert.assertTrue(SimpleYamlContractLoader.load(SKILL_ARCHITECTURE).stream()
                        .anyMatch(entry -> ReportSkillSurface.INTEGRATION_SUITE.equals(entry.get("suiteXml"))),
                "skill-architecture.yml must publish the Reporting integration suite.");
        Assert.assertTrue(SimpleYamlContractLoader.load(SKILL_ARCHITECTURE).stream()
                        .anyMatch(entry -> ReportSkillSurface.CONSUMER_SMOKE_SUITE.equals(entry.get("suiteXml"))),
                "skill-architecture.yml must publish the Reporting consumer smoke suite.");
        Assert.assertEquals(reporting.get("implementationRoot"), "src/main/java/com/ai/core/reporting");
        Assert.assertTrue(reporting.get("role").contains("facade"),
                "Reporting skill row must stay explicit that skills.reporting is a facade.");
    }

    @Test
    public void reportingFacadePublishesNeutralResponsibilitiesAndPublicApi() {
        List<Map<String, String>> contracts = SimpleYamlContractLoader.load(
                "skills/reporting/contracts/reporting-skill-surface.yml");
        Map<String, String> facade = contracts.stream()
                .filter(entry -> "reporting.skill-surface.facade".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing reporting facade surface contract"));
        Map<String, String> boundary = contracts.stream()
                .filter(entry -> "reporting.skill-surface.neutral-boundary".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing reporting neutral boundary surface contract"));

        for (String api : ReportSkillSurface.PUBLIC_API) {
            Assert.assertTrue(facade.get("publicApi").contains(api),
                    "Reporting facade YAML must expose public API: " + api);
        }
        for (String owned : ReportSkillSurface.OWNED_RESPONSIBILITIES) {
            Assert.assertTrue(boundary.get("owns").contains(owned),
                    "Reporting facade must own shared report responsibility: " + owned);
        }
        for (String forbidden : ReportSkillSurface.FORBIDDEN_OWNERSHIP) {
            Assert.assertTrue(boundary.get("forbiddenOwnership").contains(forbidden),
                    "Reporting facade must reject domain ownership: " + forbidden);
        }

        Assert.assertFalse(facade.get("routeWhen").isBlank());
        Assert.assertFalse(facade.get("stopCondition").isBlank());
        Assert.assertFalse(boundary.get("routeWhen").isBlank());
        Assert.assertFalse(boundary.get("stopCondition").isBlank());
    }

    @Test
    public void reportingPublicApiStaysSmallAndGeneric() {
        List<Class<?>> publicApi = List.of(
                ReportingManager.class,
                ReportLogger.class,
                StepLogger.class,
                HtmlReportShellValidator.class,
                BusinessGraphLayoutAdapter.class
        );

        for (Class<?> type : publicApi) {
            Assert.assertTrue(type.getName().startsWith(ReportSkillSurface.IMPLEMENTATION_PACKAGE),
                    "Reporting public API must live in the implementation package: " + type.getName());
        }
    }

    @Test
    public void reportingMainDoesNotImportSkillConsumers() throws IOException {
        List<String> violations = ArchitectureSourceScanner.sourceMatches(
                AtfSourcePaths.mainJavaPackage("com.ai.core.reporting"),
                java.util.regex.Pattern.compile("import\\s+com\\.atf\\.ai\\.skills\\.(?:mcp|atlas)\\.")
        );

        Assert.assertTrue(violations.isEmpty(),
                "Reporting main must stay consumer-neutral and must not import MCP or Atlas: " + violations);
    }

    @Test
    public void reportingReadmeDocumentsNeutralFacadeAndIntegrationProfiles() throws IOException {
        String readme = Files.readString(AtfSourcePaths.mainJavaPackage("com.ai.skills.reporting")
                .resolve(AtfSourcePaths.README_FILE_NAME));

        Assert.assertTrue(readme.contains("facade over"));
        Assert.assertTrue(readme.contains(ReportSkillSurface.IMPLEMENTATION_PACKAGE));
        Assert.assertTrue(readme.contains("testng-reporting-sanity.xml"));
        Assert.assertTrue(readme.contains("testng-reporting-integration.xml"));
        Assert.assertTrue(readme.contains("testng-reporting-consumer-smoke.xml"));
    }
}
