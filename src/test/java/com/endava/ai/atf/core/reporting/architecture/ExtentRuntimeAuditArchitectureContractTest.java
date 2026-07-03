package com.endava.ai.atf.core.reporting.architecture;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_RUNTIME_AUDIT_ARCHITECTURE_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_RUNTIME_AUDIT_ARCHITECTURE_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class ExtentRuntimeAuditArchitectureContractTest {

    private static final Path REGISTRY = coreSource("reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry");
    private static final Path PUBLISHER = coreSource("reporting.listener.internal.runtime.publish.ExtentRuntimeAuditPublisher");
    private static final Path LOCATOR = coreSource("reporting.listener.internal.runtime.audit.ExtentRuntimeReportLocator");
    private static final Path TARGET = coreSource("reporting.listener.internal.runtime.audit.model.RuntimeAuditTarget");
    private static final Path PUBLISH_RESULT = coreSource("reporting.listener.internal.runtime.audit.model.RuntimeAuditPublishResult");
    private static final Path PUBLISH_OUTCOME = coreSource("reporting.listener.internal.runtime.audit.model.RuntimeAuditPublishOutcome");
    private static final Path COORDINATOR = coreSource("reporting.listener.internal.runtime.audit.RuntimeAuditPublisherCoordinator");
    private static final Path PATCH_PLAN = coreSource("reporting.listener.internal.runtime.audit.ExtentHtmlPatchPlan");
    private static final Path SECTION_REWRITER = coreSource("reporting.listener.internal.runtime.audit.MarkedHtmlSectionRewriter");

    @Test
    public void runtimeAuditRegistryDelegatesHtmlPublishingAndReportLocating() throws IOException {
        String registry = Files.readString(REGISTRY);
        String publisher = Files.readString(PUBLISHER);
        String locator = Files.readString(LOCATOR);
        String target = Files.readString(TARGET);
        String publishResult = Files.readString(PUBLISH_RESULT);
        String publishOutcome = Files.readString(PUBLISH_OUTCOME);
        String coordinator = Files.readString(COORDINATOR);
        String patchPlan = Files.readString(PATCH_PLAN);
        String rewriter = Files.readString(SECTION_REWRITER);

        Assert.assertTrue(registry.contains("RuntimeAuditPublisherCoordinator.publishCurrentReport"));
        Assert.assertTrue(registry.contains("RuntimeInvocationFilter.isReportable"));
        Assert.assertTrue(registry.contains("sortedInvocationSnapshot()"));
        Assert.assertFalse(registry.contains("RuntimeAuditSnapshot"),
                "Suite finish publishing must not depend on an extra snapshot helper class in the forked cleanup path.");
        Assert.assertTrue(registry.contains("currentReportPublished"),
                "Registry must guard against shutdown-hook double publishing after suite finish.");
        Assert.assertTrue(registry.contains("currentReportPublished = false;"),
                "Registry must reset the publish guard when a suite is cleared or a new invocation is recorded.");
        Assert.assertFalse(registry.contains("ExtentRuntimeAuditPublisher.writeAuditToReport"));
        Assert.assertFalse(registry.contains("ExtentRuntimeReportLocator.currentOrLatestReport"));
        Assert.assertFalse(registry.contains("Files.readString"));
        Assert.assertFalse(registry.contains("Files.writeString"));
        Assert.assertFalse(registry.contains("ExtentSparkHtmlPostProcessor"));
        Assert.assertFalse(registry.contains(ExtentMarkers.RUNTIME_FAILURE_GROUPS_START));

        Assert.assertTrue(coordinator.contains("ExtentRuntimeAuditPublisher.writeAuditToReport"));
        Assert.assertTrue(coordinator.contains("ExtentRuntimeReportLocator.locate"));
        Assert.assertTrue(coordinator.contains("RuntimeAuditTarget"));
        Assert.assertTrue(coordinator.contains("RuntimeAuditPublishResult"));
        Assert.assertTrue(coordinator.contains("target.writable()"));
        Assert.assertTrue(coordinator.contains("RuntimeAuditPublishResult publishCurrentReport"),
                "Current report publishing must return a typed result, not a silent boolean.");
        Assert.assertTrue(coordinator.contains("RuntimeAuditPublishResult.skipped"),
                "Missing current/latest report must be represented as a skipped typed result.");
        Assert.assertFalse(coordinator.contains("boolean publishCurrentReport"),
                "Runtime audit publishing must not collapse written/skipped/blocked/failed into boolean.");
        Assert.assertTrue(coordinator.contains("catch (Throwable"));
        Assert.assertTrue(coordinator.contains("RuntimeAuditFailureReporter.report"));
        Assert.assertTrue(publishResult.contains("armsPublishGuard"));
        Assert.assertTrue(publishResult.contains("failureClass"));
        Assert.assertTrue(publishResult.contains("failureMessage"));
        Assert.assertTrue(publishOutcome.contains("WRITTEN"));
        Assert.assertTrue(publishOutcome.contains("SKIPPED_NO_CANDIDATE"));
        Assert.assertTrue(publishOutcome.contains("SKIPPED_AMBIGUOUS_CANDIDATES"));
        Assert.assertTrue(publishOutcome.contains("BLOCKED_FROZEN_REPORT"));
        Assert.assertTrue(publishOutcome.contains("FAILED_WRITE"));
        Assert.assertTrue(publisher.contains("Files.readString"));
        Assert.assertTrue(publisher.contains("Files.writeString"));
        Assert.assertTrue(publisher.contains("ExtentHtmlPatchPlan.removeExistingRuntimeAudit"));
        Assert.assertTrue(publisher.contains("ExtentHtmlPatchPlan.replaceCategoryRuntimeAudit"));
        Assert.assertTrue(publisher.contains("ExtentSparkHtmlPostProcessor.normalize"));
        Assert.assertFalse(publisher.contains(ExtentMarkers.RUNTIME_FAILURE_GROUPS_START),
                "Marker policy must live in ExtentHtmlPatchPlan, not in the publisher I/O class.");
        Assert.assertTrue(locator.contains("ExtentReportFiles.isExtentHtmlReport"));
        Assert.assertTrue(locator.contains("RuntimeAuditTarget.current"));
        Assert.assertTrue(locator.contains("RuntimeAuditTarget.markerMatch"));
        Assert.assertTrue(locator.contains("RuntimeAuditTarget.ambiguousCandidates"));
        Assert.assertTrue(target.contains("enum Kind"));
        Assert.assertTrue(target.contains("CURRENT_REPORT"));
        Assert.assertTrue(target.contains("MARKER_MATCH"));
        Assert.assertTrue(target.contains("AMBIGUOUS_CANDIDATES"));
        Assert.assertTrue(patchPlan.contains("MarkedHtmlSectionRewriter.removeMarkedBlock"));
        Assert.assertTrue(patchPlan.contains("MarkedHtmlSectionRewriter.replaceCategoryViewContent"));
        Assert.assertTrue(rewriter.contains("removeMarkedBlock"));
        Assert.assertTrue(rewriter.contains("replaceCategoryViewContent"));
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_RUNTIME_AUDIT_ARCHITECTURE_CONTRACT_TEST_RUNTIME_REPORT_LOCATOR_DOES_NOT_REWRITE_AMBIGUOUS_LATEST_REPORT)
    public void runtimeReportLocatorDoesNotRewriteAmbiguousLatestReport() throws IOException {
        String locator = Files.readString(LOCATOR);

        Assert.assertTrue(locator.contains("No current Extent report or marker-bearing candidate was found"),
                "Ambiguous runtime audit target must be diagnosed.");
        Assert.assertFalse(locator.contains("orElseGet(() -> candidates.stream().findFirst()"),
                "Locator must not select the latest report when no invocation marker matches.");
    }

    private static Path coreSource(String classNameSuffix) {
        return AtfSourcePaths.mainJava(AtfSourcePaths.CORE_ROOT_PACKAGE + "." + classNameSuffix);
    }
}
