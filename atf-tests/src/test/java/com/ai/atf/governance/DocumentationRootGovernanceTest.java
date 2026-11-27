package com.ai.atf.governance;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ai.atf.governance.DocumentationGovernanceFixtures.append;
import static com.ai.atf.governance.DocumentationGovernanceFixtures.assertFinding;
import static com.ai.atf.governance.DocumentationGovernanceFixtures.assertValid;
import static com.ai.atf.governance.DocumentationGovernanceFixtures.diagram;
import static com.ai.atf.governance.DocumentationGovernanceFixtures.documentation;
import static com.ai.atf.governance.DocumentationGovernanceFixtures.hash;

/**
 * Protects the project README as a concise orientation map and keeps owner routes and tables readable.
 */
public class DocumentationRootGovernanceTest {

    @Test
    public void aMissingLinkHasOwnedGuidanceAndARealRepairClearsTheFinding() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "README.md", "\n[Missing owner](docs/Unavailable.md)\n");
            List<String> findings = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(findings, "DOC-LINK");
            String report = GovernanceFiles.problems(findings);
            Assert.assertTrue(report.contains("Observed: README.md:"));
            Assert.assertTrue(report.contains("Failure meaning: A documented local route or fragment has no observed target."));
            Assert.assertTrue(report.contains("Repair hint: Correct the source-relative destination"));
            Assert.assertTrue(report.contains("Owner: docs/governance/Code_And_Documentation_Policy.md#portable-paths-and-executable-facts"));
            fixture.write("README.md", Files.readString(fixture.root.resolve("README.md"))
                    .replace("docs/Unavailable.md", "docs/Architecture.md"));
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @Test
    public void aPrefixCollisionStillFailsTheDocumentationNegativeControl() {
        List<String> findings = List.of("README.md:10 DOC-DIAGRAM-LEGEND missing explanation");
        AssertionError failure = Assert.expectThrows(AssertionError.class, () -> assertFinding(findings, "DOC-DIAGRAM"));
        Assert.assertTrue(failure.getMessage().contains("Expected DOC-DIAGRAM;"));
        Assert.assertTrue(failure.getMessage().contains("Observed: " + findings.get(0)));
    }

    @Test
    public void artifactOwnersAndIntakeTemplateAreRequiredAndAtlasStaysDocumentationOnly() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            for (String path : List.of("artifacts/README.md", "artifacts/reports/README.md", "artifacts/atlas/README.md",
                    "artifacts/atlas/Project_Map_Template.md", "tools/collect-artifacts.mjs", "tools/collect-artifacts.test.mjs",
                    "tools/verify-atlas.mjs", "tools/verify-atlas.test.mjs",
                    "atf-atlas/Report_Contract.md", "atf-atlas/Knowledge_Base.md", "atf-atlas/assets/atlas.css",
                    "atf-atlas/knowledge/atlas-contracts.json", "tools/atlas-reader.mjs", "tools/atlas-reader.test.mjs", "tools/atlas-reader-graphs.mjs", "tools/atlas-reader-graphs.test.mjs", "tools/atlas-calibration.mjs", "tools/atlas-calibration.test.mjs", "tools/atlas-report.mjs", "tools/atlas-report.test.mjs",
                    "tools/atlas-knowledge.mjs", "tools/atlas-knowledge.test.mjs", "tools/atlas-diagnostics.mjs",
                    "tools/atlas-bundle.mjs", "tools/atlas-bundle.test.mjs",
                    "tools/atlas-execution.mjs", "tools/atlas-execution.test.mjs",
                    "tools/atlas-understanding.mjs", "tools/atlas-understanding.test.mjs",
                    "tools/atlas-behavior.mjs", "tools/atlas-behavior.test.mjs")) {
                String original = Files.readString(fixture.root.resolve(path));
                Files.delete(fixture.root.resolve(path));
                assertFinding(MarkdownDocumentationRules.validate(fixture.root), path + ":1 DOC-MISSING");
                fixture.write(path, original);
            }
            fixture.write("artifacts/atlas/Analyzer.java", "class Analyzer {}\n");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "artifacts/atlas:1 DOC-ATLAS-BOUNDARY");
        }
    }

    @Test
    public void maintainedDocumentationRespectsLinksDiagramsAndTheLanguagePolicy() throws Exception {
        Path root = GovernanceFiles.root();
        assertValid(MarkdownDocumentationRules.validate(root));
        assertValid(MarkdownDocumentationRules.validateCompilableExamples(root));
        String peer = System.getProperty("atf.docs.peer.root", "");
        if (!peer.isBlank()) {
            boolean english = MarkdownDocumentationRules.readmeEdition(root, new java.util.ArrayList<>()).equals("en");
            assertValid(MarkdownDocumentationRules.validateReadmeParity(
                    english ? root : Path.of(peer), english ? Path.of(peer) : root));
        }
    }

    @Test
    public void rootReadmeStaysAnOrientationMapAndRoutesConsumerExamplesToTheirOwners() throws Exception {
        Path source = GovernanceFiles.root();
        assertValid(MarkdownDocumentationRules.validate(source));
        try (GovernanceFiles.Fixture fixture = documentation()) {
            String original = Files.readString(fixture.root.resolve("README.md"));
            fixture.write("README.md", original.replace("atf-playwright/README.md#consumer-example",
                    "atf-playwright/README.md#missing-example"));
            List<String> findings = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(findings, "DOC-README-ROUTE");
            String report = GovernanceFiles.problems(findings);
            Assert.assertTrue(report.contains("Owner: README.md#the-design-in-one-view"));
            Assert.assertTrue(report.contains("Repair hint: Restore the two-column five-module map, dependency diagram under the design section"));

            fixture.write("README.md", original.replaceFirst(Pattern.quote(diagram()), ""));
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-README-DIAGRAM");

            fixture.write("README.md", original + "\n```java compile\npublic class RootExample {}\n```\n");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-README-EXAMPLE-PLACEMENT");

            fixture.write("README.md", original.replace("| Module | Responsibility |", "| Module | Responsibility | Detail |"));
            findings = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(findings, "DOC-README-MODULE-TABLE");
            assertFinding(findings, "DOC-TABLE-SHAPE");
            fixture.write("README.md", original);
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @Test
    public void readmeTableCellsRejectDenseProseAndAcceptAConciseRepair() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            String original = Files.readString(fixture.root.resolve("README.md"));
            String longCell = "explanatory detail ".repeat(12).strip();
            String table = "\n| Field | Explanation |\n|---|---|\n| boundary | " + longCell + " |\n";
            fixture.write("README.md", original + table);
            List<String> findings = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(findings, "DOC-TABLE-LONG-CELL");
            String report = GovernanceFiles.problems(findings);
            Assert.assertTrue(report.contains("Observed: README.md:"));
            Assert.assertTrue(report.contains("Repair hint: Keep explanatory cells within 140 visible characters and 22 words"));
            Assert.assertTrue(report.contains("Owner: docs/governance/Code_And_Documentation_Policy.md#reader-orientation-and-diagrams"));

            fixture.write("README.md", original + "\n| Field | Explanation |\n|---|---|\n| boundary | The input is checked before use. |\n"
                    + "| literal | `#left|right` |\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

}
