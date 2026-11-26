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
 * Protects Markdown structure, links, technical facts, language rules and owner guidance.
 */
public class DocumentationStructureGovernanceTest {
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
    public void romanianEditionTranslatesAllMaintainedMarkdownWhileSourceExamplesStayEnglish() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            fixture.write("docs/governance/readme-edition.properties", "language=ro\n");
            String html = "docs/Living_Architecture_UI_API_doc_v1_0.html";
            fixture.write(html, Files.readString(fixture.root.resolve(html)).replace("lang=\"en\"", "lang=\"ro\""));
            append(fixture, "README.md", "\nPentru verificare, folosiți comenzile existente.\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
            append(fixture, "docs/Testing.md", "\nPentru verificare.\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
            append(fixture, "atf-playwright/README.md", "\n```java compile\n// Pentru verificare.\npublic class Example {}\n```\n");
            assertFinding(MarkdownDocumentationRules.validateCompilableExamples(fixture.root), "DOC-CODE-LANGUAGE");
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
            romanian.write(path, original.replace("## The design in one view", "## Designul într-o singură imagine"));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-ANCHOR");
            romanian.write(path, original.replace("-->", "-.->"));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-PARITY");
            romanian.write(path, original.replace("docs/Architecture.md", "docs/Testing.md"));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-PARITY");
            String provider = "atf-playwright/README.md";
            romanian.write(provider, Files.readString(romanian.root.resolve(provider))
                    .replace("System.getProperty", "System.clearProperty"));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-PARITY");
            romanian.write(path, original);
            romanian.write("pom.xml", Files.readString(romanian.root.resolve("pom.xml")).replace("1.0.0", "9.0.0"));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-CONTENT");
            Files.delete(romanian.root.resolve("atf-core/README.md"));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root), "DOC-EDITION-INVENTORY");
        }
    }

    @DataProvider(name = "allMarkdownStructureDrift")
    public Object[][] allMarkdownStructureDrift() {
        return new Object[][] {
                {"## Contract\n\nNo document title.\n", "DOC-TITLE"},
                {"# Review\n\n# Second title\n", "DOC-TITLE"},
                {"# Review\n\n### Skipped level\n", "DOC-HEADING"},
                {"## Contract\n\n# Late title\n", "DOC-HEADING"},
                {"# Review\n\n```\nliteral output\n```\n", "DOC-FENCE-LANGUAGE"},
                {"# Review\n\n~~~\nliteral output\n~~~\n", "DOC-FENCE-LANGUAGE"},
                {"# Review\n\n<details><summary>Contract</summary>\nUnclosed detail.\n", "DOC-README-DETAILS"},
                {"# Review\n\n<style>body { color: red; }</style>\n", "DOC-README-STYLE"}
        };
    }

    @Test(dataProvider = "allMarkdownStructureDrift")
    public void normativeMarkdownHasOwnedStructuralFailuresAndARealRepair(String document, String code) throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            fixture.write("docs/Review.md", document);
            List<String> findings = MarkdownDocumentationRules.validate(fixture.root);
            assertFinding(findings, code);
            String report = GovernanceFiles.problems(findings);
            Assert.assertTrue(report.contains("Observed: docs/Review.md:"));
            Assert.assertTrue(report.contains("Repair hint:"));
            Assert.assertTrue(report.contains("Owner: docs/governance/Code_And_Documentation_Policy.md#reader-orientation-and-diagrams"));
            fixture.write("docs/Review.md", "# Review\n\n## Contract\n\nOwned behavior.\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @DataProvider(name = "editorialMarkers")
    public Object[][] editorialMarkers() {
        return new Object[][] {{"@@CODE_0@@"}, {"ZXQ005ZQ"}, {"\u2581unfinished"}};
    }

    @Test(dataProvider = "editorialMarkers")
    public void editorialArtifactsFailInProseAndDiagramLabelsButLiteralExamplesRemainData(String marker) throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            String original = "# Review\n\n## Contract\n\nSource facts.\n";
            fixture.write("docs/Review.md", original + "\n" + marker + "\n");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-EDITORIAL");
            fixture.write("docs/Review.md", original + "\n```mermaid\nflowchart LR\nA[\"" + marker
                    + "\"] --> B[\"Owned result\"]\n```\n\nThe arrow shows the result owner.\n");
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "DOC-EDITORIAL");
            fixture.write("docs/Review.md", original + "\nLiteral marker `" + marker + "`.\n\n```text\n"
                    + marker + "\n### Literal heading\n<style>example</style>\n```\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @Test
    public void normativeMarkdownAcceptsTypedNestedFencesAndLegalHeadingReturns() throws Exception {
        try (GovernanceFiles.Fixture fixture = documentation()) {
            fixture.write("docs/Review.md", "#\tReview\n\n## First\n\n### Child\n\n## Second\n\n"
                    + "<details>\n<summary>Literal example</summary>\n\n````markdown\n# Sample\n\n```\noutput\n```\n````\n\n</details>\n");
            assertValid(MarkdownDocumentationRules.validate(fixture.root));
        }
    }

    @DataProvider(name = "readmeProtectedFactDrift")
    public Object[][] readmeProtectedFactDrift() {
        return new Object[][] {
                {"`ui.engine`", "`unknown.engine`"},
                {"`com.ai:atf-core:1.0.0`", "`com.ai:atf-core:9.0.0`"},
                {"`src/test/resources/suites/framework-local.xml`", "`src/test/resources/suites/absent.xml`"},
                {"| 1.0.0 | 16 |", "| 9.0.0 | 16 |"},
                {"| 1.0.0 | 16 |", "| 1.0.0 | 99 |"},
                {"`ui.engine`", "ui.engine"}
        };
    }

    @Test(dataProvider = "readmeProtectedFactDrift")
    public void readmeTechnicalFactsRejectTranslationDriftWithOwnedDiagnostics(String before, String after) throws Exception {
        try (GovernanceFiles.Fixture english = documentation(); GovernanceFiles.Fixture romanian = documentation()) {
            romanian.write("docs/governance/readme-edition.properties", "language=ro\n");
            String facts = "\n## Technical facts\n\nSelect `ui.engine`; retain `com.ai:atf-core:1.0.0` and "
                    + "`src/test/resources/suites/framework-local.xml`.\n\n| Release | Cases |\n|---|---:|\n| 1.0.0 | 16 |\n";
            append(english, "README.md", facts);
            String canonical = Files.readString(english.root.resolve("README.md"));
            romanian.write("README.md", canonical);
            assertValid(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root));
            String drifted = canonical.replace(before, after);
            Assert.assertNotEquals(drifted, canonical, "The counterexample must change its technical fact");
            romanian.write("README.md", drifted);
            List<String> findings = MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root);
            assertFinding(findings, "DOC-EDITION-FACTS");
            String report = GovernanceFiles.problems(findings);
            Assert.assertTrue(report.contains("first differing protected item="));
            Assert.assertTrue(report.contains("Owner: docs/governance/Readme_Editions.md"));
            romanian.write("README.md", canonical);
            assertValid(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root));
        }
    }

    @Test
    public void protectedFactsPermitTranslatedLabelsReflowAndEquivalentCodeSpanDelimiters() throws Exception {
        try (GovernanceFiles.Fixture english = documentation(); GovernanceFiles.Fixture romanian = documentation()) {
            romanian.write("docs/governance/readme-edition.properties", "language=ro\n");
            String facts = "\n## Technical facts\n\nChoose `ui.engine` and ``literal ` delimiter``.\n\n"
                    + "| Release | Cases |\n|---|---:|\n| 1.0.0 | 16 |\n";
            append(english, "README.md", facts);
            String canonical = Files.readString(english.root.resolve("README.md"));
            romanian.write("README.md", canonical.replace("Choose `ui.engine` and", "Alege `ui.engine`\nși")
                    .replace("``literal ` delimiter``", "```literal ` delimiter```")
                    .replace("| Release | Cases |", "| Versiune | Cazuri |"));
            assertValid(MarkdownDocumentationRules.validateReadmeParity(english.root, romanian.root));
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
            Assert.assertEquals(failures.stream().filter(failure -> failure.contains("DOC-LINK")).count(), 3L,
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

}
