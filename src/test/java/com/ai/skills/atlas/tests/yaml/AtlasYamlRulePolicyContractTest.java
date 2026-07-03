package com.ai.skills.atlas.tests.yaml;

import com.ai.skills.atlas.contract.AtlasRuleCatalog;
import com.ai.skills.atlas.contract.AtlasContractProfile;
import com.ai.skills.atlas.contract.AtlasRuleIds;
import com.ai.skills.atlas.contract.AtlasRuleCategory;
import com.ai.skills.atlas.contract.AtlasRuleSeverity;
import com.ai.skills.atlas.contract.AtlasRuleSpec;
import com.ai.skills.atlas.contract.catalog.AtlasBaseRuleCatalog;
import com.ai.skills.atlas.contract.catalog.AtlasBusinessRuleCatalog;
import com.ai.skills.atlas.contract.catalog.AtlasGraphRuleCatalog;
import com.ai.skills.atlas.contract.catalog.AtlasScoreRuleCatalog;
import com.ai.skills.atlas.contract.catalog.AtlasSkillAuditRuleCatalog;
import com.ai.skills.atlas.contract.catalog.AtlasTraceRuleCatalog;
import com.ai.skills.atlas.contract.yaml.AtlasYamlQualityLaneCatalog;
import com.ai.skills.atlas.contract.yaml.AtlasYamlQualityLaneSpec;
import com.ai.skills.atlas.contract.yaml.AtlasYamlGraphReportingCatalog;
import com.ai.skills.atlas.contract.yaml.AtlasYamlGraphReportingContract;
import com.ai.skills.atlas.contract.yaml.AtlasYamlRulePolicy;
import com.ai.skills.atlas.contract.yaml.AtlasYamlRulePolicyCatalog;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Listeners(TestListener.class)
public class AtlasYamlRulePolicyContractTest {
    private static final Set<String> REQUIRED_QUALITY_LANES = Set.of(
            "atlas.quality.business-map",
            "atlas.quality.source-trace",
            "atlas.quality.score-truth",
            "atlas.quality.graph-proof",
            "atlas.quality.validation-audit");

    @Test
    public void yamlRulePoliciesHaveRequiredSchema() {
        Assert.assertFalse(AtlasYamlRulePolicyCatalog.policies().isEmpty());
        Assert.assertTrue(AtlasYamlRulePolicyCatalog.duplicateIds().isEmpty());

        for (AtlasYamlRulePolicy policy : AtlasYamlRulePolicyCatalog.policies()) {
            Assert.assertTrue(policy.id().startsWith("atlas.policy."), "Unexpected policy id: " + policy.id());
            Assert.assertTrue(Set.of(AtlasContractProfile.values()).contains(policy.profile()),
                    "Invalid Atlas contract profile for " + policy.id() + ": " + policy.profile());
            Assert.assertFalse(policy.owner().isBlank(), "Missing owner for " + policy.id());
            Assert.assertFalse(policy.artifact().isBlank(), "Missing artifact for " + policy.id());
            Assert.assertFalse(policy.repairTarget().isBlank(), "Missing repair target for " + policy.id());
            Assert.assertFalse(policy.matrix().isBlank(), "Missing matrix guidance for " + policy.id());
            Assert.assertFalse(policy.capImpact().isBlank(), "Missing cap impact for " + policy.id());
        }
    }

    @Test
    public void yamlRulePoliciesCoverExecutableAtlasCatalogCategories() {
        Set<AtlasRuleCategory> executableCategories = AtlasRuleCatalog.allRules().stream()
                .map(AtlasRuleSpec::category)
                .collect(Collectors.toUnmodifiableSet());

        for (AtlasRuleCategory category : executableCategories) {
            AtlasYamlRulePolicy policy = AtlasYamlRulePolicyCatalog.requireFor(category);
            Assert.assertEquals(policy.category(), category);
        }
    }

