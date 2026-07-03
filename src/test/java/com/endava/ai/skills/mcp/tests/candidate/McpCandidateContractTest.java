package com.endava.ai.skills.mcp.tests.candidate;

import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.RequiresArtifacts;
import com.endava.ai.skills.mcp.contract.ValidatesMcpContract;
import com.endava.ai.skills.mcp.tests.support.McpPackageAuditAssumptions;
import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
@ValidatesMcpContract("html-example-candidate")
@RequiresArtifacts({
        "review/package-state.json",
        "analysis/langgraph-business-understanding.html"
})
@McpSuiteRef("mcp.suite.mcp-candidate-contract-test")
public class McpCandidateContractTest {
    @BeforeClass
    public void requirePackageAuditTarget() {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
    }

    @Test
    @ValidatesMcpContract("candidate.score.teacher-threshold")
    @McpCheckRef("candidate.teacher-grade")
    public void latestPackageIsTeacherGradeHtmlExampleCandidate() throws IOException {
        McpPackageAuditValidation.latestPackage().assertHtmlExampleCandidateContract();
    }
}
