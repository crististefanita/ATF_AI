package com.endava.ai.skills.mcp.validation.run;

import lombok.RequiredArgsConstructor;


import com.endava.ai.skills.mcp.validation.McpPackageAuditValidation;
import com.endava.ai.skills.mcp.McpRuntimeContract;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class McpRunStateAssertions {
    private final McpPackageAuditValidation audit;

    public void assertEachRunXmlMatchesRuntimeTargetAndHasNoFailures() throws IOException {
            List<String> mismatches = new ArrayList<>();
            audit.forEachRun(run -> {
                String xml = audit.readRunFile(run, "validation/testng-results.xml");
                int expected = audit.context.runtime().roundTargetTestCount();
                audit.compareXmlAttribute(run, xml, "total", expected, mismatches);
                audit.compareXmlAttribute(run, xml, "passed", expected, mismatches);
                audit.compareXmlAttribute(run, xml, "failed", 0, mismatches);
                audit.compareXmlAttribute(run, xml, "skipped", 0, mismatches);
            });
            audit.assertNoSyntheticViolations("Per-run TestNG XML target and failures", mismatches,
                    audit.context.packageRoot(), "run.xml.mismatch",
                    audit.details(
                            "Asserts: each run XML matches active test target and has no failed/skipped tests.",
                            "Artifact pattern: runs/run_##/validation/testng-results.xml.",
                            "Expected XML: total=ROUND_TARGET_TEST_COUNT, passed=ROUND_TARGET_TEST_COUNT, failed=0, skipped=0.",
                            "Target per run from runtime: " + audit.context.runtime().roundTargetTestCount()));
        }

    public void assertEachRunStatePublishesTargetTestsAndBusinessStatus() throws IOException {
            List<String> weakStates = new ArrayList<>();
            audit.forEachRun(run -> {
                String state = audit.readRunFile(run, "review/run-state.json");
                if (!state.contains(String.valueOf(audit.context.runtime().roundTargetTestCount()))) {
                    weakStates.add(run + " missing active ROUND_TARGET_TEST_COUNT");
                }
                if (!audit.containsAny(state, "tests_produced", "test_count", "\"tests\"")) {
                    weakStates.add(run + " missing tests produced field");
                }
                if (!audit.containsAny(state, "owned", "countable", "partial", "blocked", "verdict", "status")) {
                    weakStates.add(run + " missing business/countability status");
                }
            });
            audit.assertNoSyntheticViolations("Per-run state target and business status", weakStates,
                    audit.context.packageRoot(), "run-state.weak",
                    audit.details(
                            "Asserts: each run-state publishes target count, produced tests, and business/countability status.",
                            "Artifact pattern: runs/run_##/review/run-state.json.",
                            "Expected business status tokens: owned, countable, partial, blocked, verdict, or status."));
        }

    public void assertEachRunScoreSupportMeetsRuntimeDeltaQualityWhenPublished() throws IOException {
            List<String> weakScores = new ArrayList<>();
            audit.forEachRun(run -> {
                String score = audit.readRunFile(run, "review/score-support.md");
                BigDecimal actual = audit.extractOptionalDelta(score);
                BigDecimal expected = audit.context.runtime().expectedDeltaQualityScore();
                if (actual != null && McpRuntimeContract.normalizeScoreToHundred(actual).compareTo(expected) < 0) {
                    weakScores.add(run + " delta quality " + actual + " is below runtime threshold "
                            + audit.context.runtime().expectedDeltaQuality() + " (normalized=" + expected + ")");
                }
            });
            audit.assertNoSyntheticViolations("Per-run score support delta quality", weakScores,
                    audit.context.packageRoot(), "run-score.quality-low",
                    audit.details(
                            "Asserts: if actual_delta_quality is published, it meets RUN_EXPECTED_DELTA_QUALITY.",
                            "Artifact pattern: runs/run_##/review/score-support.md.",
                            "Expected minimum delta quality from runtime: "
                                    + audit.context.runtime().expectedDeltaQuality()
                                    + " (normalized score=" + audit.context.runtime().expectedDeltaQualityScore() + ")"));
        }

    public void assertRunXmlMapsToRealJavaTestMethods() throws IOException {
            List<String> weakRuns = new ArrayList<>();
            audit.forEachRun(run -> {
                String xml = audit.readRunFile(run, "validation/testng-results.xml");
                int passedMethods = McpTestNgXmlMethods.passedRealMethodCount(xml);
                if (passedMethods != audit.context.runtime().roundTargetTestCount()) {
                    weakRuns.add(run + " expected " + audit.context.runtime().roundTargetTestCount()
                            + " passed XML test-method entries but found " + passedMethods);
                }
            });
            audit.assertNoSyntheticViolations("Run XML to real Java @Test countability",
                    weakRuns, audit.context.packageRoot(), "real-test-countability.xml-methods-missing",
                    audit.details(
                            "Asserts: per-run XML proves real TestNG methods, not only counters.",
                            "Artifact pattern: runs/run_##/validation/testng-results.xml.",
                            "Expected: one passed <test-method> per counted Java @Test method.",
                            "Ignored XML entries: TestNG lifecycle/config methods marked is-config=\"true\".",
                            "Default validator also checks source class existence, @Test annotation, and Maven/Surefire proof."));
        }
}
