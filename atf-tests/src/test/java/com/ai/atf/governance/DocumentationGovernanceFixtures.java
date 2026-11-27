package com.ai.atf.governance;

import org.testng.Assert;

import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

/** Shared, relocatable fixtures and assertions for documentation governance tests. */
final class DocumentationGovernanceFixtures {
    static GovernanceFiles.Fixture documentation() throws Exception {
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
        for (String tool : List.of("atlas-reader.mjs", "atlas-reader.test.mjs", "atlas-reader-graphs.mjs", "atlas-reader-graphs.test.mjs", "atlas-calibration.mjs", "atlas-calibration.test.mjs", "atlas-report.mjs", "atlas-report.test.mjs", "atlas-knowledge.mjs",
                "atlas-knowledge.test.mjs", "atlas-diagnostics.mjs", "atlas-bundle.mjs", "atlas-bundle.test.mjs",
                "atlas-execution.mjs", "atlas-execution.test.mjs", "atlas-understanding.mjs", "atlas-understanding.test.mjs",
                "atlas-behavior.mjs", "atlas-behavior.test.mjs")) {
            fixture.write("tools/" + tool, "// Atlas maintenance tooling fixture.\n");
        }
        fixture.write("atf-atlas/Report_Contract.md", "# Complete Atlas report\n\nOne source-bound document with inline assets and proof limits.\n");
        fixture.write("atf-atlas/Knowledge_Base.md", "# Atlas knowledge\n\nCited requirements support a source-grounded reviewer.\n");
        fixture.write("atf-atlas/assets/atlas.css", ":root { color-scheme: light; }\n");
        fixture.write("atf-atlas/knowledge/atlas-contracts.json", "{\"schemaVersion\":1}\n");
        String moduleTable = "| Module | Responsibility |\n|---|---|\n"
                + GovernanceFiles.MODULES.stream()
                .map(module -> "| [" + module + "](" + module + "/README.md) | Existing module owner. |\n")
                .collect(java.util.stream.Collectors.joining());
        fixture.write("README.md", "# ATF\n\ncom.ai:atf-parent:1.0.0 targets Java 11.\n\n"
                + "## The design in one view\n\n" + moduleTable + "\n" + diagram()
                + "\n## How a check runs\n\nAPI and UI contracts select their current framework owners.\n\n" + diagram()
                + "\n## How to choose a consumer route\n\n"
                + "[Playwright](atf-playwright/README.md#consumer-example)\n\n"
                + "[Selenium](atf-selenium/README.md#consumer-example)\n\n"
                + "[Architecture](docs/Architecture.md)\n\n[Testing](docs/Testing.md)\n\n"
                + "<a id=\"start\"></a>\n## Build and verify\n\nRun current checks in the documented staging copy.\n\n"
                + "<a id=\"consumer-migration\"></a>\n## Configure and migrate\n\nUse the declared module coordinates.\n\n"
                + "## Read by task\n\n[Governance](docs/governance/README.md)\n\n"
                + "[Failure hints](docs/governance/Failure_Diagnostics.md)\n\n"
                + "[Coverage](atf-atlas/Coverage_Map.md)\n\n[Artifacts](artifacts/README.md)\n\n"
                + "[Documentation index](docs/README.md)\n");
        fixture.write("docs/Architecture.md", "# Architecture\n\n## Runtime\n\n" + diagram());
        fixture.write("docs/Testing.md", "# Testing\n\nRun the maintained suites in staging.\n");
        fixture.write("docs/Testing_Commands.md", "# Testing commands\n\nSelect a maintained recipe.\n");
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
        fixture.write("docs/governance/Readme_Editions.md", "# Documentation editions\n\nEnglish and Romanian documentation editions use the same project routes.\n");
        fixture.write("docs/governance/Failure_Diagnostics.md", "# Governance failure diagnostics\n\nPreserve observed findings, owned repair hints and verification.\n");
        fixture.write("atf-atlas/Coverage_Map.md", "# Coverage map\n\n[Contracts](../atf-core/README.md)\n");
        for (String module : java.util.stream.Stream.concat(GovernanceFiles.MODULES.stream(), java.util.stream.Stream.of("atf-atlas"))
                .collect(java.util.stream.Collectors.toList())) {
            String sections = module.equals("atf-core") ? "\n## Configuration\n\nUse the declared classpath defaults.\n" : "";
            if (module.equals("atf-playwright") || module.equals("atf-selenium")) {
                sections += "\n## Consumer example\n\nUse this provider's documented configuration.\n";
            }
            fixture.write(module + "/README.md", "# " + module + "\n\n## Overview\n\nModule scope.\n\n## Ownership\n\nOwn the contracts.\n"
                    + sections + "\n## Verification\n\nRun the maintained checks.\n");
        }
        for (String readme : List.of("atf-playwright/README.md", "atf-selenium/README.md")) {
            append(fixture, readme, "\n" + diagram()
                    + "\n```java compile\npublic class ConsumerExample { public void run() { System.getProperty(\"fixture\"); } }\n```\n");
        }
        String living = "# Living architecture\n\n## Overview\n\nCurrent UI and API contracts.\n\n```properties defaults\nui.engine=playwright\n```\n";
        fixture.write("docs/Living_Architecture_UI_API_doc_v1_0.md", living);
        fixture.write("docs/Living_Architecture_UI_API_doc_v1_0.html", "<!doctype html><html lang=\"en\"><head>"
                + "<meta name=\"atf-source\" content=\"Living_Architecture_UI_API_doc_v1_0.md\">"
                + "<meta name=\"atf-source-sha256\" content=\"" + hash(living) + "\"><style>body { color: black; }</style></head><body>"
                + "<h1>Living architecture</h1><a href=\"#overview\">Overview</a><h2 id=\"overview\">Overview</h2>"
                + "<p>Current UI and API contracts.</p></body></html>");
        return fixture;
    }

    static String hash(String source) throws Exception {
        StringBuilder result = new StringBuilder();
        for (byte value : MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8))) {
            result.append(String.format("%02x", value & 0xff));
        }
        return result.toString();
    }

    static String diagram() {
        return "```mermaid\nflowchart LR\nCore[Core] -->|provides| Runtime[Runtime]\n```\n\nThe arrow shows the provided runtime contract.\n";
    }

    static void append(GovernanceFiles.Fixture fixture, String path, String content) throws Exception {
        fixture.write(path, Files.readString(fixture.root.resolve(path)) + content);
    }

    static void assertValid(List<String> failures) {
        Assert.assertTrue(failures.isEmpty(), GovernanceFiles.problems(failures));
    }

    static void assertFinding(List<String> failures, String code) {
        Assert.assertTrue(failures.stream().anyMatch(failure -> GovernanceFiles.matchesFinding(failure, code)), "Expected " + code + "; " + GovernanceFiles.problems(failures));
        Assert.assertTrue(failures.stream().allMatch(GovernanceFiles::hasDiagnosticHelp),
                "Existing negative fixtures must exercise owned diagnostic guidance. " + GovernanceFiles.problems(failures));
    }
}
