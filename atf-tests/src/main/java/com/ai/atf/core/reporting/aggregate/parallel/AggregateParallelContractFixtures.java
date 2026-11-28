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

    private AggregateParallelContractFixtures() {
    }

    static List<AggregateCollectionSpec> specs(String prefix) {
        return List.of(
                spec(prefix, "core-sanity"),
                spec(prefix, "reporting-sanity"),
                spec(prefix, "ui-sanity")
        );
    }

    static List<AggregateCollectionSpec> standardSanitySpecs() {
        return List.of(
                standardSanitySpec("core-sanity", "framework-reporting.xml"),
                standardSanitySpec("reporting-sanity", "framework-api.xml"),
                standardSanitySpec("ui-sanity", "framework-ui.xml")
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

    private static AggregateCollectionSpec standardSanitySpec(String id, String suiteFile) {
        return AggregateCollectionSpec.mavenSuite(
                id,
                id,
                AtfSourcePaths.TEST_RESOURCES_ROOT.resolve("suites").resolve(suiteFile).toString(),
                AtfSourcePaths.TARGET_ROOT.resolve("parallel-aggregate").resolve(id)
        );
    }


}
