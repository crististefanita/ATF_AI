package com.ai.atf.core.reporting.architecture.surface;

import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.ai.core.reporting.aggregate.artifact.AggregateManifestWriter;
import com.ai.core.reporting.aggregate.artifact.RuntimeCoverageAuditWriter;
import com.ai.skills.reporting.AggregateReportSurface;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.REPORTING_PACKAGE_SHAPE)
@AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_CLASS)
@Listeners(TestListener.class)
public class AggregateReportSurfaceContractTest {
    private static final String REPORTING_PACKAGE_SHAPE = "skills/reporting/contracts/reporting-package-shape.yml";

    @Test
    public void aggregateReportSurfacePublishesSmallReportingOwnedEntryPoint() {
        Assert.assertEquals(
                AggregateReportSurface.IMPLEMENTATION_PACKAGE,
                "com.ai.core.reporting.aggregate"
        );
        Assert.assertEquals(
                AggregateReportSurface.SANITY_SUITE,
                "src/test/resources/atf/core/reporting/testng-reporting-sanity.xml"
        );
        Assert.assertEquals(
                AggregateReportSurface.DEEP_SUITE,
                "src/test/resources/atf/core/suites/aggregate/testng-atf-core-aggregate.xml"
        );

        List<Class<?>> publicApi = List.of(
                AggregateReportDescriptor.class,
                AggregateExtentReportWriter.class,
                AggregateManifestWriter.class,
                RuntimeCoverageAuditWriter.class
        );
        for (Class<?> type : publicApi) {
            Assert.assertTrue(type.getName().startsWith(AggregateReportSurface.IMPLEMENTATION_PACKAGE),
                    "Aggregate public API must stay in aggregate reporting implementation: " + type.getName());
            Assert.assertTrue(List.of(AggregateReportSurface.PUBLIC_API).contains(type.getSimpleName()),
                    "Aggregate facade must expose public API: " + type.getSimpleName());
        }

        for (String forbidden : AggregateReportSurface.FORBIDDEN_OWNERSHIP) {
            Assert.assertFalse(forbidden.isBlank(), "Forbidden aggregate ownership must be explicit.");
        }
    }

    @Test
    public void aggregateReportingReadmeIsRequiredByPackageShapeGovernance() throws IOException {
        List<Map<String, String>> entries = SimpleYamlContractLoader.load(REPORTING_PACKAGE_SHAPE);
        Assert.assertTrue(entries.stream()
                        .anyMatch(entry -> AggregateReportSurface.README.equals(entry.get("path"))),
                "reporting-package-shape.yml must require the aggregate README.");

        String readme = Files.readString(AtfSourcePaths.workspaceRoot().resolve(AggregateReportSurface.README));
        Assert.assertTrue(readme.contains("AggregateReportSurface"));
        Assert.assertTrue(readme.contains("AggregateReportDescriptor"));
        Assert.assertTrue(readme.contains("AggregateExtentReportWriter"));
        Assert.assertTrue(readme.contains("Do not"));
        Assert.assertTrue(readme.contains("testng-atf-core-aggregate.xml"));
    }
}