    @Test
    public void yamlRulePolicyCategoriesMayHaveProfileSpecificPolicies() {
        List<AtlasYamlRulePolicy> auditPolicies = AtlasYamlRulePolicyCatalog.policiesFor(AtlasRuleCategory.AUDIT);

        Assert.assertTrue(auditPolicies.size() >= 2,
                "Atlas policy category is a family, not a unique identity; AUDIT must allow generic and profile-specific guidance.");
        Assert.assertEquals(
                AtlasYamlRulePolicyCatalog.requireFor(AtlasRuleCategory.AUDIT, AtlasContractProfile.CANDIDATE)
                        .profile(),
                AtlasContractProfile.CANDIDATE,
                "Candidate Atlas matrix rows should use candidate-specific audit policy guidance when available.");
    }

    @Test
    public void atlasRuleSeverityOrderingIsExplicitAndStable() {
        Assert.assertTrue(AtlasRuleSeverity.BLOCKER.stricterThan(AtlasRuleSeverity.MAJOR));
        Assert.assertTrue(AtlasRuleSeverity.MAJOR.stricterThan(AtlasRuleSeverity.MINOR));
        Assert.assertTrue(AtlasRuleSeverity.MINOR.stricterThan(AtlasRuleSeverity.INFO));
        Assert.assertTrue(AtlasRuleSeverity.BLOCKER.atLeastAsSevereAs(AtlasRuleSeverity.BLOCKER));
        Assert.assertFalse(AtlasRuleSeverity.INFO.atLeastAsSevereAs(AtlasRuleSeverity.BLOCKER));
    }

    @Test
    public void yamlRulePolicySeverityCoversStrictestExecutableRuleInEachCategory() {
        List<String> underCalibratedPolicies = new ArrayList<>();
        for (AtlasRuleCategory category : AtlasRuleCategory.values()) {
            AtlasYamlRulePolicy policy = AtlasYamlRulePolicyCatalog.requireFor(category);
            AtlasRuleSpec strictestRule = AtlasRuleCatalog.allRules().stream()
                    .filter(rule -> rule.category() == category)
                    .reduce((left, right) -> left.severity().atLeastAsSevereAs(right.severity()) ? left : right)
                    .orElseThrow(() -> new AssertionError("No executable Atlas rule for category: " + category));

            if (!policy.severity().atLeastAsSevereAs(strictestRule.severity())) {
                underCalibratedPolicies.add(category + ": policy=" + policy.severity()
                        + ", strictestRule=" + strictestRule.id()
                        + "/" + strictestRule.severity());
            }
        }
        Assert.assertTrue(underCalibratedPolicies.isEmpty(),
                "YAML policy severity must be at least as strict as executable rules: "
                        + underCalibratedPolicies);
    }

    @Test
    public void yamlRulePoliciesCoverEveryAtlasCategoryAndStayActionable() {
        Set<AtlasRuleCategory> yamlCategories = AtlasYamlRulePolicyCatalog.policies().stream()
                .map(AtlasYamlRulePolicy::category)
                .collect(Collectors.toUnmodifiableSet());
        Assert.assertEquals(yamlCategories, Set.of(AtlasRuleCategory.values()),
                "Atlas YAML policy catalog must document every public rule category.");

        for (AtlasYamlRulePolicy policy : AtlasYamlRulePolicyCatalog.policies()) {
            Assert.assertTrue(policy.matrix().contains("expose")
                            || policy.matrix().contains("publish")
                            || policy.matrix().contains("connect"),
                    "Atlas policy matrix guidance must describe the visible audit row: " + policy.id());
            Assert.assertTrue(policy.repairTarget().length() >= 10,
                    "Atlas repair target should be concrete enough for agentic repair: " + policy.id());
            Assert.assertFalse(policy.capImpact().equalsIgnoreCase(policy.repairTarget()),
                    "Cap impact must explain scoring consequence, not duplicate repair target: " + policy.id());
        }
    }

