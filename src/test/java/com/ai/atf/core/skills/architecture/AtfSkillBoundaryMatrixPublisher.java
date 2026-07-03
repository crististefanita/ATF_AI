package com.ai.atf.core.skills.architecture;

import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class AtfSkillBoundaryMatrixPublisher {
    private static final String RESOURCE = "atf-core/contracts/skills/skill-architecture.yml";
    private static final List<PackageShapeContract> PACKAGE_SHAPE_CONTRACTS = List.of(
            new PackageShapeContract(
                    "Core Reporting",
                    "skills/reporting/contracts/reporting-package-shape.yml",
                    "src/main/resources/skills/reporting/contracts/reporting-package-shape.yml"
            ),
            new PackageShapeContract(
                    "Business Understanding",
                    "skills/business/contracts/package-shape.yml",
                    "src/main/resources/skills/business/contracts/package-shape.yml"
            ),
            new PackageShapeContract(
                    "MCP",
                    "skills/mcp/contracts/package-shape.yml",
                    "src/main/resources/skills/mcp/contracts/package-shape.yml"
            ),
            new PackageShapeContract(
                    "Atlas",
                    "skills/atlas/contracts/package-shape.yml",
                    "src/main/resources/skills/atlas/contracts/package-shape.yml"
            )
    );

    private AtfSkillBoundaryMatrixPublisher() {
    }

    static Path publish(List<Map<String, String>> entries, Path artifact) throws IOException {
        Files.createDirectories(artifact.getParent());
        Files.writeString(artifact, render(entries), StandardCharsets.UTF_8);
        return artifact;
    }

    private static String render(List<Map<String, String>> entries) {
        StringBuilder markdown = new StringBuilder()
                .append("# Skill Boundary Matrix\n\n")
                .append("Generated from `").append(RESOURCE).append("` and the skill package-shape contracts.\n\n");

        appendSkillSurfaces(markdown, entries);
        appendPromptAssetContracts(markdown, entries);
        appendExecutionFlows(markdown, entries);
        appendUseCaseRouting(markdown, entries);
        appendPackageShapeGovernance(markdown);
        return markdown.toString();
    }

    private static void appendPromptAssetContracts(StringBuilder markdown, List<Map<String, String>> entries) {
        List<Map<String, String>> promptAssets = entries.stream()
                .filter(entry -> "prompt-assets".equals(required(entry, "kind")))
                .collect(Collectors.toList());
        if (promptAssets.isEmpty()) {
            return;
        }

        markdown.append("## Prompt Asset Contracts\n\n")
                .append("Prompt files are versioned contract assets owned by MCP, Atlas or shared Reporting primitives; ")
                .append("they are not an independent skill surface.\n\n")
                .append("| Contract | Owner | Role | Source | Validation suites | Consumes |\n")
                .append("| --- | --- | --- | --- | --- | --- |\n");
        promptAssets.forEach(contract -> markdown.append("| `").append(required(contract, "id")).append("` | ")
                .append(cell(required(contract, "owner"))).append(" | ")
                .append(cell(required(contract, "role"))).append(" | ")
                .append(codeCell(required(contract, "sourceRoot"))).append(" | ")
                .append(listCell(List.of(required(contract, "sanitySuite"), required(contract, "deepSuite")))).append(" | ")
                .append(listCell(values(contract, "consumes"))).append(" |\n"));
        markdown.append('\n');
    }

    private static void appendSkillSurfaces(StringBuilder markdown, List<Map<String, String>> entries) {
        markdown.append("## Skill Surfaces\n\n")
                .append("| Skill | Owner | Role | Source | Implementation | Inputs | Tests | Sanity | Deep | Consumes | Forbidden main imports | Forbidden test imports |\n")
                .append("| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |\n");

        entries.stream()
                .filter(AtfSkillBoundaryMatrixPublisher::isSkillEntry)
                .forEach(skill -> markdown.append("| `").append(required(skill, "id")).append("` | ")
                        .append(cell(required(skill, "owner"))).append(" | ")
                        .append(cell(required(skill, "role"))).append(" | ")
                        .append(codeCell(required(skill, "sourceRoot"))).append(" | ")
                        .append(optionalCodeCell(skill, "implementationRoot")).append(" | ")
                        .append(listCell(values(skill, "acceptedInputs"))).append(" | ")
                        .append(codeCell(required(skill, "testRoot"))).append(" | ")
                        .append(codeCell(required(skill, "sanitySuite"))).append(" | ")
                        .append(codeCell(required(skill, "deepSuite"))).append(" | ")
                        .append(listCell(values(skill, "consumes"))).append(" | ")
                        .append(listCell(values(skill, "forbiddenMainImports"))).append(" | ")
                        .append(listCell(values(skill, "forbiddenTestImports"))).append(" |\n"));
        markdown.append('\n');
    }

    private static void appendExecutionFlows(StringBuilder markdown, List<Map<String, String>> entries) {
        markdown.append("## Execution Flows\n\n")
                .append("| Order | Flow | Owner | Suite | Gate |\n")
                .append("| --- | --- | --- | --- | --- |\n");
        entries.stream()
                .filter(entry -> "flow".equals(required(entry, "kind")))
                .sorted(Comparator.comparingInt(entry -> Integer.parseInt(required(entry, "order"))))
                .forEach(flow -> markdown.append("| ")
                        .append(required(flow, "order")).append(" | `")
                        .append(required(flow, "id")).append("` | `")
                        .append(required(flow, "owner")).append("` | ")
                        .append(codeCell(required(flow, "suiteXml"))).append(" | ")
                        .append(cell(required(flow, "gate"))).append(" |\n"));
        markdown.append('\n');
    }

    private static void appendUseCaseRouting(StringBuilder markdown, List<Map<String, String>> entries) {
        markdown.append("## Use-Case Routing\n\n")
                .append("| Use-case | Primary skill | Support skills | Sanity first | Suites | Stop condition |\n")
                .append("| --- | --- | --- | --- | --- | --- |\n");
        entries.stream()
                .filter(entry -> "use-case".equals(required(entry, "kind")))
                .forEach(useCase -> markdown.append("| `").append(required(useCase, "id")).append("` | `")
                        .append(required(useCase, "primarySkill")).append("` | ")
                        .append(listCell(values(useCase, "supportSkills"))).append(" | ")
                        .append(cell(required(useCase, "sanityFirst"))).append(" | ")
                        .append(listCell(values(useCase, "suites"))).append(" | ")
                        .append(cell(required(useCase, "stopCondition"))).append(" |\n"));
        markdown.append('\n');
    }

    private static void appendPackageShapeGovernance(StringBuilder markdown) {
        markdown.append("## Package-Shape Governance\n\n")
                .append("| Surface | Contract | Entries | Required README checks | Forbidden import checks | Budget/source checks |\n")
                .append("| --- | --- | --- | --- | --- | --- |\n");
        for (PackageShapeContract contract : PACKAGE_SHAPE_CONTRACTS) {
            List<Map<String, String>> entries = SimpleYamlContractLoader.load(contract.resource());
            markdown.append("| ").append(cell(contract.owner())).append(" | `")
                    .append(contract.path()).append("` | ")
                    .append(entries.size()).append(" | ")
                    .append(countKindContaining(entries, "readme")).append(" | ")
                    .append(countKind(entries, "forbidden-import")).append(" | ")
                    .append(countKindsContaining(entries, List.of("budget", "cap", "source"))).append(" |\n");
        }
    }

    private static boolean isSkillEntry(Map<String, String> entry) {
        String kind = required(entry, "kind");
        return "skill".equals(kind);
    }

    private static long countKind(List<Map<String, String>> entries, String kind) {
        return entries.stream()
                .filter(entry -> kind.equals(entry.get("kind")))
                .count();
    }

    private static long countKindContaining(List<Map<String, String>> entries, String token) {
        return entries.stream()
                .map(entry -> entry.getOrDefault("kind", ""))
                .filter(kind -> kind.contains(token))
                .count();
    }

    private static long countKindsContaining(List<Map<String, String>> entries, List<String> tokens) {
        return entries.stream()
                .map(entry -> entry.getOrDefault("kind", ""))
                .filter(kind -> tokens.stream().anyMatch(kind::contains))
                .count();
    }

    private static String codeCell(String value) {
        return "`" + cell(value) + "`";
    }

    private static String optionalCodeCell(Map<String, String> entry, String key) {
        String value = entry.getOrDefault(key, "").trim();
        return value.isBlank() ? "" : codeCell(value);
    }

    private static String listCell(List<String> values) {
        if (values.isEmpty()) {
            return "";
        }
        return values.stream()
                .map(value -> "`" + cell(value) + "`")
                .collect(Collectors.joining("<br>"));
    }

    private static String cell(String value) {
        return value.replace("|", "\\|");
    }

    private static String required(Map<String, String> entry, String key) {
        return SimpleYamlContractLoader.required(entry, key, RESOURCE);
    }

    private static List<String> values(Map<String, String> entry, String key) {
        String raw = entry.getOrDefault(key, "").trim();
        if (raw.isBlank()) {
            return List.of();
        }
        return Stream.of(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }

    private static final class PackageShapeContract {
        private final String owner;
        private final String resource;
        private final String path;

        private PackageShapeContract(String owner, String resource, String path) {
            this.owner = owner;
            this.resource = resource;
            this.path = path;
        }

        private String owner() {
            return owner;
        }

        private String resource() {
            return resource;
        }

        private String path() {
            return path;
        }
    }
}
