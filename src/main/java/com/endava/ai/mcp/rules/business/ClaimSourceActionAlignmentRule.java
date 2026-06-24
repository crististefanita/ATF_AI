package com.endava.ai.mcp.rules.business;

import com.endava.ai.mcp.McpPackageValidationResult;
import com.endava.ai.mcp.McpViolationBuilder;
import com.endava.ai.mcp.context.McpClaimEvidence;
import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.context.McpRunArtifacts;
import com.endava.ai.mcp.contract.McpContract;
import com.endava.ai.mcp.contract.McpRuleCategory;
import com.endava.ai.mcp.rules.McpRuleSupport;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@McpContract(id = "claim-action.source-proof-alignment",
        category = McpRuleCategory.PACKAGE,
        htmlReq = "execute-and-understand-run.html",
        artifact = "run-state + run diff + source + XML/Extent + Proof Mapping")
public final class ClaimSourceActionAlignmentRule implements McpValidationRule {
    private static final List<String> VERBS = List.of(
            "persist", "relogin", "edit", "update", "delete", "history",
            "record", "row", "document", "identity", "status", "transition", "submit", "send");

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        String finalReport = normalize(context.files().readIfRegular(context.paths().report(), result));
        for (Path runDirectory : context.files().runDirectories(context.paths().runsRoot(), result)) {
            McpClaimEvidence evidence = evidenceFor(context, result, runDirectory, finalReport);
            if (evidence.normalizedVerb() == null || evidence.normalizedVerb().isBlank()) {
                continue;
            }
            validateEvidence(evidence, result);
        }
    }

    private McpClaimEvidence evidenceFor(McpPackageContext context,
                                         McpPackageValidationResult result,
                                         Path runDirectory,
                                         String normalizedReport) {
        Path runState = runDirectory.resolve(McpRunArtifacts.STATE);
        Path runDiff = runDirectory.resolve(McpRunArtifacts.DIFF);
        String state = context.files().readIfRegular(runState, result);
        String diff = context.files().readIfRegular(runDiff, result);
        String claim = firstPresent(
                McpRuleSupport.extractStringValue(state, "claim"),
                McpRuleSupport.extractStringValue(state, "progression"),
                McpRuleSupport.extractStringValue(state, "notes"));
        String businessObject = McpRuleSupport.extractStringValue(state, "business_object");
        String status = firstPresent(
                McpRuleSupport.extractStringValue(state, "verdict"),
                McpRuleSupport.extractStringValue(state, "target_status"));
        String verb = detectVerb(claim + " " + businessObject + " " + status);
        List<Path> sources = sourceFiles(context, diff);
        String sourceText = sources.stream()
                .map(path -> context.files().readIfRegular(path, result))
                .collect(Collectors.joining("\n"));
        List<Path> machineProofs = List.of(
                runDirectory.resolve(McpRunArtifacts.TESTNG_RESULTS),
                runDirectory.resolve(McpRunArtifacts.COMMAND_RESULT));
        Path extent = runDirectory.resolve(McpRunArtifacts.REPORTS_ROOT).resolve("ExtentReport.html");
        String proofText = context.files().readIfRegular(machineProofs.get(0), result)
                + "\n" + context.files().readIfRegular(machineProofs.get(1), result)
                + "\n" + context.files().readIfRegular(extent, result);
        String proofMappingRow = proofMappingSlice(normalizedReport, runDirectory.getFileName().toString());
        return new McpClaimEvidence(claim, verb, businessObject, runDirectory.getFileName().toString(),
                runState, runDiff, sources, machineProofs, extent, proofMappingRow, status,
                normalize(diff + "\n" + sourceText), normalize(proofText + "\n" + proofMappingRow));
    }

    private void validateEvidence(McpClaimEvidence evidence, McpPackageValidationResult result) {
        switch (evidence.normalizedVerb()) {
            case "relogin":
                require(evidence, result, "claim-action.relogin-without-session-boundary",
                        any(evidence.actionCorpus() + evidence.proofCorpus(), "logout", "signout", "sign out", "session reset", "new session")
                                && any(evidence.actionCorpus() + evidence.proofCorpus(), "login", "signin", "sign in", "relogin"),
                        "relogin claim requires session boundary plus assertion after the new login",
                        "add logout/session reset + fresh login + post-login assertion, or lower the claim to reopen/support");
                break;
            case "persist":
                require(evidence, result, "claim-action.persist-without-post-boundary-assertion",
                        any(evidence.actionCorpus(), "save", "submit", "update", "set", "fill", "change")
                                && any(evidence.actionCorpus() + evidence.proofCorpus(), "refresh", "reopen", "relogin", "reload", "new session")
                                && assertionPresent(evidence),
                        "persist claim requires write action plus a post-boundary assertion",
                        "verify the value after refresh/reopen/relogin, not only immediately after save");
                break;
            case "edit":
            case "update":
                require(evidence, result, "claim-action.edit-without-save-and-assertion",
                        any(evidence.actionCorpus(), "fill", "change", "edit", "update", "save", "submit")
                                && assertionPresent(evidence),
                        "edit/update claim requires field mutation, save/submit, and assertion of the new value",
                        "move mutation/assertion into UI action + validation layer, or mark the claim partial");
                break;
            case "delete":
                require(evidence, result, "claim-action.delete-without-absence-assertion",
                        any(evidence.actionCorpus(), "delete", "remove")
                                && any(evidence.actionCorpus() + evidence.proofCorpus(), "absent", "not visible", "missing", "removed", "empty"),
                        "delete claim requires delete/remove action plus absence assertion after reload/reopen",
                        "assert the item is absent after delete and reload/reopen, or lower the claim");
                break;
            case "history":
            case "record":
            case "row":
            case "document":
            case "identity":
            case "status":
            case "transition":
                require(evidence, result, "claim-action.history-without-row-or-identity-proof",
                        any(evidence.actionCorpus() + evidence.proofCorpus(),
                                "row", "record", "detail", "details", "id", "identifier", "number",
                                "status", "history", "document", "identity", "transition", "changed", "visible"),
                        "record/history/status claim requires row/detail/id/status/transition proof",
                        "verify a concrete row, record, id, detail route, status, identity, or transition in source and proof");
                break;
            case "submit":
            case "send":
                require(evidence, result, "claim-action.submit-without-acknowledgement-proof",
                        any(evidence.actionCorpus(), "submit", "send", "click")
                                && any(evidence.actionCorpus() + evidence.proofCorpus(),
                                "confirmation", "ack", "id", "identifier", "created", "success", "row", "record"),
                        "submit/send claim requires submit action plus acknowledgement/id/record proof",
                        "assert confirmation, id, created row/record, or acknowledgement after submission");
                break;
            default:
                break;
        }
    }

    private void require(McpClaimEvidence evidence,
                         McpPackageValidationResult result,
                         String code,
                         boolean condition,
                         String meaning,
                         String repair) {
        if (condition) {
            return;
        }
        result.add(McpViolationBuilder.blocker(code)
                .message("Run " + evidence.runId() + " publishes verb '" + evidence.normalizedVerb()
                        + "' without compatible source/proof action. Claim: " + evidence.claimText())
                .path(evidence.runState())
                .meaning(meaning + ". Evidence chain: " + evidence.chainSummary()
                        + ". Missing/weak: " + evidence.missingEvidenceHint())
                .repair(repair + ". Keep the published verb no stronger than the cold evidence chain.")
                .artifact("run-state.json, thin-no-index.diff, source snapshot, Extent/XML, final Proof Mapping")
                .build());
    }

    private boolean assertionPresent(McpClaimEvidence evidence) {
        return any(evidence.actionCorpus() + evidence.proofCorpus(),
                "assert", "verify", "should", "expect", "visible", "contains", "equals", "present", "not visible");
    }

    private boolean any(String corpus, String... tokens) {
        String normalized = normalize(corpus);
        return Arrays.stream(tokens).anyMatch(normalized::contains);
    }

    private String detectVerb(String text) {
        String normalized = normalize(text);
        for (String verb : VERBS) {
            if (normalized.contains(verb)) {
                return verb;
            }
        }
        return "";
    }

    private List<Path> sourceFiles(McpPackageContext context, String diff) {
        List<Path> files = new ArrayList<>();
        Matcher matcher = Pattern.compile("(?m)^\\+\\+\\+ b/(source/[^\\r\\n]+|src/[^\\r\\n]+)").matcher(diff);
        while (matcher.find()) {
            Path path = context.packageRoot().resolve(matcher.group(1)).normalize();
            if (Files.isRegularFile(path)) {
                files.add(path);
                continue;
            }
            Path projectPath = context.projectRoot().resolve(matcher.group(1).replaceFirst("^source/", "")).normalize();
            if (Files.isRegularFile(projectPath)) {
                files.add(projectPath);
            }
        }
        return files.stream().distinct().collect(Collectors.toList());
    }

    private String proofMappingSlice(String normalizedReport, String runId) {
        int start = normalizedReport.indexOf(runId.toLowerCase(Locale.ROOT));
        if (start < 0) {
            return "";
        }
        int end = normalizedReport.indexOf("</tr>", start);
        if (end < 0) {
            end = Math.min(normalizedReport.length(), start + 900);
        }
        return normalizedReport.substring(start, Math.min(normalizedReport.length(), end + 5));
    }

    private String firstPresent(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
