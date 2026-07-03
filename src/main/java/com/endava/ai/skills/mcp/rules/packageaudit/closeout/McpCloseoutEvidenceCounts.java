package com.endava.ai.skills.mcp.rules.packageaudit.closeout;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import com.endava.ai.skills.mcp.rules.packageaudit.gate.McpGateCommandEvidence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

final class McpCloseoutEvidenceCounts {
    private McpCloseoutEvidenceCounts() {
    }

    static void validate(McpPackageContext context,
                         McpPackageValidationResult result,
                         String gateText) {
        try {
            Path xml = mcpXml(context);
            if (xml == null) {
                return;
            }
            String xmlText = Files.readString(xml);
            validateXmlProfile(xml, xmlText, result);
            McpCloseoutCounts xmlCounts = McpCloseoutCounts.fromXml(xmlText);
            McpCloseoutCounts ledgerCounts = McpCloseoutCounts.fromLedger(
                    Files.readString(context.packageRoot().resolve(McpArtifactPaths.MCP_METHOD_LEDGER)));
            McpCloseoutCounts gateCounts = McpCloseoutCounts.fromGate(gateText);
            McpCloseoutCounts extentCounts = McpCloseoutCounts.fromExtent(
                    Files.readString(context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_REPORT)));
            compareCounts("XML", xmlCounts, "ledger", ledgerCounts,
                    context.packageRoot().resolve(McpArtifactPaths.MCP_METHOD_LEDGER), result);
            compareCounts("XML", xmlCounts, "gate", gateCounts,
                    context.packageRoot().resolve(McpArtifactPaths.MCP_GATE_RESULT), result);
            compareExtentCounts(context, xmlCounts, extentCounts, result);
            validateMcpSkipReasonDoesNotContradictCommand(context, xmlText, gateText, result);
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("mcp.evidence-counts.unreadable",
                    "Cannot cross-check MCP Extent/XML/ledger counts: " + exception.getMessage(),
                    context.packageRoot().resolve(McpArtifactPaths.MCP_VALIDATION_ROOT)));
        }
    }

    private static void validateXmlProfile(Path xml,
                                           String xmlText,
                                           McpPackageValidationResult result) {
        boolean namesCloseout = McpCloseoutCommandProfile.hasFinalCloseoutEvidence(xmlText);
        boolean namesWrongPrimaryProfile = McpCloseoutCommandProfile.hasNonCloseoutPrimaryEvidence(xmlText);
        if (!namesCloseout || namesWrongPrimaryProfile) {
            result.add(McpPackageViolation.blocker("mcp.xml.not-package-closeout",
                    McpArtifactPaths.MCP_VALIDATION_ROOT
                            + " XML must be produced by the final publishability subset "
                            + McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName()
                            + "; full sweep, candidate and mixed XML artifacts "
                            + "cannot be the primary close-out evidence",
                    xml));
        }
    }

    private static void compareExtentCounts(McpPackageContext context,
                                            McpCloseoutCounts xmlCounts,
                                            McpCloseoutCounts extentCounts,
                                            McpPackageValidationResult result) {
        Path report = context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_REPORT);
        if (!xmlCounts.complete() || !extentCounts.complete()) {
            compareCounts("XML", xmlCounts, "Extent", extentCounts, report, result);
            return;
        }
        if (xmlCounts.equals(extentCounts) || isDisclosedNativeExtentGrouping(context, extentCounts, xmlCounts)) {
            return;
        }
        result.add(McpPackageViolation.blocker("mcp.evidence-counts.mismatch",
                "MCP XML counts differ from Extent: " + xmlCounts + " vs " + extentCounts,
                report));
    }

    private static boolean isDisclosedNativeExtentGrouping(McpPackageContext context,
                                                           McpCloseoutCounts extentCounts,
                                                           McpCloseoutCounts xmlCounts) {
        if (extentCounts.total == null || extentCounts.total <= 0
                || xmlCounts.total == null || extentCounts.total >= xmlCounts.total) {
            return false;
        }
        String disclosure = (safeRead(context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_ORIGIN))
                + "\n" + safeRead(context.packageRoot().resolve(McpArtifactPaths.MCP_GATE_RESULT))
                + "\n" + safeRead(context.packageRoot().resolve(McpArtifactPaths.MCP_METHOD_LEDGER)))
                .toLowerCase(Locale.ROOT);
        return disclosure.contains("native")
                && disclosure.contains("extent")
                && disclosure.contains("method accounting")
                && (disclosure.contains("testng xml") || disclosure.contains("testng-results.xml"))
                && (disclosure.contains("extent_items_native") || disclosure.contains("native extent output"));
    }

    private static Path mcpXml(McpPackageContext context) {
        Path canonical = context.packageRoot().resolve(McpArtifactPaths.MCP_TESTNG_XML);
        if (Files.isRegularFile(canonical)) {
            return canonical;
        }
        Path alternate = context.packageRoot().resolve(McpArtifactPaths.MCP_ALTERNATE_TESTNG_XML);
        return Files.isRegularFile(alternate) ? alternate : null;
    }

    private static void compareCounts(String expectedName,
                                      McpCloseoutCounts expected,
                                      String actualName,
                                      McpCloseoutCounts actual,
                                      Path artifact,
                                      McpPackageValidationResult result) {
        if (!expected.complete() || !actual.complete()) {
            result.add(McpPackageViolation.blocker("mcp.evidence-counts.incomplete",
                    "Cannot compare MCP " + expectedName + " vs " + actualName
                            + " because one side lacks total/passed/skipped/failures/errors: "
                            + expected + " vs " + actual,
                    artifact));
            return;
        }
        if (!expected.equals(actual)) {
            result.add(McpPackageViolation.blocker("mcp.evidence-counts.mismatch",
                    "MCP " + expectedName + " counts differ from " + actualName + ": "
                            + expected + " vs " + actual,
                    artifact));
        }
    }

    private static void validateMcpSkipReasonDoesNotContradictCommand(McpPackageContext context,
                                                                      String xmlText,
                                                                      String gateText,
                                                                      McpPackageValidationResult result) {
        String originCommand = McpGateCommandEvidence.originCommand(context);
        String gateCommand = McpGateCommandEvidence.gateCommand(gateText);
        boolean commandDeclaresPackageRoot = McpGateCommandEvidence.declaresPackageRoot(originCommand)
                || McpGateCommandEvidence.declaresPackageRoot(gateCommand);
        String skipEvidence = xmlText + "\n" + McpGateCommandEvidence.skippedOnlyGateEvidence(gateText);
        if (commandDeclaresPackageRoot && McpGateCommandEvidence.saysPackageRootWasMissing(skipEvidence)) {
            result.add(McpPackageViolation.blocker("mcp.skip-reason.command-contradiction",
                    "MCP origin/gate command publishes -Dmcp.package.root, but XML/gate skip reason says package root was missing",
                    context.packageRoot().resolve(McpArtifactPaths.MCP_TESTNG_XML)));
        }
    }

    private static String safeRead(Path path) {
        if (!Files.isRegularFile(path)) {
            return "";
        }
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            return "";
        }
    }
}

