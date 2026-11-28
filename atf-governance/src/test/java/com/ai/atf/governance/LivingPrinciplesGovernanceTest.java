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
 * Protects living architecture principles, their identities and the HTML projection.
 */
public class LivingPrinciplesGovernanceTest {
    @Test
    public void retainedLivingPrinciplesHaveConcreteOwnersAndBoundedEvidence() throws Exception {
        assertValid(MarkdownDocumentationRules.validateRetainedLivingPrinciples(GovernanceFiles.root()));
    }

    @DataProvider(name = "livingPrincipleMutations")
    public Object[][] livingPrincipleMutations() {
        return new Object[][] {
                {"missing", "missing LD-12"},
                {"duplicate", "duplicate LD-01"},
                {"unknown", "unknown principle LD-99"},
                {"unsupported-state", "unsupported state VERIFIED"},
                {"cleanup-promoted", "LD-11 cleanup remains PARTIAL"},
                {"cleanup-limit-removed", "LD-11 cleanup remains PARTIAL"},
                {"missing-owner", "implementation route is not an owned existing Java source"},
                {"missing-check", "check route must name an existing Java test source"},
                {"helper-as-check", "check route must name an existing Java test source"},
                {"fenced-row", "missing LD-12"},
                {"duplicate-section", "require one retained-principles anchor"},
                {"missing-intent", "requires an intent and explicit proof boundary"},
                {"wrong-columns", "requires six declared table columns"},
                {"wait-intent-swapped", "preserve the Selenium/Playwright wait distinction"},
                {"wait-verdict-swapped", "preserve WaitUtils observation and enclosing verdict ownership"}
        };
    }

