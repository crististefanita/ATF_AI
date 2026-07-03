package com.ai.skills.mcp.validation.run.extent.proof;

import lombok.RequiredArgsConstructor;


import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.context.McpPackageReadme;
import com.ai.skills.mcp.validation.McpPackageAuditValidation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.Value;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public final class McpExtentProofClassPublication {
    private final McpPackageAuditValidation audit;

    public Result evaluate() throws IOException {
        Set<String> distinctClasses = extentProofClasses();
        String readme = Files.readString(McpPackageReadme.canonicalFor(audit.context.packageRoot()))
                .toLowerCase(Locale.ROOT);
        String finalReport = audit.readFinalReport().toLowerCase(Locale.ROOT);
        String state = Files.readString(audit.context.packageRoot().resolve("review/package-state.json"));
        int overall = jsonInt(state, "overall", 101);
        List<McpPackageViolation> violations = new ArrayList<>();
        if (distinctClasses.isEmpty()) {
            violations.add(McpPackageViolation.blocker("run.extent.classification.missing",
                    "No per-run Extent proof class could be derived", audit.context.packageRoot()));
        }
        addWeakProofClassViolations(distinctClasses, readme + "\n" + finalReport, overall, violations);
        return new Result(distinctClasses, violations);
    }

    private Set<String> extentProofClasses() throws IOException {
        Set<String> classes = new LinkedHashSet<>();
        audit.forEachRun(run -> {
            for (Path report : audit.extentReports(run)) {
                classes.add(McpExtentProofClass.fromHtml(Files.readString(report)).label());
            }
        });
        return classes;
    }

    private void addWeakProofClassViolations(
            Set<String> distinctClasses,
            String combinedReportText,
            int overall,
            List<McpPackageViolation> violations
    ) {
        boolean hasSynthetic = distinctClasses.contains("synthetic");
        boolean hasStandalone = distinctClasses.contains("standalone");
        if (!hasSynthetic && !hasStandalone) {
            return;
        }
        if (!audit.containsAny(combinedReportText,
                "synthetic", "standalone", "custom proof carrier", "weak execution evidence", "weak product evidence")) {
            violations.add(McpPackageViolation.blocker("run.extent.classification.unpublished",
                    "Per-run Extent class is " + distinctClasses
                            + " but README/final report do not publish the weak proof class",
                    audit.finalReport()));
        }
        if (hasSynthetic && overall > 60) {
            violations.add(McpPackageViolation.blocker("run.extent.synthetic.score-too-high",
                    "Synthetic/custom per-run Extent proof cannot support Overall above 60; published Overall is "
                            + overall,
                    audit.context.packageRoot().resolve("review/package-state.json")));
        }
        if (!combinedReportText.contains("not real ui-visible")
                && !combinedReportText.contains("not ui-owned")
                && !combinedReportText.contains("not product-owned")
                && !combinedReportText.contains("not strong product proof")) {
            violations.add(McpPackageViolation.blocker("run.extent.classification.score-context-missing",
                    "Weak Extent proof class is not tied to an explicit UI/product-proof cap",
                    audit.finalReport()));
        }
    }

    private static int jsonInt(String json, String key, int fallback) {
        var matcher = Pattern.compile("(?is)\"" + Pattern.quote(key) + "\"\\s*:\\s*\"?(\\d+)\"?")
                .matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }

    @Value
    @Accessors(fluent = true)
    public static class Result {
        Set<String> distinctClasses;
        List<McpPackageViolation> violations;
    }
}
