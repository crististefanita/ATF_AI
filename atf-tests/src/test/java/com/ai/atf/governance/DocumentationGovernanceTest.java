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

public class DocumentationGovernanceTest {
    @Test
    public void diagnosticOwnersResolveToRealContractsAndExistingGovernanceSelections() throws Exception {
        Path root = GovernanceFiles.root();
        Set<String> owners = new LinkedHashSet<>();
        Set<String> selections = new LinkedHashSet<>();
        for (String code : GovernanceFiles.diagnosticCodes()) {
            String report = GovernanceFiles.problems(List.of(code + " observed fixture evidence"));
            Matcher owner = Pattern.compile("(?m)^   Owner: (.+)$").matcher(report);
            Assert.assertTrue(owner.find(), "Diagnostic owner is missing for " + code);
            owners.add(owner.group(1));
            Matcher selection = Pattern.compile("-Dtest=([A-Za-z0-9]+)\"").matcher(report);
            Assert.assertTrue(selection.find(), "Maintained recheck is missing for " + code);
            selections.add(selection.group(1));
        }
        Assert.assertFalse(owners.isEmpty());
        for (String owner : owners) {
            String[] route = owner.split("#", 2);
            Path target = root.resolve(route[0]).normalize();
            Assert.assertTrue(target.startsWith(root) && Files.isRegularFile(target), "Diagnostic owner must resolve: " + owner);
            if (route.length == 2) Assert.assertTrue(MarkdownDocumentationRules.anchors(Files.readAllLines(target)).contains(route[1]),
                    "Diagnostic owner fragment must resolve: " + owner);
        }
        for (String selection : selections) {
            Class<?> owner = Class.forName("com.ai.atf.governance." + selection);
            Assert.assertTrue(Arrays.stream(owner.getMethods()).anyMatch(method -> method.isAnnotationPresent(Test.class)),
                    "Recheck must select an existing executable governance class: " + selection);
        }
    }

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
                    "atf-atlas/knowledge/atlas-contracts.json", "tools/atlas-report.mjs", "tools/atlas-report.test.mjs",
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
    public void validatorAcceptsTheSmallDocumentedReactor() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            fixture.write("docs/governance/readme-edition.properties", "# Declared README edition\nlanguage = en\n");
            Assert.assertEquals(MarkdownDocumentationRules.readmeEdition(fixture.root, new java.util.ArrayList<>()), "en");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
            assertValid(MarkdownDocumentationRules.validateCompilableExamples(fixture.root));
            String policy = Files.readString(fixture.root.resolve("docs/governance/Code_And_Documentation_Policy.md"));
            fixture.write("docs/governance/Code_And_Documentation_Policy.md", policy.replace("MUST be written", "MUST\nbe written"));
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @DataProvider(name = "invalidReadmeEditions")
    public Object[][] invalidReadmeEditions() {
        return new Object[][] {{""}, {"language=fr\n"}, {"language=ro\nextra=ignored\n"},
                {"language=en\nlanguage=ro\n"}, {"language=\\uZZZZ\n"}};
    }

