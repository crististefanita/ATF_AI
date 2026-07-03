package com.endava.ai.atf.core.skills.architecture;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class AtfSkillCleanSanityRunnerContractTest {
    private static final String SKILL_CLEAN_SANITY = "atf-core/contracts/skills/skill-clean-sanity.yml";

    @Test
    public void cleanSanityRunnerWritesExecutionReceiptForSuccessfulCommands() throws IOException {
        RecordingExecutor executor = new RecordingExecutor(List.of(0, 0, 0, 0, 0, 0));
        Path receipt = Files.createTempDirectory("skill-clean-sanity-pass")
                .resolve("skill-architecture-execution-receipt.md");

        new AtfSkillCleanSanityRunner(executor).run(SKILL_CLEAN_SANITY, receipt);
        String json = Files.readString(receipt.resolveSibling("skill-architecture-execution-receipt.json"));

        Assert.assertEquals(executor.executedCommands.size(), 6);
        Assert.assertTrue(executor.executedCommands.get(0).contains("mvn -q clean"));
        Assert.assertEquals(count(json, "\"executionStatus\": \"PASS\""), 6);
        Assert.assertTrue(json.contains("\"durationMs\": 100"));
        Assert.assertTrue(json.contains("\"target/surefire-reports/TEST-command-1.xml\""));
        Assert.assertTrue(json.contains("command-01.out.log"));
        Assert.assertTrue(Files.isRegularFile(receipt.resolveSibling("command-01.out.log")));
        Assert.assertFalse(json.contains("\"executionStatus\": \"NOT_EXECUTED_BY_RECEIPT\""));
    }

    @Test
    public void cleanSanityRunnerStopsAfterFirstFailureAndMarksRemainingCommandsSkipped() throws IOException {
        RecordingExecutor executor = new RecordingExecutor(List.of(0, 1, 0, 0, 0, 0));
        Path receipt = Files.createTempDirectory("skill-clean-sanity-fail")
                .resolve("skill-architecture-execution-receipt.md");

        new AtfSkillCleanSanityRunner(executor).run(SKILL_CLEAN_SANITY, receipt);
        String json = Files.readString(receipt.resolveSibling("skill-architecture-execution-receipt.json"));

        Assert.assertEquals(executor.executedCommands.size(), 2);
        Assert.assertEquals(count(json, "\"executionStatus\": \"PASS\""), 1);
        Assert.assertEquals(count(json, "\"executionStatus\": \"FAIL\""), 1);
        Assert.assertEquals(count(json, "\"executionStatus\": \"SKIPPED_AFTER_FAILURE\""), 4);
        Assert.assertTrue(json.contains("\"failureSummary\": \"simulated failure 2\""));
        Assert.assertTrue(Files.isRegularFile(receipt.resolveSibling("command-02.out.log")));
        Assert.assertFalse(json.contains("\"executionStatus\": \"NOT_EXECUTED_BY_RECEIPT\""));
    }

    @Test
    public void commandEvidenceDiscoveryFindsSurefireAndExtentArtifacts() throws IOException {
        Path workspaceRoot = Files.createTempDirectory("skill-command-evidence");
        Files.createDirectories(workspaceRoot.resolve("target/surefire-reports"));
        Files.createDirectories(workspaceRoot.resolve("target/reports"));
        Files.writeString(workspaceRoot.resolve("target/surefire-reports/testng-results.xml"), "<testng/>");
        Files.writeString(workspaceRoot.resolve("target/surefire-reports/TEST-TestSuite.xml"), "<testsuite/>");
        Files.writeString(workspaceRoot.resolve("target/reports/ExtentReport.html"), "<html/>");

        List<String> artifacts = AtfSkillCommandEvidenceDiscovery.discover(workspaceRoot);

        Assert.assertTrue(artifacts.contains("target/surefire-reports/testng-results.xml"));
        Assert.assertTrue(artifacts.contains("target/surefire-reports/TEST-TestSuite.xml"));
        Assert.assertTrue(artifacts.contains("target/reports/ExtentReport.html"));
    }

    private static int count(String text, String token) {
        int count = 0;
        int index = text.indexOf(token);
        while (index >= 0) {
            count++;
            index = text.indexOf(token, index + token.length());
        }
        return count;
    }

    private static final class RecordingExecutor implements AtfSkillCleanSanityRunner.CommandExecutor {
        private final List<Integer> exitCodes;
        private final List<String> executedCommands = new ArrayList<>();

        private RecordingExecutor(List<Integer> exitCodes) {
            this.exitCodes = exitCodes;
        }

        @Override
        public AtfSkillCleanSanityRunner.CommandResult execute(String command) {
            executedCommands.add(command);
            int index = executedCommands.size();
            int exitCode = exitCodes.get(index - 1);
            return new AtfSkillCleanSanityRunner.CommandResult(
                    exitCode,
                    Duration.ofMillis(index * 100L),
                    List.of("target/surefire-reports/TEST-command-" + index + ".xml"),
                    exitCode == 0 ? "" : "simulated failure " + index
            );
        }
    }
}
