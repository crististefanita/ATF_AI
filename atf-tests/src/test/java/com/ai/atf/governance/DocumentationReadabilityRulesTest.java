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
        }
        Assert.assertTrue(findings.isEmpty(), GovernanceFiles.problems(findings));
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
        for (String reference : List.of("docs/Testing_Commands.md", "docs/Architecture.md", "atf-core/README.md")) {
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
