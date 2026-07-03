package com.endava.ai.skills.mcp.validation.diagnostics;

import com.endava.ai.skills.mcp.McpPackageViolation;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.policy.McpRootCauseGroupSpec;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class McpBlockerRootCauseContractTest {
    @Test
    public void yamlRootCauseGroupsCoverEveryExecutableMatcher() {
        Set<String> yamlIds = ContractCatalog.rootCauseGroups().stream()
                .map(McpRootCauseGroupSpec::id)
                .collect(Collectors.toUnmodifiableSet());

        Assert.assertEquals(yamlIds, Set.copyOf(McpBlockerRootCause.executableGroupKeys()),
                "skills/mcp/contracts/root-cause-groups.yml must describe every executable root-cause matcher.");

        for (McpRootCauseGroupSpec spec : ContractCatalog.rootCauseGroups()) {
            Assert.assertFalse(spec.label().isBlank(), "Root-cause label must be present for " + spec.id());
            Assert.assertFalse(spec.repair().isBlank(), "Root-cause repair must be present for " + spec.id());
        }
    }

    @Test
    public void primaryFocusUsesYamlRepairCoachingForDominantBucket() {
        List<McpPackageViolation> blockers = List.of(
                McpPackageViolation.blocker(
                        "report.mcp-evidence.missing",
                        "Artifact Index must hyperlink " + McpArtifactPaths.MCP_EXTENT_REPORT,
                        Path.of(McpArtifactPaths.FINAL_REPORT)),
                McpPackageViolation.blocker(
                        "report.artifact-index.core-link.missing",
                        "Artifact Index must hyperlink package-state.json",
                        Path.of(McpArtifactPaths.FINAL_REPORT)));

        String focus = McpBlockerRootCause.primaryFocus(blockers);

        Assert.assertTrue(focus.contains("Final report/README do not hyperlink the required evidence spine"));
        Assert.assertTrue(focus.contains("update " + McpArtifactPaths.README + " and " + McpArtifactPaths.FINAL_REPORT));
    }
}
