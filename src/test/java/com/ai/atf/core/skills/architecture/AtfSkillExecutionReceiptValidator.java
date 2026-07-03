package com.ai.atf.core.skills.architecture;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class AtfSkillExecutionReceiptValidator {

    private AtfSkillExecutionReceiptValidator() {
    }

    static List<String> validate(AtfSkillArchitectureReviewReceipt receipt, Path evidenceRoot) {
        List<String> violations = new ArrayList<>();
        for (AtfSkillArchitectureReviewReceipt.Command command : receipt.commands()) {
            validateCommand(command, evidenceRoot, violations);
        }
        return violations;
    }

    private static void validateCommand(
            AtfSkillArchitectureReviewReceipt.Command command,
            Path evidenceRoot,
            List<String> violations
    ) {
        if (AtfSkillArchitectureReviewReceipt.PASS.equals(command.executionStatus())) {
            validateExecutedDuration(command, violations);
            validatePassCommand(command, evidenceRoot, violations);
            return;
        }
        if (AtfSkillArchitectureReviewReceipt.FAIL.equals(command.executionStatus())) {
            validateExecutedDuration(command, violations);
            validateFailCommand(command, evidenceRoot, violations);
            return;
        }
        if (AtfSkillArchitectureReviewReceipt.SKIPPED_AFTER_FAILURE.equals(command.executionStatus())
                && command.exitCode() != null) {
            violations.add(command.command() + " is skipped but still exposes an exit code.");
        }
    }

    private static void validateExecutedDuration(
            AtfSkillArchitectureReviewReceipt.Command command,
            List<String> violations
    ) {
        if (command.durationMs() <= 0) {
            violations.add(command.command() + " was executed without a positive durationMs timing receipt.");
        }
    }

    private static void validatePassCommand(
            AtfSkillArchitectureReviewReceipt.Command command,
            Path evidenceRoot,
            List<String> violations
    ) {
        if (command.exitCode() == null || command.exitCode() != 0) {
            violations.add(command.command() + " claims PASS without exitCode=0.");
        }
        if (!command.failureSummary().isBlank()) {
            violations.add(command.command() + " claims PASS but still carries a failure summary.");
        }
        validateEvidenceLinks(command, evidenceRoot, violations);
    }

    private static void validateFailCommand(
            AtfSkillArchitectureReviewReceipt.Command command,
            Path evidenceRoot,
            List<String> violations
    ) {
        if (command.exitCode() == null || command.exitCode() == 0) {
            violations.add(command.command() + " claims FAIL without a non-zero exit code.");
        }
        if (command.failureSummary().isBlank()) {
            violations.add(command.command() + " claims FAIL without a failure summary.");
        }
        validateEvidenceLinks(command, evidenceRoot, violations);
    }

    private static void validateEvidenceLinks(
            AtfSkillArchitectureReviewReceipt.Command command,
            Path evidenceRoot,
            List<String> violations
    ) {
        if (command.artifactLinks().isEmpty()) {
            violations.add(command.command() + " has no reopenable execution evidence artifact.");
            return;
        }
        for (String artifactLink : command.artifactLinks()) {
            Path artifact = resolve(evidenceRoot, artifactLink);
            if (!Files.isRegularFile(artifact)) {
                violations.add(command.command() + " links missing execution evidence: " + artifactLink);
            }
        }
    }

    private static Path resolve(Path evidenceRoot, String artifactLink) {
        Path path = Path.of(artifactLink);
        return path.isAbsolute() ? path : evidenceRoot.resolve(path).normalize();
    }
}
