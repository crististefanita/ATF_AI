package com.ai.core.reporting.adapters.extent.spark;

import com.ai.core.reporting.adapters.extent.mode.ExtentReportMode;
import com.ai.core.reporting.adapters.extent.mode.ExtentReportModeDetector;
import com.ai.core.reporting.adapters.extent.patch.component.ExtentComponentOverviewPatchPlanner;
import com.ai.core.reporting.adapters.extent.patch.component.ExtentComponentStatusGroupPatchPlanner;
import com.ai.core.reporting.adapters.extent.patch.navigation.ExtentCollapseScriptPatchPlanner;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlDocument;
import com.ai.core.reporting.adapters.extent.patch.normalize.ExtentHtmlNormalizer;
import com.ai.core.reporting.adapters.extent.patch.apply.ExtentHtmlPatchApplier;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchPlan;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchRecord;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchResult;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchStage;
import com.ai.core.reporting.adapters.extent.patch.navigation.ExtentNavigationPatchPlanner;
import com.ai.core.reporting.adapters.extent.patch.mode.ExtentReportModePatchPlanner;
import com.ai.core.reporting.adapters.extent.patch.status.ExtentStatusDropdownPatchPlanner;
import com.ai.core.reporting.adapters.extent.overview.ExtentOverviewTopCards;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class ExtentSparkHtmlPostProcessor {
    private ExtentSparkHtmlPostProcessor() {
    }

    public static void normalizeReport(Path reportPath) throws IOException {
        if (reportPath == null || !Files.isRegularFile(reportPath)) {
            return;
        }
        String html = Files.readString(reportPath, StandardCharsets.UTF_8);
        writeAtomically(reportPath, normalize(html));
    }

    public static String normalize(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        return normalizeWithResult(html).html();
    }

    public static ExtentHtmlPatchResult normalizeWithResult(String html) {
        ExtentHtmlDocument normalized = ExtentHtmlNormalizer.normalize(ExtentHtmlDocument.of(html));
        ExtentReportMode mode = ExtentReportModeDetector.detect(normalized);
        ExtentHtmlPatchResult shellResult = applyStage(normalized, shellStage(mode));
        ExtentHtmlDocument shell = ExtentHtmlDocument.of(shellResult.html());
        ExtentHtmlPatchResult collapseResult = applyStage(shell, collapseInvariantStage());
        ArrayList<ExtentHtmlPatchRecord> records = new ArrayList<>(shellResult.records());
        records.addAll(collapseResult.records());
        String topCardAligned = ExtentOverviewTopCards.alignWithExistingTiming(collapseResult.html());
        return new ExtentHtmlPatchResult(stripTrailingHorizontalWhitespace(topCardAligned), records);
    }

    private static String stripTrailingHorizontalWhitespace(String html) {
        return html.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("(?m)[ \\t]+(?=\\n|$)", "");
    }

    private static ExtentHtmlPatchResult applyStage(ExtentHtmlDocument document, ExtentHtmlPatchStage stage) {
        return ExtentHtmlPatchApplier.apply(document, stage.plans(document));
    }

    private static ExtentHtmlPatchStage shellStage(ExtentReportMode mode) {
        return ExtentHtmlPatchStage.named("extent.shell", document -> List.of(
                navigationPlan(document),
                ExtentStatusDropdownPatchPlanner.plan(document),
                ExtentComponentStatusGroupPatchPlanner.plan(document, mode),
                ExtentComponentOverviewPatchPlanner.plan(document, mode),
                ExtentReportModePatchPlanner.plan(document, mode)
        ));
    }

    private static ExtentHtmlPatchStage collapseInvariantStage() {
        return ExtentHtmlPatchStage.named("extent.final-collapse-invariant", document -> List.of(
                ExtentCollapseScriptPatchPlanner.plan(document)
        ));
    }

    private static ExtentHtmlPatchPlan navigationPlan(ExtentHtmlDocument document) {
        return ExtentNavigationPatchPlanner.plan(document);
    }

    private static void writeAtomically(Path reportPath, String html) throws IOException {
        Path parent = reportPath.toAbsolutePath().normalize().getParent();
        Path temp = Files.createTempFile(parent, reportPath.getFileName().toString(), ".tmp");
        try {
            Files.writeString(temp, html, StandardCharsets.UTF_8);
            try {
                Files.move(temp, reportPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveFailure) {
                Files.move(temp, reportPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
