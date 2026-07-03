package com.endava.ai.skills.codequality.tests.support.assertion;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CodeQualityAssertionDensityReceiptWriter {
    public static final Path ASSERTION_DENSITY_RECEIPT =
            AtfSourcePaths.TARGET_ROOT.resolve("code-quality-assertion-density.md");

    private CodeQualityAssertionDensityReceiptWriter() {
    }

    public static void publish(CodeQualityAssertionDensityPolicy policy,
                               List<CodeQualityAssertionDensity> classes,
                               long lowDensityClasses,
                               long zeroCueClasses) throws IOException {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Code Quality Assertion Density\n\n")
                .append("| Metric | Current | Max |\n")
                .append("|---|---:|---:|\n")
                .append("| Test classes below ").append(policy.getMinimumCuePercent()).append("% cue density | ")
                .append(lowDensityClasses).append(" | ").append(policy.getMaxLowDensityClasses()).append(" |\n")
                .append("| Test classes with zero assertion cues | ")
                .append(zeroCueClasses).append(" | ").append(policy.getMaxZeroCueClasses()).append(" |\n\n")
                .append("## Lowest Density Test Classes\n\n")
                .append("| Cue % | Test methods | Assertion cues | Source |\n")
                .append("|---:|---:|---:|---|\n");

        classes.stream().limit(25).forEach(testClass -> markdown
                .append("| ").append(testClass.cuePercent())
                .append(" | ").append(testClass.getTestMethods())
                .append(" | ").append(testClass.getAssertionCues())
                .append(" | `").append(testClass.getSource()).append("` |\n"));

        Files.createDirectories(ASSERTION_DENSITY_RECEIPT.getParent());
        Files.writeString(ASSERTION_DENSITY_RECEIPT, markdown.toString());
    }
}
