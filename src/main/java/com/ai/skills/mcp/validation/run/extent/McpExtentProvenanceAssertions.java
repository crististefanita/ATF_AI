package com.ai.skills.mcp.validation.run.extent;

import lombok.RequiredArgsConstructor;


import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpRunArtifacts;
import com.ai.skills.mcp.provenance.McpExtentProvenanceVerifier;
import com.ai.skills.mcp.validation.McpPackageAuditValidation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class McpExtentProvenanceAssertions {
    private final McpPackageAuditValidation audit;
    private final McpExtentProvenanceVerifier verifier = new McpExtentProvenanceVerifier();

    public void assertPerRunExtentReportsHaveMavenProvenanceAndHashChain() throws IOException {
        List<McpPackageViolation> violations = new ArrayList<>();
        audit.forEachRun(run -> {
            Path runRoot = audit.runRoot(run);
            List<Path> extentReports = canonicalExtentReports(runRoot);
            if (extentReports.isEmpty()) {
                violations.add(McpPackageViolation.blocker("run.extent.missing",
                        "Missing Extent report, so Maven-native provenance cannot be verified",
                        runRoot.resolve(McpRunArtifacts.REPORTS_ROOT)));
                return;
            }
            for (Path report : extentReports) {
                violations.addAll(verifier.validateMavenProvenance(runRoot, report));
            }
        });
        audit.assertNoViolations("Per-run Extent Maven provenance and hash chain", violations,
                audit.details(
                        "Asserts: ExtentReport.html is a frozen Maven output, not a repaired package artifact.",
                        "Required artifacts: ExtentReport.html, ExtentReport.sha256, extent-origin.json.",
                        "Hash chain: actual Extent SHA-256 must match the frozen sha file and origin metadata.",
                        "Repair: regenerate the Extent report through Maven, then freeze sha/origin immediately after execution."));

    }

    public void assertExtentReportsHaveNoManualProofInjection() throws IOException {
        List<McpPackageViolation> violations = new ArrayList<>();
        audit.forEachRun(run -> {
            for (Path report : canonicalExtentReports(audit.runRoot(run))) {
                violations.addAll(verifier.validateNoManualProofInjection(report));
            }
        });
        audit.assertNoViolations("Per-run Extent no manual proof injection", violations,
                audit.details(
                        "Asserts: Extent remains execution evidence only.",
                        "Forbidden: final report sections, score cards, graph overlays, shared report CSS, or repair markers inside Extent.",
                        "Allowed: native Maven/TestListener/Extent runtime styling and logs if they were generated during execution.",
                        "Repair: do not edit Extent; regenerate it from Maven and report issues in package-state/final report instead."));
    }

    public void assertPerRunExtentReportsHaveNativeTestListenerProvenance() throws IOException {
        List<McpPackageViolation> violations = new ArrayList<>();
        audit.forEachRun(run -> {
            Path runRoot = audit.runRoot(run);
            for (Path report : canonicalExtentReports(runRoot)) {
                violations.addAll(verifier.validateNativeTestListenerProvenance(runRoot, report));
            }
        });
        audit.assertNoViolations("Per-run Extent native TestListener provenance", violations,
                audit.details(
                        "Asserts: ExtentReport.html is the native Maven/TestNG/TestListener Spark report, not a post-run support writer output.",
                        "Required origin command: mvn ... test.",
                        "Forbidden origin command/generator: exec:java, -DskipTests, exec.mainClass, ExtentExecutionProofWriter.",
                        "Required HTML structure: native Spark shell tokens such as body spa report, nav-dashboard, toggleView, test-wrapper and spark-script.",
                        "Repair: rerun the UI test through Maven/TestNG and freeze the generated Extent report without rewriting it."));
    }

    private List<Path> canonicalExtentReports(Path runRoot) throws IOException {
        List<Path> reports = audit.findHtml(runRoot.resolve(McpRunArtifacts.REPORTS_ROOT));
        return reports.isEmpty()
                ? audit.extentReports(runRoot.getFileName().toString())
                : reports;
    }
}
