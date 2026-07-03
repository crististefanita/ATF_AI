package com.ai.core.reporting.htmlcontract.validation;

import com.ai.core.reporting.htmlcontract.model.*;

import java.util.List;

final class HtmlReportValidationSupport {

    private HtmlReportValidationSupport() {
    }

    static void require(boolean condition,
                        String code,
                        String message,
                        List<HtmlReportShellViolation> violations) {
        if (!condition) {
            violations.add(HtmlReportShellViolation.of(code, message));
        }
    }
}