    @Test
    public void yamlQualityLanesSeparateAtlasBusinessMeaningFromMcpPackageGate() {
        Assert.assertFalse(AtlasYamlQualityLaneCatalog.lanes().isEmpty());
        Assert.assertTrue(AtlasYamlQualityLaneCatalog.duplicateIds().isEmpty());

        Set<String> ids = AtlasYamlQualityLaneCatalog.lanes().stream()
                .map(AtlasYamlQualityLaneSpec::id)
                .collect(Collectors.toUnmodifiableSet());
        Assert.assertTrue(ids.containsAll(REQUIRED_QUALITY_LANES),
                "Atlas quality lanes must cover the agentic business-quality surfaces: " + ids);

        for (AtlasYamlQualityLaneSpec lane : AtlasYamlQualityLaneCatalog.lanes()) {
            Assert.assertTrue(lane.id().startsWith("atlas.quality."), "Unexpected quality lane id: " + lane.id());
            Assert.assertFalse(lane.lane().isBlank(), "Missing quality lane name for " + lane.id());
            Assert.assertFalse(lane.owner().isBlank(), "Missing quality lane owner for " + lane.id());
            Assert.assertFalse(lane.artifact().isBlank(), "Missing quality lane artifact for " + lane.id());
            Assert.assertFalse(lane.reportSurface().isBlank(), "Missing report surface for " + lane.id());
            Assert.assertTrue(lane.mcpBoundary().contains("MCP"),
                    "Quality lane must state the MCP boundary explicitly: " + lane.id());
            Assert.assertFalse(lane.scoreImpact().isBlank(), "Missing score impact for " + lane.id());
            Assert.assertFalse(lane.repairTarget().isBlank(), "Missing repair target for " + lane.id());
            AtlasYamlRulePolicy policy = AtlasYamlRulePolicyCatalog.requireFor(lane.category());
            Assert.assertTrue(policy.severity().atLeastAsSevereAs(AtlasRuleSeverity.INFO),
                    "Quality lane must route to a known Atlas rule policy: " + lane.id());
        }

        Assert.assertTrue(
                AtlasYamlQualityLaneCatalog.require("atlas.quality.validation-audit")
                        .mcpBoundary()
                        .contains("package gate"),
                "Atlas validation audit must not pretend to replace MCP package close-out.");
    }

    @Test
    public void atlasQualityLanesDoNotBecomeMcpCloseoutContracts() {
        List<String> offenders = AtlasYamlQualityLaneCatalog.lanes().stream()
                .filter(lane -> lane.artifact().contains("validation/mcp")
                        || lane.reportSurface().contains("package-closeout")
                        || lane.repairTarget().contains("package-closeout"))
                .map(AtlasYamlQualityLaneSpec::id)
                .collect(Collectors.toUnmodifiableList());

        Assert.assertTrue(offenders.isEmpty(),
                "Atlas quality lanes explain business quality; MCP owns package close-out contracts: " + offenders);
    }

    @Test
    public void atlasGraphReportingYamlDrivesExecutableGraphAndLanguageRules() {
        Assert.assertFalse(AtlasYamlGraphReportingCatalog.contracts().isEmpty());
        Assert.assertTrue(AtlasYamlGraphReportingCatalog.duplicateIds().isEmpty());

        Set<String> executableIds = AtlasRuleCatalog.allRules().stream()
                .map(AtlasRuleSpec::id)
                .collect(Collectors.toUnmodifiableSet());

        for (AtlasYamlGraphReportingContract contract : AtlasYamlGraphReportingCatalog.contracts()) {
            Assert.assertTrue(executableIds.contains(contract.id()),
                    "Atlas graph/reporting YAML must be tied to an executable Atlas rule id: " + contract.id());
            Assert.assertFalse(contract.owner().isBlank(), "Missing owner for " + contract.id());
            Assert.assertFalse(contract.repair().isBlank(), "Missing repair guidance for " + contract.id());
            AtlasYamlRulePolicy policy = AtlasYamlRulePolicyCatalog.requireFor(contract.category());
            Assert.assertTrue(policy.severity().atLeastAsSevereAs(AtlasRuleSeverity.INFO),
                    "Graph/reporting YAML must route through a known Atlas policy: " + contract.id());
        }

        AtlasYamlGraphReportingContract graph =
                AtlasYamlGraphReportingCatalog.require(AtlasRuleIds.Candidate.GRAPH_REFERENCE_PRIMITIVES);
        Assert.assertTrue(graph.requiredClasses().containsAll(List.of("graph-panel", "diag", "node", "nt", "ns", "edge")));
        Assert.assertTrue(graph.visibleNodeShapeTags().contains("rect"));
        Assert.assertTrue(graph.forbiddenWrapperClassTags().contains("g.node"));

        AtlasYamlGraphReportingContract language =
                AtlasYamlGraphReportingCatalog.require(AtlasRuleIds.Candidate.REPORT_LANGUAGE_ROMANIAN);
        Assert.assertEquals(language.expectedLanguage(), "ro");
        Assert.assertTrue(language.requiredVisibleTokens().contains("Limba raportului"));
        Assert.assertTrue(language.requiredRomanianSentences().stream()
                        .anyMatch(sentence -> sentence.contains("atlas citeste sursa")),
                "Language YAML must model Romanian key sentences, not only html lang metadata.");
        Assert.assertTrue(language.forbiddenKeyEnglishPhrases().stream()
                        .anyMatch(phrase -> phrase.contains("Source-read-only atlas")),
                "Language YAML must block stale English key prose in the published Atlas report.");
    }

