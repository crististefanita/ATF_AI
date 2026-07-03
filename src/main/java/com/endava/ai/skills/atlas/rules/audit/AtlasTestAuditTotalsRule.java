package com.endava.ai.skills.atlas.rules.audit;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.rules.AtlasValidationRule;
import com.endava.ai.core.support.config.ConfigManager;
import com.endava.ai.core.support.config.ReportingConfigKeys;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class AtlasTestAuditTotalsRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.test-audit.total-synchronized";
    }

    @Override
    public String expected() {
        return "Atlas Test Audit declares test/failure totals that match the linked persisted Extent report before the atlas is published.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String extentLink = AtlasTestAuditSupport.actualExtentLink(context.atlasText());
        if (extentLink.isBlank()) {
            extentLink = AtlasTestAuditSupport.expectedExtentLinkFor(context);
        }
        Path extent = AtlasTestAuditSupport.extentPath(context, extentLink);
        AtlasTestAuditSupport.AuditTotals declared = AtlasTestAuditSupport.declaredTotals(context.atlasText());

        if (!declared.present()) {
            result.add(id(), "Atlas Test Audit does not declare expected test/failure totals",
                    context.atlasHtml(),
                    "Add a preliminary row like 'Result expected by this atlas = N tests / M failures' before publishing the atlas.");
            return;
        }
        if (!Files.exists(extent)) {
            return;
        }

        try {
            if (isActiveMavenOutput(extent)) {
                return;
            }
            if (!AtlasTestAuditSupport.mentionsCurrentAtlas(extent, context.atlasHtml())) {
                result.add(id(),
                        "Atlas Test Audit links a report generated for another atlas scope",
                        extent,
                        "Rerun the exact Atlas contract suite for "
                                + context.atlasHtml().getFileName()
                                + " and republish the scoped validation report.");
                return;
            }
            AtlasTestAuditSupport.AuditTotals actual = AtlasTestAuditSupport.extentTotals(extent);
            if (declared.testCount() != actual.testCount()
                    || declared.failureCount() != actual.failureCount()) {
                result.add(id(),
                        "Atlas Test Audit result is stale: declared "
                                + declared.testCount() + " tests / " + declared.failureCount()
                                + " failures, linked Extent has " + actual.testCount()
                                + " tests / " + actual.failureCount() + " failures",
                        context.atlasHtml(),
                        "Rerun the exact Atlas contract suite, copy its latest Extent report to "
                                + extentLink
                                + ", then update the Atlas Test Audit count from that report only.");
            }
            if (context.atlasHtml().getFileName().toString().endsWith(AtlasContext.BUSINESS_ATLAS_SUFFIX)
                    && AtlasTestAuditSupport.looksLikeFrameworkAtlasReport(extent)) {
                result.add(id(),
                        "Business atlas links a framework-atlas validation report",
                        extent,
                        "Publish a business-atlas Extent report for the Atlas*Test suite; do not reuse FrameworkAtlasOutputContractTest evidence.");
            }
        } catch (IOException e) {
            result.add(id(), "Cannot read linked atlas validation Extent report: " + extent,
                    extent,
                    "Ensure the linked Extent report is readable before declaring the Atlas Test Audit count.");
        }
    }

    private static boolean isActiveMavenOutput(Path extent) {
        String reportsDir = ConfigManager.get(ReportingConfigKeys.REPORTS_DIR, "");
        String reportName = ConfigManager.get(ReportingConfigKeys.REPORTS_FILE_NAME, "");
        if (reportsDir.isBlank() || reportName.isBlank()) {
            return false;
        }
        Path activeOutput = Path.of(reportsDir).resolve(reportName).toAbsolutePath().normalize();
        return activeOutput.equals(extent.toAbsolutePath().normalize());
    }
}