    @Test(dataProvider = "livingPrincipleMutations")
    public void livingPrincipleDriftHasOwnedGuidanceAndRepair(String mutation, String expected) throws Exception {
        Path root = GovernanceFiles.root();
        String canonical = Files.readString(root.resolve("docs/Living_Architecture_UI_API_doc_v1_0.md"))
                .replace("\r\n", "\n");
        List<String> lines = List.of(canonical.split("\n", -1));
        String first = lines.stream().filter(line -> line.startsWith("| LD-01 |")).findFirst().orElseThrow();
        String cleanup = lines.stream().filter(line -> line.startsWith("| LD-11 |")).findFirst().orElseThrow();
        String last = lines.stream().filter(line -> line.startsWith("| LD-12 |")).findFirst().orElseThrow();
        String changed;
        switch (mutation) {
            case "missing": changed = canonical.replace(last + "\n", ""); break;
            case "duplicate": changed = canonical.replace(first, first + "\n" + first); break;
            case "unknown": changed = canonical.replace("| LD-12 |", "| LD-99 |"); break;
            case "unsupported-state": changed = canonical.replace(first, first.replace("| MAINTAINED |", "| VERIFIED |")); break;
            case "cleanup-promoted": changed = canonical.replace(cleanup, cleanup.replace("| PARTIAL |", "| MAINTAINED |")); break;
            case "cleanup-limit-removed": changed = canonical.replace("ACCOUNT_DELETION_NOT_IMPLEMENTED", "UNSUPPORTED_CLEANUP_SUCCESS"); break;
            case "missing-owner": changed = canonical.replace(first,
                    first.replace("../atf-core/src/main/java/com/ai/core/support/config/ConfigManager.java", "../missing-owner.java")); break;
            case "missing-check": {
                String[] cells = first.split("\\|", -1); cells[4] = " No executable check ";
                changed = canonical.replace(first, String.join("|", cells)); break;
            }
            case "helper-as-check": changed = canonical.replace(first, first.replace(
                    "../atf-tests/src/test/java/com/ai/atf/core/support/config/ConfigManagerFileContractTest.java",
                    "../atf-tests/src/main/java/com/ai/ui/support/factory/UserDataFactory.java")); break;
            case "fenced-row": changed = canonical.replace(last + "\n", "") + "\n```markdown\n" + last + "\n```\n"; break;
            case "duplicate-section": changed = canonical.replace("<a id=\"retained-principles\"></a>",
                    "<a id=\"retained-principles\"></a>\n<a id=\"retained-principles\"></a>"); break;
            case "missing-intent": {
                String[] cells = first.split("\\|", -1); cells[2] = " ";
                changed = canonical.replace(first, String.join("|", cells)); break;
            }
            case "wrong-columns": {
                String[] cells = first.split("\\|", -1);
                changed = canonical.replace(first, String.join("|", Arrays.copyOf(cells, cells.length - 1))); break;
            }
            case "wait-intent-swapped": {
                String interaction = lines.stream().filter(line -> line.startsWith("| LD-02 |")).findFirst().orElseThrow();
                String wait = lines.stream().filter(line -> line.startsWith("| LD-03 |")).findFirst().orElseThrow();
                String[] cells = wait.split("\\|", -1); cells[2] = interaction.split("\\|", -1)[2];
                changed = canonical.replace(wait, String.join("|", cells)); break;
            }
            case "wait-verdict-swapped": {
                String wait = lines.stream().filter(line -> line.startsWith("| LD-03 |")).findFirst().orElseThrow();
                String verdict = lines.stream().filter(line -> line.startsWith("| LD-04 |")).findFirst().orElseThrow();
                String[] cells = verdict.split("\\|", -1); cells[6] = wait.split("\\|", -1)[6];
                changed = canonical.replace(verdict, String.join("|", cells)); break;
            }
            default: throw new AssertionError("Unknown living principle mutation: " + mutation);
        }
        Assert.assertNotEquals(changed, canonical, "The negative control must change its selected matrix boundary");
        List<String> findings = MarkdownDocumentationRules.validateRetainedLivingPrinciples(root, changed);
        assertFinding(findings, "DOC-LIVING-PRINCIPLES");
        Assert.assertTrue(findings.stream().anyMatch(finding -> GovernanceFiles.matchesFinding(
                finding, "DOC-LIVING-PRINCIPLES") && finding.contains(expected)),
                "The intended principle defect must be diagnosed: " + expected + "; " + GovernanceFiles.problems(findings));
        assertValid(MarkdownDocumentationRules.validateRetainedLivingPrinciples(root, canonical));
    }

    @Test
    public void livingPrincipleIdentityDoesNotDependOnTableOrdering() throws Exception {
        Path root = GovernanceFiles.root();
        String canonical = Files.readString(root.resolve("docs/Living_Architecture_UI_API_doc_v1_0.md"));
        List<String> lines = List.of(canonical.split("\\R"));
        String first = lines.stream().filter(line -> line.startsWith("| LD-01 |")).findFirst().orElseThrow();
        String last = lines.stream().filter(line -> line.startsWith("| LD-12 |")).findFirst().orElseThrow();
        String reordered = canonical.replace(first, "LIVING_PRINCIPLE_SWAP")
                .replace(last, first).replace("LIVING_PRINCIPLE_SWAP", last);
        assertValid(MarkdownDocumentationRules.validateRetainedLivingPrinciples(root, reordered));
    }

    @DataProvider(name = "livingHtmlPrincipleMutations")
    public Object[][] livingHtmlPrincipleMutations() {
        return new Object[][] {
                {"missing", "HTML missing LD-12"},
                {"duplicate", "HTML duplicate LD-01"},
                {"cleanup-promoted", "HTML LD-11 state differs from Markdown"},
                {"owner-changed", "HTML LD-01 implementation hrefs differ from Markdown"}
        };
    }

