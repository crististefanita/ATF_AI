package com.ai.atf.governance;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Pattern;

public class FrameworkArchitectureGovernanceTest {
    @DataProvider(name = "exactFindings")
    public Object[][] exactFindings() {
        return new Object[][] {
                {"README.md:12 DOC-EDITION invalid policy", "DOC-EDITION", true},
                {"README.md:12 DOC-EDITION-PARITY changed command", "DOC-EDITION", false},
                {"README.md:12 DOC-LINK missing DOC-EDITION example", "DOC-EDITION", false},
                {"DOC-EDITION/README.md:12 DOC-LINK missing target", "DOC-EDITION", false},
                {"SUITE-OMISSION com.ai.Sample is absent", "SUITE-OMISSION", true},
                {"pom.xml [profile: checked]: CORE-SDK concrete library", "CORE-SDK", true},
                {"atf-tests/src/test/resources/governance/test-method-inventory.txt: TEST-METHOD-REMOVED Sample#retained()", "TEST-METHOD-REMOVED", true},
                {"README.md:12 DOC-LINK missing target", "README.md:12 DOC-LINK", true},
                {"README.md:21 DOC-LINK missing target", "README.md:12 DOC-LINK", false},
                {"README.md:12 DOC-LINK missing first target", "DOC-LINK missing second", false}
        };
    }

    @Test(dataProvider = "exactFindings")
    public void negativeControlsMatchTheEmittedRuleAndRequestedEvidence(String finding, String expected, boolean matches) {
        Assert.assertEquals(GovernanceFiles.matchesFinding(finding, expected), matches,
                "The observed rule and requested location/detail must agree independently of payload text.");
    }

    @Test
    public void anUnrelatedFailureCannotSatisfyTheExpectedNegativeControl() {
        List<String> findings = List.of("README.md:9 DOC-EDITION-PARITY changed code example");
        AssertionError failure = Assert.expectThrows(AssertionError.class, () -> assertFinding(findings, "DOC-EDITION"));
        Assert.assertTrue(failure.getMessage().contains("Expected DOC-EDITION;"));
        Assert.assertTrue(failure.getMessage().contains("Observed: " + findings.get(0)));
        Assert.assertTrue(failure.getMessage().contains("Repair hint:"));
        Assert.expectThrows(IllegalArgumentException.class,
                () -> GovernanceFiles.matchesFinding(findings.get(0), "changed code example"));
    }

    @Test
    public void diagnosticsPreserveAllEvidenceAndDeduplicateOnlyRecheckCommands() {
        String first = "atf-core/pom.xml [profile: checked]: CORE-SDK concrete dependency";
        String second = "README.md:18 DOC-LINK missing-target.md\ncompiler or producer continuation";
        String third = "atf-tests/src/test/resources/governance/test-method-inventory.txt: TEST-METHOD-REMOVED com.ai.Sample#retained()";
        String report = GovernanceFiles.problems(List.of(first, second, third, first));
        Assert.assertTrue(report.startsWith("Governance failed with 4 finding(s)."));
        Assert.assertTrue(report.contains("1) Observed: " + first));
        Assert.assertTrue(report.contains("2) Observed: " + second));
        Assert.assertTrue(report.contains("3) Observed: " + third));
        Assert.assertTrue(report.contains("4) Observed: " + first));
        Assert.assertEquals(report.split(Pattern.quote("-Dtest=FrameworkArchitectureGovernanceTest"), -1).length - 1, 1);
        Assert.assertEquals(report.split(Pattern.quote("-Dtest=DocumentationGovernanceTest"), -1).length - 1, 1);
        Assert.assertTrue(report.contains("Failure meaning: A concrete SDK"));
        Assert.assertTrue(report.contains("Owner: docs/Architecture.md#provider-composition"));
        Assert.assertTrue(report.contains("Confirm the deletion was intentional"));
        Assert.assertTrue(report.contains("Owner: docs/governance/Code_And_Documentation_Policy.md#existing-test-quality"));
        Assert.assertTrue(report.contains("Repair hint: Correct the source-relative destination"));
        Assert.assertFalse(report.contains("README.md:1 "), "Do not invent a source line to fill an unknown location.");
        Assert.assertEquals(GovernanceFiles.problems(List.of()), "No governance findings supplied.");
    }

    @Test
    public void unknownFindingsRetainEvidenceWithoutGuessingAKnownCause() {
        String observation = "README.md:5 FUTURE-RULE payload mentions IMPORT-WILDCARD";
        String report = GovernanceFiles.problems(List.of(observation, "unstructured producer diagnostic"));
        Assert.assertTrue(report.contains("Observed: " + observation));
        Assert.assertTrue(report.contains("Observed: unstructured producer diagnostic"));
        Assert.assertTrue(report.contains("Failure meaning: Unclassified finding"));
        Assert.assertTrue(report.contains("Owner: docs/governance/Failure_Diagnostics.md#unknown-findings"));
        Assert.assertFalse(report.contains("Name the used Java types"), "A code mentioned in payload is not the failed rule.");
        Assert.assertFalse(report.contains("-Dtest="), "Unknown producer ownership must not be guessed.");
        Assert.assertFalse(GovernanceFiles.hasDiagnosticHelp(observation));
        Assert.assertFalse(GovernanceFiles.matchesFinding(report, "IMPORT-WILDCARD"), "Rendered advice is not an emitted finding.");
    }

