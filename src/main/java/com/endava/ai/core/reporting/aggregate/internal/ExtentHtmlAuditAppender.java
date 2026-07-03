package com.endava.ai.core.reporting.aggregate.internal;

import com.endava.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.endava.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry.RuntimeAuditInvocation;
import com.endava.ai.core.reporting.adapters.extent.spark.ExtentSparkReportFinalizer;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportKind;
import com.endava.ai.core.reporting.aggregate.artifact.AggregateReportFiles;
import com.endava.ai.core.reporting.adapters.extent.contract.ExtentReportSectionIds;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.UnaryOperator;

public final class ExtentHtmlAuditAppender {
    private static final String FINAL_AUDIT_MARKER = "id=\"" + ExtentReportSectionIds.AGGREGATE_FINAL_AUDIT + "\"";
    private static final String BODY_END = "</body>";

    private ExtentHtmlAuditAppender() {
    }

    public static Path latestExtentReport() {
        return AggregateReportFiles.latestExtentReport();
    }

    public static boolean appendToLatest(String section, UnaryOperator<String> htmlPreprocessor) {
        return appendToLatest(section, htmlPreprocessor, List.of());
    }

    public static boolean appendToLatest(
            String section,
            UnaryOperator<String> htmlPreprocessor,
            List<RuntimeAuditInvocation> runtimeInvocations
    ) {
        Path report = latestExtentReport();
        if (report == null || !Files.exists(report)) {
            return false;
        }

        try {
            String html = htmlPreprocessor.apply(Files.readString(report, StandardCharsets.UTF_8));
            String updated = html.contains(FINAL_AUDIT_MARKER)
                    ? html
                    : appendBeforeBodyEnd(html, section);
            Files.writeString(report, updated, StandardCharsets.UTF_8);
            if (runtimeInvocations == null || runtimeInvocations.isEmpty()) {
                ExtentRuntimeAuditRegistry.publishIfExtent();
            } else {
                ExtentRuntimeAuditRegistry.publishOrUpdateReportStrict(report, runtimeInvocations);
                ExtentSparkReportFinalizer.standardizeQuietly(report, ExtentReportKind.AGGREGATE_VALIDATION);
            }
            return true;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to append ATF-core final audit to Extent HTML: " + report, e);
        }
    }

    private static String appendBeforeBodyEnd(String html, String section) {
        int bodyEnd = html.lastIndexOf(BODY_END);
        if (bodyEnd < 0) {
            return html + section;
        }
        return html.substring(0, bodyEnd) + section + html.substring(bodyEnd);
    }
}
