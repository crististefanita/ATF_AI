package com.endava.ai.skills.mcp.rules.candidate.business;

import com.endava.ai.skills.mcp.McpPackageValidationResult;
import com.endava.ai.skills.mcp.McpViolationBuilder;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.context.McpRunArtifacts;
import com.endava.ai.skills.mcp.contract.McpContract;
import com.endava.ai.skills.mcp.contract.McpRuleCategory;
import com.endava.ai.skills.mcp.rules.McpRuleSupport;
import com.endava.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@McpContract(id = "candidate.business-lifecycle.depth",
        category = McpRuleCategory.HTML_EXAMPLE_CANDIDATE,
        htmlReq = "business-flow-graph-rubric.html",
        artifact = "package-state + report + run-state + run diff + execution proof")
public final class BusinessLifecycleDepthRule implements McpValidationRule {
    private static final Map<String, String[]> LIFECYCLE_SIGNALS = Map.of(
            "reopenability", new String[]{"reopen", "reopened", "reload", "revisit", "relogin", "new session"},
            "transition", new String[]{"transition", "status", "changed", "progression", "state change"},
            "persistent object", new String[]{"row", "record", "detail", "details", "document", "history", "persisted", "durable"},
            "identity", new String[]{"identity", "identifier", " id", "number", "reference"},
            "post-action proof", new String[]{"after submit", "after save", "after reload", "after relogin", "confirmation"});

    @Override
    public void validate(McpPackageContext context, McpPackageValidationResult result) {
        Path packageState = context.paths().packageState();
        if (!Files.isRegularFile(packageState)) {
            return;
        }
        String state = context.files().read(packageState, result);
        Integer overall = McpRuleSupport.firstIntValue(state, "overall");
        Integer cap = McpRuleSupport.firstIntValue(state, "cap");
        int threshold = Integer.parseInt(context.runtime().expectedDeltaQuality());
        if ((overall == null || overall < threshold) && (cap == null || cap < threshold)) {
            return;
        }

        String corpus = normalize(state + "\n"
                + context.files().readIfRegular(context.paths().report(), result) + "\n"
                + runArtifactCorpus(context, result));
        String matchedFamily = matchedFamily(corpus);
        if (!matchedFamily.isBlank()) {
            return;
        }
        result.add(McpViolationBuilder.blocker("candidate.business-lifecycle.depth-missing")
                .message("Candidate package at or above runtime threshold must prove lifecycle depth through a generic business signal")
                .path(packageState)
                .meaning("A 93+/teacher-grade candidate cannot rely only on report shape or one static assertion; it needs reopenability, transition, persistent record/detail, identity, or post-action proof")
                .repair("Add or prove one generic lifecycle depth family in run-state, diff, execution proof and report, or lower Overall/cap below the runtime threshold")
                .artifact("review/package-state.json, runs/run_##/review/run-state.json, runs/run_##/review/thin-no-index.diff, Extent/XML, final report")
                .build());
    }

    private String runArtifactCorpus(McpPackageContext context, McpPackageValidationResult result) {
        return context.files().runDirectories(context.paths().runsRoot(), result).stream()
                .map(run -> context.files().readIfRegular(run.resolve(McpRunArtifacts.STATE), result)
                        + "\n" + context.files().readIfRegular(run.resolve(McpRunArtifacts.DIFF), result)
                        + "\n" + context.files().readIfRegular(run.resolve(McpRunArtifacts.TESTNG_RESULTS), result)
                        + "\n" + context.files().readIfRegular(run.resolve(McpRunArtifacts.COMMAND_RESULT), result))
                .collect(Collectors.joining("\n"));
    }

    private String matchedFamily(String corpus) {
        for (Map.Entry<String, String[]> entry : LIFECYCLE_SIGNALS.entrySet()) {
            for (String token : entry.getValue()) {
                if (corpus.contains(token)) {
                    return entry.getKey();
                }
            }
        }
        return "";
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.ROOT);
    }
}
