package com.ai.skills.mcp.rules.report;


import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.fs.LocalLinkResolver;
import com.ai.skills.mcp.html.HtmlLinkExtractor;

import java.nio.file.Files;

@McpContract(id = "report.local-links.resolve",
        category = McpRuleCategory.LINKS,
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
