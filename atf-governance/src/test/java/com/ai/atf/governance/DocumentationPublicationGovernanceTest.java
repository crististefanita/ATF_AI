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
 * Protects complete living HTML pairs, navigation, executable command declarations and stable owner guides.
 */
public class DocumentationPublicationGovernanceTest {
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

}
