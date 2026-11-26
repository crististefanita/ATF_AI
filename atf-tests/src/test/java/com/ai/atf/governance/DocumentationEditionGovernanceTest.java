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
 * Protects the complete English and Romanian documentation review, translation policy and literal spans.
 */
public class DocumentationEditionGovernanceTest {
    @Test
    public void completeDocumentationEditionHasCurrentWholeTextReviewAndLanguageSignals() throws Exception {
        Path root = GovernanceFiles.root();
        assertValid(DocumentationTranslationRules.validateLanguage(root));
        assertValid(DocumentationTranslationRules.validateReview(root));
        String peer = System.getProperty("atf.docs.peer.root", "");
        if (!peer.isBlank()) {
            boolean english = MarkdownDocumentationRules.readmeEdition(root, new java.util.ArrayList<>()).equals("en");
            assertValid(DocumentationTranslationRules.validatePair(english ? root : Path.of(peer), english ? Path.of(peer) : root));
        }
    }

    @Test
    public void anEnglishTestingDocumentAndAnUntranslatedInteriorBlockHaveOwnedFailuresAndRealRepairs() throws Exception {
        try (GovernanceFiles.Fixture en = new GovernanceFiles.Fixture(); GovernanceFiles.Fixture ro = new GovernanceFiles.Fixture()) {
            en.write("docs/governance/readme-edition.properties", "language=en\n");
            ro.write("docs/governance/readme-edition.properties", "language=ro\n");
            String original = "# Testing\n\nRun existing checks from an isolated validation copy and retain the original native results with their exact source checkpoint.\n";
            en.write("docs/Testing.md", original);
            ro.write("docs/Testing.md", original);
            assertFinding(DocumentationTranslationRules.validateLanguage(ro.root), "DOC-EDITION-LANGUAGE");
            assertFinding(DocumentationTranslationRules.validatePair(en.root, ro.root), "DOC-EDITION-UNTRANSLATED");
            String translated = "# Testare\n\nPentru verificare, rulați testele existente într-o copie izolată și păstrați rezultatele native originale împreună cu versiunea exactă a sursei.\n";
            ro.write("docs/Testing.md", translated);
            writeTranslationReview(en, ro);
            assertValid(DocumentationTranslationRules.validateLanguage(ro.root));
            assertValid(DocumentationTranslationRules.validatePair(en.root, ro.root));
            ro.write("docs/Testing.md", translated + "\n" + original.substring(original.indexOf("Run existing")));
            assertFinding(DocumentationTranslationRules.validatePair(en.root, ro.root), "DOC-EDITION-UNTRANSLATED");
            assertFinding(DocumentationTranslationRules.validateReview(ro.root), "DOC-TRANSLATION-STALE");
            ro.write("docs/Testing.md", translated);
            assertValid(DocumentationTranslationRules.validatePair(en.root, ro.root));
        }
    }

    @Test
    public void literalEnglishCommandsAreNotTranslationProseAndWrappedParagraphsKeepTheirIdentity() {
        String prose = "Keep the original native outcomes and exact source checkpoint when reviewing a selected framework contract.";
        List<String> original = DocumentationTranslationRules.proseBlocks(prose + "\n\n```powershell\nmvn -B -ntp test\n```\n");
        Assert.assertEquals(original, DocumentationTranslationRules.proseBlocks(prose.replace("outcomes and", "outcomes\nand")));
        Assert.assertEquals(original.size(), 1);
        Assert.assertFalse(original.get(0).contains("mvn"));
    }

    @DataProvider(name = "translationLiteralSpans")
    public Object[][] translationLiteralSpans() {
        String literal = "Keep the original native outcomes and exact source checkpoint when reviewing a selected framework contract.";
        return new Object[][] {
                {"``literal ` " + literal + "``"},
                {"`" + literal.replace("source checkpoint", "source\ncheckpoint") + "`"},
                {"``literal ` " + literal.replace("source checkpoint", "source\ncheckpoint") + "``"},
                {"https://مثال.اختبار/Keep/the/original/native/outcomes/and/exact/source/checkpoint/when/reviewing/a/selected/framework/contract"}
        };
    }

