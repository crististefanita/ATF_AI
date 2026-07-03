package com.endava.ai.skills.codequality.tests;

import com.endava.ai.skills.codequality.tests.support.CodeQualityContractPolicy;
import com.endava.ai.skills.codequality.tests.support.CodeQualityPackageClassCount;
import com.endava.ai.skills.codequality.tests.support.CodeQualityReceiptWriter;
import com.endava.ai.skills.codequality.tests.support.CodeQualitySourceScanner;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
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
public class AtfCodeQualityPackageClassLimitContractTest {

    @Test
    public void javaPackagesStayUnderDefaultDirectClassLimitOrDeclareLegacyRatchet() throws IOException {
        CodeQualityContractPolicy policy = CodeQualityContractPolicy.load();
        policy.assertUniqueIds();

        int defaultLimit = policy.defaultPackageClassLimit();
        Assert.assertEquals(defaultLimit, 8, "Code Quality default package class limit must stay at 8.");

        List<CodeQualityPackageClassCount> counts =
                CodeQualitySourceScanner.packageClassCounts(policy.sourceRoots());
        Map<String, Integer> legacyCaps = policy.legacyPackageCaps();
        List<String> violations = packageViolations(counts, legacyCaps, defaultLimit);
        List<String> staleRatchets = stalePackageRatchets(counts, legacyCaps, policy.staleRatchetSlack());

        CodeQualityReceiptWriter.publishPackageClassCount(
                defaultLimit, counts, legacyCaps, violations, staleRatchets);

        Assert.assertTrue(staleRatchets.isEmpty(),
                "Legacy package caps must be lowered when actual count is safely below cap: " + staleRatchets);
        Assert.assertTrue(violations.isEmpty(),
                "Packages must stay below the Code Quality direct-class limit or declare a legacy ratchet: "
                        + violations);
    }

    private static List<String> packageViolations(List<CodeQualityPackageClassCount> counts,
                                                  Map<String, Integer> legacyCaps,
                                                  int defaultLimit) {
        return counts.stream()
                .filter(count -> count.getClasses() > legacyCaps.getOrDefault(count.getPath(), defaultLimit))
                .map(count -> count.getPath() + " has " + count.getClasses()
                        + " direct classes, max " + legacyCaps.getOrDefault(count.getPath(), defaultLimit))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<String> stalePackageRatchets(List<CodeQualityPackageClassCount> counts,
                                                     Map<String, Integer> legacyCaps,
                                                     int slack) {
        Map<String, Integer> actualByPath = counts.stream()
                .collect(Collectors.toMap(CodeQualityPackageClassCount::getPath,
                        CodeQualityPackageClassCount::getClasses));
        return legacyCaps.entrySet().stream()
                .filter(entry -> !actualByPath.containsKey(entry.getKey())
                        || actualByPath.get(entry.getKey()) <= entry.getValue() - slack)
                .map(entry -> entry.getKey() + " actual="
                        + actualByPath.getOrDefault(entry.getKey(), 0) + ", cap=" + entry.getValue())
                .collect(Collectors.toUnmodifiableList());
    }
}
