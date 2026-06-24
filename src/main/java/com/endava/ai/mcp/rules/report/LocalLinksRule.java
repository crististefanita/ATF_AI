package com.endava.ai.mcp.rules.report;


import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.fs.LocalLinkResolver;
import com.endava.ai.mcp.html.HtmlLinkExtractor;

import java.nio.file.Files;

@com.endava.ai.mcp.contract.McpContract(id = "report.local-links.resolve",
        category = com.endava.ai.mcp.contract.McpRuleCategory.LINKS,
        htmlReq = "reporting.html",
        artifact = "analysis/langgraph-business-understanding.html")
public final class LocalLinksRule implements McpValidationRule {
    private final HtmlLinkExtractor links = new HtmlLinkExtractor();
    private final LocalLinkResolver resolver = new LocalLinkResolver();

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        var report = context.paths().report();
        if (!Files.isRegularFile(report)) {
            return;
        }

        String html = context.files().read(report, result);
        for (String href : links.hrefs(html)) {
            if (resolver.isExternalOrInline(href)) {
                continue;
            }

            String pathOnly = href.split("#", 2)[0];
            if (pathOnly.isBlank()) {
                continue;
            }

            var target = resolver.resolve(report.getParent(), pathOnly);
            if (!Files.exists(target)) {
                result.add(McpPackageViolation.blocker("report.link.broken",
                        "Broken local report link: " + href + " -> " + target, report));
            }
        }
    }
}
