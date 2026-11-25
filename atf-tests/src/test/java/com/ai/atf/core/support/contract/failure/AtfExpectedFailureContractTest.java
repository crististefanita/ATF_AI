package com.ai.atf.core.support.contract.failure;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
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
import java.util.regex.Pattern;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXPECTED_FAILURE_STYLE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXPECTED_FAILURE_STYLE_CLASS)
@Listeners(TestListener.class)
public class AtfExpectedFailureContractTest {

    private static final Path ATF_TEST_ROOT = AtfSourcePaths.TEST_ATF_ROOT;
    private static final Pattern EXPECTED_FAILURE_GROUP =
            Pattern.compile("groups\\s*=\\s*\\{[^}]*expected-failure[^}]*}", Pattern.DOTALL);
    private static final Pattern EXPECTED_FAILURE_NARRATIVE =
            Pattern.compile("@AtfExpectedFailureContract(?:Ref)?\\s*\\(");
    private static final Pattern EXPECTED_FAILURE_CONTRACT_REF =
            Pattern.compile("@AtfExpectedFailureContractRef\\(\"([^\"]+)\"\\)");

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXPECTED_FAILURE_STYLE_SHARED_NARRATIVE)
    public void expectedFailureTestsUseSharedContractNarrative() throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "expectedFailureTestsUseSharedContractNarrative"
        );
        AtfContractSteps.expected(contract);

        List<Path> files = expectedFailureFiles();
        Assert.assertFalse(files.isEmpty(), "Expected at least one ATF expected-failure test file");

        List<Path> missingNarrative = files.stream()
                .filter(path -> !hasExpectedFailureNarrative(path))
                .collect(Collectors.toList());
        List<Path> missingHelper = files.stream()
                .filter(path -> !contains(path, "AtfExpectedFailure.expect"))
                .collect(Collectors.toList());
        List<String> invalidRefs = files.stream()
                .flatMap(path -> expectedFailureRefs(path).stream())
                .filter(id -> !isKnownExpectedFailureRef(id))
                .collect(Collectors.toList());

        AtfContractSteps.observed("expected.failure.files=" + files
                + " | missing.narrative=" + missingNarrative
                + " | missing.helper=" + missingHelper
                + " | invalid.refs=" + invalidRefs);

        Assert.assertTrue(missingNarrative.isEmpty(),
                "Expected-failure tests without inline or YAML-backed narrative: " + missingNarrative);
        Assert.assertTrue(missingHelper.isEmpty(), "Expected-failure tests without helper: " + missingHelper);
        Assert.assertTrue(invalidRefs.isEmpty(),
                "@AtfExpectedFailureContractRef ids must resolve to kind=expectedFailure rows: " + invalidRefs);
        AtfContractSteps.verdict(
                "Expected-failure tests use one auditable report grammar.",
                contract.repairHint()
        );
    }

    private static List<Path> expectedFailureFiles() throws IOException {
        try (Stream<Path> stream = Files.walk(ATF_TEST_ROOT)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(AtfSourcePaths::isJavaSource)
                    .filter(AtfExpectedFailureContractTest::hasExpectedFailureGroup)
                    .collect(Collectors.toList());
        }
    }

    private static boolean hasExpectedFailureGroup(Path path) {
        try {
            return EXPECTED_FAILURE_GROUP.matcher(Files.readString(path)).find();
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading " + path, e);
        }
    }

    private static boolean contains(Path path, String token) {
        try {
            return Files.readString(path).contains(token);
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading " + path, e);
        }
    }

    private static boolean hasExpectedFailureNarrative(Path path) {
        try {
            return EXPECTED_FAILURE_NARRATIVE.matcher(Files.readString(path)).find();
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading " + path, e);
        }
    }

    private static List<String> expectedFailureRefs(Path path) {
        try {
            return EXPECTED_FAILURE_CONTRACT_REF.matcher(Files.readString(path))
                    .results()
                    .map(match -> match.group(1))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Failed reading " + path, e);
        }
    }

    private static boolean isKnownExpectedFailureRef(String id) {
        try {
            AtfCoreYamlContractCatalog.requireContractNarrativeExpectedFailure(id);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
