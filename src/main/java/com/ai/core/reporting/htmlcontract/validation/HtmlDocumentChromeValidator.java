package com.ai.core.reporting.htmlcontract.validation;

import com.ai.core.reporting.htmlcontract.model.*;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

final class HtmlDocumentChromeValidator {

    private HtmlDocumentChromeValidator() {
    }

    static void validate(String html,
                         HtmlReportContract contract,
                         List<HtmlReportShellViolation> violations) {
        String lower = html.toLowerCase(Locale.ROOT);
        HtmlReportValidationSupport.require(lower.contains("<!doctype html"),
                "report.doctype", "Report is missing <!doctype html>.", violations);
        HtmlReportValidationSupport.require(lower.contains("<meta charset=\"utf-8\"")
                        || lower.contains("<meta charset='utf-8'"),
                "report.charset", "Report is missing UTF-8 charset metadata.", violations);
        HtmlReportValidationSupport.require(lower.contains("name=\"viewport\"")
                        || lower.contains("name='viewport'"),
                "report.viewport", "Report is missing responsive viewport metadata.", violations);
        HtmlReportValidationSupport.require(lower.contains("<title>"),
                "report.title", "Report is missing an HTML title.", violations);
        HtmlReportValidationSupport.require(html.contains("shared-report-reference.css"),
                "report.shared-css", "Report must link shared-report-reference.css.", violations);
        requireLanguage(lower, contract, violations);
    }

    private static void requireLanguage(String lower,
                                        HtmlReportContract contract,
                                        List<HtmlReportShellViolation> violations) {
        String expectedLanguage = contract.expectedLanguage().toLowerCase(Locale.ROOT);
        if (expectedLanguage.isBlank() || "any".equals(expectedLanguage)) {
            return;
        }
        HtmlReportValidationSupport.require(lower.matches("(?s).*<html\\b[^>]*\\blang\\s*=\\s*[\"']"
                        + Pattern.quote(expectedLanguage) + "[\"'][^>]*>.*"),
                "report.language",
                "Report profile " + contract.profile() + " must declare html lang=\"" + expectedLanguage
                        + "\" from YAML contract " + contract.id() + ".",
                violations);
    }
}
