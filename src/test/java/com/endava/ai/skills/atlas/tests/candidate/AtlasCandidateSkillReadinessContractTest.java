package com.endava.ai.skills.atlas.tests.candidate;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.contract.AtlasContractSuiteRef;
import com.endava.ai.skills.atlas.contract.AtlasSuiteIds;
import com.endava.ai.skills.atlas.rules.audit.AtlasTestAuditUiRuntimeScopeParityRule;
import com.endava.ai.skills.atlas.rules.audit.manifest.AtlasTestAuditManifestPublisher;
import com.endava.ai.skills.atlas.tests.support.AtlasContractTestSupport;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.endava.ai.skills.atlas.contract.AtlasRuleIds.Candidate.*;

@Listeners(TestListener.class)
@AtlasContractSuiteRef(AtlasSuiteIds.CANDIDATE_SKILL_READINESS)
public class AtlasCandidateSkillReadinessContractTest extends AtlasContractTestSupport {
    @Test
    public void candidateRuleMatrixIsPublished() throws IOException {
        publishCandidateRuleMatrix();

        String matrix = Files.readString(Path.of("target/atlas-rule-matrix.md"));
        org.testng.Assert.assertTrue(matrix.contains("Owner"),
                "Atlas rule matrix must expose YAML policy owner.");
        org.testng.Assert.assertTrue(matrix.contains("Policy repair target"),
                "Atlas rule matrix must expose YAML policy repair target.");
        org.testng.Assert.assertTrue(matrix.contains("Cap impact"),
                "Atlas rule matrix must expose YAML policy cap impact.");
    }

    @Test
    public void candidateAtlasRequirementsAreReadyForCodexSkillMigration() throws IOException {
        assertCandidateRule(SKILL_MIGRATION_READINESS);
    }

    @Test
    public void candidateAtlasSkillParametersStayGeneric() throws IOException {
        assertCandidateRule(SKILL_PARAMETERS_GENERIC);
    }

    @Test
    public void skillReadinessRulesShareRequirementsReader() throws IOException {
        Path skillRules = AtfSourcePaths.MAIN_ATLAS_ROOT.resolve("rules").resolve("skill");
        try (Stream<Path> files = Files.walk(skillRules)) {
            List<String> violations = files
                    .filter(path -> path.getFileName().toString().endsWith("Rule.java"))
                    .filter(path -> !usesSharedRequirementsReader(path))
                    .map(path -> skillRules.relativize(path).toString())
                    .collect(Collectors.toList());

            org.testng.Assert.assertTrue(violations.isEmpty(),
                    "Skill-readiness rules must use AtlasSkillRequirementText instead of duplicating HTML "
                            + "loading/lowercase/error handling: " + violations);
        }
    }

    @Test
    public void candidateAtlasPublishesAtlasTestAuditExtent() throws IOException {
        assertCandidateRule(TEST_AUDIT_EXTENT_LINKED);
    }

    @Test
    public void candidateAtlasTestAuditTotalsMatchLinkedExtent() throws IOException {
        assertCandidateRule(TEST_AUDIT_TOTAL_SYNCHRONIZED);
    }

    @Test
    public void candidateAtlasTestAuditExtentRowsPublishSummaryAndCommand() throws IOException {
        assertCandidateRule(TEST_AUDIT_EXTENT_SUMMARY_COMMAND);
    }

    @Test(groups = "atlas-adapter-parity")
    public void candidateAtlasUiRuntimeAuditRowsUseSameTestScope() throws IOException {
        assertCandidateRule(TEST_AUDIT_UI_RUNTIME_SCOPE_PARITY);
    }

