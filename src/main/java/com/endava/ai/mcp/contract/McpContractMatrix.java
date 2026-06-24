package com.endava.ai.mcp.contract;

import com.endava.ai.mcp.McpPackageViolation;
import com.endava.ai.mcp.rules.McpValidationRule;
import com.endava.ai.mcp.rules.McpRuleSet;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class McpContractMatrix {
    private McpContractMatrix() {
    }

    public static String ruleMatrix(List<McpValidationRule> rules) {
        String header = "rule_id | category | severity | html_req | artifact";
        String separator = "--- | --- | --- | --- | ---";
        String rows = rules.stream()
                .map(McpValidationRule::contractSpec)
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

    private static String ruleRow(McpContractSpec contract) {
        return String.join(" | ",
                contract.id(),
                contract.category().name(),
                contract.severity().name(),
                contract.htmlReq(),
                contract.artifact());
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
        if (value == null) {
            return "";
        }
        return value.replace('|', '/')
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("\\s+", " ")
                .trim();
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
