package com.ai.core.reporting.listener.internal.runtime.publish;

import com.ai.core.reporting.listener.internal.runtime.render.*;

import com.ai.core.reporting.listener.internal.runtime.model.*;

import com.ai.core.reporting.listener.internal.runtime.audit.ExtentHtmlPatchPlan;
import com.ai.core.reporting.listener.internal.runtime.audit.RuntimeAuditWriteGuard;
import com.ai.core.reporting.listener.internal.runtime.audit.model.RuntimeAuditWriteRecord;
import com.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlDocument;
import com.ai.core.reporting.adapters.extent.patch.apply.ExtentHtmlPatchApplier;
import com.ai.core.reporting.adapters.extent.overview.ExtentOverviewTopCards;
import com.ai.core.reporting.adapters.extent.mode.ExtentReportMode;
import com.ai.core.reporting.adapters.extent.structure.ExtentHtmlStructure;
import com.ai.core.reporting.adapters.extent.spark.ExtentSparkHtmlPostProcessor;
import com.ai.core.reporting.adapters.extent.patch.mode.ExtentReportModePatchPlanner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;

public final class ExtentRuntimeAuditPublisher {
    private static final String PASS_CARD_LABEL =
            ExtentHtmlStructure.topCardLabel("pass", ExtentDashboardLabels.TESTS_PASSED);
    private static final String FAIL_CARD_LABEL =
            ExtentHtmlStructure.topCardLabel("fail", ExtentDashboardLabels.TESTS_FAILED);

    private ExtentRuntimeAuditPublisher() {
    }

    public static RuntimeAuditWriteRecord writeAuditToReport(Path report, List<ExtentRuntimeInvocation> invocations) throws IOException {
        return writeAuditToReport(report, invocations, false);
    }

    public static RuntimeAuditWriteRecord writeAuditToReport(
            Path report,
            List<ExtentRuntimeInvocation> invocations,
            boolean activeRuntimeReport
    ) throws IOException {
        RuntimeAuditWriteGuard.requireMutableRuntimeReport(report, activeRuntimeReport);
        String beforeSha256 = sha256(report);
        String html = Files.readString(report, StandardCharsets.UTF_8);
        html = ExtentHtmlPatchPlan.removeExistingRuntimeAudit(html);
        html = reconcileDashboard(html, invocations);
        String updated = ExtentHtmlPatchPlan.replaceCategoryRuntimeAudit(
                html,
                ExtentRuntimeFailureRenderer.section(ExtentRuntimeSummary.from(invocations))
        );
        updated = markRuntimeAudited(updated);
        updated = ExtentSparkHtmlPostProcessor.normalize(updated);
        writeAtomically(report, updated);
        return new RuntimeAuditWriteRecord(report, Instant.now(), beforeSha256, sha256(report));
    }

    private static String markRuntimeAudited(String html) {
        ExtentHtmlDocument document = ExtentHtmlDocument.of(html);
        return ExtentHtmlPatchApplier.apply(
                document,
                List.of(ExtentReportModePatchPlanner.plan(document, ExtentReportMode.runtimeAudited()))
        ).html();
    }

    private static String reconcileDashboard(String html, List<ExtentRuntimeInvocation> invocations) {
        ExtentRuntimeSummary summary = ExtentRuntimeSummary.from(invocations);
        DashboardSummary dashboard = DashboardSummary.from(summary);
        String patched = replaceFirst(
                html,
                PASS_CARD_LABEL + "\\s*<h3[^>]*>.*?</h3>",
                PASS_CARD_LABEL + System.lineSeparator()
                        + "<h3>" + dashboard.passed() + "</h3>"
        );
        patched = replaceFirst(
                patched,
                FAIL_CARD_LABEL + "\\s*<h3[^>]*>.*?</h3>",
                FAIL_CARD_LABEL + System.lineSeparator()
                        + "<h3>" + dashboard.failed() + "</h3>"
        );
        patched = ExtentRuntimeDashboardRenderer.replaceTestsDashboardRow(patched, summary);
        patched = ExtentOverviewTopCards.alignWithExistingTiming(patched);
        patched = ExtentRuntimeDashboardRenderer.replaceTimelineRow(patched, invocations);
        patched = ExtentRuntimeDashboardRenderer.replaceTagAuditRow(patched, summary);
        return ExtentRuntimeDashboardRenderer.replaceStatusGroupScript(patched, summary);
    }

    private static String replaceFirst(String html, String regex, String replacement) {
        return html.replaceFirst("(?s)" + regex, java.util.regex.Matcher.quoteReplacement(replacement));
    }

    private static String sha256(Path report) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Files.readAllBytes(report));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                hex.append(String.format("%02x", value & 0xff));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static void writeAtomically(Path report, String html) throws IOException {
        Path parent = report.toAbsolutePath().normalize().getParent();
        Path temp = Files.createTempFile(parent, report.getFileName().toString(), ".runtime-audit.tmp");
        try {
            Files.writeString(temp, html, StandardCharsets.UTF_8);
            try {
                Files.move(temp, report, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(temp, report, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
