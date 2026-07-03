package com.ai.atf.core.reporting.aggregate;

import com.ai.atf.core.reporting.aggregate.descriptor.AtfCoreAggregateReportDescriptor;
import com.ai.atf.core.reporting.aggregate.publish.AtfCoreAggregateReportPublisher;
import com.ai.atf.core.reporting.aggregate.runtime.AtfCoreAggregateRuntimeLedger;
import com.ai.atf.core.reporting.aggregate.suite.AtfCoreAggregateCaseCollector;
import com.ai.atf.core.reporting.config.AtfCoreAggregateConfig;
import com.ai.atf.core.reporting.config.AtfCoreAggregateEngine;
import com.ai.atf.core.reporting.model.AtfCoreAggregateCase;
import com.ai.core.support.config.ConfigManager;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry.RuntimeAuditInvocation;
import com.ai.core.reporting.adapters.ExtentAdapter;
import com.ai.core.reporting.aggregate.AggregateExtentReportWriter;
import com.ai.core.reporting.aggregate.AggregateReportDescriptor;
import com.ai.core.reporting.aggregate.artifact.RuntimeCoverageAuditWriter;
import com.ai.core.reporting.aggregate.internal.AggregateRuntimeAuditMapper;
import com.ai.core.reporting.aggregate.internal.ExtentFinalAuditHtml;
import com.ai.core.reporting.aggregate.internal.ExtentHtmlAuditAppender;
import com.ai.core.reporting.aggregate.model.AggregateSummary;
import com.ai.core.reporting.aggregate.model.AggregateRuntimeAuditSnapshot;
import com.ai.core.reporting.aggregate.model.AggregateRuntimeCase;
import com.ai.core.reporting.aggregate.model.AggregateRuntimeSummary;
import com.ai.core.reporting.aggregate.model.AggregateTimingSummary;
import io.qameta.allure.model.Status;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AtfCoreAggregateReportListener implements IReporter, ISuiteListener, ITestListener {

    private static final String RUNTIME_EXTENT_AUDIT_KEY = "atf.core.extent.coverage.audit.enabled";
    private static final String EXTENT_HTML_AUDIT_KEY = "atf.core.extent.html.audit.enabled";
    private static final AggregateReportDescriptor ATF_CORE_DESCRIPTOR = AtfCoreAggregateReportDescriptor.create();

    private static volatile boolean runtimeExtentAuditWritten;
    private static volatile boolean htmlExtentAuditWritten;
    private static final List<AggregateRuntimeCase> RUNTIME_CASES = Collections.synchronizedList(new ArrayList<>());
    private static final List<AtfCoreAggregateCase> RUNTIME_AGGREGATE_CASES =
            Collections.synchronizedList(new ArrayList<>());
    private static final List<AtfCoreAggregateCase> PENDING_HTML_AUDIT_CASES =
            Collections.synchronizedList(new ArrayList<>());

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (htmlExtentAuditWritten) {
                return;
            }
            if (!PENDING_HTML_AUDIT_CASES.isEmpty()) {
                ExtentAdapter.getInstance().flush();
                ExtentRuntimeAuditRegistry.publishIfExtent();
                List<AtfCoreAggregateCase> cases;
                synchronized (PENDING_HTML_AUDIT_CASES) {
                    cases = new ArrayList<>(PENDING_HTML_AUDIT_CASES);
                }
                appendRuntimeAuditToExtentHtml(AggregateSummary.from(cases), cases);
            } else if (!RUNTIME_CASES.isEmpty()) {
                ExtentAdapter.getInstance().flush();
                ExtentRuntimeAuditRegistry.publishIfExtent();
                List<AggregateRuntimeCase> cases;
                synchronized (RUNTIME_CASES) {
                    cases = new ArrayList<>(RUNTIME_CASES);
                }
                appendRuntimeAuditToExtentHtml(AggregateRuntimeSummary.from(cases), cases);
            }
        }));
    }

    @Override
    public void onStart(ISuite suite) {
        if (System.getProperty(ReportingConfigKeys.EXTENT_MIRROR_ENABLED) == null) {
            System.setProperty(ReportingConfigKeys.EXTENT_MIRROR_ENABLED, "true");
        }
        RUNTIME_CASES.clear();
        RUNTIME_AGGREGATE_CASES.clear();
        PENDING_HTML_AUDIT_CASES.clear();
        htmlExtentAuditWritten = false;
    }

    @Override
    public void onFinish(ISuite suite) {
        writeRuntimeExtentCoverageAuditOnce(List.of(suite));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        recordRuntimeCase(result, Status.PASSED);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        recordRuntimeCase(result, Status.FAILED);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        recordRuntimeCase(result, Status.SKIPPED);
    }

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        List<AtfCoreAggregateCase> cases = collectCases(suites);
        AggregateSummary summary = AggregateSummary.from(cases);

        synchronized (PENDING_HTML_AUDIT_CASES) {
            PENDING_HTML_AUDIT_CASES.clear();
            PENDING_HTML_AUDIT_CASES.addAll(cases);
        }
        ExtentRuntimeAuditRegistry.publishIfExtent();
        appendRuntimeAuditToExtentHtml(summary, cases);
        writeRuntimeExtentCoverageAuditOnce(suites);

        if (!AtfCoreAggregateConfig.enabled()) {
            return;
        }

        AtfCoreAggregateConfig config = AtfCoreAggregateConfig.fromSystemProperties();
        if (config.engine() == AtfCoreAggregateEngine.NONE) {
            return;
        }

        AtfCoreAggregateReportPublisher.publish(config, ATF_CORE_DESCRIPTOR, summary, cases);
    }

    private static void appendRuntimeAuditToExtentHtml(AggregateSummary summary, List<AtfCoreAggregateCase> cases) {
        if (!canAppendExtentHtmlAudit()) {
            return;
        }

        AggregateRuntimeSummary runtimeSummary = runtimeSummaryOrFallback(summary);
        AggregateTimingSummary timing = AggregateTimingSummary.from(cases);
        appendToLatestExtentHtml(
                AtfCoreAggregateFinalAuditSections.runtimeOrFallback(
                        ATF_CORE_DESCRIPTOR,
                        runtimeSummary,
                        runtimeCasesSnapshot(),
                        summary,
                        cases
                ),
                html -> ExtentFinalAuditHtml.reconcileSparkTimingCards(
                        reconcileSparkTopCards(html, runtimeSummary.passed(), runtimeSummary.failed()),
                        timing
                ),
                runtimeInvocationsForHtmlAudit(cases)
        );
    }

    private static void appendRuntimeAuditToExtentHtml(AggregateRuntimeSummary summary, List<AggregateRuntimeCase> cases) {
        if (!canAppendExtentHtmlAudit()) {
            return;
        }
        appendToLatestExtentHtml(
                AtfCoreAggregateFinalAuditSections.runtime(ATF_CORE_DESCRIPTOR, summary, cases),
                html -> html,
                runtimeInvocations(cases)
        );
    }

    private static boolean canAppendExtentHtmlAudit() {
        return Boolean.parseBoolean(System.getProperty(EXTENT_HTML_AUDIT_KEY, "true"))
                && ReportingConfigKeys.ENGINE_EXTENT.equalsIgnoreCase(
                        ConfigManager.get(ReportingConfigKeys.REPORTING_ENGINE, ""));
    }

    private static void appendToLatestExtentHtml(
            String section,
            java.util.function.UnaryOperator<String> htmlPreprocessor,
            List<RuntimeAuditInvocation> runtimeInvocations
    ) {
        if (ExtentHtmlAuditAppender.appendToLatest(section, htmlPreprocessor, runtimeInvocations)) {
            htmlExtentAuditWritten = true;
        }
    }

    private static List<RuntimeAuditInvocation> runtimeInvocations(List<AggregateRuntimeCase> cases) {
        return AtfCoreAggregateRuntimeLedger.runtimeInvocations(cases);
    }

    private static List<RuntimeAuditInvocation> runtimeInvocationsForHtmlAudit(List<AtfCoreAggregateCase> cases) {
        List<RuntimeAuditInvocation> aggregateInvocations = AggregateRuntimeAuditMapper.from(cases);
        if (!aggregateInvocations.isEmpty()) {
            return aggregateInvocations;
        }
        return runtimeInvocations(runtimeCasesSnapshot());
    }

    static String reconcileSparkTopCards(String html, long passed, long failed) {
        return ExtentFinalAuditHtml.reconcileSparkTopCards(html, passed, failed);
    }

    private static AggregateRuntimeSummary runtimeSummaryOrFallback(AggregateSummary fallback) {
        List<AggregateRuntimeCase> cases = runtimeCasesSnapshot();
        if (!cases.isEmpty()) {
            return AggregateRuntimeSummary.from(cases);
        }
        return AggregateRuntimeSummary.fromAggregate(fallback);
    }

    private static Path latestExtentReport() {
        return ExtentHtmlAuditAppender.latestExtentReport();
    }

    private static List<AggregateRuntimeCase> runtimeCasesSnapshot() {
        return AtfCoreAggregateRuntimeLedger.snapshotCases(RUNTIME_CASES);
    }

    private static synchronized void writeRuntimeExtentCoverageAuditOnce(List<ISuite> suites) {
        if (runtimeExtentAuditWritten) {
            return;
        }
        if (!Boolean.parseBoolean(System.getProperty(RUNTIME_EXTENT_AUDIT_KEY, "false"))) {
            return;
        }
        List<AtfCoreAggregateCase> cases = collectCases(suites);
        RuntimeCoverageAuditWriter.write(ATF_CORE_DESCRIPTOR, AggregateSummary.from(cases), cases);
        runtimeExtentAuditWritten = true;
    }

    public static AggregateRuntimeAuditSnapshot runtimeAuditSnapshotIncludingCurrentAudit(
            String currentClassName,
            String currentMethodName
    ) {
        return AtfCoreAggregateRuntimeLedger.snapshotIncludingCurrentAudit(
                RUNTIME_CASES,
                currentClassName,
                currentMethodName
        );
    }

    public static AggregateRuntimeAuditSnapshot runtimeAuditSnapshot() {
        return AtfCoreAggregateRuntimeLedger.snapshot(RUNTIME_CASES);
    }

    private static void recordRuntimeCase(ITestResult result, Status status) {
        AtfCoreAggregateRuntimeLedger.record(RUNTIME_CASES, RUNTIME_AGGREGATE_CASES, result, status);
    }

    private static List<AtfCoreAggregateCase> collectCases(List<ISuite> suites) {
        return AtfCoreAggregateCaseCollector.collect(suites, RUNTIME_AGGREGATE_CASES);
    }

    private static void writeExtentReport(Path report, AggregateSummary summary, List<AtfCoreAggregateCase> cases) {
        AtfCoreAggregateReportPublisher.writeExtentReport(
                report,
                AtfCoreAggregateConfig.engineName(),
                ATF_CORE_DESCRIPTOR,
                summary,
                cases
        );
    }

}
