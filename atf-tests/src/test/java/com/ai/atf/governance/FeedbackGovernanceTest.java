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
 * Protects feedback schema, evidence-bound scores and the documentation-only Atlas boundary.
 */
public class FeedbackGovernanceTest {
    @Test
    public void feedbackRequiresUniqueIdsActionableOwnersAndHonestClosureStates() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "docs/feedback/README.md", "\n| G-001 | Contract improved | TODO |\n");
            String register = Files.readString(fixture.root.resolve("docs/feedback/README.md"));
            fixture.write("docs/feedback/README.md", register.replace("| atf-tests | 75 | 60 | TODO |", "| TBD | 75 | 60 | VERIFIED |"));
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(failures, "DOC-FEEDBACK duplicate ID");
            assertFinding(failures, "DOC-FEEDBACK active item has invalid status");
            assertFinding(failures, "DOC-FEEDBACK verified item has pending");
        }
        try (GovernanceFiles.Fixture fixture = documentation()) {
            String path = "docs/feedback/README.md";
            String original = Files.readString(fixture.root.resolve(path));
            String empty = original.replaceAll("(?m)^\\| G-001 \\|[^\\r\\n]*\\r?\\n", "");
            String marker = "<!-- feedback:empty -->";
            fixture.write(path, empty);
            List<String> missing = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(missing, "DOC-FEEDBACK-EMPTY");
            String report = GovernanceFiles.problems(missing);
            Assert.assertTrue(report.contains("Repair hint:"));
            Assert.assertTrue(report.contains("Owner: docs/feedback/README.md#recording-and-closing-feedback"));
            String declared = empty.replace("## Active TODOs", "## Active TODOs\n\n" + marker);
            fixture.write(path, declared);
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
            for (String invalid : List.of(
                    original.replace("## Active TODOs", "## Active TODOs\n\n" + marker),
                    empty + "\n" + marker,
                    empty.replace("## Active TODOs", "## Active TODOs\n\n```text\n" + marker + "\n```"),
                    declared.replace(marker, marker + "\n" + marker))) {
                fixture.write(path, invalid);
                assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-FEEDBACK-EMPTY");
            }
            fixture.write(path, original);
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @org.testng.annotations.DataProvider(name = "invalidFeedbackScores")
    public Object[][] invalidFeedbackScores() {
        return new Object[][] {
                {"0", "60", "DOC-FEEDBACK-PRIORITY"}, {"101", "60", "DOC-FEEDBACK-PRIORITY"},
                {"urgent", "60", "DOC-FEEDBACK-PRIORITY"}, {"", "60", "DOC-FEEDBACK-PRIORITY"},
                {"75", "0", "DOC-FEEDBACK-QUALITY"}, {"75", "101", "DOC-FEEDBACK-QUALITY"},
                {"75", "good", "DOC-FEEDBACK-QUALITY"}, {"75", "", "DOC-FEEDBACK-QUALITY"}
        };
    }

    @Test(dataProvider = "invalidFeedbackScores")
    public void activeFeedbackRequiresSeparateBoundedIntegerPriorityAndQuality(String priority, String quality, String diagnostic) throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            String path = "docs/feedback/README.md";
            String register = Files.readString(fixture.root.resolve(path));
            fixture.write(path, register.replace("| 75 | 60 | TODO |", "| " + priority + " | " + quality + " | TODO |"));
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), diagnostic);
        }
    }

    @Test
    public void feedbackAcceptsBothScoreBoundariesWithoutRescoringVerifiedHistory() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            String path = "docs/feedback/README.md";
            String register = Files.readString(fixture.root.resolve(path));
            fixture.write(path, register.replace("| 75 | 60 | TODO |", "| 1 | 100 | TODO |"));
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
            fixture.write(path, register.replace("| 75 | 60 | TODO |", "| 100 | 1 | TODO |"));
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
            fixture.write(path, register.replace("| Priority (1-100) | Quality (1-100) |", "| Score | Score |"));
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-FEEDBACK-SCHEMA");
            fixture.write(path, register.replace("| 75 | 60 | TODO |", "| 75 | TODO |"));
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-FEEDBACK invalid register columns");

            String baseline = "\nRetained baseline component scores:\n\n"
                    + "| ID | Impact / risk / urgency | Correctness / verification / clarity | Evidence scope |\n"
                    + "|---|---|---|---|\n| G-001 | 1 / 100 / 75 | 100 / 1 / 60 | Observed baseline scope |\n";
            fixture.write(path, register + baseline);
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
            for (String invalid : List.of(
                    "| G-003 | Delivered summary | Existing checks passed |",
                    "| G-003 | 0 / 80 / 75 | 90 / 70 / 60 | Observed scope |",
                    "| G-003 | 75 / 101 / 80 | 90 / 70 / 60 | Observed scope |",
                    "| G-003 | 75 / 80 | 90 / 70 / 60 | Observed scope |",
                    "| G-003 | 75 / 80 / 90 | 90 / 70 / 60 | |")) {
                fixture.write(path, register + baseline + invalid + "\n");
                List<String> findings = MarkdownDocumentationRules.validate(fixture.root);
                assertFinding(findings, "DOC-FEEDBACK-SCHEMA baseline component row");
                String report = GovernanceFiles.problems(findings);
                Assert.assertTrue(report.contains("Owner: docs/feedback/README.md"));
                Assert.assertTrue(report.contains("Repair hint: Correct the specific row/owner/state"));
            }
            fixture.write(path, register + baseline + "| G-001 | 90 / 80 / 75 | 70 / 60 / 50 | Another scope |\n");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-FEEDBACK duplicate baseline component ID");
            fixture.write(path, register + baseline.replace("Impact / risk / urgency", "Impact / risc / urgență")
                    .replace("Correctness / verification / clarity", "Corectitudine / verificare / claritate")
                    .replace("Evidence scope", "Limitele dovezilor"));
            fixture.write("docs/governance/readme-edition.properties", "language=ro\n");
            String projection = "docs/Living_Architecture_UI_API_doc_v1_0.html";
            fixture.write(projection, Files.readString(fixture.root.resolve(projection)).replace("lang=\"en\"", "lang=\"ro\""));
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @Test
    public void atlasCannotSilentlyBecomeARuntimeOrMavenModule() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            fixture.write("atf-atlas/pom.xml", "<project/>");
            fixture.write("atf-atlas/src/main/java/Runtime.java", "class Runtime {}\n");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-ATLAS-BOUNDARY");
        }
    }

}
