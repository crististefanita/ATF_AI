package com.endava.ai.core.reporting.htmlcontract.validation;

import com.endava.ai.core.reporting.htmlcontract.model.*;

import java.util.ArrayList;
import java.util.List;

public final class HtmlReportShellValidator {

    public List<HtmlReportShellViolation> validate(String html, HtmlReportShellProfile profile) {
        return validateContract(html, HtmlReportContract.forProfile(profile)).violations();
    }

    public HtmlReportContractResult validateContract(String html, HtmlReportContract contract) {
        List<HtmlReportShellViolation> violations = new ArrayList<>();
        HtmlDocumentChromeValidator.validate(html, contract, violations);
        HtmlVisualShellValidator.validate(html, contract, violations);
        HtmlGraphPrimitiveValidator.validate(html, contract, violations);
        HtmlCanonicalSectionValidator.validate(html, contract, violations);
        HtmlScoreOverviewTableValidator.validate(html, contract, violations);
        return HtmlReportContractResult.of(violations);
    }
}
