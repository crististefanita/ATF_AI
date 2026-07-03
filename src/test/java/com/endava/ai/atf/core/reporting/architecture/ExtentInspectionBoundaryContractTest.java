package com.endava.ai.atf.core.reporting.architecture;

import com.endava.ai.atf.core.reporting.architecture.support.ArchitectureSourceScanner;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_INSPECTION_BOUNDARY_CONTRACT_TEST_SUITE)
@Listeners(TestListener.class)
public class ExtentInspectionBoundaryContractTest {
    private static final int MAX_REPORTING_ASSERTION_LINES = 300;

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_INSPECTION_BOUNDARY_CONTRACT_TEST_EXTENT_SPARK_INSPECTION_MAIN_PACKAGE_STAYS_PURE)
    public void extentSparkInspectionMainPackageStaysPure() throws IOException {
        List<Path> pureMainRoots = List.of(
                AtfSourcePaths.mainJavaPackage(
                        AtfSourcePaths.CORE_ROOT_PACKAGE + ".reporting.adapters.extent.inspection"
                ),
                AtfSourcePaths.mainJavaPackage(
                        AtfSourcePaths.CORE_ROOT_PACKAGE + ".reporting.adapters.extent.synthetic"
                )
        );

        List<String> forbidden = new ArrayList<>();
        for (Path root : pureMainRoots) {
            Assert.assertTrue(Files.isDirectory(root), "Missing pure reporting helper package: " + root);
            try (Stream<Path> files = Files.walk(root)) {
                files.filter(AtfSourcePaths::isJavaSource)
                        .forEach(path -> inspectSource(path, forbidden));
            }
        }
        inspectSource(
                AtfSourcePaths.mainJava(
                        AtfSourcePaths.CORE_ROOT_PACKAGE
                                + ".reporting.aggregate.artifact.AggregateManifestInspector"
                ),
                forbidden
        );

        Assert.assertTrue(
                forbidden.isEmpty(),
                "Main Extent inspection package must stay pure parser/model code, not test harness: " + forbidden
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_INSPECTION_BOUNDARY_CONTRACT_TEST_REPORTING_ASSERTION_HELPERS_STAY_BELOW_PARSER_BLOAT_THRESHOLD)
    public void reportingAssertionHelpersStayBelowParserBloatThreshold() throws IOException {
        Path reportingTests = AtfSourcePaths.TEST_ATF_ROOT.resolve("core/reporting");
        List<String> oversized = new ArrayList<>();
        try (Stream<Path> files = Files.walk(reportingTests)) {
            files.filter(path -> path.getFileName().toString().endsWith("Assertions.java"))
                    .forEach(path -> {
                        try {
                            long lines = ArchitectureSourceScanner.lineCount(path);
                            if (lines > MAX_REPORTING_ASSERTION_LINES) {
                                oversized.add(path + " has " + lines + " lines, max " + MAX_REPORTING_ASSERTION_LINES);
                            }
                        } catch (IOException e) {
                            throw new IllegalStateException("Cannot inspect assertion helper " + path, e);
                        }
                    });
        }

        Assert.assertTrue(
                oversized.isEmpty(),
                "Reporting assertion helpers must stay readable and delegate parsing to main inspectors: " + oversized
        );
    }

    private static void inspectSource(Path path, List<String> forbidden) {
        String source = ArchitectureSourceScanner.readUnchecked(path);
        if (source.contains("org.testng")
                || source.contains("Assert.")
                || source.contains("com.microsoft.playwright")
                || source.contains("org.mockito")
                || source.contains("com.endava.ai.atf")
                || source.contains("@AtfContract")
                || source.contains("resetForTests")) {
            forbidden.add(path.toString());
        }
    }
}
