package com.endava.ai.atf.core.skills.architecture;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class AtfSkillArchitectureReviewReceiptPublisher {

    private AtfSkillArchitectureReviewReceiptPublisher() {
    }

    static Path publish(Path output, Map<String, String> cleanSanityProfile) throws IOException {
        Files.createDirectories(output.getParent());
        List<String> commands = commandsFrom(cleanSanityProfile);
        List<String> receiptFields = values(cleanSanityProfile.get("receiptFields"));
        String timestamp = Instant.now().toString();
        String commit = AtfSkillGitSnapshot.commit();
        String dirtyState = AtfSkillGitSnapshot.dirtyState();
        AtfSkillArchitectureReviewReceipt receipt = AtfSkillArchitectureReviewReceipt.fromProfile(
                cleanSanityProfile,
                commands,
                receiptFields,
                timestamp,
                commit,
                dirtyState
        );

        return publish(output, receipt);
    }

    static Path publish(Path output, AtfSkillArchitectureReviewReceipt receipt) throws IOException {
        Files.createDirectories(output.getParent());
        Path sidecar = jsonSidecar(output);

        String markdown = String.format("# Skill Architecture Review Receipt%n%n")
                + String.format("| Field | Value |%n")
                + String.format("|---|---|%n")
                + row("profile", receipt.profile())
                + row("status", AtfSkillArchitectureReviewReceipt.CONTRACT_RECORDED)
                + row("timestamp", receipt.timestamp())
                + row("commit", receipt.commit())
                + row("dirtyState", receipt.dirtyState())
                + row("overallStatus", receipt.overallStatus())
                + row("totalDurationMs", String.valueOf(receipt.totalDurationMs()))
                + row("executedCount", String.valueOf(receipt.executedCount()))
                + row("passedCount", String.valueOf(receipt.passedCount()))
                + row("failedCount", String.valueOf(receipt.failedCount()))
                + row("skippedAfterFailureCount", String.valueOf(receipt.skippedAfterFailureCount()))
                + row("stoppedAtCommand", receipt.stoppedAtCommand())
                + row("failedOwner", receipt.failedOwner())
                + row("receiptFields", String.join(", ", receipt.receiptFields()))
                + row("jsonSidecar", AtfSourcePaths.unixPath(sidecar))
                + String.format("%n## Commands%n%n")
                + receipt.commands().stream()
                .map(command -> "- `" + command.command() + "`")
                .collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator();

        Files.writeString(output, markdown, StandardCharsets.UTF_8);
        Files.writeString(sidecar, json(receipt), StandardCharsets.UTF_8);
        return output;
    }

    private static Path jsonSidecar(Path output) {
        String fileName = output.getFileName().toString();
        String sidecarName = fileName.endsWith(".md")
                ? fileName.substring(0, fileName.length() - 3) + ".json"
                : fileName + ".json";
        return output.resolveSibling(sidecarName);
    }

    private static String json(AtfSkillArchitectureReviewReceipt receipt) {
        return "{\n"
                + jsonField("schemaVersion", "1", 1, true)
                + jsonField("profile", receipt.profile(), 1, true)
                + jsonField("status", AtfSkillArchitectureReviewReceipt.CONTRACT_RECORDED, 1, true)
                + jsonField("timestamp", receipt.timestamp(), 1, true)
                + jsonField("commit", receipt.commit(), 1, true)
                + jsonField("dirtyState", receipt.dirtyState(), 1, true)
                + jsonField("overallStatus", receipt.overallStatus(), 1, true)
                + jsonNumberField("totalDurationMs", receipt.totalDurationMs(), 1, true)
                + jsonNumberField("executedCount", receipt.executedCount(), 1, true)
                + jsonNumberField("passedCount", receipt.passedCount(), 1, true)
                + jsonNumberField("failedCount", receipt.failedCount(), 1, true)
                + jsonNumberField("skippedAfterFailureCount", receipt.skippedAfterFailureCount(), 1, true)
                + jsonField("stoppedAtCommand", receipt.stoppedAtCommand(), 1, true)
                + jsonField("failedOwner", receipt.failedOwner(), 1, true)
                + "  \"receiptFields\": ["
                + receipt.receiptFields().stream().map(AtfSkillArchitectureReviewReceiptPublisher::quoted)
                .collect(Collectors.joining(", "))
                + "],\n"
                + "  \"commands\": [\n"
                + commandJson(receipt.commands())
                + "\n  ]\n"
                + "}\n";
    }

    private static String commandJson(List<AtfSkillArchitectureReviewReceipt.Command> commands) {
        return commands.stream()
                .map(command -> "    {\n"
                        + "      \"owner\": " + quoted(command.owner()) + ",\n"
                        + "      \"command\": " + quoted(command.command()) + ",\n"
                        + "      \"executionStatus\": " + quoted(command.executionStatus()) + ",\n"
                        + "      \"exitCode\": " + exitCode(command) + ",\n"
                        + "      \"durationMs\": " + command.durationMs() + ",\n"
                        + "      \"artifactLinks\": ["
                        + command.artifactLinks().stream()
                        .map(AtfSkillArchitectureReviewReceiptPublisher::quoted)
                        .collect(Collectors.joining(", "))
                        + "],\n"
                        + "      \"failureSummary\": " + quoted(command.failureSummary()) + "\n"
                        + "    }")
                .collect(Collectors.joining(",\n"));
    }

    private static String exitCode(AtfSkillArchitectureReviewReceipt.Command command) {
        return command.exitCode() == null ? "null" : String.valueOf(command.exitCode());
    }

    private static String jsonField(String key, String value, int indent, boolean comma) {
        return " ".repeat(indent * 2) + quoted(key) + ": " + quoted(value) + (comma ? "," : "") + "\n";
    }

    private static String jsonNumberField(String key, long value, int indent, boolean comma) {
        return " ".repeat(indent * 2) + quoted(key) + ": " + value + (comma ? "," : "") + "\n";
    }

    private static String quoted(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static List<String> values(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return List.of(raw.split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }

    private static List<String> commandsFrom(Map<String, String> profile) {
        List<String> commands = new ArrayList<>();
        String cleanCompileCommand = profile.get("cleanCompileCommand");
        if (cleanCompileCommand != null && !cleanCompileCommand.isBlank()) {
            commands.add(cleanCompileCommand.trim());
        }
        commands.addAll(values(profile.get("commands")));
        return commands;
    }

    private static String row(String field, String value) {
        return String.format("| `%s` | %s |%n", field, value == null || value.isBlank() ? "n/a" : value);
    }

}
