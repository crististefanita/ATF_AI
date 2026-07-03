package com.ai.core.reporting.adapters.extent.patch.apply;

import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlDocument;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchOperation;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchPlan;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchRecord;
import com.ai.core.reporting.adapters.extent.patch.model.ExtentHtmlPatchResult;
import com.ai.core.reporting.adapters.extent.patch.model.PatchOutcome;

import java.util.ArrayList;
import java.util.List;

public final class ExtentHtmlPatchApplier {
    private ExtentHtmlPatchApplier() {
    }

    public static ExtentHtmlPatchResult apply(ExtentHtmlDocument document, List<ExtentHtmlPatchPlan> plans) {
        String html = document.html();
        List<ExtentHtmlPatchRecord> records = new ArrayList<>();
        for (ExtentHtmlPatchPlan plan : plans) {
            for (ExtentHtmlPatchOperation operation : plan.operations()) {
                if (!operation.marker().isBlank() && html.contains(operation.marker())) {
                    records.add(new ExtentHtmlPatchRecord(
                            operation.id(),
                            operation.marker(),
                            PatchOutcome.SKIPPED_ALREADY_PRESENT,
                            "Patch marker already present"
                    ));
                    continue;
                }
                String updated = operation.apply(html);
                PatchOutcome outcome = updated.equals(html)
                        ? PatchOutcome.SKIPPED_NOT_APPLICABLE
                        : PatchOutcome.APPLIED;
                records.add(new ExtentHtmlPatchRecord(
                        operation.id(),
                        operation.marker(),
                        outcome,
                        outcome == PatchOutcome.APPLIED ? "Patch applied" : "Patch did not apply to this document"
                ));
                html = updated;
            }
        }
        return new ExtentHtmlPatchResult(html, records);
    }
}
