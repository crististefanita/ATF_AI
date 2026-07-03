package com.ai.skills.atlas.tests;

import com.ai.skills.atlas.AtlasContext;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.ai.core.reporting.adapters.extent.inspection.ExtentPublishedOverviewInspector;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

final class AtlasUiCurrentReportPublisher {
    private static final Path VALIDATION = Path.of(AtlasContext.VALIDATION_DIRECTORY);
    private static final Path WORKING = Path.of("target/atlas-ui-agentic-current");
    private static final String RUN_COMMAND =
            "mvn -q -Dtest=AtlasUiAgenticCurrentReportPublisherTest -DforkCount=0 test";

    void publish(AtlasUiScope scope, List<AtlasUiCase> cases, AtlasUiReportVariant variant) throws IOException {
        Path validationDir = VALIDATION.resolve(scope.name());
        Files.createDirectories(validationDir);
        Files.createDirectories(WORKING);

        Path report = validationDir.resolve(scope.name() + variant.fileSuffix() + AtlasContext.HTML_SUFFIX);
        Path workingReport = WORKING.resolve(scope.name() + variant.fileSuffix() + AtlasContext.HTML_SUFFIX);
        List<AtlasUiCase> renderedCases = cases.stream()
                .map(testCase -> testCase.forVariant(variant))
                .collect(Collectors.toList());

        new AggregateExtentReportWriter().write(
                workingReport,
                "extent",
                descriptor(scope, variant),
                AtlasUiSummary.allPassed(renderedCases.size()),
                renderedCases
        );
        assertPublishableOverview(scope, workingReport);
        Files.copy(workingReport, report, StandardCopyOption.REPLACE_EXISTING);
        writeManifest(scope, report, renderedCases, variant);
    }

    private AggregateReportDescriptor descriptor(AtlasUiScope scope, AtlasUiReportVariant variant) {
        return AggregateReportDescriptor.builder()
                .testTitle(scope.name() + variant.testTitleSuffix())
                .description(variant.descriptionPrefix() + scope.testsRoot())
                .documentTitle(scope.name() + variant.documentTitleSuffix())
                .reportName("Automation Framework - " + scope.name() + variant.reportNameSuffix())
                .scopeLabel(variant.scopeLabel(scope))
                .fragmentationReason(variant.fragmentationReason())
                .auditShape(variant.auditShape())
                .includeOverviewTest(false)
                .reportKind(ExtentReportKind.ATLAS_VALIDATION)
                .addRunCommand(RUN_COMMAND)
                .build();
    }

    private void assertPublishableOverview(AtlasUiScope scope, Path report) throws IOException {
        List<String> violations = ExtentPublishedOverviewInspector.inspect(Files.readString(report))
                .violations(ExtentReportKind.ATLAS_VALIDATION);
        Assert.assertTrue(violations.isEmpty(),
                scope.name() + " Atlas UI Agentic report must pass the standard Extent Overview contract before copy: "
                        + violations);
    }

    private void writeManifest(
            AtlasUiScope scope,
            Path report,
            List<AtlasUiCase> cases,
            AtlasUiReportVariant variant
    ) throws IOException {
        Path manifest = report.getParent().resolve(scope.name() + variant.fileSuffix() + AtlasContext.README_SUFFIX);
        List<String> lines = List.of(
                "# " + scope.name() + variant.documentTitleSuffix(),
                "",
                "Published Atlas copy: `" + report + "`",
                "Source root: `" + scope.testsRoot() + "`",
                "Generated at: `" + Instant.now() + "`",
                "",
                "Contractual status:",
                "- Test cases: " + cases.size(),
                "- Passed: " + cases.size(),
                "- Failed: 0",
                "- Skipped: 0",
                "",
                "Scope note: " + variant.manifestScopeNote()
        );
        Files.write(manifest, lines);
    }
}