    @Test
    public void atlasJavaCodeDoesNotImportMcpProductCode() throws IOException {
        List<String> violations = new ArrayList<>();
        collectDirectImportViolations(AtfSourcePaths.MAIN_ATLAS_ROOT, "com.ai.skills.mcp", violations);
        collectDirectImportViolations(AtfSourcePaths.TEST_ATLAS_ROOT, "com.ai.skills.mcp", violations);

        Assert.assertTrue(violations.isEmpty(),
                "Atlas must stay a business-understanding skill. It may document the MCP boundary in YAML/text, "
                        + "but it must not import MCP package-gate implementation code: " + violations);
    }

    @Test
    public void publicAtlasRuleCatalogRemainsFacadeOverFocusedDomainCatalogs() throws Exception {
        String source = Files.readString(AtfSourcePaths.mainJava(AtlasRuleCatalog.class));

        Assert.assertTrue(source.contains("AtlasBaseRuleCatalog")
                        && source.contains("AtlasScoreRuleCatalog")
                        && source.contains("AtlasGraphRuleCatalog")
                        && source.contains("AtlasBusinessRuleCatalog")
                        && source.contains("AtlasTraceRuleCatalog")
                        && source.contains("AtlasSkillAuditRuleCatalog"),
                "Public AtlasRuleCatalog must compose focused domain catalogs.");
        Assert.assertFalse(source.contains("AtlasRuleSpec.of("),
                "AtlasRuleCatalog must stay a facade; rule declarations belong in atlas.contract.catalog.*.");

        List<AtlasRuleSpec> composed = new ArrayList<>();
        composed.addAll(AtlasBaseRuleCatalog.rules());
        composed.addAll(AtlasScoreRuleCatalog.openingRules());
        composed.addAll(AtlasGraphRuleCatalog.openingRules());
        composed.addAll(AtlasScoreRuleCatalog.auditRules());
        composed.addAll(AtlasTraceRuleCatalog.rules());
        composed.addAll(AtlasBusinessRuleCatalog.rules());
        composed.addAll(AtlasSkillAuditRuleCatalog.rules());

        Assert.assertEquals(
                composed.stream().map(AtlasRuleSpec::id).collect(Collectors.toUnmodifiableList()),
                AtlasRuleCatalog.allRules().stream().map(AtlasRuleSpec::id).collect(Collectors.toUnmodifiableList()),
                "Facade order must match the focused catalog composition order.");
    }

    @Test
    public void yamlRulePoliciesRemainMetadataNotExecutableTruth() {
        for (AtlasRuleSpec rule : AtlasRuleCatalog.allRules()) {
            AtlasYamlRulePolicy policy = AtlasYamlRulePolicyCatalog.requireFor(rule.category());
            Assert.assertNotEquals(policy.capImpact().toLowerCase(), rule.repair().toLowerCase(),
                    "YAML policy should guide calibration, not duplicate executable rule repair text: " + rule.id());
        }
    }

    private static void collectDirectImportViolations(Path root, String forbiddenPackage, List<String> violations)
            throws IOException {
        if (!Files.isDirectory(root)) {
            return;
        }
        String forbiddenImport = "import " + forbiddenPackage;
        try (Stream<Path> files = Files.walk(root)) {
            files
                    .filter(AtfSourcePaths::isJavaSource)
                    .filter(path -> source(path).contains(forbiddenImport))
                    .map(AtfSourcePaths::unixPath)
                    .forEach(violations::add);
        }
    }

    private static String source(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }
}
