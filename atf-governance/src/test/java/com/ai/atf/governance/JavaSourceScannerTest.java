package com.ai.atf.governance;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class JavaSourceScannerTest {
    // One known-marker vocabulary keeps source and documentation-code checks aligned.
    private static final Pattern NON_ENGLISH_MARKERS = MarkdownDocumentationRules.languageMarkers();
    private static final String GOVERNANCE_PATH = "atf-governance/src/test/java/com/ai/atf/governance/";
    private static final Map<String, String> LANGUAGE_DATA_OWNERS = Map.of(
            "atf-governance/src/main/java/com/ai/atf/governance/MarkdownDocumentationRules.java", "Known language-policy vocabulary",
            GOVERNANCE_PATH + "DocumentationEditionGovernanceTest.java", "Deliberate Romanian translation examples and localized-data fixtures",
            GOVERNANCE_PATH + "DocumentationStructureGovernanceTest.java", "Deliberate Romanian translation and literal-data fixtures",
            GOVERNANCE_PATH + "FeedbackGovernanceTest.java", "Deliberate Romanian feedback-schema labels",
            GOVERNANCE_PATH + "JavaSourceScannerTest.java", "Code-language vocabulary and deliberate failing fixtures");

    @Test
    public void authoredJavaAndCommentsFollowTheEnglishLanguagePolicy() throws Exception {
        List<String> failures = languageViolations(GovernanceFiles.root(), LANGUAGE_DATA_OWNERS);
        Assert.assertTrue(failures.isEmpty(), GovernanceFiles.problems(failures));
    }

    @Test
    public void anAuthoredLanguageFailureCarriesGuidanceWithoutGrantingABlanketExemption() throws Exception {
        try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture()) {
            String owner = "atf-core/src/main/java/Sample.java";
            fixture.write(owner, "class Sample {} // Pentru rulare, inspect the owner.\n");
            List<String> findings = languageViolations(fixture.root, Map.of());
            Assert.assertEquals(findings.size(), 1);
            Assert.assertTrue(GovernanceFiles.matchesFinding(findings.get(0), "ENG-COMMENT"));
            String report = GovernanceFiles.problems(findings);
            Assert.assertTrue(report.contains("Observed: " + owner + ": ENG-COMMENT"));
            Assert.assertTrue(report.contains("Repair hint: Use English names, comments and messages"));
            Assert.assertTrue(report.contains("Owner: docs/governance/Code_And_Documentation_Policy.md#english-source-and-comments"));
            Assert.assertTrue(report.contains("-Dtest=JavaSourceScannerTest"));
            fixture.write(owner, "class Sample {} // Inspect the owner before running.\n");
            Assert.assertTrue(languageViolations(fixture.root, Map.of()).isEmpty());
        }
    }

    @DataProvider(name = "nonEnglishCode")
    public Object[][] nonEnglishCode() {
        String escape = "\\";
        return new Object[][] {
                {"class Sample {} // Pentru rulare, use this method."},
                {"/** Validează factura. */ class Sample {}"},
                {"class Sample { int numarUtilizatori; }"},
                {"class Sample { void valideazaFactura() {} }"},
                {"class Sample { String diagnostic = \"Raspuns invalid pentru cerere\"; }"},
                {"// P" + escape + "u0065ntru rulare\nclass Sample {}"}
        };
    }

    @Test(dataProvider = "nonEnglishCode")
    public void languagePolicyRejectsAuthoredCommentIdentifierAndDiagnosticMarkers(String source) throws Exception {
        try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture()) {
            fixture.write("atf-core/src/main/java/Sample.java", source);
            Assert.assertFalse(languageViolations(fixture.root, Map.of()).isEmpty(), source);
        }
    }

    @Test
    public void languageDataExceptionsAreExactAndCannotHideCommentsOrIdentifiers() throws Exception {
        try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture()) {
            String data = "atf-tests/src/test/java/LanguageFixture.java";
            fixture.write(data, "class LanguageFixture { String city = \"Timișoara\"; }\n");
            Map<String, String> owners = Map.of(data, "Explicit localized input fixture");
            Assert.assertTrue(languageViolations(fixture.root, Map.of(data, " ")).stream()
                    .anyMatch(value -> value.contains("ENG-DIAGNOSTIC")), "Blank ownership cannot waive a diagnostic marker.");
            Assert.assertTrue(languageViolations(fixture.root, owners).isEmpty());
            fixture.write(data, "// Pentru rulare\nclass LanguageFixture { int numarUtilizatori; String city = \"Timișoara\"; }");
            List<String> failures = languageViolations(fixture.root, owners);
            Assert.assertTrue(failures.stream().anyMatch(value -> value.contains("ENG-COMMENT")));
            Assert.assertTrue(failures.stream().anyMatch(value -> value.contains("ENG-IDENTIFIER")));
            fixture.write(data, "class LanguageFixture { String city = \"Local input\"; }");
            Assert.assertTrue(languageViolations(fixture.root, owners).stream().anyMatch(value -> value.contains("ENG-STALE-EXCEPTION")));
        }
    }

    @Test
    public void languageRegionsPreserveEnglishCommentsAndLiteralUrlsWithoutConfusingDelimiters() {
        String source = "// Keep the request local.\nclass Sample { String url = \"http://localhost/*literal*/\"; }";
        String comments = JavaSourceScanner.commentsOnly(source);
        String literals = JavaSourceScanner.literalsOnly(source);
        Assert.assertTrue(comments.contains("Keep the request local"));
        Assert.assertFalse(comments.contains("localhost"));
        Assert.assertTrue(literals.contains("http://localhost/*literal*/"));
        Assert.assertFalse(literals.contains("Keep the request local"));
        Assert.assertFalse(NON_ENGLISH_MARKERS.matcher(comments).find());
        Assert.assertEquals(comments.length(), source.length());
        Assert.assertEquals(literals.length(), source.length());
    }

    private static List<String> languageViolations(Path root, Map<String, String> literalOwners) throws Exception {
        List<String> failures = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for (Path file : GovernanceFiles.files(root, ".java")) {
            String relative = GovernanceFiles.relative(root, file);
            String source = Files.readString(file);
            String identifiers = JavaSourceScanner.codeOnly(source)
                    .replaceAll("([a-z])([A-Z])", "$1 $2").replace('_', ' ');
            if (NON_ENGLISH_MARKERS.matcher(identifiers).find()) failures.add(relative + ": ENG-IDENTIFIER use English authored names");
            if (NON_ENGLISH_MARKERS.matcher(JavaSourceScanner.commentsOnly(source)).find()) failures.add(relative + ": ENG-COMMENT use English comments and Javadoc");
            if (NON_ENGLISH_MARKERS.matcher(JavaSourceScanner.literalsOnly(source)).find()) {
                if (literalOwners.containsKey(relative) && !literalOwners.get(relative).isBlank()) used.add(relative);
                else failures.add(relative + ": ENG-DIAGNOSTIC use English authored messages or declare an exact data-fixture owner");
            }
        }
        for (String owner : literalOwners.keySet()) {
            if (GovernanceFiles.MODULES.stream().noneMatch(module -> owner.startsWith(module + "/src/test/java/"))
                    && !owner.equals("atf-governance/src/main/java/com/ai/atf/governance/MarkdownDocumentationRules.java")) {
                failures.add(owner + ": ENG-EXCEPTION-SCOPE language-data owners belong to exact test/policy fixtures");
            }
            if (!used.contains(owner)) failures.add(owner + ": ENG-STALE-EXCEPTION remove unused language-data exceptions");
        }
        return failures;
    }

    @Test
    public void aProductionDiagnosticCannotReceiveABlanketLanguageDataExemption() throws Exception {
        try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture()) {
            String source = "atf-core/src/main/java/Sample.java";
            fixture.write(source, "class Sample { String diagnostic = \"Eroare pentru cerere\"; }");
            Assert.assertTrue(languageViolations(fixture.root, Map.of(source, "Unreviewed blanket exemption"))
                    .stream().anyMatch(value -> value.contains("ENG-EXCEPTION-SCOPE")));
        }
    }

    @DataProvider(name = "references")
    public Object[][] references() {
        String escape = "\\";
        return new Object[][] {
                {"import com.ai.backend.Widget;", true},
                {"import static com.ai.backend.Widget.VALUE;", true},
                {"import com.ai.backend.*;", true},
                {"package com.ai.backend.internal;", true},
                {"class Sample { com.ai.backend.Widget value; }", true},
                {"class Sample { com . ai /* boundary */ . backend . Widget value; }", true},
                {"import " + escape + "u0063om.ai.backend.Widget;", true},
                {"// apparent comment " + escape + "u000aimport com.ai.backend.Widget;", true},
                {"// import com.ai.backend.Widget;", false},
                {"/* com.ai.backend.Widget */ class Sample {}", false},
                {"class Sample { String key = \"com.ai.backend.enabled\"; }", false},
                {"import com.ai.backendish.Widget;", false},
                {"import other.com.ai.backend.Widget;", false},
                {"String sample = \"escaped \\\" com.ai.backend.Widget\";", false},
                {"String sample = \"\"\"\ncom.ai.backend.Widget\n\"\"\";", false}
        };
    }

    @Test(dataProvider = "references")
    public void distinguishesExecutableReferencesFromExamples(String source, boolean expected) {
        Assert.assertEquals(JavaSourceScanner.containsNamespace(source, "com.ai.backend"), expected, source);
    }

    @Test
    public void masksLiteralContentWhileKeepingSourceLineNumbers() {
        String source = "// hidden\nclass Sample { char quote = '\\''; String text = \"/* hidden */\"; }\n";
        String code = JavaSourceScanner.codeOnly(source);
        Assert.assertEquals(code.length(), source.length());
        Assert.assertEquals(code.chars().filter(value -> value == '\n').count(), 2L);
        Assert.assertFalse(code.contains("hidden"));
        Assert.assertTrue(code.contains("class Sample"));
    }

    @Test
    public void invalidUnicodeAndInvalidNamespaceFailExplicitly() {
        Assert.expectThrows(IllegalArgumentException.class, () -> JavaSourceScanner.codeOnly("\\" + "u00XX"));
        Assert.expectThrows(IllegalArgumentException.class, () -> JavaSourceScanner.codeOnly("\\" + "u001"));
        Assert.expectThrows(IllegalArgumentException.class, () -> JavaSourceScanner.containsNamespace("", "bad.*"));
    }

    @Test
    public void escapedBackslashesDoNotInventUnicodeReferences() {
        String code = JavaSourceScanner.codeOnly("// literal " + "\\\\" + "u000a com.ai.backend.Widget");
        Assert.assertFalse(code.contains("com.ai.backend"));
    }
}
