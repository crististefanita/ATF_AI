package com.ai.atf.governance;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Component checks of guide readability; no browser or application journey is selected. */
public class DocumentationReadabilityRulesTest {
    private static final Path SAMPLE_ROOT = Path.of("readability-example").toAbsolutePath();

    @Test
    public void maintainedGuidesKeepOrientationReadableAndMavenCommandsCollapsed() throws Exception {
        Path root = GovernanceFiles.root();
        List<String> findings = new ArrayList<>();
        for (Path file : GovernanceFiles.files(root, ".md")) {
            findings.addAll(DocumentationReadabilityRules.validate(root, file, Files.readAllLines(file)));
            findings.addAll(DocumentationPresentationRules.validate(GovernanceFiles.relative(root, file), Files.readAllLines(file)));
        }
        findings.addAll(DocumentationReadabilityRules.validateTestingStrategy(Files.readAllLines(root.resolve("docs/Testing.md"))));
        Assert.assertTrue(findings.isEmpty(), GovernanceFiles.problems(findings));
    }

    @Test
    public void diagramsRequireAnAdjacentClosedExplainedLegendWithOwnedRepairHints() {
        String graph = "# Guide\n\n```mermaid\nflowchart LR\nA --> B\n```\n\n";
        String legend = "<details>\n<summary>Legend: ownership</summary>\n\nArrows show delegation; vendor details stay with the provider.\n\n</details>\n";
        for (String broken : List.of("", "Unrelated text.\n\n" + legend,
                legend.replace("<details>", "<details open>"), legend.replace("Legend: ownership", "More"),
                legend.replace("Arrows show delegation; vendor details stay with the provider.", ""),
                legend.replace("</details>", ""))) {
            List<String> findings = DocumentationPresentationRules.validate("docs/Guide.md", List.of((graph + broken).split("\\n", -1)));
            assertFinding(findings, "DOC-DIAGRAM-DISCLOSURE", "next block");
            String report = GovernanceFiles.problems(findings);
            Assert.assertTrue(report.contains("Repair hint:"));
            Assert.assertTrue(report.contains("Owner: docs/governance/Code_And_Documentation_Policy.md#reader-orientation-and-diagrams"));
        }
        Assert.assertTrue(DocumentationPresentationRules.validate("docs/Guide.md", List.of((graph + legend).split("\\n", -1))).isEmpty());
        Assert.assertTrue(DocumentationPresentationRules.validate("docs/Guide.md",
                List.of((graph + legend.replace("<details>", "<details data-note=\"the word open is data\">")).split("\\n", -1))).isEmpty());
    }

