package com.endava.ai.skills.mcp.validation;

import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.McpContractMatrix;
import com.endava.ai.skills.mcp.contract.McpContractSpec;
import com.endava.ai.skills.mcp.contract.McpProfile;
import com.endava.ai.skills.mcp.contract.McpRuleCatalog;
import com.endava.ai.skills.mcp.contract.suites.McpSuiteMetadata;
import com.endava.ai.skills.mcp.rules.McpDefaultRules;
import com.endava.ai.skills.mcp.rules.McpValidationRule;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class McpContractMetadataValidation {
    private McpContractMetadataValidation() {
    }

    public static void assertDefaultRulesExposeMcpContractMetadata() {
        StepLogger.startStep("MCP rule metadata coverage");
        List<McpValidationRule> rules = allProfileRules();
        List<McpContractSpec> specs = rules.stream()
                .map(McpValidationRule::contractSpec)
                .collect(Collectors.toList());

        List<String> missingFromCatalog = rules.stream()
                .map(McpValidationRule::getClass)
                .filter(ruleClass -> !McpRuleCatalog.ruleClasses().contains(ruleClass))
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
        List<String> duplicateIds = specs.stream()
                .collect(Collectors.groupingBy(McpContractSpec::id, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        List<String> incompleteNarrative = specs.stream()
                .filter(spec -> spec.why().isBlank() || spec.repairHint().isBlank())
                .map(McpContractSpec::id)
                .collect(Collectors.toList());

        StepLogger.info("Rules in active MCP profiles: " + rules.size());
        StepLogger.info("MCP catalog entries: " + McpRuleCatalog.ruleClasses().size());
        StepLogger.info("Missing catalog classes: " + missingFromCatalog);
        StepLogger.info("Duplicate MCP contract ids: " + duplicateIds);
        StepLogger.info("Incomplete MCP contract narratives: " + incompleteNarrative);
        StepLogger.logCodeBlock(McpContractMatrix.allProfilesRuleMatrix());
        if (!missingFromCatalog.isEmpty() || !duplicateIds.isEmpty() || !incompleteNarrative.isEmpty()) {
            throw new AssertionError("MCP rule metadata is incomplete. Missing catalog: " + missingFromCatalog
                    + " | duplicate ids: " + duplicateIds
                    + " | incomplete narratives: " + incompleteNarrative);
        }
        StepLogger.pass("All MCP profile rules expose unique catalog metadata and repair narratives");
    }

    public static void assertLatestAndCandidateSuitesExposeRepresentativeMcpChecks(Class<?>... suiteClasses) {
        assertSuitesExposeRepresentativeMcpChecks("latest/candidate", suiteClasses);
    }

    public static void assertSuitesExposeRepresentativeMcpChecks(String suiteFamily, Class<?>... suiteClasses) {
        StepLogger.startStep("MCP suite check metadata coverage");
        List<String> missing = new ArrayList<>();
        for (Class<?> suiteClass : suiteClasses) {
            McpSuiteMetadata.fromClass(suiteClass);
            boolean hasTest = false;
            boolean hasCheck = false;
            for (Method method : suiteClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    hasTest = true;
                    hasCheck = hasCheck || method.isAnnotationPresent(McpCheckRef.class);
                }
            }
            if (hasTest && !hasCheck) {
                missing.add(suiteClass.getName());
            }
        }

        String family = suiteFamily == null || suiteFamily.isBlank() ? "MCP" : suiteFamily;
        StepLogger.info("Suites checked: " + suiteClasses.length);
        StepLogger.info("Suite family: " + family);
        StepLogger.info("Suites missing representative @McpCheckRef: " + missing);
        if (!missing.isEmpty()) {
            throw new AssertionError("MCP " + family
                    + " suites must expose at least one representative @McpCheckRef: " + missing);
        }
        StepLogger.pass("MCP " + family + " suites expose representative check metadata");
    }

    private static List<McpValidationRule> allProfileRules() {
        return Stream.of(
                        McpDefaultRules.rulesFor(McpProfile.UNIVERSAL),
                        McpDefaultRules.rulesFor(McpProfile.UI_PACKAGE),
                        McpDefaultRules.rulesFor(McpProfile.HTML_REPORT),
                        McpDefaultRules.rulesFor(McpProfile.HTML_EXAMPLE_CANDIDATE),
                        McpDefaultRules.rulesFor(McpProfile.SUITE_ATLAS))
                .flatMap(List::stream)
                .collect(Collectors.toMap(
                        rule -> rule.getClass().getName(),
                        rule -> rule,
                        (left, right) -> left,
                        java.util.LinkedHashMap::new))
                .values()
                .stream()
                .collect(Collectors.toList());
    }
}
