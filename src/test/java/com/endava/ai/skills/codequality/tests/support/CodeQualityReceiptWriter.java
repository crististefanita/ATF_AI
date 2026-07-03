package com.endava.ai.skills.codequality.tests.support;

import com.endava.ai.skills.codequality.tests.support.compatibility.CodeQualityJavaCompatibilityRule;
import com.endava.ai.skills.codequality.tests.support.compatibility.CodeQualityJavaCompatibilityViolation;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.skills.codequality.tests.support.dependency.CodeQualityDependencyBoundary;
import com.endava.ai.skills.codequality.tests.support.dependency.CodeQualityImportViolation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class CodeQualityReceiptWriter {
    public static final Path PACKAGE_CLASS_COUNT_RECEIPT =
            AtfSourcePaths.TARGET_ROOT.resolve("code-quality-package-class-count.md");
    public static final Path CLASS_SIZE_RECEIPT =
            AtfSourcePaths.TARGET_ROOT.resolve("code-quality-class-size.md");
    public static final Path GOVERNANCE_RESOURCE_RECEIPT =
            AtfSourcePaths.TARGET_ROOT.resolve("code-quality-governance-resources.md");
    public static final Path DEPENDENCY_BOUNDARY_RECEIPT =
            AtfSourcePaths.TARGET_ROOT.resolve("code-quality-dependency-boundaries.md");
    public static final Path JAVA_COMPATIBILITY_RECEIPT =
            AtfSourcePaths.TARGET_ROOT.resolve("code-quality-java-compatibility.md");

    private CodeQualityReceiptWriter() {
    }

    public static void publishPackageClassCount(int defaultLimit,
                                                List<CodeQualityPackageClassCount> counts,
                                                Map<String, Integer> legacyCaps,
                                                List<String> violations,
                                                List<String> staleRatchets) throws IOException {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Code Quality Package Class Count\n\n")
                .append("| Metric | Value |\n")
                .append("|---|---:|\n")
                .append("| Default direct classes per package | ").append(defaultLimit).append(" |\n")
                .append("| Legacy package caps | ").append(legacyCaps.size()).append(" |\n")
                .append("| Violations | ").append(violations.size()).append(" |\n")
                .append("| Stale ratchets | ").append(staleRatchets.size()).append(" |\n\n")
                .append("## Largest Direct Packages\n\n")
                .append("| Classes | Package |\n")
                .append("|---:|---|\n");

        counts.stream().limit(25).forEach(count -> markdown
                .append("| ").append(count.getClasses())
                .append(" | `").append(count.getPath()).append("` |\n"));

        write(PACKAGE_CLASS_COUNT_RECEIPT, markdown);
    }

    public static void publishClassSize(CodeQualityClassSizePolicy policy,
                                        List<CodeQualitySourceSize> sources,
                                        Map<String, Integer> legacyCaps,
                                        long warningFiles,
                                        long overMaxFiles,
                                        List<String> violations,
                                        List<String> staleRatchets) throws IOException {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Code Quality Class Size\n\n")
                .append("| Metric | Current | Max |\n")
                .append("|---|---:|---:|\n")
                .append("| Java files >= warning threshold ").append(policy.getWarningLines()).append(" | ")
                .append(warningFiles).append(" | ").append(policy.getMaxWarningFiles()).append(" |\n")
                .append("| Java files > default max ").append(policy.getMaxLines()).append(" | ")
                .append(overMaxFiles).append(" | ").append(policy.getMaxOverMaxFiles()).append(" |\n")
                .append("| Legacy class-size caps | ").append(legacyCaps.size()).append(" | |\n")
                .append("| Violations | ").append(violations.size()).append(" | 0 |\n")
                .append("| Stale ratchets | ").append(staleRatchets.size()).append(" | 0 |\n\n")
                .append("## Largest Java Sources\n\n")
                .append("| Lines | Source |\n")
                .append("|---:|---|\n");

        sources.stream().limit(25).forEach(source -> markdown
                .append("| ").append(source.getLines())
                .append(" | `").append(source.getPath()).append("` |\n"));

        write(CLASS_SIZE_RECEIPT, markdown);
    }

    public static void publishGovernanceResources(CodeQualityYamlSizePolicy policy,
                                                  List<CodeQualityResourceSize> yamlResources,
                                                  Map<String, Integer> legacyCaps,
                                                  List<String> suites,
                                                  int maxSuites,
                                                  long warningFiles,
                                                  long overMaxFiles,
                                                  List<String> violations,
                                                  List<String> staleRatchets) throws IOException {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Code Quality Governance Resources\n\n")
                .append("| Metric | Current | Max |\n")
                .append("|---|---:|---:|\n")
                .append("| YAML files | ").append(yamlResources.size()).append(" | |\n")
                .append("| YAML files >= warning threshold ").append(policy.getWarningBytes()).append(" bytes | ")
                .append(warningFiles).append(" | ").append(policy.getMaxWarningFiles()).append(" |\n")
                .append("| YAML files > default max ").append(policy.getMaxBytes()).append(" bytes | ")
                .append(overMaxFiles).append(" | ").append(policy.getMaxOverMaxFiles()).append(" |\n")
                .append("| Legacy YAML caps | ").append(legacyCaps.size()).append(" | |\n")
                .append("| TestNG suites | ").append(suites.size()).append(" | ").append(maxSuites).append(" |\n")
                .append("| Violations | ").append(violations.size()).append(" | 0 |\n")
                .append("| Stale ratchets | ").append(staleRatchets.size()).append(" | 0 |\n\n")
                .append("## Largest YAML Resources\n\n")
                .append("| Bytes | Resource |\n")
                .append("|---:|---|\n");

        yamlResources.stream().limit(25).forEach(resource -> markdown
                .append("| ").append(resource.getBytes())
                .append(" | `").append(resource.getPath()).append("` |\n"));

        write(GOVERNANCE_RESOURCE_RECEIPT, markdown);
    }

    public static void publishDependencyBoundaries(List<CodeQualityDependencyBoundary> boundaries,
                                                   List<CodeQualityImportViolation> violations) throws IOException {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Code Quality Dependency Boundaries\n\n")
                .append("| Metric | Value |\n")
                .append("|---|---:|\n")
                .append("| Declared boundaries | ").append(boundaries.size()).append(" |\n")
                .append("| Violations | ").append(violations.size()).append(" |\n\n")
                .append("## Boundaries\n\n")
                .append("| Boundary | Root | Forbidden package |\n")
                .append("|---|---|---|\n");

        boundaries.forEach(boundary -> markdown
                .append("| `").append(boundary.getId())
                .append("` | `").append(boundary.getRoot())
                .append("` | `").append(boundary.getForbiddenPackage())
                .append("` |\n"));

        if (!violations.isEmpty()) {
            markdown.append("\n## Violations\n\n")
                    .append("| Boundary | Source | Line | Import |\n")
                    .append("|---|---|---:|---|\n");
            violations.forEach(violation -> markdown
                    .append("| `").append(violation.getBoundaryId())
                    .append("` | `").append(violation.getSource())
                    .append("` | ").append(violation.getLine())
                    .append(" | `").append(violation.getForbiddenImport())
                    .append("` |\n"));
        }

        write(DEPENDENCY_BOUNDARY_RECEIPT, markdown);
    }

    public static void publishJavaCompatibility(List<CodeQualityJavaCompatibilityRule> rules,
                                                List<CodeQualityJavaCompatibilityViolation> violations)
            throws IOException {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Code Quality Java Compatibility\n\n")
                .append("| Metric | Value |\n")
                .append("|---|---:|\n")
                .append("| Declared rules | ").append(rules.size()).append(" |\n")
                .append("| Violations | ").append(violations.size()).append(" |\n\n")
                .append("## Rules\n\n")
                .append("| Rule | Forbidden API |\n")
                .append("|---|---|\n");

        rules.forEach(rule -> markdown
                .append("| `").append(rule.getId())
                .append("` | `").append(rule.getForbiddenApi())
                .append("` |\n"));

        if (!violations.isEmpty()) {
            markdown.append("\n## Violations\n\n")
                    .append("| Rule | Source | Line | Snippet |\n")
                    .append("|---|---|---:|---|\n");
            violations.forEach(violation -> markdown
                    .append("| `").append(violation.getRuleId())
                    .append("` | `").append(violation.getSource())
                    .append("` | ").append(violation.getLine())
                    .append(" | `").append(violation.getSnippet().replace("|", "\\|"))
                    .append("` |\n"));
        }

        write(JAVA_COMPATIBILITY_RECEIPT, markdown);
    }

    private static void write(Path receipt, StringBuilder markdown) throws IOException {
        Files.createDirectories(receipt.getParent());
        Files.writeString(receipt, markdown.toString());
    }
}