    @Test(dataProvider = "invalidReadmeEditions")
    public void readmeLanguagePolicyRejectsMissingUnsupportedOrMalformedValues(String policy) throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            fixture.write("docs/governance/readme-edition.properties", policy);
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-EDITION");
        }
    }

    @Test
    public void romanianEditionExemptionAppliesOnlyToReadmes() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            fixture.write("docs/governance/readme-edition.properties", "language=ro\n");
            append(fixture, "README.md", "\nPentru verificare, folosiți comenzile existente.\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
            append(fixture, "docs/Testing.md", "\nPentru verificare.\n");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-LANGUAGE");
        }
    }

    @DataProvider(name = "readmeStyleViolations")
    public Object[][] readmeStyleViolations() {
        return new Object[][] {{"<font size='7'>Contract</font>", "DOC-README-STYLE"},
                {"<details><summary>Owner</summary>\nContract", "DOC-README-DETAILS"},
                {"<details>Contract</details>", "DOC-README-DETAILS"},
                {"<summary>Orphan</summary>", "DOC-README-DETAILS"},
                {"```mermaid\nflowchart LR\nA --> B\n```\n", "DOC-DIAGRAM-LEGEND"}};
    }

    @Test(dataProvider = "readmeStyleViolations")
    public void readmesRejectPresentationMarkupBrokenDisclosureAndUnexplainedDiagrams(String text, String rule) throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "README.md", "\n" + text + "\n");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), rule);
        }
    }

    @Test
    public void readmeEditionParityRejectsIndependentDriftAtEachBoundary() throws Exception {
        try (GovernanceFiles.Fixture english = documentation(); GovernanceFiles.Fixture romanian = documentation()) {
            romanian.write("docs/governance/readme-edition.properties", "language=ro\n");
            english.write(".gitignore", "target/\n");
            romanian.write(".gitignore", "target/\r\n");
            String service = "atf-playwright/src/main/resources/META-INF/services/example.Provider";
            english.write(service, "example.Engine\n");
            romanian.write(service, "example.Engine\r\n");
            romanian.write("atf-reporting/allure-results/run.json", "{}");
            romanian.write("atf-tests/.codex-artifacts/run.json", "{}");
            assertValid(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root));
            String owned = "atf-core/src/main/resources/allure-results/contract.json";
            romanian.write(owned, "{}");
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-INVENTORY");
            english.write(owned, "{}");
            assertValid(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root));
            romanian.write(service, "example.DifferentEngine\r\n");
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-CONTENT");
            romanian.write(service, "example.Engine\r\n");
            String path = "README.md";
            String original = Files.readString(romanian.root.resolve(path));
            romanian.write(path, original.replace("Core[Core]", "Core[Principal]").replace("|provides|", "|oferă|"));
            assertValid(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root));
            romanian.write(path, original.replace("## Overview", "## Prezentare generală"));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-ANCHOR");
            romanian.write(path, original.replace("-->", "-.->"));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-PARITY");
            romanian.write(path, original.replace("docs/Architecture.md#runtime", "docs/Testing.md"));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-PARITY");
            romanian.write(path, original.replace("System.getProperty", "System.clearProperty"));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-PARITY");
            romanian.write(path, original);
            romanian.write("pom.xml", Files.readString(romanian.root.resolve("pom.xml")).replace("1.0.0", "9.0.0"));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-CONTENT");
            Files.delete(romanian.root.resolve("atf-core/README.md"));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-INVENTORY");
        }
    }

    @Test
    public void validatorRejectsMissingLinkTargetsAndMissingFragments() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "README.md", "\n[Missing](docs/Missing.md)\n[Missing fragment](docs/Architecture.md#absent)\n");
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(failures, "DOC-LINK");
            assertFinding(failures, "DOC-ANCHOR");
        }
    }

    @Test
    public void validatorAcceptsDuplicateHeadingAnchorsExplicitAnchorsAndEncodedPaths() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "docs/Architecture.md", "\n## Details\n\n## Details\n\n<a id=\"stable-contract\"></a>\n");
            fixture.write("docs/Space guide.md", "# Space guide\n\n## Runtime\n");
            append(fixture, "README.md", "\n[Repeated](docs/Architecture.md#details-1)\n"
                    + "[Explicit](docs/Architecture.md#stable-contract)\n[Encoded](docs/Space%20guide.md#runtime)\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @Test
    public void validatorIgnoresLiteralMarkdownExamplesWhenResolvingLinksAndAnchors() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "README.md", "\n````markdown\n[Example](missing.md)\n## Hidden\n```\n````\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
            Set<String> anchors = MarkdownDocumentationRules.anchors(Files.readAllLines(fixture.root.resolve("README.md")));
            Assert.assertFalse(anchors.contains("hidden"), "Fenced headings do not create real anchors");
        }
    }

    @Test
    public void validatorRejectsOpenFencesEmptyDiagramsAndMissingArchitectureDiagrams() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            fixture.write("docs/Architecture.md", "# Architecture\n\nRuntime contracts.\n");
            append(fixture, "README.md", "\n```mermaid\nflowchart LR\n```\n\n~~~java\nclass Unclosed {}\n");
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(failures, "DOC-FENCE");
            assertFinding(failures, "DOC-MERMAID");
            assertFinding(failures, "DOC-DIAGRAM");
        }
    }

    @Test
    public void languagePolicyRejectsKnownRomanianMarkersWhileAllowingCodeExamples() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "README.md", "\n```java\nString example = \"pentru rulare\";\n```\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
            append(fixture, "README.md", "\nPentru rulare, use the suite.\n\n```mermaid\nflowchart LR\nA[Pentru rulare] --> B[Runtime]\n```\n");
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            Assert.assertEquals(failures.stream().filter(failure -> failure.contains("DOC-LANGUAGE")).count(), 2L,
                    GovernanceFiles.problems(failures));
        }
    }

    @DataProvider(name = "referenceLinks")
    public Object[][] referenceLinks() {
        return new Object[][] {
                {"[Guide][g]\n\n[g]: docs/Missing.md\n"},
                {"[Guide][]\n\n[Guide]: docs/Missing.md\n"},
                {"[Guide]\n\n[Guide]: docs/Missing.md\n"},
                {"![Diagram][d]\n\n[d]: docs/Missing.svg\n"}
        };
    }

    @Test(dataProvider = "referenceLinks")
    public void validatorRejectsBrokenFullCollapsedShortcutAndImageReferenceLinks(String reference) throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "README.md", "\n" + reference);
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-LINK");
        }
    }

    @Test
    public void validatorAcceptsReferenceLinksAndIgnoresLinksInsideCodeSpans() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "README.md", "\n[Guide][g]\n[Architecture][]\n[g]\n![Diagram][d]\n"
                    + "\n[g]: docs/Architecture.md#runtime\n[Architecture]: docs/Architecture.md\n[d]: docs/Architecture.md\n"
                    + "\n`[Guide](missing.md)`\n``[Guide](missing.md) and `[g]: missing.md` ``\n"
                    + "\n`[g]: missing.md`\n``[literal][absent]``\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @Test
    public void validatorRejectsUndefinedExplicitReferencesAndFencedDiagramExamples() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "README.md", "\n[Guide][absent]\n");
            fixture.write("docs/Architecture.md", "# Architecture\n\n## Runtime\n\n````markdown\n```mermaid\nflowchart LR\nA --> B\n```\n````\n");
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(failures, "DOC-LINK");
            assertFinding(failures, "DOC-DIAGRAM");
        }
    }

    @Test
    public void languagePolicyRejectsCommonRomanianProseAndIgnoresLiteralProductData() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "README.md", "\n`docs/pornire.md` and `Timișoara` are literal examples.\n"
                    + "\n```json\n{\"city\":\"Timișoara\"}\n```\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
            append(fixture, "README.md", "\nGhid de pornire. Acest modul pornește browserul și validează facturile.\n");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-LANGUAGE");
        }
    }

    @Test
    public void validatorRejectsMissingOwnerDocsUnsafeLinksAndInvalidEscapes() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            Files.delete(fixture.root.resolve("atf-reporting/README.md"));
            append(fixture, "README.md", "\n[Outside](../outside.md)\n[Invalid](docs/%XX.md)\n");
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(failures, "DOC-MISSING");
            Assert.assertEquals(failures.stream().filter(failure -> failure.contains("DOC-LINK")).count(), 2L,
                    GovernanceFiles.problems(failures));
            fixture.write("atf-selenium/README.md", "# atf-selenium\n\n```java compile\npublic class BrokenExample { void run(){ missingApi(); } }\n```\n");
            fixture.write("atf-playwright/README.md", "# atf-playwright\n\n````markdown\n```java compile\npublic class LiteralExample { void run(){ missingApi(); } }\n```\n````\n");
            List<String> compiled = MarkdownDocumentationRules.validateCompilableExamples(fixture.root);
            Assert.assertEquals(compiled.size(), 1, GovernanceFiles.problems(compiled));
            assertFinding(compiled, "DOC-JAVA-COMPILE");
            fixture.write("atf-selenium/README.md", "# atf-selenium\n\n```java compile\n// Pentru rulare\npublic class EnglishExample {}\n```\n");
            assertFinding(MarkdownDocumentationRules.validateCompilableExamples(fixture.root), "DOC-CODE-LANGUAGE");
            fixture.write("atf-selenium/README.md", "# atf-selenium\n\n```java compile\npublic class IdentifierExample { void valideazaFactura() {} }\n```\n");
            assertFinding(MarkdownDocumentationRules.validateCompilableExamples(fixture.root), "DOC-CODE-LANGUAGE");
            fixture.write("atf-selenium/README.md", "# atf-selenium\n\n```java compile\npublic class DiagnosticExample { String diagnostic = \"Eroare pentru cerere\"; }\n```\n");
            assertFinding(MarkdownDocumentationRules.validateCompilableExamples(fixture.root), "DOC-CODE-LANGUAGE");
        }
    }

    @Test
    public void validatorRequiresTheDocumentationIndexFeedbackAndRecoveredOwnerGuides() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            for (String file : List.of("docs/README.md", "docs/feedback/README.md", "docs/governance/README.md",
                    "atf-atlas/README.md", "docs/Living_Architecture_UI_API_doc_v1_0.md")) {
                Files.delete(fixture.root.resolve(file));
            }
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            Assert.assertEquals(failures.stream().filter(value -> value.contains("DOC-MISSING")).count(), 5L,
                    GovernanceFiles.problems(failures));
        }
    }

    @Test
    public void validatorRejectsMissingOwnerResponsibilitiesAndVerification() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            fixture.write("atf-core/README.md", "# atf-core\n\n## Overview\n\nCore contracts.\n");
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            Assert.assertEquals(failures.stream().filter(value -> value.contains("DOC-OWNER")).count(), 2L,
                    GovernanceFiles.problems(failures));
            fixture.write("atf-playwright/README.md", "# atf-playwright\n\n## Overview\n\nScope.\n\n## Ownership\n\nProvider.\n\n## Verification\n\nRun the maintained checks.\n");
            String policy = Files.readString(fixture.root.resolve("docs/governance/Code_And_Documentation_Policy.md"));
            fixture.write("docs/governance/Code_And_Documentation_Policy.md", policy.replace("MUST be written in English", "uses a reviewed convention"));
            failures = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(failures, "DOC-README-EXAMPLE");
            assertFinding(failures, "DOC-README-ARCHITECTURE");
            assertFinding(failures, "DOC-ENGLISH-REQUIREMENT");
        }
    }

    @Test
    public void compilerAndCoordinateFactsMustMatchTheCurrentPomOutsideLiteralExamples() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            String readme = Files.readString(fixture.root.resolve("README.md"));
            fixture.write("README.md", readme.replace("com.ai:atf-parent:1.0.0", "com.ai:test-framework:1.0.0")
                    .replace("Java 11", "Java 21") + "\n```text\ncom.ai:atf-parent:1.0.0 Java 11\n```\n");
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            Assert.assertEquals(failures.stream().filter(value -> value.contains("DOC-BUILD-FACT")).count(), 2L,
                    GovernanceFiles.problems(failures));
        }
    }

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

    @Test
    public void livingHtmlDetectsStaleMarkdownMissingSectionsAndBrokenNavigation() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            String file = "docs/Living_Architecture_UI_API_doc_v1_0.html";
            String html = Files.readString(fixture.root.resolve(file));
            fixture.write(file, html.replace("<h2 id=\"overview\">Overview</h2>", "<h2 id=\"overview\">Different section</h2>")
                    .replace("href=\"#overview\"", "href=\"#missing\""));
            append(fixture, "docs/Living_Architecture_UI_API_doc_v1_0.md", "\nCurrent ownership changed.\n");
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(failures, "DOC-PAIR source metadata/hash");
            assertFinding(failures, "DOC-PAIR keep the ordered section map");
            assertFinding(failures, "DOC-PAIR missing HTML anchor");
        }
    }

    @Test
    public void livingPairAcceptsWindowsLineEndingsAndChecksLocalHtmlLinks() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            String markdown = "docs/Living_Architecture_UI_API_doc_v1_0.md";
            fixture.write(markdown, Files.readString(fixture.root.resolve(markdown)).replace("\n", "\r\n"));
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
            append(fixture, "docs/Living_Architecture_UI_API_doc_v1_0.html", "<a href=\"Missing.md\">Owner</a>");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-LINK missing target");
        }
    }

    @Test
    public void invalidFeedbackIdsAndDuplicateHtmlIdsCannotHideBrokenNavigation() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "docs/feedback/README.md", "\n| G-2 | Contract improved | Negative fixture passed |\n");
            append(fixture, "docs/Living_Architecture_UI_API_doc_v1_0.html", "<div id=\"overview\">Duplicate</div>");
            append(fixture, "README.md", "\n[Broken HTML section](docs/Living_Architecture_UI_API_doc_v1_0.html#absent)\n");
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(failures, "DOC-FEEDBACK invalid item ID");
            assertFinding(failures, "DOC-PAIR duplicate HTML anchor");
            assertFinding(failures, "DOC-ANCHOR missing HTML fragment");
        }
    }

    @Test
    public void projectGovernanceCannotReintroduceAgentInstructionFiles() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            fixture.write("AGENTS.md", "# Agent instructions\n\nUse this file.\n");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-AGENT-CONTROL");
        }
    }

    @DataProvider(name = "invalidMavenSelections")
    public Object[][] invalidMavenSelections() {
        return new Object[][] {
                {"mvn -Pmissing test"},
                {"mvn -pl missing test"},
                {"mvn -f missing/pom.xml test"},
                {"mvn -fmissing/pom.xml test"},
                {"mvn -Datf.test.suite=src/test/resources/suites/missing.xml test"},
                {"mvn -Dsurefire.suiteXmlFiles= test"},
                {"mvn -Dtest=MissingTest test"},
                {"mvn -Dtest=Helper test"},
                {"mvn -f atf-core/pom.xml -Pbrowser test"},
                {"mvn -f atf-core/pom.xml -pl atf-tests test"},
                {"mvn -Dtest=ProbeTest#check test"},
                {"mvn -DskipTests=false -Datf.test.suite=missing.xml test"},
                {"mvn test && echo accepted"},
                {"mvn -pl \"atf-tests test"}
        };
    }

    @Test(dataProvider = "invalidMavenSelections")
    public void executableCommandsRejectMissingOrUnsupportedSelections(String command) throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "docs/Testing.md", "\n```powershell\n" + command + "\n```\n");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-COMMAND");
        }
    }

    @Test
    public void executableCommandsAcceptQuotedCurrentSelectionsAndLiteralExamples() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "docs/Testing.md", "\n```powershell\n"
                    + "mvn -B -ntp test\n"
                    + "mvn -pl atf-tests -am -Pbrowser test\n"
                    + "mvn -f atf-tests/pom.xml -Paggregate verify\n"
                    + "mvn \"-Dtest=ProbeTest,example.*Test\" test\n"
                    + "mvn -Dsurefire.suiteXmlFiles=src/test/resources/suites/framework-local.xml test\n"
                    + "mvn -f atf-core/pom.xml test\n"
                    + "mvn -f atf-playwright/pom.xml org.codehaus.mojo:exec-maven-plugin:3.5.0:java \"-Dexec.args=install chromium\"\n"
                    + "mvn test # -Pmissing is a comment, not an active profile\n```\n"
                    + "\n````markdown\n```powershell\nmvn -Pmissing test\n```\n````\n"
                    + "\n```text\nmvn -f missing/pom.xml -Dtest=MissingTest test\n```\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @Test
    public void defaultAndProfileSuitesAreResolvedFromTheirCurrentPomDeclarations() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "docs/Testing.md", "\n```powershell\nmvn test\nmvn -Pbrowser test\n```\n");
            Files.delete(fixture.root.resolve("atf-tests/src/test/resources/suites/framework-browser.xml"));
            Files.delete(fixture.root.resolve("atf-tests/src/test/resources/suites/framework-local.xml"));
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            Assert.assertEquals(failures.stream().filter(value -> value.contains("DOC-COMMAND suite is not")).count(), 2L,
                    GovernanceFiles.problems(failures));
        }
    }

    @Test
    public void profileRenamesAndExcludedVerificationOwnersInvalidateTheirDocumentedCommands() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            String pom = Files.readString(fixture.root.resolve("atf-tests/pom.xml"));
            fixture.write("atf-tests/pom.xml", pom.replace("<id>browser</id>", "<id>renamed-browser</id>"));
            append(fixture, "docs/Testing.md", "\n```powershell\nmvn -Pbrowser test\n"
                    + "mvn -pl atf-core -Dtest=ProbeTest test\n```\n");
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(failures, "DOC-COMMAND profile is not declared");
            assertFinding(failures, "DOC-COMMAND documented class filters must select");
        }
    }

    @Test
    public void declaredDefaultsRejectSourceDriftUnknownKeysDuplicatesAndEmptyClaims() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            fixture.write("atf-core/src/main/resources/framework.properties", "ui.engine=selenium\nauth.token=\nreports.dir=target/reports\n");
            append(fixture, "docs/Testing.md", "\n```properties defaults\nunknown.setting=value\n"
                    + "auth.token=\nauth.token=\n```\n\n```properties defaults\n# Empty claim\n```\n");
            List<String> failures = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(failures, "DOC-DEFAULT ui.engine differs");
            assertFinding(failures, "DOC-DEFAULT unknown.setting differs");
            assertFinding(failures, "DOC-DEFAULT duplicate default key");
            assertFinding(failures, "DOC-DEFAULT declare at least one");
        }
    }

    @Test
    public void defaultDeclarationsPreserveBlankValuesAndDoNotMisclassifyOverrideExamples() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "docs/Testing.md", "\n```properties defaults\nauth.token=\nreports.dir=target/reports\n```\n"
                    + "\n```properties example\nui.engine=selenium\nunknown.local.option=value\n```\n"
                    + "\n````markdown\n```properties defaults\nunknown.example=not-a-claim\n```\n````\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @Test
    public void livingConfigurationCannotDropItsExplicitDefaultDeclaration() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            String path = "docs/Living_Architecture_UI_API_doc_v1_0.md";
            String original = Files.readString(fixture.root.resolve(path));
            String changed = original.replace("properties defaults", "properties example");
            fixture.write(path, changed);
            String html = Files.readString(fixture.root.resolve("docs/Living_Architecture_UI_API_doc_v1_0.html"));
            fixture.write("docs/Living_Architecture_UI_API_doc_v1_0.html", html.replace(hash(original), hash(changed)));
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-DEFAULT retain explicitly declared");
        }
    }

    @DataProvider(name = "stableReceiptMarkers")
    public Object[][] stableReceiptMarkers() {
        return new Object[][] {
                {"Verification passed\non 2026-11-24."},
                {"| 2026-11-24 | PASS |"},
                {"**Status: VERIFIED on 2026-11-24.**"},
                {"ATF_AI score: 91/100."},
                {"Rating: 88/100."},
                {"393/393 checks passed."},
                {"393 tests passed."},
                {"passed: 393"}
        };
    }

    @Test(dataProvider = "stableReceiptMarkers")
    public void stableOwnerGuidesRejectExecutionReceiptsAndReviewScorecards(String receipt) throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "docs/Testing.md", "\n" + receipt + "\n");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-LIFECYCLE");
        }
    }

    @Test
    public void lifecyclePreservesContractsFeedbackReceiptsAndFencedCounterexamples() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            append(fixture, "docs/Testing.md", "\nPASS requires actual execution; a copied report is not new evidence.\n"
                    + "\n```text\n393 tests passed on 2026-11-24. Rating: 88/100.\n```\n");
            fixture.write("docs/feedback/Historical_Receipt.md", "# Historical receipt\n\nStatus: VERIFIED on 2026-11-24.\n"
                    + "\n393 tests passed.\n\n```powershell\nmvn -Pold-profile test\n```\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    private static GovernanceFiles.Fixture documentation() throws Exception {
        GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture();
        fixture.write("docs/governance/readme-edition.properties", "language=en\n");
        for (String guide : List.of("artifacts/README.md", "artifacts/reports/README.md", "artifacts/atlas/README.md")) {
            fixture.write(guide, "# Artifact owner\n\n## Overview\n\nEvidence scope.\n\n## Ownership\n\nMaintain the contract.\n\n## Verification\n\nInspect the existing checks.\n");
        }
        fixture.write("artifacts/atlas/Project_Map_Template.md", "# Project map template\n\nRecord source facts and explicit unknowns.\n");
        fixture.write("tools/collect-artifacts.mjs", "// Collection tooling fixture.\n");
        fixture.write("tools/collect-artifacts.test.mjs", "// Tooling regression fixture.\n");
        fixture.write("tools/verify-atlas.mjs", "// Atlas verification fixture.\n");
        fixture.write("tools/verify-atlas.test.mjs", "// Atlas verifier regression fixture.\n");
        for (String tool : List.of("atlas-report.mjs", "atlas-report.test.mjs", "atlas-knowledge.mjs",
                "atlas-knowledge.test.mjs", "atlas-diagnostics.mjs", "atlas-bundle.mjs", "atlas-bundle.test.mjs",
                "atlas-execution.mjs", "atlas-execution.test.mjs", "atlas-understanding.mjs", "atlas-understanding.test.mjs",
                "atlas-behavior.mjs", "atlas-behavior.test.mjs")) {
            fixture.write("tools/" + tool, "// Atlas maintenance tooling fixture.\n");
        }
        fixture.write("atf-atlas/Report_Contract.md", "# Complete Atlas report\n\nOne source-bound document with inline assets and proof limits.\n");
        fixture.write("atf-atlas/Knowledge_Base.md", "# Atlas knowledge\n\nCited requirements support a source-grounded reviewer.\n");
        fixture.write("atf-atlas/assets/atlas.css", ":root { color-scheme: light; }\n");
        fixture.write("atf-atlas/knowledge/atlas-contracts.json", "{\"schemaVersion\":1}\n");
        fixture.write("README.md", "# ATF\n\ncom.ai:atf-parent:1.0.0 targets Java 11.\n\n## Overview\n\n" + String.join(", ", GovernanceFiles.MODULES)
                + "\n\n[Architecture](docs/Architecture.md#runtime)\n\n" + diagram());
        fixture.write("docs/Architecture.md", "# Architecture\n\n## Runtime\n\n" + diagram());
        fixture.write("docs/Testing.md", "# Testing\n\nRun the maintained suites in staging.\n");
        fixture.write("docs/Development.md", "# Development\n\nReview and synchronize verified source commits.\n");
        String modules = GovernanceFiles.MODULES.stream().map(module -> "<module>" + module + "</module>")
                .collect(java.util.stream.Collectors.joining());
        fixture.write("pom.xml", "<project><groupId>com.ai</groupId><artifactId>atf-parent</artifactId><version>1.0.0</version>"
                + "<properties><maven.compiler.release>11</maven.compiler.release></properties><modules>" + modules + "</modules></project>");
        for (String module : GovernanceFiles.MODULES) {
            String selection = module.equals("atf-tests") ? "<properties><atf.test.suite>src/test/resources/suites/framework-local.xml</atf.test.suite></properties>"
                    + "<profiles><profile><id>browser</id><properties><atf.test.suite>src/test/resources/suites/framework-browser.xml</atf.test.suite></properties></profile>"
                    + "<profile><id>aggregate</id><properties><atf.test.suite>src/test/resources/suites/framework-aggregate.xml</atf.test.suite></properties></profile></profiles>" : "";
            fixture.write(module + "/pom.xml", "<project><artifactId>" + module + "</artifactId>" + selection + "</project>");
        }
        fixture.write("atf-tests/src/test/java/example/ProbeTest.java", "package example; public class ProbeTest { @org.testng.annotations.Test public void check() {} }\n");
        fixture.write("atf-tests/src/test/java/example/Helper.java", "package example; public class Helper {}\n");
        for (String suite : List.of("local", "browser", "aggregate")) fixture.write("atf-tests/src/test/resources/suites/framework-" + suite + ".xml",
                "<suite name=\"fixture\"><test name=\"contracts\"><classes><class name=\"example.ProbeTest\"/></classes></test></suite>");
        fixture.write("atf-core/src/main/resources/framework.properties", "ui.engine=playwright\nauth.token=\nreports.dir=target/reports\n");
        fixture.write("docs/README.md", "# Documentation\n\n[Owners](../atf-core/README.md)\n");
        fixture.write("docs/feedback/README.md", "# Feedback\n\n## Active TODOs\n\n| ID | Finding | Owner | Priority (1-100) | Quality (1-100) | Status | Closure evidence |\n"
                + "|---|---|---|---|---|---|---|\n| G-001 | Improve contract | atf-tests | 75 | 60 | TODO | Execute negative cases |\n"
                + "\n## Verified corrections\n\n| ID | Delivered behavior | Verification |\n|---|---|---|\n"
                + "| G-002 | Preserved ownership | Negative fixture passed |\n");
        fixture.write("docs/governance/README.md", "# Governance\n\n[Policy](Code_And_Documentation_Policy.md)\n");
        fixture.write("docs/governance/Code_And_Documentation_Policy.md", "# Code and documentation policy\n\nAuthored identifiers, comments and messages MUST be written in English.\n");
        fixture.write("docs/governance/Failure_Diagnostics.md", "# Governance failure diagnostics\n\nPreserve observed findings, owned repair hints and verification.\n");
        fixture.write("atf-atlas/Coverage_Map.md", "# Coverage map\n\n[Contracts](../atf-core/README.md)\n");
        for (String module : java.util.stream.Stream.concat(GovernanceFiles.MODULES.stream(), java.util.stream.Stream.of("atf-atlas"))
                .collect(java.util.stream.Collectors.toList())) {
            fixture.write(module + "/README.md", "# " + module + "\n\n## Overview\n\nModule scope.\n\n## Ownership\n\nOwn the contracts.\n\n## Verification\n\nRun the maintained checks.\n");
        }
        for (String readme : List.of("README.md", "atf-playwright/README.md", "atf-selenium/README.md")) {
            append(fixture, readme, "\n" + diagram()
                    + "\n```java compile\npublic class ConsumerExample { public void run() { System.getProperty(\"fixture\"); } }\n```\n");
        }
        String living = "# Living architecture\n\n## Overview\n\nCurrent UI and API contracts.\n\n```properties defaults\nui.engine=playwright\n```\n";
        fixture.write("docs/Living_Architecture_UI_API_doc_v1_0.md", living);
        fixture.write("docs/Living_Architecture_UI_API_doc_v1_0.html", "<!doctype html><html lang=\"en\"><head>"
                + "<meta name=\"atf-source\" content=\"Living_Architecture_UI_API_doc_v1_0.md\">"
                + "<meta name=\"atf-source-sha256\" content=\"" + hash(living) + "\"></head><body>"
                + "<h1>Living architecture</h1><a href=\"#overview\">Overview</a><h2 id=\"overview\">Overview</h2>"
                + "<p>Current UI and API contracts.</p></body></html>");
        return fixture;
    }

    private static String hash(String source) throws Exception {
        StringBuilder result = new StringBuilder();
        for (byte value : MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8))) {
            result.append(String.format("%02x", value & 0xff));
        }
        return result.toString();
    }

    private static String diagram() {
        return "```mermaid\nflowchart LR\nCore[Core] -->|provides| Runtime[Runtime]\n```\n\nThe arrow shows the provided runtime contract.\n";
    }

    private static void append(GovernanceFiles.Fixture fixture, String path, String content) throws Exception {
        fixture.write(path, Files.readString(fixture.root.resolve(path)) + content);
    }

    private static void assertValid(List<String> failures) {
        Assert.assertTrue(failures.isEmpty(), GovernanceFiles.problems(failures));
    }

    private static void assertFinding(List<String> failures, String code) {
        Assert.assertTrue(failures.stream().anyMatch(failure -> GovernanceFiles.matchesFinding(failure, code)), "Expected " + code + "; " + GovernanceFiles.problems(failures));
        Assert.assertTrue(failures.stream().allMatch(GovernanceFiles::hasDiagnosticHelp),
                "Existing negative fixtures must exercise owned diagnostic guidance. " + GovernanceFiles.problems(failures));
    }
}
