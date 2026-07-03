package com.ai.atf.core.support.contract.core;

import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.COVERAGE_AUDIT)
@AtfContractRef(AtfContractNarrativeIds.Contract.COVERAGE_AUDIT_CLASS)
@Listeners(TestListener.class)
public class AtfCoreContractNarrativeCoverageTest {

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.COVERAGE_AUDIT_METHOD)
    public void publicAtfCoreContractSuitesAreExtentAuditable()
            throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "publicAtfCoreContractSuitesAreExtentAuditable"
        );
        AtfContractSteps.expected(contract);

        List<String> nonAuditable = atfContractTestSourcesWithoutReportAudit();
        Assert.assertTrue(
                nonAuditable.isEmpty(),
                "@AtfContract suites without TestListener wiring or self-auditing Extent contract: " + nonAuditable
        );

        AtfContractSteps.observed(
                "All executable @AtfContract suites under com.ai.atf are listener-backed or self-auditing Extent contracts."
        );
        AtfContractSteps.verdict(
                "ATF-core contract tests can be audited from the generated report without reading Java first.",
                contract
        );
    }

    private static List<String> atfContractTestSourcesWithoutReportAudit()
            throws IOException {
        Path root = AtfSourcePaths.TEST_ATF_ROOT;
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(AtfSourcePaths::isTestJavaSource)
                    .filter(path -> !AtfSourcePaths.unixPath(path).contains("/contract/"))
                    .filter(AtfCoreContractNarrativeCoverageTest::sourceContainsAtfContractNarrative)
                    .filter(path -> !sourceIsReportAuditable(path))
                    .map(root::relativize)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        }
    }

    private static boolean sourceIsReportAuditable(Path path) {
        String source = readSource(path);
        boolean listenerBacked = source.contains("@Listeners(TestListener.class)");
        boolean selfAuditingExtent = source.contains("@NoFrameworkReporting")
                && hasExtentOnlyGroup(source)
                && (source.contains("@AtfContractSuite") || source.contains("@AtfContractSuiteRef"));
        return listenerBacked || selfAuditingExtent;
    }

    private static boolean sourceContainsAtfContractNarrative(Path path) {
        String source = readSource(path);
        return source.contains("@AtfContract")
                || source.contains("@AtfContractRef")
                || source.contains("@AtfContractSuite")
                || source.contains("@AtfContractSuiteRef");
    }

    private static boolean hasExtentOnlyGroup(String source) {
        return source.contains("\"" + AtfTestGroups.EXTENT_ONLY + "\"")
                || source.contains("AtfTestGroups.EXTENT_ONLY");
    }

    private static String readSource(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + path, e);
        }
    }
}
