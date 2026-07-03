package com.ai.skills.reporting.tests;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.ai.skills.reporting.AggregateReportSurface;
import com.ai.skills.reporting.ReportSkillSurface;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Listeners(TestListener.class)
public class ReportingSkillFacadeSmokeTest {
    private static final String SKILL_ARCHITECTURE = "atf-core/contracts/skills/skill-architecture.yml";
    private static final String REPORTING_SURFACE = "skills/reporting/contracts/reporting-skill-surface.yml";

    @Test
    public void reportingSkillFacadeStaysSmallPublicAndCoreBacked() throws IOException {
        List<String> facadeClasses = Files.walk(AtfSourcePaths.mainJavaPackage("com.ai.skills.reporting"))
                .filter(path -> path.toString().endsWith(".java"))
                .map(path -> path.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());

        Assert.assertEquals(facadeClasses, List.of(
                "AggregateReportSurface.java",
                "ReportSkillSurface.java"
        ), "Reporting skill facade must stay small; implementation belongs in core.reporting.");
        Assert.assertEquals(ReportSkillSurface.IMPLEMENTATION_PACKAGE, "com.ai.core.reporting");
        Assert.assertEquals(AggregateReportSurface.IMPLEMENTATION_PACKAGE,
                "com.ai.core.reporting.aggregate");
    }

    @Test
    public void reportingSkillFacadeMatchesYamlRoutingContract() {
        Map<String, String> architecture = SimpleYamlContractLoader.load(SKILL_ARCHITECTURE).stream()
                .filter(entry -> "skill.reporting".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing skill.reporting architecture contract"));
        Map<String, String> facade = SimpleYamlContractLoader.load(REPORTING_SURFACE).stream()
                .filter(entry -> "reporting.skill-surface.facade".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing Reporting facade surface contract"));

        Assert.assertEquals(architecture.get("sourceRoot"), facade.get("facadeRoot"));
        Assert.assertEquals(architecture.get("implementationRoot"), facade.get("implementationRoot"));
        Assert.assertEquals(architecture.get("facadeTestRoot"), "src/test/java/com/ai/skills/reporting");
        Assert.assertEquals(ReportSkillSurface.SANITY_SUITE, architecture.get("sanitySuite"));
        Assert.assertTrue(architecture.get("role").contains("facade"),
                "Reporting must stay declared as a facade over the Core reporting implementation.");
    }

    @Test
    public void reportingSkillReadmeKeepsDomainBoundariesVisible() throws IOException {
        String readme = Files.readString(AtfSourcePaths.mainJavaPackage("com.ai.skills.reporting")
                .resolve(AtfSourcePaths.README_FILE_NAME));

        Assert.assertTrue(readme.contains("facade over com.ai.core.reporting"));
        Assert.assertTrue(readme.contains("Do not decide MCP package publishability"));
        Assert.assertTrue(readme.contains("Do not decide Atlas business meaning"));
        Assert.assertTrue(readme.contains("testng-reporting-sanity.xml"));
    }
}