    @Test
    public void actualImportFailureProvidesACorrectionAndTheRepairedFixturePasses() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            String owner = "atf-core/src/main/java/com/ai/core/Sample.java";
            fixture.write(owner, "package com.ai.core; import java.util.*; class Sample {}\n");
            List<String> findings = FrameworkArchitectureRules.validate(fixture.root);
            assertFinding(findings, "IMPORT-WILDCARD");
            String report = GovernanceFiles.problems(findings);
            Assert.assertTrue(report.contains("Observed: " + owner + ": IMPORT-WILDCARD"));
            Assert.assertTrue(report.contains("Repair hint: Name the used Java types/static members explicitly"));
            Assert.assertTrue(report.contains("Owner: docs/governance/Code_And_Documentation_Policy.md#explicit-imports-and-visible-dependencies"));
            fixture.write(owner, "package com.ai.core; class Sample {}\n");
            assertValid(FrameworkArchitectureRules.validate(fixture.root));
        }
    }

    @DataProvider(name = "wildcardImports")
    public Object[][] wildcardImports() {
        String escape = "\\";
        return new Object[][] {{"import java.util.*;"}, {"import static java.util.Collections.*;"},
                {"import java . util . * ;"}, {"import java.util." + escape + "u002a;"}};
    }

    @Test(dataProvider = "wildcardImports")
    public void importOwnershipRejectsNormalStaticAndEscapedWildcards(String declaration) throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-core/src/main/java/example/Imports.java", "package example; " + declaration + " class Imports {}\n");
            assertFinding(FrameworkArchitectureRules.validate(fixture.root), "IMPORT-WILDCARD");
        }
    }

    @Test
    public void importPolicyIgnoresCommentAndLiteralExamples() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-core/src/main/java/example/Imports.java", "package example; import java.util.List; "
                    + "/* import java.util.*; */ class Imports { String example = \"import static java.util.Collections.*;\"; }\n");
            Assert.assertFalse(FrameworkArchitectureRules.validate(fixture.root).stream()
                    .anyMatch(value -> value.contains("IMPORT-WILDCARD")));
        }
    }

    @Test
    public void artifactGuidesAndTemplatesRemainOwnedWhileDeclaredRunBundlesAreExcluded() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("artifacts/README.md", "# Artifacts\n\n[Missing owner](Missing.md)\n");
            fixture.write("artifacts/atlas/Project_Map_Template.md", "# Project map\n");
            String drive = "R:";
            String machinePath = drive + "/private/report";
            fixture.write("artifacts/reports/runs/run/index.html", machinePath);
            fixture.write("artifacts/atlas/runs/project/run/map.md", machinePath);
            fixture.write("artifacts/reports/current/native.xml", machinePath);
            fixture.write("artifacts/atlas/current/project/atlas.html", machinePath);
            fixture.write("artifacts/publication.json", machinePath);
            String owned = "atf-core/src/main/resources/artifacts/reports/runs/contract.json";
            fixture.write(owned, "{}");
            String currentOwned = "atf-core/src/main/resources/artifacts/atlas/current/contract.json";
            fixture.write(currentOwned, "{}");
            String publicationOwned = "atf-core/src/main/resources/artifacts/publication.json";
            fixture.write(publicationOwned, "{}");
            String nestedOwned = "docs/artifacts/reports/current/contract.md";
            fixture.write(nestedOwned, "# Maintained contract\n");
            String nestedRunsOwned = "docs/artifacts/reports/runs/contract.md";
            fixture.write(nestedRunsOwned, "# Maintained run contract\n");
            List<java.nio.file.Path> files = GovernanceFiles.files(fixture.root, "");
            Assert.assertTrue(files.contains(fixture.root.resolve("artifacts/README.md")));
            Assert.assertTrue(files.contains(fixture.root.resolve("artifacts/atlas/Project_Map_Template.md")));
            Assert.assertTrue(files.contains(fixture.root.resolve(owned)), "Owned source resources cannot be hidden by an output path name.");
            Assert.assertTrue(files.contains(fixture.root.resolve(currentOwned)), "Owned current resources remain source.");
            Assert.assertTrue(files.contains(fixture.root.resolve(publicationOwned)), "Owned publication resources remain source.");
            Assert.assertTrue(files.contains(fixture.root.resolve(nestedOwned)), "Only reactor-root current publications are excluded.");
            Assert.assertTrue(files.contains(fixture.root.resolve(nestedRunsOwned)), "Nested maintained run documents remain governed.");
            Assert.assertFalse(files.contains(fixture.root.resolve("artifacts/reports/runs/run/index.html")));
            Assert.assertFalse(files.contains(fixture.root.resolve("artifacts/atlas/runs/project/run/map.md")));
            Assert.assertFalse(files.contains(fixture.root.resolve("artifacts/reports/current/native.xml")));
            Assert.assertFalse(files.contains(fixture.root.resolve("artifacts/atlas/current/project/atlas.html")));
            Assert.assertFalse(files.contains(fixture.root.resolve("artifacts/publication.json")));
            assertFinding(MarkdownDocumentationRules.validate(fixture.root), "artifacts/README.md:3 DOC-LINK");
            assertValid(GovernanceFiles.validatePortablePaths(fixture.root));
        }
    }

    @Test
    public void maintainedSourceConfigurationAndDocumentationUsePortablePaths() throws Exception {
        assertValid(GovernanceFiles.validatePortablePaths(GovernanceFiles.root()));
    }

    @Test
    public void portablePathsAcceptRelativeReferencesExplicitRootsAndRuntimeResolution() throws Exception {
        try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture()) {
            fixture.write("README.md", "# Portable paths\n${ATF_WORK_ROOT}/runs/check.log\n../config/local.properties\n"
                    + "[External](https://example.invalid/examples/R:/work/report.html)\n/api/users\n");
            fixture.write("atf-core/src/main/resources/framework.properties", "reports.dir=target/reports\n"
                    + "framework.properties=../config/local.properties\nbase.url.api=https://example.invalid/public/v2\n");
            fixture.write("atf-core/src/main/java/Portable.java", "import java.nio.file.Path; class Portable { "
                    + "Path temporary = Path.of(System.getProperty(\"java.io.tmpdir\"), \"atf\"); "
                    + "Path cache = Path.of(System.getProperty(\"user.home\"), \".cache\", \"selenium\"); "
                    + "Path explicit = Path.of(System.getenv(\"ATF_WORK_ROOT\"), \"runs\"); "
                    + "String route = \"/api/users\"; String regex = \"\\\\binvoice\\\\s+number\"; "
                    + "String row = \"xpath=//tbody/tr[td[normalize-space(.)='INV-1']]\"; }");
            fixture.write("config/browser.toml", "cache-path = '${HOME}/.cache/selenium'\n");
            fixture.write("config/local.ini", "cache=~/atf-cache\nworkspace=%USERPROFILE%/atf-work\n");
            fixture.write("docs/feedback/History.md", "# Receipts\nEvidence: ${ATF_WORK_ROOT}/runs/previous.log\n");
            fixture.write("atf-core/target/generated/ignored.md", "Generated output is outside the source inventory.\n");
            assertValid(GovernanceFiles.validatePortablePaths(fixture.root));
        }
    }

    @DataProvider(name = "filesystemRootExamples")
    public Object[][] filesystemRootExamples() {
        String drive = "R:";
        String slash = "/";
        String backslash = "\\";
        String scheme = "file:";
        String tilde = "~";
        String dollar = "$";
        String percent = "%";
        return new Object[][] {
                {drive + "/team/project", true}, {drive + backslash + "team" + backslash + "project", true},
                {backslash + backslash + "build-host" + backslash + "share" + backslash + "project", true},
                {slash + slash + "build-host/share/project", true},
                {scheme + "///project/report.html", true}, {scheme + slash + slash + "build-host/share/report.html", true},
                {slash + "home/alex/project", true}, {slash + "Users/alex/project", true}, {slash + "root/project", true},
                {tilde + "/project", false}, {dollar + "HOME/project", false}, {dollar + "{HOME}/project", false},
                {percent + "USERPROFILE%" + backslash + "project", false},
                {percent + "HOMEDRIVE%" + percent + "HOMEPATH%" + backslash + "project", false}
        };
    }

    @Test(dataProvider = "filesystemRootExamples")
    public void portablePathPolicyDistinguishesWorkstationRootsFromDynamicHomeInputs(String path, boolean forbidden) throws Exception {
        try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture()) {
            fixture.write("docs/feedback/History.md", "# Historical receipt\nEvidence: " + path + "\n");
            List<String> failures = GovernanceFiles.validatePortablePaths(fixture.root);
            if (forbidden) assertFinding(failures, "PATH-PORTABILITY");
            else assertValid(failures);
        }
    }

    @DataProvider(name = "portableTextOwners")
    public Object[][] portableTextOwners() {
        return new Object[][] {
                {"README.md"}, {"docs/reference.html"}, {"docs/reference.svg"}, {"pom.xml"},
                {"atf-core/src/main/resources/framework.properties"}, {"atf-core/src/main/resources/contracts/sample.yml"},
                {"atf-tests/src/test/resources/input.json"}, {"atf-tests/src/test/resources/target/input.csv"},
                {"scripts/verify.ps1"}, {".mvn/maven.config"}, {"config/browser.toml"}, {"config/local.ini"},
                {"atf-core/src/main/resources/META-INF/services/example.Provider"}
        };
    }

    @Test(dataProvider = "portableTextOwners")
    public void portablePathInventoryIncludesCodeFencesConfigurationAndSourceOwnedOutputNames(String owner) throws Exception {
        try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture()) {
            String drive = "R:";
            fixture.write(owner, "```text\n" + drive + "/private/project\n```\n");
            List<String> failures = GovernanceFiles.validatePortablePaths(fixture.root);
            Assert.assertEquals(failures.size(), 1, GovernanceFiles.problems(failures));
            Assert.assertTrue(failures.get(0).startsWith(owner + ": PATH-PORTABILITY"), failures.get(0));
        }
    }

    @Test
    public void portablePathPolicyDecodesJavaLiteralsAndStillChecksComments() throws Exception {
        try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture()) {
            String drive = "R:";
            String slash = "\\";
            String unc = slash + slash + "build-host" + slash + "share";
            String encoded = unc.replace(slash, slash + slash);
            fixture.write("atf-core/src/main/java/LocalPath.java", "class LocalPath { String path = \"" + encoded + "\"; }");
            assertFinding(GovernanceFiles.validatePortablePaths(fixture.root), "PATH-PORTABILITY");
            fixture.write("atf-core/src/main/java/LocalPath.java", "// Local source: " + drive + "/private/project\nclass LocalPath {}");
            assertFinding(GovernanceFiles.validatePortablePaths(fixture.root), "PATH-PORTABILITY");
            fixture.write("atf-core/src/main/java/LocalPath.java", "class LocalPath { String path = \""
                    + slash + "u0052:/private/project\"; }");
            assertFinding(GovernanceFiles.validatePortablePaths(fixture.root), "PATH-PORTABILITY");
            fixture.write("atf-core/src/main/java/LocalPath.java", "class LocalPath { String path = \"https://example.invalid/guide"
                    + slash + "n" + drive + "/private/project\"; }");
            assertFinding(GovernanceFiles.validatePortablePaths(fixture.root), "PATH-PORTABILITY");
        }
    }

    @Test
    public void runtimeExecutionUsesOwnedPausesConsoleAndInterruptHandling() throws Exception {
        assertValid(FrameworkArchitectureRules.validateExecution(GovernanceFiles.root()));
    }

    @Test
    public void legalOutputNamedPackagesStayCompiledAndVisibleToGovernance() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            addExecutionOwners(fixture);
            String hidden = "atf-core/src/main/java/com/ai/sample/target/Hidden.java";
            String output = "atf-core/src/main/java/com/ai/sample/artifacts/HiddenOutput.java";
            fixture.write(hidden, "package com.ai.sample.target; import java.util.List; import java . util . List; public class Hidden {}");
            fixture.write(output, "package com.ai.sample.artifacts; public class HiddenOutput { void emit(){ System.out.println(\"observed\"); } }");
            fixture.write("atf-core/target/generated/Uncompiled.java", "import duplicate.Type; import duplicate.Type;");
            fixture.write("atf-core/src/main/resources/assets/target/guide.md", "# Owned resource\n");
            fixture.write("atf-core/target/reports/guide.md", "# Generated report\n");
            String ownedResults = "atf-core/src/main/resources/allure-results/contract.json";
            String ownedArtifacts = "atf-core/src/test/resources/.codex-artifacts/contract.json";
            fixture.write(ownedResults, "{}");
            fixture.write(ownedArtifacts, "{}");
            fixture.write("atf-core/allure-results/generated.json", "{}");
            fixture.write("atf-core/.codex-artifacts/generated.json", "{}");
            List<java.nio.file.Path> json = GovernanceFiles.files(fixture.root, ".json");
            Assert.assertTrue(json.contains(fixture.root.resolve(ownedResults)) && json.contains(fixture.root.resolve(ownedArtifacts)));
            Assert.assertFalse(json.contains(fixture.root.resolve("atf-core/allure-results/generated.json")));
            Assert.assertFalse(json.contains(fixture.root.resolve("atf-core/.codex-artifacts/generated.json")));
            java.nio.file.Path classes = Files.createDirectories(fixture.root.resolve(".tmp/classes"));
            Assert.assertEquals(javax.tools.ToolProvider.getSystemJavaCompiler().run(null, null, null,
                    "-proc:none", "--release", "11", "-d", classes.toString(), fixture.root.resolve(hidden).toString(),
                    fixture.root.resolve(output).toString()), 0, "The legal target/artifacts packages must actually compile");
            List<java.nio.file.Path> all = GovernanceFiles.files(fixture.root, ".java");
            Assert.assertTrue(all.contains(fixture.root.resolve(hidden)) && all.contains(fixture.root.resolve(output)));
            Assert.assertFalse(all.contains(fixture.root.resolve("atf-core/target/generated/Uncompiled.java")));
            Assert.assertEquals(GovernanceFiles.files(fixture.root.resolve("atf-core/src/main/java/com/ai/sample/target"), ".java"),
                    List.of(fixture.root.resolve(hidden)), "A caller starting inside a source package retains its source semantics");
            Assert.assertEquals(GovernanceFiles.files(fixture.root, ".md"),
                    List.of(fixture.root.resolve("atf-core/src/main/resources/assets/target/guide.md")));
            assertFinding(FrameworkArchitectureRules.validate(fixture.root), "DUPLICATE-IMPORT");
            assertFinding(FrameworkArchitectureRules.validateExecution(fixture.root), "EXECUTION-CONSOLE");
        }
    }

    @Test
    public void executionPolicyAcceptsOwnedCallsAndIgnoresSourceExamples() throws Exception {
        try (GovernanceFiles.Fixture fixture = executionFixture()) {
            fixture.write("atf-core/src/main/java/example/Probe.java", "package example; class Probe { "
                    + "String example = \"Thread.sleep(1); System.out.println(); catch (InterruptedException e) {}\"; "
                    + "/* Thread.sleep(1); System.err.println(); */ }");
            assertValid(FrameworkArchitectureRules.validateExecution(fixture.root));
        }
    }

    @DataProvider(name = "rawPauses")
    public Object[][] rawPauses() {
        return new Object[][] {
                {"", "Thread.sleep(1L);"},
                {"", "java.lang.Thread.sleep(1L);"},
                {"import static java.lang.Thread.sleep;", "sleep(1L);"},
                {"import static java.lang.Thread.*;", "sleep(1L);"},
                {"import java.util.concurrent.TimeUnit;", "TimeUnit.MILLISECONDS.sleep(1L);"},
                {"import static java.util.concurrent.TimeUnit.MILLISECONDS;", "MILLISECONDS.sleep(1L);"}
        };
    }

    @Test(dataProvider = "rawPauses")
    public void directJdkPausesCannotMoveOutsideTheirReviewedOwners(String imports, String body) throws Exception {
        try (GovernanceFiles.Fixture fixture = executionFixture()) {
            fixture.write("atf-selenium/src/main/java/example/Probe.java", "package example; " + imports
                    + " class Probe { void run() throws Exception { " + body + " } }");
            assertFinding(FrameworkArchitectureRules.validateExecution(fixture.root), "EXECUTION-PAUSE");
        }
    }

    @DataProvider(name = "rawConsole")
    public Object[][] rawConsole() {
        return new Object[][] {
                {"", "java.io.PrintStream channel = System.out; channel.println(\"observed\");"},
                {"", "java.lang.System.err.println(\"observed\");"},
                {"import static java.lang.System.out;", "out.println(\"observed\");"},
                {"import static java.lang.System.*;", "err.println(\"observed\");"}
        };
    }

    @Test(dataProvider = "rawConsole")
    public void rawConsoleChannelsRemainInTheReviewedSink(String imports, String body) throws Exception {
        try (GovernanceFiles.Fixture fixture = executionFixture()) {
            fixture.write("atf-playwright/src/main/java/example/Probe.java", "package example; " + imports
                    + " class Probe { void run(){ " + body + " } }");
            assertFinding(FrameworkArchitectureRules.validateExecution(fixture.root), "EXECUTION-CONSOLE");
        }
    }

    @DataProvider(name = "missingInterruptRestoration")
    public Object[][] missingInterruptRestoration() {
        return new Object[][] {
                {""},
                {"new Thread().interrupt();"},
                {"String example = \"Thread.currentThread().interrupt();\"; /* Thread.currentThread().interrupt(); */"},
                {"Runnable later = () -> Thread.currentThread().interrupt();"},
                {"class Later { void restore(){ Thread.currentThread().interrupt(); } }"},
                {"try { if (System.nanoTime() > 0L) throw new IllegalStateException(); } catch (Exception nested) { Thread.currentThread().interrupt(); }"}
        };
    }

    @Test(dataProvider = "missingInterruptRestoration")
    public void interruptedHandlersRequireTheirOwnCurrentThreadRestoration(String body) throws Exception {
        try (GovernanceFiles.Fixture fixture = executionFixture()) {
            fixture.write("atf-reporting/src/main/java/example/Probe.java", "package example; class Probe { void run(){ "
                    + "try { throw new InterruptedException(); } catch (InterruptedException failure) { " + body + " } } }");
            assertFinding(FrameworkArchitectureRules.validateExecution(fixture.root), "EXECUTION-INTERRUPT");
        }
    }

    @DataProvider(name = "restoredInterrupts")
    public Object[][] restoredInterrupts() {
        return new Object[][] {
                {"", "java.lang.Thread.currentThread().interrupt();"},
                {"import static java.lang.Thread.currentThread;", "currentThread().interrupt();"}
        };
    }

    @Test(dataProvider = "restoredInterrupts")
    public void qualifiedAndStaticRestorationRemainValidInMultiCatch(String imports, String body) throws Exception {
        try (GovernanceFiles.Fixture fixture = executionFixture()) {
            fixture.write("atf-reporting/src/main/java/example/Probe.java", "package example; " + imports + " class Probe { void run(){ "
                    + "try { if(System.nanoTime() == Long.MIN_VALUE) throw new java.io.IOException(); throw new InterruptedException(); } "
                    + "catch (java.io.IOException | InterruptedException failure) { " + body + " } } }");
            assertValid(FrameworkArchitectureRules.validateExecution(fixture.root));
        }
    }

    @Test
    public void executionOwnersCannotBecomeMissingOrUnusedWaivers() throws Exception {
        try (GovernanceFiles.Fixture fixture = executionFixture()) {
            Files.delete(fixture.root.resolve("atf-core/src/main/java/com/ai/api/core/utils/ApiSleeper.java"));
            fixture.write(FrameworkArchitectureRules.CONSOLE_OWNER, "package com.ai.core.reporting.utils; public class ConsoleLogger {}");
            List<String> failures = FrameworkArchitectureRules.validateExecution(fixture.root);
            Assert.assertEquals(failures.stream().filter(value -> value.contains("EXECUTION-OWNER-STALE")).count(), 2L,
                    GovernanceFiles.problems(failures));
        }
    }

    @Test
    public void executionShadowsAreLexicalAndCannotHideUnrelatedJdkCalls() throws Exception {
        try (GovernanceFiles.Fixture fixture = executionFixture()) {
            String file = "atf-core/src/main/java/example/Probe.java";
            fixture.write(file, "package example; class Probe { static class Thread { static void sleep(long n){} } "
                    + "void run(){ Thread.sleep(1L); } void sink(Object System){} }");
            assertValid(FrameworkArchitectureRules.validateExecution(fixture.root));
            fixture.write(file, "package example; class Probe { void unrelated(){ class Thread {} String System = \"literal\"; } "
                    + "void run() throws Exception { Thread.sleep(1L); System.out.println(\"observed\"); } }");
            List<String> failures = FrameworkArchitectureRules.validateExecution(fixture.root);
            assertFinding(failures, "EXECUTION-PAUSE");
            assertFinding(failures, "EXECUTION-CONSOLE");
        }
    }

    @Test
    public void maintainedSuitesSelectTheOwnedRegressionsWithoutRunningManualJourneys() throws Exception {
        assertValid(FrameworkArchitectureRules.validateSuites(GovernanceFiles.root()));
    }

    @Test
    public void suiteInventoryIgnoresNestedFixturesAndAcceptsClassLevelTests() throws Exception {
        try (GovernanceFiles.Fixture fixture = suiteFixture()) {
            fixture.write("atf-tests/src/test/java/com/ai/atf/fixture/SuiteHelper.java",
                    "package com.ai.atf.fixture; public class SuiteHelper { "
                            + "String example = \"@org.testng.annotations.Test\"; "
                            + "public static class Nested { @org.testng.annotations.Test public void fixture(){} } }");
            fixture.write("atf-tests/src/test/java/com/ai/atf/fixture/ClassScopedTest.java",
                    "package com.ai.atf.fixture; @org.testng.annotations.Test public class ClassScopedTest { public void runs(){} }");
            addSuiteClass(fixture, "framework-local.xml", "com.ai.atf.fixture.ClassScopedTest");
            addSuiteClass(fixture, "framework-aggregate.xml", "com.ai.atf.fixture.ClassScopedTest");
            assertValid(FrameworkArchitectureRules.validateSuites(fixture.root));
        }
    }

    @DataProvider(name = "disabledMaintainedTests")
    public Object[][] disabledMaintainedTests() {
        String type = "com.ai.atf.fixture.SuiteProbeTest";
        return new Object[][] {
                {"@org.testng.annotations.Test(enabled = false) public class SuiteProbeTest { public void runs(){} }", type},
                {"public class SuiteProbeTest { @org.testng.annotations.Test(enabled = false) public void runs(){} }", type + "#runs"},
                {"import org.testng.annotations.Test; public class SuiteProbeTest { @Test(enabled = (false)) public void runs(){} }", type + "#runs"},
                {"import org.testng.annotations.*; public class SuiteProbeTest { @Test(enabled = !true) public void runs(){} }", type + "#runs"},
                {"public class SuiteProbeTest { static final boolean ACTIVE = false; "
                        + "@org.testng.annotations.Test(enabled = ACTIVE) public void runs(){} }", type + "#runs"},
                {"public class SuiteProbeTest { static final boolean ACTIVE = true; "
                        + "@org.testng.annotations.Test(enabled = ACTIVE) public void runs(){} }", type + "#runs"},
                {"public class SuiteProbeTest { @org.testng.annotations.Test(enabled = fal" + "\\u0073" + "e) public void runs(){} }", type + "#runs"}
        };
    }

    @Test(dataProvider = "disabledMaintainedTests")
    public void maintainedSourceTestsCannotHideBehindDisabledOrIndirectFlags(String declaration, String owner) throws Exception {
        try (GovernanceFiles.Fixture fixture = suiteFixture()) {
            String path = "atf-tests/src/test/java/com/ai/atf/fixture/SuiteProbeTest.java";
            fixture.write(path, "package com.ai.atf.fixture; " + declaration);
            List<String> findings = FrameworkArchitectureRules.validateSuites(fixture.root);
            Assert.assertEquals(findings.size(), 1, GovernanceFiles.problems(findings));
            assertFinding(findings, "SUITE-DISABLED " + owner + " must omit enabled or explicitly use true");

            // Repair the maintained test itself; do not change suite selections or add an exemption.
            fixture.write(path, "package com.ai.atf.fixture; public class SuiteProbeTest { "
                    + "@org.testng.annotations.Test(enabled = (true)) public void runs(){} }");
            assertValid(FrameworkArchitectureRules.validateSuites(fixture.root));
        }
    }

    @Test
    public void enabledSourceTestsIgnoreExamplesOtherAnnotationsAndReviewedManualFixtures() throws Exception {
        try (GovernanceFiles.Fixture fixture = suiteFixture()) {
            fixture.write("atf-tests/src/test/java/com/ai/atf/fixture/SuiteProbeTest.java",
                    "package com.ai.atf.fixture; import org.testng.annotations.Test; public class SuiteProbeTest { "
                            + "// @Test(enabled = false) is an example only.\n"
                            + "String example = \"@Test(enabled = false)\"; "
                            + "@interface OtherTest { boolean enabled() default true; } "
                            + "@OtherTest(enabled = false) @Test(enabled = (true), description = \"enabled=false\") public void runs(){} "
                            + "public static class NestedFixture { @Test(enabled = false) public void nested(){} } }");
            fixture.write("atf-tests/src/test/java/com/ai/api/tests/PositiveUserTests.java",
                    "package com.ai.api.tests; @org.testng.annotations.Test(enabled = false) "
                            + "public class PositiveUserTests { public void manualOnly(){} }");
            assertValid(FrameworkArchitectureRules.validateSuites(fixture.root));
            fixture.write("atf-tests/src/test/java/com/ai/atf/fixture/SuiteProbeTest.java",
                    "package com.ai.atf.fixture; import org.testng.annotations.*; public class SuiteProbeTest { "
                            + "@interface Test { boolean enabled() default true; } "
                            + "@Test(enabled = false) public void unrelatedAnnotation(){} "
                            + "@org.testng.annotations.Test public void runs(){} }");
            assertValid(FrameworkArchitectureRules.validateSuites(fixture.root));
        }
    }

    @Test
    public void suiteInventoryRejectsAnUnselectedRegression() throws Exception {
        try (GovernanceFiles.Fixture fixture = suiteFixture()) {
            fixture.write("atf-tests/src/test/java/com/ai/atf/fixture/ForgottenTest.java",
                    "package com.ai.atf.fixture; public class ForgottenTest { @org.testng.annotations.Test public void omitted(){} }");
            assertFinding(FrameworkArchitectureRules.validateSuites(fixture.root), "SUITE-OMISSION");
        }
    }

    @Test
    public void suiteInventoryRejectsDuplicateAndMissingClassReferences() throws Exception {
        try (GovernanceFiles.Fixture fixture = suiteFixture()) {
            addSuiteClass(fixture, "framework-api.xml", "com.ai.atf.fixture.SuiteProbeTest");
            addSuiteClass(fixture, "framework-ui.xml", "com.ai.atf.fixture.MissingTest");
            List<String> failures = FrameworkArchitectureRules.validateSuites(fixture.root);
            assertFinding(failures, "SUITE-DUPLICATE");
            assertFinding(failures, "SUITE-CLASS");
        }
    }

    @Test
    public void suiteInventoryRejectsStaleExemptionsAndAccidentalLiveSelections() throws Exception {
        try (GovernanceFiles.Fixture fixture = suiteFixture()) {
            String removed = "com.ai.atf.api.lifecycle.BeforeTestOne_TestTwo";
            Files.delete(fixture.root.resolve("atf-tests/src/test/java/" + removed.replace('.', '/') + ".java"));
            addSuiteClass(fixture, "framework-api.xml", "com.ai.api.tests.PositiveUserTests");
            String registration = "com.ai.atf.ui.RegistrationTests";
            addSuiteClass(fixture, "framework-browser.xml", registration);
            List<String> failures = FrameworkArchitectureRules.validateSuites(fixture.root);
            assertFinding(failures, "SUITE-EXEMPTION-STALE");
            assertFinding(failures, "SUITE-OPT-IN");
            Assert.assertTrue(failures.stream().anyMatch(failure -> failure.startsWith(
                            "framework-browser.xml: SUITE-OPT-IN " + registration + " is ")),
                    GovernanceFiles.problems(failures));
        }
    }

    @Test
    public void suiteInventoryRejectsAggregateDriftAndMethodFilters() throws Exception {
        try (GovernanceFiles.Fixture fixture = suiteFixture()) {
            fixture.write("atf-tests/src/test/java/com/ai/atf/fixture/OtherTest.java",
                    "package com.ai.atf.fixture; public class OtherTest { @org.testng.annotations.Test public void retained(){} }");
            addSuiteClass(fixture, "framework-local.xml", "com.ai.atf.fixture.OtherTest");
            String local = Files.readString(fixture.root.resolve("atf-tests/src/test/resources/suites/framework-local.xml"));
            fixture.write("atf-tests/src/test/resources/suites/framework-local.xml",
                    local.replace("<classes>", "<groups><run><include name=\"fast\"/></run></groups><classes>"));
            List<String> failures = FrameworkArchitectureRules.validateSuites(fixture.root);
            assertFinding(failures, "SUITE-AGGREGATE");
            assertFinding(failures, "SUITE-FILTER");
        }
    }

    @Test
    public void suiteInventoryRejectsEmptyMissingAndUncataloguedGates() throws Exception {
        try (GovernanceFiles.Fixture fixture = suiteFixture()) {
            Files.delete(fixture.root.resolve("atf-tests/src/test/resources/suites/framework-ui.xml"));
            fixture.write("atf-tests/src/test/resources/suites/framework-reporting.xml", "<suite name=\"empty\"/>");
            fixture.write("atf-tests/src/test/resources/suites/forgotten-gate.xml", "<suite name=\"uncatalogued\"/>");
            List<String> failures = FrameworkArchitectureRules.validateSuites(fixture.root);
            assertFinding(failures, "SUITE-MISSING");
            assertFinding(failures, "SUITE-EMPTY");
            assertFinding(failures, "SUITE-UNDECLARED");
        }
    }

    @Test
    public void executableTestMethodsMatchTheReviewedInventoryAndDetectLossOrUnregisteredChanges() throws Exception {
        List<String> maintained = TestMethodInventoryRules.validate(GovernanceFiles.root());
        assertValid(maintained);
        String canonical = Files.readString(GovernanceFiles.root().resolve(TestMethodInventoryRules.INVENTORY))
                .replace("\r\n", "\n");
        String reviewed = TestMethodInventoryRules.sha256(GovernanceFiles.root().resolve(TestMethodInventoryRules.INVENTORY));
        try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture()) {
            for (String equivalent : List.of(canonical, canonical.replace("\n", "\r\n"))) {
                fixture.write(TestMethodInventoryRules.INVENTORY, equivalent);
                Assert.assertEquals(TestMethodInventoryRules.sha256(fixture.root.resolve(TestMethodInventoryRules.INVENTORY)), reviewed,
                        "Git line-ending conversion must preserve the reviewed method-content seal.");
            }
            for (String changed : List.of(canonical + "# changed metadata\n", canonical.replaceFirst("\n", " \n"))) {
                fixture.write(TestMethodInventoryRules.INVENTORY, changed);
                Assert.assertNotEquals(TestMethodInventoryRules.sha256(fixture.root.resolve(TestMethodInventoryRules.INVENTORY)), reviewed);
                assertFinding(TestMethodInventoryRules.validate(fixture.root), "TEST-METHOD-INVENTORY baseline digest changed");
            }
        }
        try (GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture()) {
            String path = "atf-tests/src/test/java/com/ai/atf/fixture/SampleTest.java";
            fixture.write(path, "package com.ai.atf.fixture; import org.testng.annotations.Test; "
                    + "class SampleTest { @Test public void preserved() {} "
                    + "@Test public void overloaded(String value) {} @Test public void overloaded(int value) {} }");
            List<String> sourceErrors = new java.util.ArrayList<>();
            Set<String> baseline = TestMethodInventoryRules.inventory(
                    fixture.root.resolve("atf-tests/src/test/java"), sourceErrors);
            assertValid(sourceErrors);
            Assert.assertTrue(baseline.contains("com.ai.atf.fixture.SampleTest#overloaded(String)"));
            Assert.assertTrue(baseline.contains("com.ai.atf.fixture.SampleTest#overloaded(int)"));

            fixture.write(path, "package com.ai.atf.fixture; import org.testng.annotations.Test; "
                    + "class SampleTest { @Test public void overloaded(String value) {} "
                    + "@Test public void overloaded(int value) {} @Test public void replacement() {} }");
            Set<String> current = TestMethodInventoryRules.inventory(
                    fixture.root.resolve("atf-tests/src/test/java"), sourceErrors);
            List<String> findings = TestMethodInventoryRules.compare(new java.util.ArrayList<>(baseline), current);
            assertFinding(findings, "TEST-METHOD-REMOVED");
            assertFinding(findings, "TEST-METHOD-UNREGISTERED");
            String report = GovernanceFiles.problems(findings);
            Assert.assertTrue(report.contains("preserved()"));
            Assert.assertTrue(report.contains("replacement()"));
            Assert.assertTrue(report.contains("Confirm the deletion was intentional"));
            Assert.assertTrue(report.contains("Repair hint:"));
        }
    }

    @Test
    public void wholeReactorRespectsRuntimeAndOwnershipBoundaries() throws Exception {
        assertValid(FrameworkArchitectureRules.validate(GovernanceFiles.root()));
    }

    @Test
    public void providersAreDiscoverableWithoutStartingBrowsers() throws Exception {
        Assert.assertEquals(providers(FrameworkArchitectureRules.UI_SPI), Set.of(
                "com.ai.ui.core.engine.playwright.PlaywrightProvider",
                "com.ai.ui.core.engine.selenium.SeleniumProvider"));
        Assert.assertEquals(providers(FrameworkArchitectureRules.REPORTING_SPI),
                Set.of("com.ai.core.reporting.DefaultReportingBackend"));
    }

    @Test
    public void validatorAcceptsTheDeclaredModuleAndServiceGraph() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            assertValid(FrameworkArchitectureRules.validate(fixture.root));
        }
    }

    @Test
    public void validatorRejectsMissingModulesAndDependencyCycles() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            String pom = Files.readString(fixture.root.resolve("pom.xml"));
            fixture.write("pom.xml", pom.replace("<module>atf-selenium</module>", ""));
            fixture.write("atf-core/pom.xml", modulePom("atf-core", dependency("atf-reporting", "compile")));
            List<String> failures = FrameworkArchitectureRules.validate(fixture.root);
            assertFinding(failures, "MODULES");
            assertFinding(failures, "DEPENDENCY-CYCLE");
        }
    }

    @Test
    public void validatorRejectsCompileScopedGovernanceDependenciesAndInheritedDependencies() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            String pom = Files.readString(fixture.root.resolve("pom.xml"));
            fixture.write("pom.xml", pom.replace("</project>", "<dependencies>" + dependency("atf-core", "compile") + "</dependencies></project>"));
            fixture.write("atf-tests/pom.xml", modulePom("atf-tests", dependency("atf-core", "compile")));
            List<String> failures = FrameworkArchitectureRules.validate(fixture.root);
            assertFinding(failures, "INHERITED-DEPENDENCIES");
            assertFinding(failures, "DEPENDENCY-DIRECTION");
        }
    }

    @Test
    public void validatorRejectsBrowserSdkOwnershipInPomAndExecutableSource() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-core/pom.xml", modulePom("atf-core", "<dependency><groupId>com.microsoft.playwright</groupId><artifactId>playwright</artifactId></dependency>"));
            fixture.write("atf-core/src/main/java/com/ai/core/Sample.java", "package com.ai.core; import com.microsoft.playwright.*; class Sample {}");
            List<String> failures = FrameworkArchitectureRules.validate(fixture.root);
            assertFinding(failures, "CORE-SDK");
            assertFinding(failures, "SDK-OWNERSHIP");
            fixture.write("atf-core/pom.xml", modulePom("atf-core", ""));
            Files.delete(fixture.root.resolve("atf-core/src/main/java/com/ai/core/Sample.java"));
            Path reportingPom = fixture.root.resolve("atf-reporting/pom.xml");
            String original = Files.readString(reportingPom);
            fixture.write("atf-reporting/pom.xml", original.replace("</dependencies>",
                    "<dependency><groupId>io.qameta.allure</groupId><artifactId>allure-testng</artifactId></dependency></dependencies>"));
            fixture.write("atf-reporting/src/main/java/com/ai/core/reporting/RetiredReporter.java",
                    "package com.ai.core.reporting; import io.qameta.allure.Allure; class RetiredReporter {}\n");
            List<String> retired = FrameworkArchitectureRules.validate(fixture.root);
            assertFinding(retired, "RETIRED-REPORTING");
            assertFinding(retired, "SDK-OWNERSHIP");
            fixture.write("atf-reporting/pom.xml", original);
            Files.delete(fixture.root.resolve("atf-reporting/src/main/java/com/ai/core/reporting/RetiredReporter.java"));
            Path parentPom = fixture.root.resolve("pom.xml");
            String originalParent = Files.readString(parentPom);
            fixture.write("pom.xml", originalParent.replace("</project>",
                    "<dependencyManagement><dependencies><dependency><groupId>io.qameta.allure</groupId>"
                            + "<artifactId>allure-java-commons</artifactId></dependency></dependencies></dependencyManagement></project>"));
            assertFinding(FrameworkArchitectureRules.validate(fixture.root), "RETIRED-REPORTING");
            fixture.write("pom.xml", originalParent);
            assertValid(FrameworkArchitectureRules.validate(fixture.root));
        }
    }

    @Test
    public void validatorIgnoresExamplesAndRejectsFullyQualifiedCrossAdapterReferences() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            String example = "package com.ai.core; // com.ai.ui.core.engine.playwright.PlaywrightProvider\n"
                    + "class Sample { String example = \"com.ai.ui.core.engine.playwright.PlaywrightProvider\"; }";
            fixture.write("atf-core/src/main/java/com/ai/core/Sample.java", example);
            assertValid(FrameworkArchitectureRules.validate(fixture.root));
            fixture.write("atf-core/src/main/java/com/ai/core/Sample.java",
                    "package com.ai.core; class Sample { com.ai.ui.core.engine.playwright.PlaywrightProvider value; }");
            assertFinding(FrameworkArchitectureRules.validate(fixture.root), "IMPORT-OWNERSHIP");
        }
    }

    @Test
    public void duplicateImportsIgnoreLiteralExamplesAndNormalizeWhitespace() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-core/src/main/java/com/ai/core/Sample.java",
                    "package com.ai.core; import java.util.List; // import java.util.List;\n"
                            + "class Sample { String example = \"import java.util.List;\"; }");
            assertValid(FrameworkArchitectureRules.validate(fixture.root));
            fixture.write("atf-core/src/main/java/com/ai/core/Sample.java",
                    "package com.ai.core; import java.util.List; import java . util . List; class Sample {}");
            assertFinding(FrameworkArchitectureRules.validate(fixture.root), "DUPLICATE-IMPORT");
        }
    }

    @Test
    public void validatorRejectsProductSupportAndMismatchedPackageLocations() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-core/src/main/java/com/ai/ui/support/pages/CheckoutPage.java",
                    "package com.ai.ui.support.pages; class CheckoutPage {}");
            fixture.write("atf-core/src/main/java/com/ai/core/Wrong.java", "package wrong.location; class Wrong {}");
            List<String> failures = FrameworkArchitectureRules.validate(fixture.root);
            assertFinding(failures, "TEST-PRODUCT-LEAK");
            assertFinding(failures, "PACKAGE-PATH");
        }
    }

    @Test
    public void validatorRejectsMissingProviderOwnersAndProductionCodeInGovernance() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-playwright/src/main/resources/META-INF/services/" + FrameworkArchitectureRules.UI_SPI, "missing.Provider\n");
            fixture.write("atf-tests/src/main/java/com/ai/atf/Example.java", "package com.ai.atf; class Example {}");
            List<String> failures = FrameworkArchitectureRules.validate(fixture.root);
            assertFinding(failures, "SERVICE-OWNER");
            assertFinding(failures, "TESTS-MAIN");
        }
    }

    @Test
    public void validatorRejectsUnsafeSourceRootsAndTestResourcePackaging() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            String pom = modulePom("atf-core", "").replace("</project>",
                    "<build><sourceDirectory>../atf-tests/src/test/java</sourceDirectory>"
                            + "<resources><resource><directory>src/test/resources</directory></resource></resources></build></project>");
            fixture.write("atf-core/pom.xml", pom);
            List<String> failures = FrameworkArchitectureRules.validate(fixture.root);
            assertFinding(failures, "SOURCE-ROOTS");
            assertFinding(failures, "TEST-RESOURCE-LEAK");
        }
    }

    @Test
    public void validatorRejectsMisplacedImplementationPackagesAndLeftoverMonolithSources() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-core/src/main/java/com/ai/ui/core/engine/selenium/Misplaced.java",
                    "package com.ai.ui.core.engine.selenium; class Misplaced {}");
            fixture.write("src/main/java/com/ai/Leftover.java", "package com.ai; class Leftover {}");
            List<String> failures = FrameworkArchitectureRules.validate(fixture.root);
            assertFinding(failures, "PACKAGE-OWNER");
            assertFinding(failures, "ROOT-SOURCES");
        }
    }

    @Test
    public void validatorRejectsDependencyEdgesAndSdkDependenciesInsideProfiles() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            String profile = "<profiles><profile><id>browser</id><activation><activeByDefault>true</activeByDefault></activation><dependencies>"
                    + dependency("atf-reporting", "compile")
                    + "<dependency><groupId>com.microsoft.playwright</groupId><artifactId>playwright</artifactId></dependency>"
                    + "</dependencies></profile></profiles>";
            fixture.write("atf-core/pom.xml", modulePom("atf-core", "").replace("</project>", profile + "</project>"));
            List<String> failures = FrameworkArchitectureRules.validate(fixture.root);
            assertFinding(failures, "DEPENDENCY-DIRECTION");
            assertFinding(failures, "DEPENDENCY-CYCLE");
            assertFinding(failures, "CORE-SDK");
        }
    }

    @Test
    public void validatorRejectsInheritedSourceRootsResourcesAndProfileDependencies() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            String parent = Files.readString(fixture.root.resolve("pom.xml"));
            String build = "<build><sourceDirectory>../shared-runtime/src/main/java</sourceDirectory>"
                    + "<resources><resource><directory>atf-tests/src/test/resources</directory></resource></resources></build>";
            String profile = "<profiles><profile><id>inherited</id><dependencies>" + dependency("atf-reporting", "compile")
                    + "</dependencies></profile></profiles>";
            fixture.write("pom.xml", parent.replace("</project>", build + profile + "</project>"));
            List<String> failures = FrameworkArchitectureRules.validate(fixture.root);
            assertFinding(failures, "SOURCE-ROOTS");
            assertFinding(failures, "TEST-RESOURCE-LEAK");
            assertFinding(failures, "INHERITED-DEPENDENCIES");
        }
    }

    @Test
    public void validatorRejectsProfileBuildHelperAdditionalSourceRoots() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            String profile = "<profiles><profile><id>extra-source</id><build><plugins><plugin>"
                    + "<groupId>org.codehaus.mojo</groupId><artifactId>build-helper-maven-plugin</artifactId>"
                    + "<executions><execution><id>include-tests</id><phase>generate-sources</phase><goals><goal>add-source</goal></goals>"
                    + "<configuration><sources><source>../atf-tests/src/test/java</source></sources></configuration>"
                    + "</execution></executions></plugin></plugins></build></profile></profiles>";
            fixture.write("atf-core/pom.xml", modulePom("atf-core", "").replace("</project>", profile + "</project>"));
            assertFinding(FrameworkArchitectureRules.validate(fixture.root), "SOURCE-ROOTS");
        }
    }

    @Test
    public void validatorRejectsForeignGroupWithAnInternalArtifactName() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-playwright/pom.xml", modulePom("atf-playwright",
                    dependency("atf-core", "compile").replace("<groupId>com.ai</groupId>", "<groupId>org.other</groupId>")));
            assertFinding(FrameworkArchitectureRules.validate(fixture.root), "DEPENDENCY-COORDINATE");
            fixture.write("atf-playwright/pom.xml", modulePom("atf-playwright", dependency("atf-core", "compile")));
            String dynamic = "<profiles><profile><id>dynamic-coordinate</id><dependencies>"
                    + dependency("${adapter.artifact}", "compile") + "</dependencies></profile></profiles>";
            fixture.write("atf-core/pom.xml", modulePom("atf-core", "").replace("</project>", dynamic + "</project>"));
            List<String> findings = FrameworkArchitectureRules.validate(fixture.root);
            assertFinding(findings, "DEPENDENCY-COORDINATE");
            Assert.assertTrue(findings.stream().anyMatch(finding -> GovernanceFiles.matchesFinding(finding, "DEPENDENCY-COORDINATE")
                    && finding.contains("static ownership inventory") && finding.contains("${adapter.artifact}")),
                    "The coordinate finding must retain the actual unresolved artifact. " + GovernanceFiles.problems(findings));
        }
    }

    @Test
    public void validatorRejectsUnimportedTypesOwnedByAnotherModuleInTheSamePackage() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-core/src/main/java/com/ai/core/reporting/Sample.java",
                    "package com.ai.core.reporting; class Sample { DefaultReportingBackend value; }");
            assertFinding(FrameworkArchitectureRules.validate(fixture.root), "SIMPLE-TYPE-OWNERSHIP");
        }
    }

    @Test
    public void samePackageTypeCheckIgnoresLiteralsVariableNamesAndExplicitImportedTypes() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-core/src/main/java/com/ai/core/reporting/Sample.java",
                    "package com.ai.core.reporting; class Sample { String DefaultReportingBackend = \"DefaultReportingBackend\"; }");
            assertValid(FrameworkArchitectureRules.validate(fixture.root));
            fixture.write("atf-core/src/main/java/com/ai/core/safe/DefaultReportingBackend.java",
                    "package com.ai.core.safe; public class DefaultReportingBackend {}");
            fixture.write("atf-core/src/main/java/com/ai/core/reporting/Sample.java",
                    "package com.ai.core.reporting; import com.ai.core.safe.DefaultReportingBackend; class Sample { DefaultReportingBackend value; }");
            assertValid(FrameworkArchitectureRules.validate(fixture.root));
        }
    }

    @DataProvider(name = "samePackageTypePositions")
    public Object[][] samePackageTypePositions() {
        return new Object[][] {
                {"class Sample { static <T> void use(){} void probe(){ Sample.<DefaultReportingBackend>use(); } }"},
                {"class Sample { Class<?> leak = DefaultReportingBackend[].class; }"},
                {"class Sample { <T> Sample(){} Object leak = new <DefaultReportingBackend> Sample(); }"},
                {"class Sample { java.util.function.IntFunction<?> factory = DefaultReportingBackend[]::new; }"},
                {"class Sample { static <T> Object make(){ return null; } java.util.function.Supplier<Object> leak = Sample::<DefaultReportingBackend>make; }"}
        };
    }

    @Test(dataProvider = "samePackageTypePositions")
    public void validatorRejectsExplicitTypeArgumentsAndArrayTypeExpressions(String declaration) throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-core/src/main/java/com/ai/core/reporting/Sample.java", "package com.ai.core.reporting; " + declaration);
            assertFinding(FrameworkArchitectureRules.validate(fixture.root), "SIMPLE-TYPE-OWNERSHIP");
        }
    }

    @Test
    public void unrelatedMethodTypeParametersAndVariablesCannotHideARealTypeReference() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-core/src/main/java/com/ai/core/reporting/Sample.java", "package com.ai.core.reporting; "
                    + "class Sample { DefaultReportingBackend value; <DefaultReportingBackend> void unrelated(){} }");
            assertFinding(FrameworkArchitectureRules.validate(fixture.root), "SIMPLE-TYPE-OWNERSHIP");
            fixture.write("atf-core/src/main/java/com/ai/core/reporting/Sample.java", "package com.ai.core.reporting; "
                    + "class Sample { void unrelated(){ String DefaultReportingBackend = \"literal\"; } "
                    + "Object leak = DefaultReportingBackend.someStaticMember; }");
            assertFinding(FrameworkArchitectureRules.validate(fixture.root), "SIMPLE-TYPE-OWNERSHIP");
        }
    }

    @Test
    public void realTypeParameterAndLocalClassShadowsStayWithinTheirLexicalScopes() throws Exception {
        try (GovernanceFiles.Fixture fixture = reactor()) {
            fixture.write("atf-core/src/main/java/com/ai/core/reporting/Sample.java", "package com.ai.core.reporting; "
                    + "class Sample { <DefaultReportingBackend> DefaultReportingBackend identity(DefaultReportingBackend value){ return value; } "
                    + "void local(){ class DefaultReportingBackend {} DefaultReportingBackend value = new DefaultReportingBackend(); } }");
            assertValid(FrameworkArchitectureRules.validate(fixture.root));
            fixture.write("atf-core/src/main/java/com/ai/core/reporting/Sample.java", "package com.ai.core.reporting; "
                    + "class Sample { void local(){ class DefaultReportingBackend {} } DefaultReportingBackend value; }");
            assertFinding(FrameworkArchitectureRules.validate(fixture.root), "SIMPLE-TYPE-OWNERSHIP");
        }
    }

    private static Set<String> providers(String spi) throws Exception {
        Set<String> names = new HashSet<>();
        for (Object provider : ServiceLoader.load(Class.forName(spi))) names.add(provider.getClass().getName());
        return names;
    }

    private static GovernanceFiles.Fixture reactor() throws Exception {
        GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture();
        StringBuilder modules = new StringBuilder();
        for (String module : GovernanceFiles.MODULES) modules.append("<module>").append(module).append("</module>");
        fixture.write("pom.xml", "<project><groupId>com.ai</groupId><artifactId>atf-parent</artifactId><version>1.0.0</version><packaging>pom</packaging><modules>" + modules + "</modules></project>");
        for (String module : GovernanceFiles.MODULES) {
            StringBuilder dependencies = new StringBuilder();
            if (module.equals("atf-tests")) {
                for (String owner : GovernanceFiles.MODULES) if (!owner.equals(module)) dependencies.append(dependency(owner, "test"));
            } else if (!module.equals("atf-core")) dependencies.append(dependency("atf-core", "compile"));
            fixture.write(module + "/pom.xml", modulePom(module, dependencies.toString()));
        }
        fixture.write("atf-core/src/main/java/com/ai/ui/core/engine/UiEngineProvider.java", "package com.ai.ui.core.engine; public interface UiEngineProvider {}");
        fixture.write("atf-core/src/main/java/com/ai/core/reporting/spi/ReportingBackend.java", "package com.ai.core.reporting.spi; public interface ReportingBackend {}");
        provider(fixture, "atf-playwright", "com.ai.ui.core.engine.playwright.PlaywrightProvider", FrameworkArchitectureRules.UI_SPI);
        provider(fixture, "atf-selenium", "com.ai.ui.core.engine.selenium.SeleniumProvider", FrameworkArchitectureRules.UI_SPI);
        provider(fixture, "atf-reporting", "com.ai.core.reporting.DefaultReportingBackend", FrameworkArchitectureRules.REPORTING_SPI);
        return fixture;
    }

    private static GovernanceFiles.Fixture executionFixture() throws Exception {
        GovernanceFiles.Fixture fixture = new GovernanceFiles.Fixture();
        addExecutionOwners(fixture);
        return fixture;
    }

    private static void addExecutionOwners(GovernanceFiles.Fixture fixture) throws Exception {
        for (String file : FrameworkArchitectureRules.PAUSE_OWNERS) {
            String qualified = file.substring(file.indexOf("/java/") + 6, file.length() - 5).replace('/', '.');
            int separator = qualified.lastIndexOf('.');
            fixture.write(file, "package " + qualified.substring(0, separator) + "; public class " + qualified.substring(separator + 1)
                    + " { static void pause(){ try { Thread.sleep(1L); } catch(InterruptedException failure){ Thread.currentThread().interrupt(); } } }");
        }
        fixture.write(FrameworkArchitectureRules.CONSOLE_OWNER,
                "package com.ai.core.reporting.utils; public class ConsoleLogger { static void emit(){ System.out.println(\"observed\"); } }");
    }

    private static GovernanceFiles.Fixture suiteFixture() throws Exception {
        GovernanceFiles.Fixture fixture = reactor();
        fixture.write("atf-tests/src/test/java/com/ai/atf/fixture/SuiteProbeTest.java",
                "package com.ai.atf.fixture; public class SuiteProbeTest { @org.testng.annotations.Test public void runs(){} }");
        for (String type : FrameworkArchitectureRules.SUITE_EXEMPTIONS.keySet()) {
            int separator = type.lastIndexOf('.');
            fixture.write("atf-tests/src/test/java/" + type.replace('.', '/') + ".java",
                    "package " + type.substring(0, separator) + "; public class " + type.substring(separator + 1)
                            + " { @org.testng.annotations.Test public void manualOrNested(){} }");
        }
        String xml = "<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">"
                + "<suite name=\"fixture\"><test name=\"fixture\"><classes>"
                + "<class name=\"com.ai.atf.fixture.SuiteProbeTest\"/>"
                + "</classes></test></suite>";
        for (String suite : FrameworkArchitectureRules.MAINTAINED_SUITES) {
            fixture.write("atf-tests/src/test/resources/suites/" + suite, xml);
        }
        return fixture;
    }

    private static void addSuiteClass(GovernanceFiles.Fixture fixture, String suite, String type) throws Exception {
        String relative = "atf-tests/src/test/resources/suites/" + suite;
        fixture.write(relative, Files.readString(fixture.root.resolve(relative))
                .replace("</classes>", "<class name=\"" + type + "\"/></classes>"));
    }

    private static void provider(GovernanceFiles.Fixture fixture, String module, String name, String spi) throws Exception {
        int separator = name.lastIndexOf('.');
        fixture.write(module + "/src/main/java/" + name.replace('.', '/') + ".java", "package " + name.substring(0, separator)
                + "; public class " + name.substring(separator + 1) + " implements " + spi + " {}");
        fixture.write(module + "/src/main/resources/META-INF/services/" + spi, name + '\n');
    }

    private static String modulePom(String module, String dependencies) {
        return "<project><parent><groupId>com.ai</groupId><artifactId>atf-parent</artifactId><version>1.0.0</version></parent>"
                + "<artifactId>" + module + "</artifactId><dependencies>" + dependencies + "</dependencies></project>";
    }

    private static String dependency(String artifact, String scope) {
        return "<dependency><groupId>com.ai</groupId><artifactId>" + artifact + "</artifactId><version>1.0.0</version><scope>" + scope + "</scope></dependency>";
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
