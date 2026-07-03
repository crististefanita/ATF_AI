package com.ai.skills.codequality.tests;

import com.ai.skills.codequality.tests.support.CodeQualityContractPolicy;
import com.ai.skills.codequality.tests.support.CodeQualityReceiptWriter;
import com.ai.skills.codequality.tests.support.CodeQualityResourceSize;
import com.ai.skills.codequality.tests.support.CodeQualitySourceScanner;
import com.ai.skills.codequality.tests.support.CodeQualityYamlSizePolicy;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.CORE_ARCHITECTURE_GUARD_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.CORE_ARCHITECTURE_GUARD_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class AtfCodeQualityGovernanceResourceContractTest {

    @Test
    public void governanceResourcesStayUnderYamlAndSuiteBudgets() throws IOException {
        CodeQualityContractPolicy policy = CodeQualityContractPolicy.load();
        policy.assertUniqueIds();

        CodeQualityYamlSizePolicy yamlPolicy = policy.yamlSizePolicy();
        List<CodeQualityResourceSize> yamlResources =
                CodeQualitySourceScanner.yamlResourceSizes(policy.resourceRoots());
        Map<String, Integer> legacyCaps = policy.legacyYamlSizeCaps();
        List<String> suites = CodeQualitySourceScanner.testNgSuites(policy.resourceRoots());
        List<String> violations = yamlViolations(yamlResources, legacyCaps, yamlPolicy.getMaxBytes());
        List<String> staleRatchets = staleYamlRatchets(yamlResources, legacyCaps, policy.staleYamlRatchetSlack());
        long warningFiles = countAtOrAbove(yamlResources, yamlPolicy.getWarningBytes());
        long overMaxFiles = yamlResources.stream()
                .filter(resource -> resource.getBytes() > yamlPolicy.getMaxBytes())
                .count();

        CodeQualityReceiptWriter.publishGovernanceResources(
                yamlPolicy, yamlResources, legacyCaps, suites, policy.maxSuiteCount(),
                warningFiles, overMaxFiles, violations, staleRatchets);

        Assert.assertTrue(warningFiles <= yamlPolicy.getMaxWarningFiles(),
                "Warning-level YAML count drifted upward. Current=" + warningFiles
                        + ", max=" + yamlPolicy.getMaxWarningFiles());
        Assert.assertTrue(overMaxFiles <= yamlPolicy.getMaxOverMaxFiles(),
                "Over-max YAML count drifted upward. Current=" + overMaxFiles
                        + ", max=" + yamlPolicy.getMaxOverMaxFiles());
        Assert.assertTrue(suites.size() <= policy.maxSuiteCount(),
                "TestNG suite count drifted upward. Current=" + suites.size()
                        + ", max=" + policy.maxSuiteCount());
        Assert.assertTrue(staleRatchets.isEmpty(),
                "Legacy YAML-size caps must be lowered when actual bytes are safely below cap: " + staleRatchets);
        Assert.assertTrue(violations.isEmpty(),
                "YAML resources must stay below Code Quality YAML-size limit or declare a legacy ratchet: "
                        + violations);
    }

    private static long countAtOrAbove(List<CodeQualityResourceSize> resources, long threshold) {
        return resources.stream().filter(resource -> resource.getBytes() >= threshold).count();
    }

    private static List<String> yamlViolations(List<CodeQualityResourceSize> resources,
                                               Map<String, Integer> legacyCaps,
                                               long defaultMax) {
        return resources.stream()
                .filter(resource -> resource.getBytes() > legacyCaps.getOrDefault(resource.getPath(), (int) defaultMax))
                .map(resource -> resource.getPath() + " has " + resource.getBytes()
                        + " bytes, max " + legacyCaps.getOrDefault(resource.getPath(), (int) defaultMax))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<String> staleYamlRatchets(List<CodeQualityResourceSize> resources,
                                                  Map<String, Integer> legacyCaps,
                                                  int slack) {
        Map<String, Long> actualByPath = resources.stream()
                .collect(Collectors.toMap(CodeQualityResourceSize::getPath, CodeQualityResourceSize::getBytes));
        return legacyCaps.entrySet().stream()
                .filter(entry -> !actualByPath.containsKey(entry.getKey())
                        || actualByPath.get(entry.getKey()) <= entry.getValue() - slack)
                .map(entry -> entry.getKey() + " actual="
                        + actualByPath.getOrDefault(entry.getKey(), 0L) + ", cap=" + entry.getValue())
                .collect(Collectors.toUnmodifiableList());
    }
}
