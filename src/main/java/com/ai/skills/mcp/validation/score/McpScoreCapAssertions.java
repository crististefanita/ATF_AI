package com.ai.skills.mcp.validation.score;

import com.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.ai.skills.mcp.validation.profile.McpReportSections;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public final class McpScoreCapAssertions {
    private final McpPackageAuditValidation audit;

    public void assertScoreCapIsConsistentWithWeakestAxis() {
            audit.assertNoBlockers("Score-cap consistency",
                    violation -> violation.code().startsWith("score.cap."),
                    audit.details(
                            "Asserts: Overall does not exceed the weakest published score axis.",
                            "Artifact: " + McpReportSections.SCORE_OVERVIEW + " inside analysis/langgraph-business-understanding.html.",
                            "Expected: Overall <= min(business substance, graph, history, proof, contract/artifact axes)."));
        }

    public void assertPublishedOverallDoesNotExceedPackageStateCap() throws IOException {
            Path packageState = audit.context.packageRoot().resolve("review/package-state.json");
            String packageStateText = Files.readString(packageState);
            String reportText = audit.readFinalReport();
            Integer cap = jsonInt(packageStateText, "cap");
            Integer stateOverall = jsonInt(packageStateText, "overall");
            Integer reportOverall = scoreCardInt(reportText, "Overall");
            List<String> blockers = new ArrayList<>();
            if (cap == null) {
                blockers.add("package-state.json does not publish numeric cap");
            }
            if (stateOverall != null && cap != null && stateOverall > cap) {
                blockers.add("package-state overall " + stateOverall + " exceeds cap " + cap);
            }
            if (reportOverall != null && cap != null && reportOverall > cap) {
                blockers.add("final report Overall " + reportOverall + " exceeds package-state cap " + cap);
            }
            audit.assertNoSyntheticViolations("Published Overall does not exceed package-state cap",
                    blockers, packageState, "score.overall.exceeds-cap",
                    audit.details(
                            "Asserts: no report may publish a higher Overall than review/package-state.json cap.",
                            "Artifacts: review/package-state.json and analysis/langgraph-business-understanding.html.",
                            "Expected: package-state overall <= cap and report Overall <= cap."));
        }

    public void assertModelOnlyPackagesStayCappedBelowUiOwnedPackages() throws IOException {
            String packageStateText = Files.readString(audit.context.packageRoot().resolve("review/package-state.json"));
            String reportText = audit.readFinalReport();
            String combined = (packageStateText + "\n" + reportText).toLowerCase();
            Integer overall = jsonInt(packageStateText, "overall");
            List<String> blockers = new ArrayList<>();
            boolean modelOnly = combined.contains("model-only")
                    || combined.contains("modeled-only")
                    || combined.contains("modeled commerce")
                    || combined.contains("not ui-owned")
                    || combined.contains("not product-owned");
            boolean uiOwned = combined.contains("ui-owned") && !combined.contains("not ui-owned")
                    || combined.contains("product-owned ui-visible") && !combined.contains("not a product-owned");
            if (modelOnly && !uiOwned && overall != null && overall > 60) {
                blockers.add("model/service-only package publishes Overall " + overall
                        + "; model-only packages must stay capped below UI-owned packages");
            }
            if (modelOnly && !combined.contains("business substance")) {
                blockers.add("model-only package does not make the Business Substance cap explicit");

            }
            audit.assertNoSyntheticViolations("Model/service-only package score ceiling",
                    blockers, audit.finalReport(), "score.model-only.too-high",
                    audit.details(
                            "Asserts: model/service-only packages cannot score like UI-owned business proof.",
                            "Artifacts: review/package-state.json and final report.",
                            "Expected: model-only or not-UI-owned packages keep Overall <= 60 and explain the Business Substance cap."));

        }

    private Integer jsonInt(String json, String key) {
            Matcher matcher = Pattern.compile("(?is)\"" + Pattern.quote(key) + "\"\\s*:\\s*\"?(\\d+)\"?").matcher(json);
            return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
        }

    private Integer scoreCardInt(String html, String label) {
            Matcher matcher = Pattern.compile("(?is)<div class=\"label\">\\s*" + Pattern.quote(label)
                    + "\\s*</div>\\s*<div class=\"score\">\\s*(\\d+)\\s*</div>").matcher(html);
            return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
        }
}
