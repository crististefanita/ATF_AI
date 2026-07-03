package com.endava.ai.core.reporting.htmlcontract.validation;

import com.endava.ai.core.reporting.htmlcontract.model.*;

import com.endava.ai.core.reporting.html.HtmlClassMatcher;

import java.util.List;
import java.util.Locale;

final class HtmlVisualShellValidator {

    private HtmlVisualShellValidator() {
    }

    static void validate(String html,
                         HtmlReportContract contract,
                         List<HtmlReportShellViolation> violations) {
        for (String className : contract.requiredShellClasses()) {
            HtmlReportValidationSupport.require(HtmlClassMatcher.hasClass(html, className),
                    visualShellCode(className),
                    "Report must use the CSS-backed shared visual shell class: " + className + ".",
                    violations);
        }
        HtmlReportValidationSupport.require(html.toLowerCase(Locale.ROOT).contains("<header"),
                "report.visual-shell.header", "Report must publish a header.", violations);
        boolean hasScoreWall = HtmlClassMatcher.hasClass(html, "score-wall")
                && HtmlClassMatcher.countClass(html, "score-card") >= 3;
        HtmlReportValidationSupport.require(hasScoreWall,
                "report.visual-shell.score-wall",
                "Report must publish a visible score-wall with score-card entries.",
                violations);
    }

    private static String visualShellCode(String className) {
        if ("wrap".equals(className)) {
            return "report.visual-shell.container";
        }
        return "report.visual-shell." + className;
    }
}
