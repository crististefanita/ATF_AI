package com.endava.ai.skills.mcp.validation.profile.graph;

import org.testng.Assert;
import org.testng.annotations.Test;

public class McpGraphSemanticTruthProfileContractTest {

    @Test
    public void acceptsSingleOwnedObjectAndMatchingFrontier() {
        McpGraphSemanticTruthProfile profile = McpGraphSemanticTruthProfile.from(
                report(
                        node("node", "Customer Account", "source identity")
                                + node("node owned strong", "Support Message Record", "primary owned object")
                                + node("node cap", "Customer Message History", "next closure frontier"),
                        "Support Message Record is proven in the support evidence row.",
                        "Support Message Record drives the score; Customer Message History remains capped.",
                        "Customer Message History is not owned yet and is the next closure frontier.",
                        "Primary owned object; cap node means not-owned frontier; proof boundary stays in Proof Mapping."),
                state("support message record", "customer message history", "customer message history"));

        Assert.assertTrue(profile.primaryOwnedObjectBlockers().isEmpty());
        Assert.assertTrue(profile.proofArtifactsNotOwnedBlockers().isEmpty());
        Assert.assertTrue(profile.frontierConsistencyBlockers().isEmpty());
        Assert.assertTrue(profile.proofAndScoreConsistencyBlockers().isEmpty());
    }

    @Test
    public void rejectsMultiplePrimaryOwnedObjects() {
        McpGraphSemanticTruthProfile profile = McpGraphSemanticTruthProfile.from(
                report(
                        node("node owned strong", "Support Message Record", "primary owned object")
                                + node("node owned strong", "Refund Decision", "second owned object")
                                + node("node cap", "Customer Message History", "next closure frontier"),
                        "Support Message Record evidence.",
                        "Support Message Record score.",
                        "Customer Message History frontier.",
                        "Primary owned object and capped frontier."),
                state("support message record", "customer message history", "customer message history"));

        Assert.assertTrue(profile.primaryOwnedObjectBlockers().stream()
                        .anyMatch(message -> message.contains("multiple primary owned objects")),
                "Expected duplicate owned object blocker.");
    }

    @Test
    public void rejectsProofArtifactClaimedAsOwnedObject() {
        McpGraphSemanticTruthProfile profile = McpGraphSemanticTruthProfile.from(
                report(
                        node("node owned strong", "Extent Report", "MCP gate evidence")
                                + node("node cap", "Customer Message History", "next closure frontier"),
                        "Support Message Record evidence.",
                        "Support Message Record score.",
                        "Customer Message History frontier.",
                        "Proof boundary belongs in Proof Mapping."),
                state("support message record", "customer message history", "customer message history"));

        Assert.assertTrue(profile.primaryOwnedObjectBlockers().stream()
                        .anyMatch(message -> message.contains("proof/runtime evidence")),
                "Expected primary owned proof artifact blocker.");
        Assert.assertTrue(profile.proofArtifactsNotOwnedBlockers().stream()
                        .anyMatch(message -> message.contains("supporting proof/runtime artifact")),
                "Expected owned proof artifact blocker.");
    }

    @Test
    public void rejectsFrontierThatDoesNotMatchPackageState() {
        McpGraphSemanticTruthProfile profile = McpGraphSemanticTruthProfile.from(
                report(
                        node("node owned strong", "Support Message Record", "primary owned object")
                                + node("node cap", "Billing Queue", "next closure frontier"),
                        "Support Message Record evidence.",
                        "Support Message Record score.",
                        "Customer Message History frontier remains capped.",
                        "Billing Queue is a cap node."),
                state("support message record", "customer message history", "customer message history"));

        Assert.assertTrue(profile.frontierConsistencyBlockers().stream()
                        .anyMatch(message -> message.contains("package-state")),
                "Expected package-state frontier mismatch blocker.");
    }

    @Test
    public void rejectsGraphLabelsNotAnchoredInProofOrScore() {
        McpGraphSemanticTruthProfile profile = McpGraphSemanticTruthProfile.from(
                report(
                        node("node owned strong", "Refund Decision", "primary owned object")
                                + node("node cap", "Customer Message History", "next closure frontier"),
                        "Support Message Record evidence.",
                        "Support Message Record score.",
                        "Customer Message History frontier.",
                        "Customer Message History is the cap frontier."),
                state("", "customer message history", "customer message history"));

        Assert.assertTrue(profile.proofAndScoreConsistencyBlockers().stream()
                        .anyMatch(message -> message.contains("Proof Mapping or Score Overview")),
                "Expected proof/score traceability blocker for invented primary label.");
    }

    private String report(String graphSvg, String proofMapping, String scoreOverview,
                          String executiveContext, String legend) {
        return "<details><summary>Executive Context</summary>" + executiveContext + "</details>"
                + "<details open><summary>Business Flow Graph</summary><svg class=\"diag\" role=\"img\">"
                + graphSvg
                + "</svg><details class=\"graph-legend subdetails\"><summary>Legend</summary>"
                + legend
                + "</details></details>"
                + "<details><summary>Score Overview</summary>" + scoreOverview + "</details>"
                + "<details><summary>Proof Mapping</summary>" + proofMapping + "</details>";
    }

    private String node(String cssClass, String title, String subtitle) {
        return "<rect class=\"" + cssClass + "\" x=\"0\" y=\"0\" width=\"200\" height=\"80\"></rect>"
                + "<text class=\"nt\">" + title + "</text>"
                + "<text class=\"ns\">" + subtitle + "</text>";
    }

    private String state(String businessObject, String nextObject, String newFrontier) {
        return "{"
                + "\"business_object\":\"" + businessObject + "\","
                + "\"primary_next_business_object\":\"" + nextObject + "\","
                + "\"new_frontier\":\"" + newFrontier + "\""
                + "}";
    }
}
