package com.ai.skills.mcp.contract;

import com.ai.skills.mcp.McpPackageViolation;
import com.ai.skills.mcp.rules.McpValidationRule;
import com.ai.skills.mcp.rules.McpRuleSet;
import com.ai.core.support.html.HtmlText;
import com.ai.skills.mcp.contract.execution.McpRuleExecutionDescriptor;
import com.ai.skills.mcp.contract.execution.McpRuleRoute;
import com.ai.skills.mcp.contract.execution.McpRuleRoutingCatalog;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class McpContractMatrix {
    private McpContractMatrix() {
    }

    public static String ruleMatrix(List<McpValidationRule> rules) {
        String header = "rule_id | family | artifact_family | cost | category | severity | repair_route | html_req | artifact | why | repair";
        String separator = "--- | --- | --- | --- | --- | --- | --- | --- | --- | --- | ---";
        String rows = rules.stream()
                .filter(Objects::nonNull)
                .map(McpContractMatrix::ruleRow)
                .collect(Collectors.joining(System.lineSeparator()));
        return header + System.lineSeparator() + separator + System.lineSeparator() + rows;
    }

    public static String ruleMatrix(McpRuleSet ruleSet) {
        return "profile: " + ruleSet.profile().name()
                + System.lineSeparator()
                + ruleMatrix(ruleSet.rules());
    }

    public static String allProfilesRuleMatrix() {
        String header = "profile | rule_id | family | artifact_family | cost | category | severity | repair_route | html_req | artifact | why | repair";
        String separator = "--- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | ---";
        String rows = McpRuleRoutingCatalog.routes().stream()
                .map(McpContractMatrix::profileRuleRow)
                .collect(Collectors.joining(System.lineSeparator()));
        return header + System.lineSeparator() + separator + System.lineSeparator() + rows;
    }

    public static String blockerMatrix(Path packageRoot, List<McpPackageViolation> blockers) {
        String header = "severity | failure_code | artifact | message";
        String separator = "--- | --- | --- | ---";
        String rows = blockers.stream()
                .map(blocker -> String.join(" | ",
                        blocker.severity().name(),
                        blocker.code(),
                        relativize(packageRoot, blocker.path()),
                        sanitize(blocker.message())))
                .collect(Collectors.joining(System.lineSeparator()));
        return header + System.lineSeparator() + separator + System.lineSeparator() + rows;
    }

    public static String contractFailureMatrix(List<McpValidationRule> rules,
                                               List<McpPackageViolation> blockers) {
        String header = "rule_id | html_req | artifact | observed_failure_codes_on_artifact";
        String separator = "--- | --- | --- | ---";
        String rows = rules.stream()
                .map(McpValidationRule::contractSpec)
                .filter(Objects::nonNull)
                .map(contract -> String.join(" | ",
                        contract.id(),
                        contract.htmlReq(),
                        contract.artifact(),
                        observedFailuresFor(contract, blockers)))
                .collect(Collectors.joining(System.lineSeparator()));
        return header + System.lineSeparator() + separator + System.lineSeparator() + rows;
    }

    private static String observedFailuresFor(McpContractSpec contract, List<McpPackageViolation> blockers) {
        String artifact = normalizeArtifact(contract.artifact());
        String codes = blockers.stream()
                .filter(blocker -> sameArtifactFamily(artifact, blocker.path()))
                .map(McpPackageViolation::code)
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));
        return codes.isBlank() ? "none" : codes;
    }

    private static String ruleRow(McpValidationRule rule) {
        McpContractSpec contract = rule.contractSpec();
        McpRuleExecutionDescriptor descriptor = McpRuleExecutionDescriptor.from(rule);
        return String.join(" | ",
                contract.id(),
                descriptor.family().name(),
                descriptor.artifactFamilyName(),
                descriptor.cost().name(),
                contract.category().name(),
                contract.severity().name(),
                sanitize(descriptor.repairRoute()),
                contract.htmlReq(),
                contract.artifact(),
                sanitize(contract.why()),
                sanitize(contract.repairHint()));
    }

    private static String profileRuleRow(McpRuleRoute route) {
        McpContractSpec contract = route.contractSpec();
        return String.join(" | ",
                route.profile().name(),
                contract.id(),
                route.descriptor().family().name(),
                route.descriptor().artifactFamilyName(),
                route.descriptor().cost().name(),
                contract.category().name(),
                contract.severity().name(),
                sanitize(route.descriptor().repairRoute()),
                contract.htmlReq(),
                contract.artifact(),
                sanitize(contract.why()),
                sanitize(contract.repairHint()));
    }

    private static String relativize(Path packageRoot, Path path) {
        if (path == null) {
            return "n/a";
        }
        Path absolutePath = path.toAbsolutePath().normalize();
        Path absolutePackage = packageRoot == null ? null : packageRoot.toAbsolutePath().normalize();
        try {
            if (absolutePackage != null && absolutePath.startsWith(absolutePackage)) {
                String relative = absolutePackage.relativize(absolutePath).toString().replace('\\', '/');
                return relative.isBlank() ? "." : relative;
            }
        } catch (IllegalArgumentException ignored) {
            // Different roots; return absolute path below.
        }
        return absolutePath.toString().replace('\\', '/');
    }

    private static String sanitize(String value) {
        return HtmlText.markdownCell(value);
    }

    private static boolean sameArtifactFamily(String contractArtifact, Path blockerPath) {
        if (contractArtifact.isBlank() || blockerPath == null) {
            return false;
        }
        String path = normalizeArtifact(blockerPath.toString());
        return path.endsWith(contractArtifact) || path.contains("/" + contractArtifact)
                || contractArtifact.endsWith(path) || contractArtifact.contains("/" + path);
    }

    private static String normalizeArtifact(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('\\', '/').replaceAll("^/+", "").trim();
    }
}