    @Test(dataProvider = "translationLiteralSpans")
    public void literalCodeSpansAndUrlsStayOutsideUntranslatedProse(String literal) throws Exception {
        Assert.assertEquals(DocumentationTranslationRules.proseBlocks(literal), List.of(),
                "Literal code and URL payloads are data, including matching multiline backtick runs.");
        try (GovernanceFiles.Fixture en = new GovernanceFiles.Fixture(); GovernanceFiles.Fixture ro = new GovernanceFiles.Fixture()) {
            en.write("docs/governance/readme-edition.properties", "language=en\n");
            ro.write("docs/governance/readme-edition.properties", "language=ro\n");
            en.write("docs/Testing.md", "# Testing\n\nInspect the retained evidence.\n\n" + literal + "\n");
            ro.write("docs/Testing.md", "# Testare\n\nPentru verificare, inspectați dovezile păstrate.\n\n" + literal + "\n");
            writeTranslationReview(en, ro);
            assertValid(DocumentationTranslationRules.validatePair(en.root, ro.root));
        }
    }

    @Test
    public void escapedBackticksRemainVisibleAndWrappedListProseCannotHideUntranslatedEnglish() throws Exception {
        String first = "Keep the original native outcomes and exact source checkpoint";
        String second = "when reviewing a selected framework contract and retain every observed failure.";
        String escaped = "\\`" + first + " " + second + "\\`";
        Assert.assertEquals(DocumentationTranslationRules.proseBlocks(escaped), List.of(escaped),
                "Escaped backticks display prose rather than opening a literal code span.");
        try (GovernanceFiles.Fixture en = new GovernanceFiles.Fixture(); GovernanceFiles.Fixture ro = new GovernanceFiles.Fixture()) {
            en.write("docs/governance/readme-edition.properties", "language=en\n");
            ro.write("docs/governance/readme-edition.properties", "language=ro\n");
            en.write("docs/Testing.md", "# Testing\n\n- " + first + "\n  " + second + "\n");
            String intro = "# Testare\n\nPentru verificare, inspectați dovezile native păstrate.\n\n";
            ro.write("docs/Testing.md", intro + "- Keep the original native outcomes\n  and exact source checkpoint " + second + "\n");
            writeTranslationReview(en, ro);
            assertFinding(DocumentationTranslationRules.validatePair(en.root, ro.root), "DOC-EDITION-UNTRANSLATED");
            ro.write("docs/Testing.md", intro + "- Păstrați rezultatele native originale și reperul exact al sursei\n"
                    + "  când revizuiți un contract selectat al framework-ului și păstrați fiecare eșec observat.\n");
            writeTranslationReview(en, ro);
            assertValid(DocumentationTranslationRules.validatePair(en.root, ro.root));
        }
    }

