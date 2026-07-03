package com.ai.core.reporting.htmlcontract.validation;

import com.ai.core.reporting.htmlcontract.model.*;

import com.ai.core.reporting.html.HtmlClassMatcher;

import java.util.List;
import java.util.regex.Pattern;

final class HtmlGraphPrimitiveValidator {
    private static final Pattern VISIBLE_NODE_SHAPE = Pattern.compile(
            "(?is)<(?:rect|circle|polygon)\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\bnode\\b[^\"']*[\"']");
    private static final Pattern WRAPPER_NODE_CLASS = Pattern.compile(
            "(?is)<g\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\bnode\\b[^\"']*[\"']");

    private HtmlGraphPrimitiveValidator() {
    }

    static void validate(String html,
                         HtmlReportContract contract,
                         List<HtmlReportShellViolation> violations) {
        if (!HtmlClassMatcher.hasClass(html, "graph-panel") && !HtmlClassMatcher.hasClass(html, "diag")) {
            return;
        }
        for (String className : contract.requiredGraphClasses()) {
            HtmlReportValidationSupport.require(HtmlClassMatcher.hasClass(html, className),
                    "report.graph." + className,
                    "Report graphs must use shared graph primitive class: " + className + ".",
                    violations);
        }
        HtmlReportValidationSupport.require(visibleNodeShape(html, contract.visibleNodeShapeTags()),
                "report.graph.node-shape",
                "Report graph node classes must be attached to visible rect/circle/polygon shapes.",
                violations);
        HtmlReportValidationSupport.require(!forbiddenGraphWrapper(html, contract.forbiddenGraphWrapperTags()),
                "report.graph.node-wrapper",
                "Do not put class=\"node\" on <g> wrappers; shared CSS paints visible SVG shapes such as rect.node.",
                violations);
    }

    private static boolean visibleNodeShape(String html, List<String> tags) {
        if (tags.isEmpty()) {
            return VISIBLE_NODE_SHAPE.matcher(html).find();
        }
        for (String tag : tags) {
            Pattern pattern = Pattern.compile("(?is)<" + Pattern.quote(tag)
                    + "\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\bnode\\b[^\"']*[\"']");
            if (pattern.matcher(html).find()) {
                return true;
            }
        }
        return false;
    }

    private static boolean forbiddenGraphWrapper(String html, List<String> wrappers) {
        if (wrappers.isEmpty()) {
            return WRAPPER_NODE_CLASS.matcher(html).find();
        }
        for (String wrapper : wrappers) {
            String[] parts = wrapper.split("\\.", 2);
            if (parts.length != 2) {
                continue;
            }
            Pattern pattern = Pattern.compile("(?is)<" + Pattern.quote(parts[0])
                    + "\\b[^>]*class\\s*=\\s*[\"'][^\"']*\\b" + Pattern.quote(parts[1])
                    + "\\b[^\"']*[\"']");
            if (pattern.matcher(html).find()) {
                return true;
            }
        }
        return false;
    }
}
