package com.ai.skills.atlas.rules.structure;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.rules.AtlasValidationRule;
import com.ai.core.reporting.htmlcontract.model.HtmlReportShellProfile;
import com.ai.core.reporting.htmlcontract.validation.HtmlReportShellValidator;

public final class AtlasSharedReportShellRule implements AtlasValidationRule {
    @Override
    public String id() {
        return "atlas.candidate.report-shell.shared";
    }

    @Override
    public String expected() {
        return "Atlas final reports reuse the shared Core report shell while keeping Atlas-owned sections: business flow understanding, evidence tables, source trace and Atlas test audit.";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        new HtmlReportShellValidator().validate(context.atlasText(), HtmlReportShellProfile.ATLAS_FINAL_REPORT)
                .forEach(violation -> result.add(id() + "." + violation.code(),
                        violation.message(),
                        context.atlasHtml(),
                        "Reuse docs/prompts/html_ro/shared/report-shell.html. Keep Atlas-specific comprehension in Atlas sections: Business Flow Understanding, Business Evidence Tables, Source-to-Business Trace and Atlas Test Audit."));
    }
}