    @Test(dataProvider = "livingHtmlPrincipleMutations")
    public void livingHtmlPrinciplesPreserveDeclaredRowsStatesAndOwnerRoutes(String mutation, String expected) throws Exception {
        Path root = GovernanceFiles.root();
        String markdown = Files.readString(root.resolve("docs/Living_Architecture_UI_API_doc_v1_0.md"));
        String canonical = Files.readString(root.resolve("docs/Living_Architecture_UI_API_doc_v1_0.html"));
        String changed;
        switch (mutation) {
            case "missing": changed = canonical.replace(livingPrincipleHtmlRow(canonical, "LD-12"), ""); break;
            case "duplicate": {
                String row = livingPrincipleHtmlRow(canonical, "LD-01");
                changed = canonical.replace(row, row + row); break;
            }
            case "cleanup-promoted": {
                String row = livingPrincipleHtmlRow(canonical, "LD-11");
                changed = canonical.replace(row, row.replace("<td>PARTIAL</td>", "<td>MAINTAINED</td>")); break;
            }
            case "owner-changed": {
                String row = livingPrincipleHtmlRow(canonical, "LD-01");
                changed = canonical.replace(row, row.replace("../atf-core/src/main/java/com/ai/core/support/config/ConfigManager.java",
                        "../atf-core/src/main/java/com/ai/core/reporting/StepLogger.java")); break;
            }
            default: throw new AssertionError("Unknown HTML principle mutation: " + mutation);
        }
        Assert.assertNotEquals(changed, canonical, "The HTML negative control must change its selected projection boundary");
        List<String> findings = MarkdownDocumentationRules.validateRetainedLivingProjection(root, markdown, changed);
        assertFinding(findings, "DOC-LIVING-PRINCIPLES");
        Assert.assertTrue(findings.stream().anyMatch(finding -> GovernanceFiles.matchesFinding(finding, "DOC-LIVING-PRINCIPLES")
                && finding.contains(expected)), "The intended HTML projection defect must be diagnosed: " + expected
                + "; " + GovernanceFiles.problems(findings));
        assertValid(MarkdownDocumentationRules.validateRetainedLivingProjection(root, markdown, canonical));
    }

    @Test
    public void livingHtmlProjectionIgnoresEscapedPrincipleExamples() throws Exception {
        Path root = GovernanceFiles.root();
        String markdown = Files.readString(root.resolve("docs/Living_Architecture_UI_API_doc_v1_0.md"));
        String canonical = Files.readString(root.resolve("docs/Living_Architecture_UI_API_doc_v1_0.html"));
        String example = livingPrincipleHtmlRow(canonical, "LD-01").replace("<td>MAINTAINED</td>", "<td>UNSUPPORTED_EXAMPLE_STATE</td>")
                .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        Matcher heading = Pattern.compile("(?is)<h3\\b(?=[^>]*\\sid\\s*=\\s*[\"']retained-principles[\"'])[^>]*>.*?</h3\\s*>").matcher(canonical);
        Assert.assertTrue(heading.find(), "The source-bound principle section must have its stable heading ID in either edition.");
        String changed = canonical.substring(0, heading.end()) + "<pre><code>" + example + "</code></pre>"
                + canonical.substring(heading.end());
        Assert.assertFalse(changed.equals(canonical), "The positive control must add the escaped example at the actual section.");
        assertValid(MarkdownDocumentationRules.validateRetainedLivingProjection(root, markdown, changed));
        String inlineCode = canonical.replace("ACCOUNT_DELETION_NOT_IMPLEMENTED",
                "<code>ACCOUNT_DELETION_NOT_IMPLEMENTED</code>");
        Assert.assertFalse(inlineCode.equals(canonical), "The inline-code control must format the actual cleanup marker.");
        assertValid(MarkdownDocumentationRules.validateRetainedLivingProjection(root, markdown, inlineCode));
    }

    private static String livingPrincipleHtmlRow(String html, String id) {
        Matcher row = Pattern.compile("(?is)<tr\\b[^>]*>\\s*<td\\b[^>]*>\\s*" + Pattern.quote(id)
                + "\\s*</td>[\\s\\S]*?</tr>").matcher(html);
        Assert.assertTrue(row.find(), "Missing original HTML principle row: " + id);
        return row.group();
    }

}
