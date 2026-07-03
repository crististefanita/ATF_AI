package com.ai.skills.codequality.tests;

import com.ai.skills.codequality.tests.support.CodeQualityClassSizePolicy;
import com.ai.skills.codequality.tests.support.CodeQualityContractPolicy;
import com.ai.skills.codequality.tests.support.CodeQualityReceiptWriter;
import com.ai.skills.codequality.tests.support.CodeQualitySourceScanner;
import com.ai.skills.codequality.tests.support.CodeQualitySourceSize;
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
public class AtfCodeQualityClassSizeContractTest {

    @Test
    public void javaSourcesStayUnderClassSizeBudgetOrDeclareLegacyRatchet() throws IOException {
        CodeQualityContractPolicy policy = CodeQualityContractPolicy.load();
        policy.assertUniqueIds();

        CodeQualityClassSizePolicy classSize = policy.classSizePolicy();
        List<CodeQualitySourceSize> sources = CodeQualitySourceScanner.javaSourceSizes(policy.sourceRoots());
        Map<String, Integer> legacyCaps = policy.legacyClassSizeCaps();
        long warningFiles = countAtOrAbove(sources, classSize.getWarningLines());
        long overMaxFiles = sources.stream().filter(source -> source.getLines() > classSize.getMaxLines()).count();
        List<String> violations = classSizeViolations(sources, legacyCaps, classSize.getMaxLines());
        List<String> staleRatchets = staleClassRatchets(sources, legacyCaps, policy.staleRatchetSlack());

        CodeQualityReceiptWriter.publishClassSize(
                classSize, sources, legacyCaps, warningFiles, overMaxFiles, violations, staleRatchets);

        Assert.assertTrue(warningFiles <= classSize.getMaxWarningFiles(),
                "Warning-level class count drifted upward. Current=" + warningFiles
                        + ", max=" + classSize.getMaxWarningFiles());
        Assert.assertTrue(overMaxFiles <= classSize.getMaxOverMaxFiles(),
                "Over-max class count drifted upward. Current=" + overMaxFiles
                        + ", max=" + classSize.getMaxOverMaxFiles());
        Assert.assertTrue(staleRatchets.isEmpty(),
                "Legacy class-size caps must be lowered when actual lines are safely below cap: " + staleRatchets);
        Assert.assertTrue(violations.isEmpty(),
                "Java sources must stay below the Code Quality class-size limit or declare a legacy ratchet: "
                        + violations);
    }

    private static long countAtOrAbove(List<CodeQualitySourceSize> sources, int threshold) {
        return sources.stream().filter(source -> source.getLines() >= threshold).count();
    }

    private static List<String> classSizeViolations(List<CodeQualitySourceSize> sources,
                                                    Map<String, Integer> legacyCaps,
                                                    int defaultMax) {
        return sources.stream()
                .filter(source -> source.getLines() > legacyCaps.getOrDefault(source.getPath(), defaultMax))
                .map(source -> source.getPath() + " has " + source.getLines()
                        + " lines, max " + legacyCaps.getOrDefault(source.getPath(), defaultMax))
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<String> staleClassRatchets(List<CodeQualitySourceSize> sources,
                                                   Map<String, Integer> legacyCaps,
                                                   int slack) {
        Map<String, Integer> actualByPath = sources.stream()
                .collect(Collectors.toMap(CodeQualitySourceSize::getPath, CodeQualitySourceSize::getLines));
        return legacyCaps.entrySet().stream()
                .filter(entry -> !actualByPath.containsKey(entry.getKey())
                        || actualByPath.get(entry.getKey()) <= entry.getValue() - slack)
                .map(entry -> entry.getKey() + " actual="
                        + actualByPath.getOrDefault(entry.getKey(), 0) + ", cap=" + entry.getValue())
                .collect(Collectors.toUnmodifiableList());
    }
}