    @Test
    public void livingTranslationAcceptsEquivalentAttributeFormattingAndCssLineEndings() throws Exception {
        try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture()) {
            String english = translationHtmlFixture();
            String romanian = english.replace("lang=\"en\"", "lang=\"ro\"")
                    .replace("<style>", "<STYLE \n>").replace("</style>", "</STYLE >")
                    .replace("href=\"Architecture.md\"", "href = 'Architecture.md'")
                    .replace("src=\"diagram.svg\"", "src = diagram.svg").replace("\n", "\r\n");
            fixture.write("english.html", english);
            fixture.write("romanian.html", romanian);
            assertValid(DocumentationTranslationRules.validateHtmlParity(
                    fixture.root.resolve("english.html"), fixture.root.resolve("romanian.html")));
            fixture.write("english.html", english.replace("</body>", "<script>runUnexpectedCode();</script></body>"));
            assertFinding(DocumentationTranslationRules.validateHtmlParity(
                    fixture.root.resolve("english.html"), fixture.root.resolve("romanian.html")), "DOC-EDITION-PARITY");
            fixture.write("english.html", english);
            assertValid(DocumentationTranslationRules.validateHtmlParity(
                    fixture.root.resolve("english.html"), fixture.root.resolve("romanian.html")));
        }
    }

    @DataProvider(name = "translationHtmlMutations")
    public Object[][] translationHtmlMutations() {
        return new Object[][] {{"media"}, {"css"}, {"route"}, {"extra-source"}, {"external-css"}, {"missing-style"}, {"script"}};
    }

    @Test(dataProvider = "translationHtmlMutations")
    public void livingTranslationRejectsDisabledStylingChangedRoutesAndExternalCssThenAcceptsRepair(String mutation) throws Exception {
        try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture()) {
            String english = translationHtmlFixture();
            String romanian = english.replace("lang=\"en\"", "lang=\"ro\"");
            String changed;
            switch (mutation) {
                case "media": changed = romanian.replace("<style>", "<style media = 'print'>"); break;
                case "css": changed = romanian.replace("color: black", "color: red"); break;
                case "route": changed = romanian.replace("href=\"Architecture.md\"", "href = 'Development.md'"); break;
                case "extra-source": changed = romanian.replace("</body>", "<img src = 'https://example.invalid/extra.png'></body>"); break;
                case "external-css": changed = romanian.replace("</head>", "<link rel = 'alternate stylesheet' href = 'https://example.invalid/style.css'></head>"); break;
                case "missing-style": changed = romanian.replaceAll("(?s)<style>.*?</style>", ""); break;
                case "script": changed = romanian.replace("</body>", "<script>console.log('not permitted');</script></body>"); break;
                default: throw new AssertionError("Unknown HTML mutation: " + mutation);
            }
            fixture.write("english.html", english);
            fixture.write("romanian.html", changed);
            assertFinding(DocumentationTranslationRules.validateHtmlParity(
                    fixture.root.resolve("english.html"), fixture.root.resolve("romanian.html")), "DOC-EDITION-PARITY");
            fixture.write("romanian.html", romanian);
            assertValid(DocumentationTranslationRules.validateHtmlParity(
                    fixture.root.resolve("english.html"), fixture.root.resolve("romanian.html")));
        }
    }

    private static String translationHtmlFixture() {
        return "<!doctype html><html lang=\"en\"><head><style>\nbody { color: black; }\n</style></head>"
                + "<body><a href=\"Architecture.md\">Owner</a><img src=\"diagram.svg\"></body></html>";
    }

    @DataProvider(name = "translationReviewMutations")
    public Object[][] translationReviewMutations() {
        return new Object[][] {{"missing"}, {"duplicate"}, {"unowned"}, {"hash"}, {"dimensions"}, {"method"}, {"malformed"}};
    }

    @Test(dataProvider = "translationReviewMutations")
    public void wholeTextReviewRejectsMissingStaleAndUnsupportedDeclarationsThenAcceptsRepair(String mutation) throws Exception {
        try (GovernanceFiles.Fixture en = new GovernanceFiles.Fixture(); GovernanceFiles.Fixture ro = new GovernanceFiles.Fixture()) {
            en.write("docs/governance/readme-edition.properties", "language=en\n");
            ro.write("docs/governance/readme-edition.properties", "language=ro\n");
            en.write("docs/Testing.md", "# Testing\n\nInspect the native evidence.\n");
            ro.write("docs/Testing.md", "# Testare\n\nVerificați dovezile native pentru testele existente.\n");
            writeTranslationReview(en, ro);
            String original = Files.readString(ro.root.resolve(DocumentationTranslationRules.REVIEW));
            String changed;
            switch (mutation) {
                case "missing": changed = original.replaceAll("<document[^>]+/>", ""); break;
                case "duplicate": changed = original.replace("</translation-review>", original.substring(original.indexOf("<document "), original.indexOf("/>") + 2) + "</translation-review>"); break;
                case "unowned": changed = original.replace("docs/Testing.md", "../Unowned.md"); break;
                case "hash": changed = original.replaceAll("ro-sha256=\"[a-f0-9]+\"", "ro-sha256=\"wrong\""); break;
                case "dimensions": changed = original.replace("language meaning conditions facts links diagrams disclosures", "language"); break;
                case "method": changed = original.replace("source-grounded-full-text", "title-only"); break;
                case "malformed": changed = "<translation-review>"; break;
                default: throw new AssertionError("Unknown mutation: " + mutation);
            }
            ro.write(DocumentationTranslationRules.REVIEW, changed);
            assertFinding(DocumentationTranslationRules.validateReview(ro.root), "DOC-TRANSLATION-REVIEW");
            ro.write(DocumentationTranslationRules.REVIEW, original);
            assertValid(DocumentationTranslationRules.validateReview(ro.root));
        }
    }

    private static void writeTranslationReview(GovernanceFiles.Fixture en, GovernanceFiles.Fixture ro) throws Exception {
        String xml = "<translation-review version=\"1\" method=\"source-grounded-full-text\" reviewer=\"Fixture reviewer\" retrieval-sha256=\""
                + DocumentationTranslationRules.digest("Retained fixture policy context") + "\"><document path=\"docs/Testing.md\" status=\"REVIEWED\" checks=\"language meaning conditions facts links diagrams disclosures\" en-sha256=\""
                + DocumentationTranslationRules.digest(Files.readString(en.root.resolve("docs/Testing.md"))) + "\" ro-sha256=\""
                + DocumentationTranslationRules.digest(Files.readString(ro.root.resolve("docs/Testing.md"))) + "\"/></translation-review>\n";
        en.write(DocumentationTranslationRules.REVIEW, xml);
        ro.write(DocumentationTranslationRules.REVIEW, xml);
    }

    @DataProvider(name = "testingEditionDrift")
    public Object[][] testingEditionDrift() {
        return new Object[][] {
                {"`ui.engine`", "`unknown.engine`", "DOC-EDITION-FACTS"},
                {"| 16 |", "| 99 |", "DOC-EDITION-FACTS"},
                {"mvn -B -ntp test", "mvn -B -ntp verify", "DOC-EDITION-PARITY"},
                {"<details>", "<details open>", "DOC-EDITION-PARITY"},
                {"../pom.xml", "../missing.xml", "DOC-EDITION-PARITY"},
                {"<a id=\"testing\"></a>\n", "", "DOC-EDITION-ANCHOR"}
        };
    }

    @Test(dataProvider = "testingEditionDrift")
    public void nonReadmeTranslationProtectsRealTechnicalBoundariesAndRepair(String before, String after, String rule) throws Exception {
        try (GovernanceFiles.Fixture en = documentation(); GovernanceFiles.Fixture ro = documentation()) {
            ro.write("docs/governance/readme-edition.properties", "language=ro\n");
            String tail = "\n\nSelect `ui.engine`.\n\n| Cases |\n|---:|\n| 16 |\n\n[Build](../pom.xml)\n\n<details>\n<summary>Run local checks</summary>\n\n```powershell\nmvn -B -ntp test\n```\n\n</details>\n";
            en.write("docs/Testing.md", "# Testing" + tail);
            String translated = "<a id=\"testing\"></a>\n# Testare" + tail;
            ro.write("docs/Testing.md", translated);
            assertValid(MarkdownDocumentationRules.validateReadmeParity(en.root, ro.root));
            Assert.assertTrue(translated.contains(before), "The selected counterexample must mutate its input.");
            ro.write("docs/Testing.md", translated.replace(before, after));
            assertFinding(MarkdownDocumentationRules.validateReadmeParity(en.root, ro.root), rule);
            ro.write("docs/Testing.md", translated);
            assertValid(MarkdownDocumentationRules.validateReadmeParity(en.root, ro.root));
        }
    }

}