    @Test
    public void normativeTablesUseTheSameConciseProseBoundaryAsReadmes() throws Exception {
        try (GovernanceFiles.Fixture fixture = DocumentationGovernanceFixtures.documentation()) {
            String dense = "explanatory detail ".repeat(12).strip();
            String original = "# Owner contract\n\n| Boundary | Meaning |\n|---|---|\n| ownership | " + dense + " |\n";
            fixture.write("docs/Owner_Contract.md", original);
            List<String> findings = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(findings, "DOC-TABLE-LONG-CELL", "column 2");
            String report = GovernanceFiles.problems(findings);
            Assert.assertTrue(report.contains("Repair hint:"));
            fixture.write("docs/Owner_Contract.md", original.replace(dense, "The caller owns the resource."));
            DocumentationGovernanceFixtures.assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @Test
    public void sharedPresentationRulesPreserveLiteralExamplesCommentsAndSequenceMeaning() {
        String legend = "\n<details>\n<summary>Legend: call sequence</summary>\n\nArrows show calls.\n</details>\n";
        for (String direction : List.of("TB", "TD", "BT", "RL")) {
            String invalid = "```mermaid\nflowchart " + direction + "\nA --> B\n```\n" + legend;
            List<String> findings = DocumentationPresentationRules.validate("README.md", List.of(invalid.split("\\n", -1)));
            assertFinding(findings, "DOC-DIAGRAM-ORIENTATION", "observed " + direction);
            String nested = invalid.replace("flowchart " + direction,
                    "flowchart LR\nsubgraph Group\ndirection " + direction + "\nend");
            assertFinding(DocumentationPresentationRules.validate("README.md", List.of(nested.split("\\n", -1))),
                    "DOC-DIAGRAM-ORIENTATION", "nested direction");
            Assert.assertTrue(DocumentationPresentationRules.validate("README.md",
                    List.of(nested.replace("direction " + direction, "direction LR").split("\\n", -1))).isEmpty());
            Assert.assertTrue(DocumentationPresentationRules.validate("README.md", List.of(invalid.replace("flowchart " + direction, "flowchart LR").split("\\n", -1))).isEmpty());
        }
        String example = "````text\n```mermaid\nflowchart TB\nA --> B\n```\n````\n<!--\n```mermaid\nflowchart TB\nA --> B\n```\n-->\n";
        Assert.assertTrue(DocumentationPresentationRules.validate("docs/Guide.md", List.of(example.split("\\n", -1))).isEmpty());
        String commented = "\n```mermaid\nflowchart LR\n%% direction TB is documentation\nA --> B\n```\n" + legend;
        Assert.assertTrue(DocumentationPresentationRules.validate("README.md", List.of(commented.split("\\n", -1))).isEmpty());
        String sequence = "```mermaid\nsequenceDiagram\nA->>B: Invoke\n```\n" + legend;
        Assert.assertTrue(DocumentationPresentationRules.validate("docs/Guide.md", List.of(sequence.split("\\n", -1))).isEmpty());
    }

    @Test
    public void strategyFlowRejectsVerticalDirectionsAndLiteralDecoysWithActionableRepairs() {
        String valid = strategy();
        Assert.assertTrue(strategyFindings(valid).isEmpty());
        for (String direction : List.of("TB", "TD", "BT", "RL")) {
            String invalid = valid.replace("flowchart LR", "flowchart " + direction);
            List<String> findings = strategyFindings(invalid);
            assertFinding(findings, "DOC-STRATEGY-FLOW", "observed " + direction);
            String report = GovernanceFiles.problems(findings);
            Assert.assertTrue(report.contains("Repair hint:"));
            Assert.assertTrue(report.contains("Owner: docs/governance/Code_And_Documentation_Policy.md#reader-oriented-guides"));
            Assert.assertTrue(strategyFindings(invalid.replace("flowchart " + direction, "flowchart LR")).isEmpty());
        }
        assertFinding(strategyFindings(valid.replace("flowchart LR", "graph TB")), "DOC-STRATEGY-FLOW", "observed TB");
        assertFinding(strategyFindings(valid.replace("flowchart LR", "flowchart LR\nsubgraph Group\ndirection TB\nend")),
                "DOC-STRATEGY-FLOW", "nested direction");
        String literal = valid.replace("```mermaid", "````text\n```mermaid").replace("```\n\nArrows", "```\n````\n\nArrows");
        assertFinding(strategyFindings(literal), "DOC-STRATEGY-FLOW", "include a horizontal");
        String commented = valid.replace("```mermaid", "<!--\n```mermaid").replace("```\n\nArrows", "```\n-->\n\nArrows");
        assertFinding(strategyFindings(commented), "DOC-STRATEGY-FLOW", "include a horizontal");
        Assert.assertTrue(strategyFindings(valid.replace("flowchart LR", "%% initialization and direction TB are comments\nflowchart LR")).isEmpty());
        Assert.assertTrue(strategyFindings("```text\n<!-- literal data\n```\n\n" + valid).isEmpty(),
                "Comment-like payload inside a literal fence cannot hide the real following strategy.");
    }

    @Test
    public void strategyComparesEachTransportOwnerOracleAndLimitInsteadOfCountingNames() {
        String valid = strategy();
        String row = "| Playwright | [Provider](provider.java) | Observe page response | Local page only |\n";
        String absent = valid.replace(row, "") + "\nPlaywright is mentioned in unrelated prose.\n";
        assertFinding(strategyFindings(absent), "DOC-STRATEGY-LANES", "0 rows for Playwright");
        assertFinding(strategyFindings(valid.replace(row, row + row)), "DOC-STRATEGY-LANES", "2 rows for Playwright");
        for (String invalid : List.of(valid.replace("[Provider](provider.java)", "Provider"),
                valid.replace("Observe page response", ""), valid.replace("Local page only", ""),
                valid.replace("Observe page response |", "Observe page response | Extra |"))) {
            assertFinding(strategyFindings(invalid), "DOC-STRATEGY-LANES", "four columns");
        }
        String wrongSection = valid.replace("<a id=\"test-design-contracts\"></a>", "")
                + "\n<a id=\"test-design-contracts\"></a>\n## Design\n";
        assertFinding(strategyFindings(wrongSection), "DOC-STRATEGY-LANES", "0 rows for API");
        String hidden = valid.replace("<a id=\"test-design-contracts\"></a>",
                "<details><summary>Secondary reference</summary>\n<a id=\"test-design-contracts\"></a>") + "\n</details>";
        assertFinding(strategyFindings(hidden), "DOC-STRATEGY-LANES", "0 rows for API");
        Assert.assertTrue(strategyFindings(valid).isEmpty(), "Restoring the complete comparison clears the findings.");
    }

    private static List<String> strategyFindings(String document) {
        return DocumentationReadabilityRules.validateTestingStrategy(List.of(document.split("\\n", -1)));
    }

    private static String strategy() {
        return "# Testing\n\n```mermaid\nflowchart LR\nA[Contract] --> B[Oracle]\n```\n\nArrows show the review sequence.\n\n"
                + "<a id=\"test-design-contracts\"></a>\n## Design contracts\n\n"
                + "| Lane | Design owner | Observable oracle | Proof limit |\n|---|---|---|---|\n"
                + "| API | [Executor](executor.java) | Observe request ordering | Local fixture only |\n"
                + "| Playwright | [Provider](provider.java) | Observe page response | Local page only |\n"
                + "| Selenium | [Driver](driver.java) | Observe driver response | Selected driver only |\n";
    }

    @Test
    public void denseDecisionProseFailsAcrossWrapsAndARealListRepairClearsIt() {
        String prose = "Select local checks. Inspect the outcomes. Preserve the evidence. Explain the limits.";
        List<String> flat = validate("docs/Testing.md", "# Testing\n\n" + prose);
        List<String> wrapped = validate("docs/Testing.md", "# Testing\n\n" + prose.replace(". ", ".\n"));
        assertFinding(flat, "DOC-PROSE-DENSE", ":3 ");
        Assert.assertEquals(wrapped, flat, "Physical wrapping must not change the visible paragraph finding.");
        Assert.assertTrue(validate("docs/Testing.md", "# Testing\n\n- Select local checks.\n- Inspect the outcomes.\n- Preserve the evidence.\n- Explain the limits.").isEmpty());
    }

    @Test
    public void aRunOnSentenceCannotEvadeTheBoundByDroppingPunctuationOrAddingWraps() {
        String runOn = "word ".repeat(46).strip();
        assertFinding(validate("docs/Testing.md", "# Testing\n\n" + runOn), "DOC-PROSE-SENTENCE", "46 visible words");
        assertFinding(validate("docs/Testing.md", "# Testing\n\n" + runOn.replace(" ", "\n")), "DOC-PROSE-SENTENCE", ":3 ");
        Assert.assertTrue(validate("docs/Testing.md", "# Testing\n\n" + "word ".repeat(45).strip()).isEmpty());
    }

    @Test
    public void theParagraphWordBoundAndItsExactBoundaryAreIndependentOfSentenceCount() {
        String boundary = sentence(33) + " " + sentence(33) + " " + sentence(34);
        Assert.assertTrue(validate("docs/Testing_Commands.md", "# Commands\n\n" + boundary).isEmpty());
        assertFinding(validate("docs/Testing_Commands.md", "# Commands\n\n" + boundary + " extra"), "DOC-PROSE-DENSE", "101 visible words");
        Assert.assertTrue(validate("docs/Testing.md", "# Testing\n\n- " + sentence(33) + "\n- " + sentence(33) + "\n- " + sentence(35)).isEmpty());
    }

    @Test
    public void theStrategyAndDetailedReferencesHaveDifferentReaderTasksButBothKeepWordBounds() {
        String conciseContract = "Select local checks. Inspect the outcomes. Preserve the evidence. Explain the limits.";
        assertFinding(validate("docs/Testing.md", "# Testing\n\n" + conciseContract), "DOC-PROSE-DENSE", "testing strategy");
        assertFinding(validate("atf-core/README.md", "# Reference\n\n" + conciseContract), "DOC-PROSE-DENSE", "README orientation");
        for (String reference : List.of("docs/Testing_Commands.md", "docs/Architecture.md")) {
            Assert.assertTrue(validate(reference, "# Reference\n\n" + conciseContract).isEmpty());
            assertFinding(validate(reference, "# Reference\n\n" + sentence(34) + " " + sentence(34) + " " + sentence(33)), "DOC-PROSE-DENSE", "101 visible words");
        }
    }

    @Test
    public void abbreviationsVersionsLinksAndArbitraryCodeDelimitersRemainTechnicalData() {
        String prose = "Use local fixtures, e.g. the API double, with SDK 1.2.3. "
                + "Dr. Smith compares the U.S. reference, i.e. [the owner](https://example.test/a.b?q=1.2). "
                + "Inspect ``a ` literal " + "word ".repeat(65) + "`` and https://example.test/guide.v1.";
        Assert.assertTrue(validate("docs/Testing.md", "# Testing\n\n" + prose).isEmpty());
        Assert.assertTrue(validate("docs/Testing.md", "# Testing\n\n" + prose.replace("API double", "API\ndouble")).isEmpty());
    }

    @Test
    public void historicalIssueProseIsPreservedWhileItsRunnableCommandStillNeedsDisclosure() {
        String prose = "Observed one result. Investigated another result. Preserved the original evidence. Explained the pending scope.";
        Assert.assertTrue(validate("docs/feedback/Specific_Review.md", "# Review\n\n" + prose).isEmpty());
        String denseEvidence = sentence(34) + " " + sentence(34) + " " + sentence(33);
        Assert.assertTrue(validate("docs/feedback/Specific_Review.md", "# Review\n\n" + denseEvidence).isEmpty());
        assertFinding(validate("docs/feedback/README.md", "# Feedback\n\n" + denseEvidence), "DOC-PROSE-DENSE", ":3 ");
        assertFinding(validate("docs/feedback/Specific_Review.md", "# Review\n\n```powershell\nmvn test\n```"), "DOC-COMMAND-DISCLOSURE", ":4 ");
    }

    @DataProvider(name = "invalidCommandDisclosures")
    public Object[][] invalidCommandDisclosures() {
        return new Object[][] {
                {"", ""},
                {"<details open><summary>Run local checks</summary>\n", "\n</details>"},
                {"<details OPEN='false'><summary>Run local checks</summary>\n", "\n</details>"},
                {"<details open=\"\"><summary>Run local checks</summary>\n", "\n</details>"},
                {"<details open=true><summary>Run local checks</summary>\n", "\n</details>"},
                {"<details>\n", "\n</details>"},
                {"<details><summary></summary>\n", "\n</details>"},
                {"<details><summary>More details</summary>\n", "\n</details>"},
                {"<details>\n", "\n<summary>Run local checks</summary></details>"}
        };
    }

    @Test(dataProvider = "invalidCommandDisclosures")
    public void runnableMavenCommandsRejectOpenOrUnnamedDisclosureAndAcceptTheRealRepair(String before, String after) {
        String command = "```powershell\nmvn -B -ntp test\n```";
        List<String> findings = validate("docs/Testing.md", "# Testing\n\n" + before + command + after);
        assertFinding(findings, "DOC-COMMAND-DISCLOSURE", "descriptive summary");
        String repaired = "# Testing\n\nSelect the proof boundary first.\n\n<details>\n<summary>Run local checks</summary>\n\n" + command + "\n\n</details>";
        Assert.assertTrue(validate("docs/Testing.md", repaired).isEmpty());
    }

    @DataProvider(name = "shellCommandForms")
    public Object[][] shellCommandForms() {
        return new Object[][] {
                {"powershell", "mvn test"}, {"pwsh", "& .\\mvnw.cmd test"},
                {"bash", "./mvnw test"}, {"console", "$ mvn -B test"},
                {"cmd", "mvn.cmd test"}, {"shell", "mvnw test"}
        };
    }

    @Test(dataProvider = "shellCommandForms")
    public void disclosureChecksTheActualRunnableShellCommand(String language, String command) {
        assertFinding(validate("docs/Testing.md", "# Testing\n\n```" + language + "\n" + command + "\n```"), "DOC-COMMAND-DISCLOSURE", ":4 ");
    }

    @Test
    public void fencedOrCommentedFakeTagsCannotHideTheActualCommand() {
        assertFinding(validate("docs/Testing.md", "# Testing\n\n```powershell\n<details><summary>Run local checks</summary>\nmvn test\n</details>\n```"), "DOC-COMMAND-DISCLOSURE", ":5 ");
        assertFinding(validate("docs/Testing.md", "# Testing\n\n<!-- <details><summary>Run local checks</summary> -->\n```powershell\nmvn test\n```"), "DOC-COMMAND-DISCLOSURE", ":5 ");
    }

    @Test
    public void literalExamplesInlineRechecksAndNonMavenShellCommandsStayVisible() {
        String document = "# Testing\n\nRecheck with `mvn -B -ntp test`; `<!--` is a literal marker.\n\n"
                + "````text\n```powershell\nmvn test\n```\n<details open>\n````\n\n"
                + "~~~java\nString command = \"mvn test\";\n~~~\n\n"
                + "```powershell\nWrite-Output 'mvn test'\nnode --test tools/example.test.mjs\n```";
        Assert.assertTrue(validate("docs/Testing.md", document).isEmpty());
    }

    @Test
    public void urlAndQuotationPunctuationStillSeparateVisibleDecisions() {
        String prose = "Inspect https://example.test/one. Compare https://example.test/two. "
                + "Record \"the observed result.\" Preserve https://example.test/three.";
        assertFinding(validate("docs/Testing.md", "# Testing\n\n" + prose), "DOC-PROSE-DENSE", "4 sentences");
    }

    @Test
    public void openDetailsCannotHideVisibleDenseProseFromTheGuideGuard() {
        String paragraph = sentence(34) + " " + sentence(34) + " " + sentence(33);
        String document = "# Guide\n\n<details open><summary>Visible review context</summary>\n\n" + paragraph + "\n\n</details>";
        assertFinding(validate("docs/Architecture.md", document), "DOC-PROSE-DENSE", "101 visible words");
        Assert.assertTrue(validate("docs/Architecture.md", document.replace("<details open>", "<details>")).isEmpty());
    }

    @Test
    public void closedDisclosuresAllowMultilineSummariesAndUnrelatedAttributeValues() {
        String document = "# Testing\n\n<details data-note=\"the word open is data\">\n<summary>\nRun native API checks\n</summary>\n\n"
                + "```powershell\nmvn -B -ntp test\n```\n\n"
                + "This rationale has several independent sentences. It preserves diagnostic context. "
                + "Its details explain an exception. It remains secondary to the visible proof boundary.\n\n</details>";
        Assert.assertTrue(validate("docs/Testing.md", document).isEmpty());
    }

    @Test
    public void aNestedCommandUsesItsOwnDisclosureBoundary() {
        String command = "```powershell\nmvn test\n```";
        String document = "# Testing\n\n<details open><summary>Broader review context</summary>\n"
                + "<details><summary>Run local checks</summary>\n" + command + "\n</details>\n</details>";
        Assert.assertTrue(validate("docs/Testing.md", document).isEmpty());
        String unlabelledInner = document.replace("<details><summary>Run local checks</summary>", "<details>");
        assertFinding(validate("docs/Testing.md", unlabelledInner), "DOC-COMMAND-DISCLOSURE", "descriptive summary");
    }

    private static String sentence(int words) { return "word ".repeat(words).strip() + "."; }

    private static List<String> validate(String relative, String document) {
        return DocumentationReadabilityRules.validate(SAMPLE_ROOT, SAMPLE_ROOT.resolve(relative), List.of(document.split("\\n", -1)));
    }

    private static void assertFinding(List<String> findings, String code, String detail) {
        Assert.assertTrue(findings.stream().anyMatch(finding -> GovernanceFiles.matchesFinding(finding, code)
                && finding.contains(detail)), "Expected " + code + " with " + detail + "; observed " + findings);
    }
}
