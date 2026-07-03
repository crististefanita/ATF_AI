package com.ai.skills.business.tests;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Listeners(TestListener.class)
public class BusinessUnderstandingExtentNarrativeContractTest {
    private static final Path BUSINESS_TEST_ROOT = AtfSourcePaths.workspaceRoot()
            .resolve("src/test/java/com/ai/skills/business/tests");
    private static final Pattern TEST_METHOD = Pattern.compile("@Test\\b");
    private static final Pattern NARRATIVE_CALL = Pattern.compile("BusinessUnderstandingReportNarrative\\s*\\.\\s*verify\\s*\\(");
    private static final Pattern DIRECT_STEP_LOGGING = Pattern.compile("\\bStepLogger\\s*\\.\\s*(startStep|logDetail|pass|fail)\\b");

    @Test
    public void businessUnderstandingContractTestsPublishSemanticNarrativeToExtent() throws IOException {
        List<String> violations = new ArrayList<>();
        for (Path source : businessContractTestSources()) {
            String content = Files.readString(source);
            int testMethods = count(TEST_METHOD, content);
            int narrativeCalls = count(NARRATIVE_CALL, content);
            if (!content.contains("@Listeners(TestListener.class)")) {
                violations.add(relative(source) + " is missing TestListener.");
            }
            if (narrativeCalls < testMethods) {
                violations.add(relative(source) + " has " + testMethods
                        + " @Test methods but only " + narrativeCalls
                        + " BusinessUnderstandingReportNarrative.verify calls.");
            }
            if (DIRECT_STEP_LOGGING.matcher(content).find()) {
                violations.add(relative(source) + " uses StepLogger directly instead of the shared narrative helper.");
            }
        }

        BusinessUnderstandingReportNarrative.verify(
                "business-understanding.extent-narrative.semantic-completeness",
                "Business Understanding contract tests must publish expected contract, observed validator result, cold verdict and repair hint to Extent.",
                "Scanned business contract test sources under " + relative(BUSINESS_TEST_ROOT)
                        + "; violations=" + violations,
                "Wrap business-understanding assertions in BusinessUnderstandingReportNarrative.verify(...).",
                () -> Assert.assertTrue(violations.isEmpty(),
                        "Business-understanding tests with incomplete Extent narrative: " + violations));
    }

    private static List<Path> businessContractTestSources() throws IOException {
        try (Stream<Path> files = Files.list(BUSINESS_TEST_ROOT)) {
            return files.filter(AtfSourcePaths::isJavaSource)
                    .filter(path -> path.getFileName().toString().endsWith("ContractTest.java"))
                    .filter(path -> !path.getFileName().toString().equals(
                            BusinessUnderstandingExtentNarrativeContractTest.class.getSimpleName() + ".java"))
                    .collect(Collectors.toList());
        }
    }

    private static int count(Pattern pattern, String content) {
        Matcher matcher = pattern.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static String relative(Path path) {
        return AtfSourcePaths.unixPath(AtfSourcePaths.workspaceRoot().relativize(path.normalize()));
    }
}
