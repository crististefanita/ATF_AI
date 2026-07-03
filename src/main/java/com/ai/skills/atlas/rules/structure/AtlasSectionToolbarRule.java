package com.ai.skills.atlas.rules.structure;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.contract.yaml.AtlasYamlGraphReportingCatalog;
import com.ai.skills.atlas.contract.yaml.AtlasYamlGraphReportingContract;
import com.ai.skills.atlas.rules.AtlasValidationRule;

public final class AtlasSectionToolbarRule implements AtlasValidationRule {
    private static final AtlasYamlGraphReportingContract CONTRACT =
            AtlasYamlGraphReportingCatalog.require("atlas.candidate.sections.toolbar");

    @Override
    public String id() {
        return CONTRACT.id();
    }

    @Override
    public String expected() {
        return "Candidate atlas reports expose a small section toolbar with "
                + CONTRACT.openLabel() + " / " + CONTRACT.closeLabel()
                + " controls backed by " + CONTRACT.functionName() + "(open).";
    }

    @Override
    public void validate(AtlasContext context, AtlasValidationResult result) {
        String html = context.atlasText();
        String lower = context.lowerAtlasText();
        require(context, result, hasClassToken(lower, CONTRACT.toolbarClass()),
                "Atlas report is missing the section toolbar container",
                "Add <div class=\"" + CONTRACT.toolbarClass() + "\" aria-label=\""
                        + CONTRACT.toolbarAriaLabel() + "\"> before the score wall.");
        require(context, result, hasAttribute(lower, "aria-label", CONTRACT.toolbarAriaLabel()),
                "Atlas toolbar is missing the expected accessibility label",
                "Use aria-label=\"" + CONTRACT.toolbarAriaLabel()
                        + "\" so the report controls are explicit and reusable.");
        require(context, result, html.contains(CONTRACT.openLabel()),
                "Atlas toolbar is missing " + CONTRACT.openLabel(),
                "Add the open-all button with the same visible label as the canonical atlas report.");
        require(context, result, html.contains(CONTRACT.closeLabel()),
                "Atlas toolbar is missing " + CONTRACT.closeLabel(),
                "Add the close-all button with the same visible label as the canonical atlas report.");
        require(context, result, hasAttribute(lower, "onclick", CONTRACT.openAction()),
                "Atlas toolbar open button is not wired to toggleAll(true)",
                "Wire " + CONTRACT.openLabel() + " to onclick=\"" + CONTRACT.openAction() + "\".");
        require(context, result, hasAttribute(lower, "onclick", CONTRACT.closeAction()),
                "Atlas toolbar close button is not wired to toggleAll(false)",
                "Wire " + CONTRACT.closeLabel() + " to onclick=\"" + CONTRACT.closeAction() + "\".");
        require(context, result, lower.contains("function " + CONTRACT.functionName().toLowerCase() + "(open)")
                        && lower.contains("queryselectorall")
                        && lower.contains(".foreach"),
                "Atlas report does not define the toggleAll(open) section control",
                "Define " + CONTRACT.functionName() + "(open) at the end of the report and update details elements consistently.");
    }

    private static void require(AtlasContext context,
                                AtlasValidationResult result,
                                boolean condition,
                                String message,
                                String repair) {
        if (!condition) {
            result.add(CONTRACT.id(), message, context.atlasHtml(), repair);
        }
    }

    private static boolean hasClassToken(String lowerHtml, String className) {
        String lowerClass = className.toLowerCase();
        return lowerHtml.contains("class=\"" + lowerClass + "\"")
                || lowerHtml.contains("class='" + lowerClass + "'");
    }

    private static boolean hasAttribute(String lowerHtml, String attribute, String value) {
        String lowerValue = value.toLowerCase();
        return lowerHtml.contains(attribute + "=\"" + lowerValue + "\"")
                || lowerHtml.contains(attribute + "='" + lowerValue + "'");
    }
}
