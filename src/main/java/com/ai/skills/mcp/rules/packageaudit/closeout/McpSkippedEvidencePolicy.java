package com.ai.skills.mcp.rules.packageaudit.closeout;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import com.ai.skills.mcp.rules.McpRuleSupport;
import com.ai.skills.mcp.rules.packageaudit.gate.McpGateCommandEvidence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

final class McpSkippedEvidencePolicy {
    private McpSkippedEvidencePolicy() {
    }

    static void validate(McpPackageContext context,
                         McpPackageValidationResult result,
                         String gateText) {
        Path report = context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_REPORT);
        Path ledger = context.packageRoot().resolve(McpArtifactPaths.MCP_METHOD_LEDGER);
        if (!Files.isRegularFile(report) || !Files.isRegularFile(ledger)) {
            return;
        }
        try {
            String html = Files.readString(report);
            McpCloseoutCounts extentCounts = McpCloseoutCounts.fromExtent(html);
            if (extentCounts.skipped == null || extentCounts.skipped == 0) {
                return;
            }

            String ledgerText = Files.readString(ledger);
            String disclosure = gateText + "\n" + ledgerText;
            String lower = disclosure.toLowerCase(Locale.ROOT);
            validateAccountingDisclosure(lower, ledger, result);
            validateReasonDisclosure(lower, ledger, result);
            Integer publishedCap = McpGateCommandEvidence.publishedCap(context);
            validatePublishedCap(context, result, publishedCap);
            validateScorePolicy(context, result, publishedCap, extentCounts.skipped);
            McpCloseoutReportDisclosure.validateSkippedSummary(context, result, extentCounts.skipped);
            validateSkippedClassesNamed(html, disclosure, ledger, result);
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("mcp.skipped-profile.unreadable",
                    "Cannot inspect final MCP skipped profile disclosure: " + exception.getMessage(),
                    context.packageRoot().resolve(McpArtifactPaths.MCP_VALIDATION_ROOT)));
        }
    }

    private static void validateAccountingDisclosure(String lower,
                                                     Path ledger,
                                                     McpPackageValidationResult result) {
        if (!McpRuleSupport.containsAny(lower,
                "maven package-closeout outcome",
                "package-closeout outcome",
                "maven full-suite outcome",
                "full-suite outcome",
                "surefire tests",
                "discovered")) {
            result.add(McpPackageViolation.blocker("mcp.closeout-accounting.missing",
                    "Final MCP evidence has skipped tests but does not publish package-closeout discovered/executed/skipped accounting",
                    ledger));
        }
    }

    private static void validateReasonDisclosure(String lower,
                                                 Path ledger,
                                                 McpPackageValidationResult result) {
        if (!McpRuleSupport.containsAny(lower,
                "skipped profile",
                "skipped classes",
                "profile/precondition",
                "precondition skipped")) {
            result.add(McpPackageViolation.blocker("mcp.skipped-profile.unexplained",
                    "Final MCP evidence has skipped tests but does not publish the skipped profile/class group",
                    ledger));
        }
        if (!lower.contains("reason")) {
            result.add(McpPackageViolation.blocker("mcp.skipped-profile.reason-missing",
                    "Final MCP skipped tests must publish a human-readable reason, not only a numeric skipped count",
                    ledger));
        }
        if (!lower.contains("accepted")) {
            result.add(McpPackageViolation.blocker("mcp.skipped-profile.acceptance-missing",
                    "Final MCP skipped tests must publish whether each skipped profile/class group is accepted for close-out",
                    ledger));
        }
        if (!lower.contains("accepted=true")) {
            result.add(McpPackageViolation.blocker("mcp.skipped-profile.not-accepted",
                    "Final MCP skipped tests are allowed only when the skipped profile/class group is explicitly accepted=true",
                    ledger));
        }
    }

    private static void validatePublishedCap(McpPackageContext context,
                                             McpPackageValidationResult result,
                                             Integer publishedCap) {
        if (publishedCap == null || publishedCap >= 95) {
            result.add(McpPackageViolation.blocker("mcp.skipped-profile.cap-missing",
                    "Full MCP sweep evidence with skipped tests must cap the package below 95 and publish that cap in package-state.json",
                    context.packageRoot().resolve(McpArtifactPaths.PACKAGE_STATE)));
        }
    }

    private static void validateScorePolicy(McpPackageContext context,
                                            McpPackageValidationResult result,
                                            Integer publishedCap,
                                            int skipped) {
        String originCommand = McpGateCommandEvidence.originCommand(context);
        String gateCommand = McpGateCommandEvidence.gateCommand(context.files().read(
                context.packageRoot().resolve(McpArtifactPaths.MCP_GATE_RESULT), result));
        McpCloseoutCommandProfile profile = McpCloseoutCommandProfile.strongest(originCommand, gateCommand);
        if (skipped > 0 && profile == McpCloseoutCommandProfile.PACKAGE_CLOSEOUT) {
            result.add(McpPackageViolation.blocker("mcp.closeout.skipped-profile.forbidden",
                    "Package close-out profile has skipped MCP methods; final publishability close-out must be rerun with 0 skipped, 0 failures and 0 errors",
                    context.packageRoot().resolve(McpArtifactPaths.PACKAGE_STATE)));
        }
        if (publishedCap != null && publishedCap >= 95 && profile != McpCloseoutCommandProfile.PACKAGE_CLOSEOUT) {
            result.add(McpPackageViolation.blocker("mcp.closeout.profile-required-for-95",
                    "Overall/cap 95+ requires " + McpArtifactPaths.MCP_VALIDATION_ROOT + " evidence from "
                            + McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName()
                            + ", not only the broad full MCP sweep",
                    context.packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_ORIGIN)));
        }
    }

    private static void validateSkippedClassesNamed(String html,
                                                    String disclosure,
                                                    Path ledger,
                                                    McpPackageValidationResult result) {
        for (String skippedName : new java.util.LinkedHashSet<>(McpGateCommandEvidence.skippedTestNames(html))) {
            if (McpGateCommandEvidence.isPackageAuditProfileName(skippedName) && !disclosure.contains(skippedName)) {
                result.add(McpPackageViolation.blocker("mcp.skipped-profile.class-missing",
                        "Final MCP ledger must name skipped package-audit profile class: " + skippedName,
                        ledger));
            }
        }
    }
}

