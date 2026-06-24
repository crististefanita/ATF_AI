package com.endava.ai.mcp.tests.negative;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.core.reporting.StepLogger;
import com.endava.ai.mcp.contract.McpContractMatrix;
import com.endava.ai.mcp.contract.McpContractSpec;
import com.endava.ai.mcp.contract.McpProfile;
import com.endava.ai.mcp.contract.ValidatesMcpContract;
import com.endava.ai.mcp.rules.McpDefaultRules;
import com.endava.ai.mcp.rules.McpValidationRule;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Listeners(TestListener.class)
@ValidatesMcpContract("generated-package-validator-metadata-contract")
public class McpGeneratedPackageValidatorTest {
    @Test
    public void allDefaultRulesExposeMcpContractMetadata() {
        StepLogger.startStep("MCP rule metadata coverage");
        List<Class<?>> ruleClasses = allProfileRules();
        List<String> missing = ruleClasses.stream()
                .filter(ruleClass -> McpContractSpec.fromClass(ruleClass) == null)
                .map(Class::getSimpleName)
                .collect(Collectors.toList());
        List<String> duplicateIds = ruleClasses.stream()
                .map(McpContractSpec::fromClass)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.groupingBy(McpContractSpec::id, Collectors.counting()))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        StepLogger.info("Missing metadata classes: " + missing);
        StepLogger.info("Duplicate MCP contract ids: " + duplicateIds);
        StepLogger.logCodeBlock(McpContractMatrix.ruleMatrix(
                McpDefaultRules.ruleSet(McpProfile.HTML_EXAMPLE_CANDIDATE)));
        Assert.assertTrue(missing.isEmpty(), "Rules without @McpContract metadata: " + missing);
        Assert.assertTrue(duplicateIds.isEmpty(), "Duplicate @McpContract ids: " + duplicateIds);
        StepLogger.pass("All MCP profile rules expose unique contract metadata");
    }

    private List<Class<?>> allProfileRules() {
        return Stream.of(
                        McpDefaultRules.rulesFor(McpProfile.UNIVERSAL),
                        McpDefaultRules.rulesFor(McpProfile.UI_PACKAGE),
                        McpDefaultRules.rulesFor(McpProfile.HTML_REPORT),
                        McpDefaultRules.rulesFor(McpProfile.HTML_EXAMPLE_CANDIDATE),
                        McpDefaultRules.rulesFor(McpProfile.SUITE_ATLAS))
                .flatMap(List::stream)
                .map(McpValidationRule::getClass)
                .distinct()
                .collect(Collectors.toList());
    }
}