    @Test
    public void candidateAtlasUiRuntimeScopeParityRejectsMixedRuntimeInventory() throws IOException {
        Path fixture = Path.of("target", "atlas-contract-fixtures", "ui-runtime-scope-parity");
        Path sourceRoot = fixture.resolve("src").resolve("test").resolve("java").resolve("com").resolve("endava")
                .resolve("ai").resolve("ui").resolve("tests");
        Path uiRoot = fixture.resolve("src").resolve("main").resolve("java").resolve("com").resolve("endava")
                .resolve("ai").resolve("ui");
        Path atlas = fixture.resolve("ATF_fixture_business-test-atlas.html");
        Files.createDirectories(sourceRoot);
        Files.createDirectories(uiRoot);
        Files.writeString(sourceRoot.resolve("FixtureUiTest.java"), javaSourceWithTestAnnotations(39));
        Files.writeString(atlas,
                "<html><body>\n"
                        + "<details>\n"
                        + "  <summary><span>6. Atlas Test Audit</span></summary>\n"
                        + "  <table>\n"
                        + "    <tr><td>UI Agentic current Extent</td><td><a href=\"validation/ATF/ATF_ui-agentic-current-extent-report.html\">Playwright</a><br><code>Passed: 39; Failed: 0; Skipped: 0</code></td></tr>\n"
                        + "    <tr><td>UI Agentic Selenium Extent</td><td><a href=\"validation/ATF/ATF_ui-agentic-selenium-all-extent-report.html\">Selenium</a><br><code>Passed: 5; Failed: 15; Skipped: 50</code></td></tr>\n"
                        + "  </table>\n"
                        + "</details>\n"
                        + "</body></html>\n");

        withAtlasProperties(atlas, sourceRoot, uiRoot, () -> {
            AtlasValidationResult result = new AtlasValidationResult();
            new AtlasTestAuditUiRuntimeScopeParityRule().validate(AtlasContext.fromWorkingDirectory(), result);
            Assert.assertFalse(result.byCode(TEST_AUDIT_UI_RUNTIME_SCOPE_PARITY).isEmpty(),
                    "Mixed Playwright/Selenium inventories must produce a scope parity blocker.");
            Assert.assertTrue(result.byCode(TEST_AUDIT_UI_RUNTIME_SCOPE_PARITY).stream()
                            .anyMatch(violation -> violation.toString().contains("Selenium UI Extent total")),
                    "Scope parity blocker must name the mismatched Selenium total.");
        });
    }

    @Test
    public void candidateAtlasAggregateManifestAndCommandStaySynchronized() throws IOException {
        AtlasTestAuditManifestPublisher.publishFor(AtlasContext.fromWorkingDirectory());
        assertCandidateRule(TEST_AUDIT_MANIFEST_COMMAND_SYNC);
    }

    private static boolean usesSharedRequirementsReader(Path path) {
        try {
            return Files.readString(path).contains("AtlasSkillRequirementText.readLowercase");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect Atlas skill rule " + path, e);
        }
    }

    private static String javaSourceWithTestAnnotations(int count) {
        String methods = IntStream.rangeClosed(1, count)
                .mapToObj(index -> "    @Test public void generatedScopeTest" + index + "() {}\n")
                .collect(Collectors.joining());
        return "package com.endava.ai.ui.tests;\n"
                + "import org.testng.annotations.Test;\n"
                + "public class FixtureUiTest {\n"
                + methods
                + "}\n";
    }

    private static void withAtlasProperties(Path atlas, Path sourceRoot, Path uiRoot, ThrowingRunnable runnable)
            throws IOException {
        String previousAtlas = System.getProperty("atlas.html");
        String previousSource = System.getProperty("atlas.sourceRoot");
        String previousUi = System.getProperty("atlas.uiSourceRoot");
        try {
            System.setProperty("atlas.html", atlas.toString());
            System.setProperty("atlas.sourceRoot", sourceRoot.toString());
            System.setProperty("atlas.uiSourceRoot", uiRoot.toString());
            runnable.run();
        } finally {
            restoreProperty("atlas.html", previousAtlas);
            restoreProperty("atlas.sourceRoot", previousSource);
            restoreProperty("atlas.uiSourceRoot", previousUi);
        }
    }

    private static void restoreProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws IOException;
    }
}
