package com.ai.atf.core.reporting.aggregate.parallel;

import com.ai.core.reporting.aggregate.parallel.AggregateCollectionResult;
import com.ai.core.reporting.aggregate.parallel.AggregateCollectionSpec;
import com.ai.core.reporting.aggregate.parallel.AggregateCollectionTestCase;
import com.ai.core.reporting.aggregate.parallel.AggregateTestNgResultParser;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class AggregateParallelContractFixtures {
    static final Path FULL_ROOT = AtfSourcePaths.TARGET_ROOT.resolve("atf-core-parallel-aggregate-full");
    static final Path FULL_COLLECTION_REPORT = FULL_ROOT.resolve("collection-closeout").resolve("ExtentReport.html");
    static final Path FULL_METHOD_REPORT = FULL_ROOT.resolve("method-closeout").resolve("ExtentReport.html");
    static final int FULL_METHOD_EVIDENCE_FLOOR = 580;

    private AggregateParallelContractFixtures() {
    }

    static List<AggregateCollectionSpec> specs(String prefix) {
        return List.of(
                spec(prefix, "core-sanity"),
                spec(prefix, "reporting-sanity"),
                spec(prefix, "mcp-sanity")
        );
    }

    static List<AggregateCollectionSpec> standardSanitySpecs() {
        return List.of(
                standardSanitySpec("core-sanity",
                        "src/test/resources/atf/core/suites/core/testng-atf-core-sanity.xml"),
                standardSanitySpec("business-understanding-sanity",
                        "src/test/resources/skills/business/testng-business-understanding-sanity.xml"),
                standardSanitySpec("reporting-sanity",
                        "src/test/resources/atf/core/reporting/testng-reporting-sanity.xml"),
                standardSanitySpec("mcp-sanity",
                        "src/test/resources/skills/mcp/testng-mcp-sanity.xml"),
                standardSanitySpec("atlas-sanity",
                        "src/test/resources/skills/atlas/testng-atlas-sanity.xml")
        );
    }

    static List<AggregateCollectionSpec> fullPackageSpecs() {
        return List.of(
                fullPackageSpec("core-adapters-status", "ATF Core reporting adapters status",
                        "testng-atf-core-reporting-adapters-status.xml"),
                fullPackageSpec("core-adapters-tags", "ATF Core reporting adapters tags",
                        "testng-atf-core-reporting-adapters-tags.xml"),
                fullPackageSpec("core-adapters-shell", "ATF Core reporting adapters shell",
                        "testng-atf-core-reporting-adapters-shell.xml"),
                fullPackageSpec("core-runtime-audit", "ATF Core reporting runtime audit",
                        "testng-atf-core-reporting-runtime-audit.xml"),
                fullPackageSpec("core-visual-zoom", "ATF Core reporting visual zoom",
                        "testng-atf-core-reporting-visual-zoom.xml"),
                fullPackageSpec("core-visual-browser", "ATF Core reporting visual browser",
                        "testng-atf-core-reporting-visual-browser.xml"),
                fullPackageSpec("core-visual-screenshot", "ATF Core reporting visual screenshot",
                        "testng-atf-core-reporting-visual-screenshot.xml"),
                fullPackageSpec("ui-package", "ATF UI package", "testng-atf-core-ui.xml"),
                fullPackageSpec("core-reporting-rest", "ATF Core reporting rest",
                        "testng-atf-core-reporting-rest.xml"),
                fullPackageSpec("core-visual-rest", "ATF Core reporting visual rest",
                        "testng-atf-core-reporting-visual-rest.xml"),
                fullPackageSpec("core-support-skills", "ATF Core support and skills",
                        "testng-atf-core-support-skills.xml"),
                fullPackageSpec("api-package", "ATF API package", "testng-atf-core-api.xml")
        );
    }

    static List<AggregateCollectionTestCase> methodCases(List<AggregateCollectionResult> results)
            throws IOException {
        List<AggregateCollectionTestCase> cases = new ArrayList<>();
        for (AggregateCollectionResult result : results) {
            Path xml = result.spec().testNgResultsXml();
            Assert.assertTrue(Files.isRegularFile(xml), "Missing isolated TestNG XML for " + result.spec().id());
            cases.addAll(AggregateTestNgResultParser.parse(xml, result.spec()));
        }
        return List.copyOf(cases);
    }

    static void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            List<Path> ordered = paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
            for (Path path : ordered) {
                Files.deleteIfExists(path);
            }
        }
    }

    static AggregateCollectionSpec spec(String prefix, String id) {
        String fullId = prefix + "-" + id;
        return AggregateCollectionSpec.of(
                fullId,
                fullId,
                "mvn -q -Dsurefire.suiteXmlFiles=" + id + ".xml test",
                AtfSourcePaths.TARGET_ROOT.resolve("parallel-aggregate").resolve(fullId)
        );
    }

    private static AggregateCollectionSpec standardSanitySpec(String id, String suiteXml) {
        return AggregateCollectionSpec.mavenSuite(
                id,
                id,
                suiteXml,
                AtfSourcePaths.TARGET_ROOT.resolve("parallel-aggregate").resolve(id)
        );
    }

    private static AggregateCollectionSpec fullPackageSpec(String id, String displayName, String suiteFile) {
        String suiteXml = AtfSourcePaths.unixPath(
                AtfSourcePaths.CORE_TEST_RESOURCES_ROOT.resolve("parallel").resolve(suiteFile)
        );
        return AggregateCollectionSpec.mavenSuiteReusingWorkspaceBuild(
                id,
                displayName,
                suiteXml,
                FULL_ROOT.resolve("collections").resolve(id)
        );
    }
}
