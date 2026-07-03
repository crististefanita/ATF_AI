package com.endava.ai.skills.mcp.support.shape;

import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.contracts.policy.McpSizeBudgetSpec;
import org.testng.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class McpPackageShapeBudgetAssertions {
    private McpPackageShapeBudgetAssertions() {
    }

    public static void assertLineCapBudgets() {
        assertNoBudgetViolations(
                "line-cap",
                McpSizeBudgetSpec::maxLines,
                McpPackageShapeBudgetAssertions::lineCapViolation,
                "MCP line-cap budgets must be declared.",
                "MCP large-class ratchet failed: ");
    }

    public static void assertYamlByteBudgets() {
        assertNoBudgetViolations(
                "resource-max-bytes",
                McpSizeBudgetSpec::maxBytes,
                McpPackageShapeBudgetAssertions::byteCapViolation,
                "MCP YAML resource byte budgets must be declared.",
                "MCP YAML resource ratchet failed; split metadata by family before raising caps: ");
    }

    public static void assertRecursiveSurfaceBudgets() {
        assertNoBudgetViolations(
                "recursive-package-cap",
                McpSizeBudgetSpec::maxClasses,
                McpPackageShapeBudgetAssertions::recursiveClassCapViolation,
                "MCP recursive package surface budgets must be declared.",
                "MCP broad surface ratchet failed; split ownership or add a focused subpackage before raising caps: ");
    }

    private static void assertNoBudgetViolations(String type,
                                                 ToIntFunction<McpSizeBudgetSpec> capExtractor,
                                                 BudgetViolationRule violationRule,
                                                 String emptyMessage,
                                                 String failurePrefix) {
        Map<Path, Integer> caps = budgetCaps(type, capExtractor);
        List<String> violations = caps.entrySet().stream()
                .map(entry -> violationRule.apply(entry.getKey(), entry.getValue()))
                .filter(violation -> !violation.isEmpty())
                .collect(Collectors.toList());

        Assert.assertFalse(caps.isEmpty(), emptyMessage);
        Assert.assertTrue(violations.isEmpty(), failurePrefix + violations);
    }

    private static Map<Path, Integer> budgetCaps(String type, ToIntFunction<McpSizeBudgetSpec> capExtractor) {
        Map<Path, Integer> caps = new LinkedHashMap<>();
        for (McpSizeBudgetSpec budget : ContractCatalog.sizeBudgetContracts()) {
            if (type.equals(budget.type())) {
                caps.put(Path.of(budget.path()), capExtractor.applyAsInt(budget));
            }
        }
        return caps;
    }

    private static String lineCapViolation(Path path, int cap) {
        try (Stream<String> lines = Files.lines(path)) {
            long count = lines.count();
            return count > cap ? path + " has " + count + " lines, cap " + cap : "";
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect MCP class size for " + path, e);
        }
    }

    private static String byteCapViolation(Path path, int cap) {
        try {
            long count = Files.size(path);
            return count > cap ? path + " has " + count + " bytes, cap " + cap : "";
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect MCP resource size for " + path, e);
        }
    }

    private static String recursiveClassCapViolation(Path path, int cap) {
        try (Stream<Path> files = Files.walk(path)) {
            long count = files.filter(McpPackageShapePolicyAssertions::isJavaSource).count();
            return count > cap ? path + " has " + count + " Java files recursively, cap " + cap : "";
        } catch (IOException e) {
            throw new IllegalStateException("Cannot inspect MCP recursive package size for " + path, e);
        }
    }

    private interface BudgetViolationRule {
        String apply(Path path, int cap);
    }
}
