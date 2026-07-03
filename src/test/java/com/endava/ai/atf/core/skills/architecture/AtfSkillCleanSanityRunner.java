package com.endava.ai.atf.core.skills.architecture;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class AtfSkillCleanSanityRunner {
    private final CommandExecutor executor;

    AtfSkillCleanSanityRunner(CommandExecutor executor) {
        this.executor = executor;
    }

    Path run(String contractResource, Path output) throws IOException {
        Map<String, String> profile = SimpleYamlContractLoader.load(contractResource).stream()
                .filter(entry -> "skill.clean-sanity.chain".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing skill.clean-sanity.chain contract"));
        List<String> commands = commandsFrom(profile);
        List<AtfSkillArchitectureReviewReceipt.Command> receiptCommands = new ArrayList<>();
        boolean stopped = false;

        for (String command : commands) {
            if (stopped) {
                receiptCommands.add(AtfSkillArchitectureReviewReceipt.Command.skipped(command));
                continue;
            }
            CommandResult result = executor.execute(command);
            CommandResult resultWithEvidence = withCommandEvidence(output, receiptCommands.size() + 1, command, result);
            receiptCommands.add(AtfSkillArchitectureReviewReceipt.Command.executed(
                    command,
                    resultWithEvidence.exitCode(),
                    resultWithEvidence.duration().toMillis(),
                    resultWithEvidence.artifactLinks(),
                    resultWithEvidence.failureSummary()
            ));
            stopped = resultWithEvidence.exitCode() != 0;
        }

        AtfSkillArchitectureReviewReceipt receipt = AtfSkillArchitectureReviewReceipt.fromCommands(
                profile,
                receiptCommands,
                values(profile.get("receiptFields")),
                Instant.now().toString(),
                AtfSkillGitSnapshot.commit(),
                AtfSkillGitSnapshot.dirtyState()
        );
        return AtfSkillArchitectureReviewReceiptPublisher.publish(output, receipt);
    }

    static CommandExecutor processExecutor() {
        return command -> {
            Instant started = Instant.now();
            ProcessBuilder builder = new ProcessBuilder("powershell", "-NoProfile", "-Command", command);
            builder.directory(AtfSourcePaths.workspaceRoot().toFile());
            builder.redirectErrorStream(true);
            Process process = builder.start();
            int exitCode;
            String failureSummary;
            try {
                String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                exitCode = process.waitFor();
                failureSummary = exitCode == 0 ? "" : firstLine(output);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                exitCode = 130;
                failureSummary = "Interrupted while executing clean sanity command.";
            }
            return new CommandResult(
                    exitCode,
                    Duration.between(started, Instant.now()),
                    AtfSkillCommandEvidenceDiscovery.discover(AtfSourcePaths.workspaceRoot()),
                    failureSummary
            );
        };
    }

    interface CommandExecutor {
        CommandResult execute(String command) throws IOException;
    }

    static final class CommandResult {
        private final int exitCode;
        private final Duration duration;
        private final List<String> artifactLinks;
        private final String failureSummary;

        CommandResult(int exitCode, Duration duration, List<String> artifactLinks, String failureSummary) {
            this.exitCode = exitCode;
            this.duration = duration;
            this.artifactLinks = List.copyOf(artifactLinks);
            this.failureSummary = failureSummary == null ? "" : failureSummary;
        }

        int exitCode() {
            return exitCode;
        }

        Duration duration() {
            return duration;
        }

        List<String> artifactLinks() {
            return artifactLinks;
        }

        String failureSummary() {
            return failureSummary;
        }
    }

    private static CommandResult withCommandEvidence(
            Path output,
            int commandIndex,
            String command,
            CommandResult result
    ) throws IOException {
        Path evidence = commandEvidence(output, commandIndex);
        Files.createDirectories(evidence.getParent());
        Files.writeString(evidence, commandEvidenceBody(command, result), StandardCharsets.UTF_8);
        List<String> artifactLinks = new ArrayList<>(result.artifactLinks());
        artifactLinks.add(evidence.toString());
        return new CommandResult(
                result.exitCode(),
                result.duration(),
                artifactLinks,
                result.failureSummary()
        );
    }

    private static Path commandEvidence(Path output, int commandIndex) {
        return output.resolveSibling(String.format("command-%02d.out.log", commandIndex));
    }

    private static String commandEvidenceBody(String command, CommandResult result) {
        return "command=" + command + System.lineSeparator()
                + "exitCode=" + result.exitCode() + System.lineSeparator()
                + "durationMs=" + result.duration().toMillis() + System.lineSeparator()
                + "failureSummary=" + result.failureSummary() + System.lineSeparator();
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

    private static String firstLine(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.lines().findFirst().orElse("");
    }
}
