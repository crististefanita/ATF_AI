package com.endava.ai.atf.core.skills.architecture;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class AtfSkillExecutionReceiptValidatorContractTest {

    @Test
    public void executionReceiptPassRequiresReopenableEvidence() throws IOException {
        Path evidenceRoot = Files.createTempDirectory("skill-receipt-evidence");
        Files.createDirectories(evidenceRoot.resolve("target/surefire-reports"));
        Files.writeString(evidenceRoot.resolve("target/surefire-reports/TEST-command-1.xml"), "<testsuite/>");
        AtfSkillArchitectureReviewReceipt receipt = receiptWith(AtfSkillArchitectureReviewReceipt.Command.executed(
                "mvn -q test",
                0,
                100L,
                List.of("target/surefire-reports/TEST-command-1.xml"),
                ""
        ));

        List<String> violations = AtfSkillExecutionReceiptValidator.validate(receipt, evidenceRoot);

        Assert.assertTrue(violations.isEmpty(), "Expected valid receipt, got: " + violations);
    }

    @Test
    public void executionReceiptRejectsPassWithoutEvidence() throws IOException {
        Path evidenceRoot = Files.createTempDirectory("skill-receipt-missing-evidence");
        AtfSkillArchitectureReviewReceipt receipt = receiptWith(AtfSkillArchitectureReviewReceipt.Command.executed(
                "mvn -q test",
                0,
                100L,
                List.of(),
                ""
        ));

        List<String> violations = AtfSkillExecutionReceiptValidator.validate(receipt, evidenceRoot);

        Assert.assertTrue(violations.stream().anyMatch(text -> text.contains("no reopenable execution evidence")),
                "Expected missing evidence violation, got: " + violations);
    }

    @Test
    public void executionReceiptRejectsExecutedCommandWithoutPositiveDuration() throws IOException {
        Path evidenceRoot = Files.createTempDirectory("skill-receipt-zero-duration");
        Files.createDirectories(evidenceRoot.resolve("target/surefire-reports"));
        Files.writeString(evidenceRoot.resolve("target/surefire-reports/TEST-command-1.xml"), "<testsuite/>");
        AtfSkillArchitectureReviewReceipt receipt = receiptWith(AtfSkillArchitectureReviewReceipt.Command.executed(
                "mvn -q test",
                0,
                0L,
                List.of("target/surefire-reports/TEST-command-1.xml"),
                ""
        ));

        List<String> violations = AtfSkillExecutionReceiptValidator.validate(receipt, evidenceRoot);

        Assert.assertTrue(violations.stream().anyMatch(text -> text.contains("positive durationMs")),
                "Expected timing receipt violation, got: " + violations);
    }

    @Test
    public void executionReceiptRejectsFailWithoutFailureSummary() throws IOException {
        Path evidenceRoot = Files.createTempDirectory("skill-receipt-fail-evidence");
        Files.writeString(evidenceRoot.resolve("command-01.out.log"), "exit=1");
        AtfSkillArchitectureReviewReceipt receipt = receiptWith(AtfSkillArchitectureReviewReceipt.Command.executed(
                "mvn -q test",
                1,
                100L,
                List.of("command-01.out.log"),
                ""
        ));

        List<String> violations = AtfSkillExecutionReceiptValidator.validate(receipt, evidenceRoot);

        Assert.assertTrue(violations.stream().anyMatch(text -> text.contains("without a failure summary")),
                "Expected failure-summary violation, got: " + violations);
    }

    private static AtfSkillArchitectureReviewReceipt receiptWith(AtfSkillArchitectureReviewReceipt.Command command) {
        return AtfSkillArchitectureReviewReceipt.fromCommands(
                Map.of("id", "skill.clean-sanity.chain"),
                List.of(command),
                List.of("commit", "dirtyState", "command", "status", "timestamp", "profile"),
                "2026-07-02T00:00:00Z",
                "abc123",
                "clean"
        );
    }
}
