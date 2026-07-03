package com.ai.atf.core.skills.architecture;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class AtfSkillArchitectureReviewReceipt {
    static final String CONTRACT_RECORDED = "CONTRACT_RECORDED";
    static final String NOT_EXECUTED_BY_RECEIPT = "NOT_EXECUTED_BY_RECEIPT";
    static final String PASS = "PASS";
    static final String FAIL = "FAIL";
    static final String SKIPPED_AFTER_FAILURE = "SKIPPED_AFTER_FAILURE";

    private final String profile;
    private final String timestamp;
    private final String commit;
    private final String dirtyState;
    private final List<String> receiptFields;
    private final List<Command> commands;

    private AtfSkillArchitectureReviewReceipt(
            String profile,
            String timestamp,
            String commit,
            String dirtyState,
            List<String> receiptFields,
            List<Command> commands
    ) {
        this.profile = profile;
        this.timestamp = timestamp;
        this.commit = commit;
        this.dirtyState = dirtyState;
        this.receiptFields = List.copyOf(receiptFields);
        this.commands = List.copyOf(commands);
    }

    static AtfSkillArchitectureReviewReceipt fromProfile(
            Map<String, String> profile,
            List<String> commands,
            List<String> receiptFields,
            String timestamp,
            String commit,
            String dirtyState
    ) {
        return new AtfSkillArchitectureReviewReceipt(
                profile.getOrDefault("id", "unknown"),
                timestamp,
                commit,
                dirtyState,
                receiptFields,
                commands.stream()
                        .map(Command::planned)
                        .collect(Collectors.toList())
        );
    }

    static AtfSkillArchitectureReviewReceipt fromCommands(
            Map<String, String> profile,
            List<Command> commands,
            List<String> receiptFields,
            String timestamp,
            String commit,
            String dirtyState
    ) {
        return new AtfSkillArchitectureReviewReceipt(
                profile.getOrDefault("id", "unknown"),
                timestamp,
                commit,
                dirtyState,
                receiptFields,
                commands
        );
    }

    String profile() { return profile; }

    String timestamp() { return timestamp; }

    String commit() { return commit; }

    String dirtyState() { return dirtyState; }

    List<String> receiptFields() { return receiptFields; }

    List<Command> commands() { return commands; }

    String overallStatus() {
        if (failedCount() > 0) {
            return FAIL;
        }
        if (executedCount() > 0) {
            return PASS;
        }
        return NOT_EXECUTED_BY_RECEIPT;
    }

    long totalDurationMs() {
        return commands.stream()
                .mapToLong(Command::durationMs)
                .sum();
    }

    long executedCount() {
        return commands.stream()
                .filter(command -> PASS.equals(command.executionStatus()) || FAIL.equals(command.executionStatus()))
                .count();
    }

    long passedCount() {
        return countStatus(PASS);
    }

    long failedCount() {
        return countStatus(FAIL);
    }

    long skippedAfterFailureCount() {
        return countStatus(SKIPPED_AFTER_FAILURE);
    }

    String stoppedAtCommand() {
        return commands.stream()
                .filter(command -> FAIL.equals(command.executionStatus()))
                .map(Command::command)
                .findFirst()
                .orElse("");
    }

    String failedOwner() {
        return commands.stream()
                .filter(command -> FAIL.equals(command.executionStatus()))
                .map(Command::owner)
                .findFirst()
                .orElse("");
    }

    private long countStatus(String status) {
        return commands.stream()
                .filter(command -> status.equals(command.executionStatus()))
                .count();
    }

    static String ownerOf(String command) {
        if (command.contains("mvn -q clean") || command.contains("test-compile")) {
            return "skill.core";
        }
        if (command.contains("skills/business")) {
            return "skill.business-understanding";
        }
        if (command.contains("atf/core/reporting")) {
            return "skill.reporting";
        }
        if (command.contains("skills/mcp")) {
            return "skill.mcp";
        }
        if (command.contains("skills/atlas")) {
            return "skill.atlas";
        }
        if (command.contains("atf/core")) {
            return "skill.core";
        }
        return "unknown";
    }

    static final class Command {
        private final String owner;
        private final String command;
        private final String executionStatus;
        private final Integer exitCode;
        private final long durationMs;
        private final List<String> artifactLinks;
        private final String failureSummary;

        private Command(
                String owner,
                String command,
                String executionStatus,
                Integer exitCode,
                long durationMs,
                List<String> artifactLinks,
                String failureSummary
        ) {
            this.owner = owner;
            this.command = command;
            this.executionStatus = executionStatus;
            this.exitCode = exitCode;
            this.durationMs = durationMs;
            this.artifactLinks = List.copyOf(artifactLinks);
            this.failureSummary = failureSummary;
        }

        static Command planned(String command) {
            return new Command(ownerOf(command), command, NOT_EXECUTED_BY_RECEIPT, null, 0L, List.of(), "");
        }

        static Command executed(
                String command,
                int exitCode,
                long durationMs,
                List<String> artifactLinks,
                String failureSummary
        ) {
            return new Command(
                    ownerOf(command),
                    command,
                    exitCode == 0 ? PASS : FAIL,
                    exitCode,
                    Math.max(0L, durationMs),
                    artifactLinks,
                    failureSummary == null ? "" : failureSummary
            );
        }

        static Command skipped(String command) {
            return new Command(ownerOf(command), command, SKIPPED_AFTER_FAILURE, null, 0L, List.of(), "");
        }

        String owner() {
            return owner;
        }

        String command() {
            return command;
        }

        String executionStatus() {
            return executionStatus;
        }

        Integer exitCode() {
            return exitCode;
        }

        long durationMs() {
            return durationMs;
        }

        List<String> artifactLinks() {
            return artifactLinks;
        }

        String failureSummary() {
            return failureSummary;
        }
    }
}
