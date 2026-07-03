package com.ai.skills.mcp.rules.run;

import com.ai.skills.mcp.contract.McpContract;
import com.ai.skills.mcp.contract.McpRuleCategory;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.provenance.McpExtentProvenanceVerifier;
import com.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Path;

@McpContract(id = "run.extent.maven-provenance",
        category = McpRuleCategory.RUN,
        htmlReq = "execute-and-understand-run.html",
        artifact = "runs/run_##/validation/reports/ExtentReport.html")
public final class RunExtentProvenanceRule implements McpValidationRule {
    private final McpExtentProvenanceVerifier verifier = new McpExtentProvenanceVerifier();

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        for (Path runDirectory : RunRuleSupport.runDirectories(context, result)) {
            for (Path extentReport : RunRuleSupport.canonicalExtentReports(context, runDirectory, result)) {
                verifier.validateMavenProvenance(runDirectory, extentReport).forEach(result::add);
                verifier.validateNoManualProofInjection(extentReport).forEach(result::add);
                verifier.validateNativeTestListenerProvenance(runDirectory, extentReport).forEach(result::add);
            }
        }
    }
}
