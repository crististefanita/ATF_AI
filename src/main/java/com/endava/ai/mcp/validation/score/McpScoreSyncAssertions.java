package com.endava.ai.mcp.validation.score;

import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class McpScoreSyncAssertions {
    private final McpPackageAuditValidation audit;

    public McpScoreSyncAssertions(McpPackageAuditValidation audit) {
        this.audit = audit;
    }

    public void assertOverallTruthIsSynchronizedAcrossCanonicalArtifacts() throws IOException {
            Path readme = audit.context.packageRoot().resolveSibling("README.md");
            Path packageState = audit.context.packageRoot().resolve("review/package-state.json");
            Path gate = audit.context.packageRoot().resolve("validation/mcp/mcp-gate-result.md");
            String readmeText = Files.readString(readme);
            String packageStateText = Files.readString(packageState);
            String reportText = audit.readFinalReport();
            String gateText = Files.readString(gate);
            Integer stateOverall = jsonInt(packageStateText, "overall");
            Integer reportOverall = scoreCardInt(reportText, "Overall");
            Integer readmeOverall = readmeOverall(readmeText);
            List<String> blockers = new ArrayList<>();
            if (stateOverall == null) {
                blockers.add("package-state.json does not publish numeric overall");
            }
            if (reportOverall == null) {
                blockers.add("final report does not publish an Overall score card");
            }
            if (readmeOverall == null) {
                blockers.add("README.md does not publish cold Overall package score");
            }
            if (stateOverall != null && reportOverall != null && !stateOverall.equals(reportOverall)) {
                blockers.add("Overall drift: package-state=" + stateOverall + " final report=" + reportOverall);
            }
            if (stateOverall != null && readmeOverall != null && !stateOverall.equals(readmeOverall)) {
                blockers.add("Overall drift: package-state=" + stateOverall + " README=" + readmeOverall);
            }
            if (stateOverall != null && !gateText.contains("Overall") && !gateText.contains(String.valueOf(stateOverall))) {
                blockers.add("MCP gate result does not echo the current Overall/cap truth");
            }
            audit.assertNoSyntheticViolations("Overall truth synchronized across README, package-state, report, and MCP gate",
                    blockers, packageState, "score.overall.truth-drift",
                    audit.details(
                            "Asserts: canonical frozen artifacts publish the same Overall truth; this check is artifact-only.",
                            "Artifacts: docs/out/README.md, review/package-state.json, final HTML, validation/mcp/mcp-gate-result.md.",
                            "Expected chain: README Overall == package-state Overall == report Overall; MCP gate echoes the same cold truth."));
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

    private Integer readmeOverall(String readme) {
            Matcher packageRow = Pattern.compile("(?im)^\\|\\s*Overall package\\s*\\|\\s*(\\d+)\\s*\\|").matcher(readme);
            if (packageRow.find()) {
                return Integer.parseInt(packageRow.group(1));
            }
            Matcher coldOverall = Pattern.compile("(?i)cold overall remains:\\s*`?(\\d+)\\s*/\\s*100`?").matcher(readme);
            return coldOverall.find() ? Integer.parseInt(coldOverall.group(1)) : null;
        }
}
