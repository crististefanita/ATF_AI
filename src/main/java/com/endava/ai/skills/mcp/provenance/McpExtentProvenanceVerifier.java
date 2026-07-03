package com.endava.ai.skills.mcp.provenance;

import com.endava.ai.skills.mcp.McpPackageViolation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade for Extent execution-evidence provenance checks.
 * <p>
 * The individual collaborators keep the business of each contract visible:
 * hash immutability, origin command, manual-proof injection, and native Spark
 * shell integrity are separate concerns.
 */
public final class McpExtentProvenanceVerifier {
    private final McpExtentHashVerifier hashVerifier = new McpExtentHashVerifier();
    private final McpExtentOriginVerifier originVerifier = new McpExtentOriginVerifier();
    private final McpExtentManualProofDetector manualProofDetector = new McpExtentManualProofDetector();
    private final McpNativeSparkShellVerifier nativeSparkShellVerifier = new McpNativeSparkShellVerifier();

    public List<McpPackageViolation> validateMavenProvenance(Path runDirectory, Path extentReport) {
        List<McpPackageViolation> violations = new ArrayList<>();
        violations.addAll(hashVerifier.verify(runDirectory, extentReport));
        violations.addAll(originVerifier.verifySnapshot(runDirectory, extentReport));
        return violations;
    }

    public List<McpPackageViolation> validateNoManualProofInjection(Path extentReport) {
        return manualProofDetector.verify(extentReport);
    }

    public List<McpPackageViolation> validateNativeTestListenerProvenance(Path runDirectory, Path extentReport) {
        List<McpPackageViolation> violations = new ArrayList<>();
        violations.addAll(originVerifier.verifyNativeMavenTest(runDirectory));
        violations.addAll(nativeSparkShellVerifier.verify(extentReport));
        return violations;
    }
}
