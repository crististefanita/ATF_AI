package com.endava.ai.skills.atlas.rules.audit;

import com.endava.ai.skills.atlas.rules.AtlasValidationRule;

import com.endava.ai.skills.atlas.AtlasContext;
import com.endava.ai.skills.atlas.AtlasValidationResult;
import com.endava.ai.skills.atlas.contract.AtlasContractArtifacts;

import java.nio.file.Files;
import java.nio.file.Path;

public final class AtlasTestAuditExtentRule implements AtlasValidationRule {
    private static final String AUDIT_SECTION = "atlas test audit";

    @Override
    public String id() {
        return "atlas.candidate.test-audit.extent-linked";
    }

    @Override
    public String expected() {
        return "Candidate atlas links a persisted Extent report for the Atlas*Test validation run.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String expectedExtentLink = AtlasTestAuditSupport.expectedExtentLinkFor(context);
        String actualExtentLink = AtlasTestAuditSupport.actualExtentLink(context.atlasText());
        String extentLink = actualExtentLink.isBlank() ? expectedExtentLink : actualExtentLink;
        String extentPath = AtlasContractArtifacts.ATLAS_DIRECTORY + "/" + extentLink;
        Path extent = AtlasTestAuditSupport.extentPath(context, extentLink);
        String html = context.lowerAtlasText();
        if (!Files.exists(extent)) {
            result.add(id(), "Missing persisted atlas test Extent report", extent,
                    "Copy the latest Atlas*Test Extent report to " + extentPath + " after running the atlas validation suite.");
        }
        if (!html.contains(AUDIT_SECTION)) {
            result.add(id(), "Atlas Test Audit section is missing", context.atlasHtml(),
                    "Add a collapsed Atlas Test Audit section with command, result and audit artifact link.");
        }
        if (actualExtentLink.isBlank()) {
            result.add(id(), "Atlas does not link the persisted atlas test Extent report", context.atlasHtml(),
                    "Link the audit artifact as " + expectedExtentLink + " so the validation proof is reopenable.");
        }
        if (!actualExtentLink.isBlank() && !actualExtentLink.equals(expectedExtentLink)) {
            result.add(id(), "Atlas links a non-scoped validation Extent report: " + actualExtentLink,
                    context.atlasHtml(),
                    "Use the scoped artifact " + expectedExtentLink + " so one atlas cannot accidentally reuse another atlas validation report.");
        }
    }
}
