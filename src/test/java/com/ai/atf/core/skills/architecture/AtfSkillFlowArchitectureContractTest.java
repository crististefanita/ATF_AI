package com.ai.atf.core.skills.architecture;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.assertBefore;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.assertKnownSkill;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.assertRegularFile;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.entries;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.entriesByKind;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.entry;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.isSkillEntry;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.path;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.required;
import static com.ai.atf.core.skills.architecture.AtfSkillArchitectureContractSupport.values;

@Listeners(TestListener.class)
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.YAML_MANIFEST)
@AtfContractRef(AtfContractNarrativeIds.Contract.YAML_MANIFEST_CATALOG)
public class AtfSkillFlowArchitectureContractTest {
    private static final String SKILL_ARCHITECTURE_REVIEW_RECEIPT =
            "target/skill-architecture-review-receipt.md";
    private static final String SKILL_BOUNDARY_MATRIX = "target/skill-boundary-matrix.md";
    private static final String SKILL_CLEAN_SANITY = "atf-core/contracts/skills/skill-clean-sanity.yml";
    private static final String SKILL_REVIEW_LOOP = "atf-core/contracts/skills/skill-review-loop.yml";

    @Test
    public void generatedPackageCloseoutFlowOrdersCalibrationBeforeFinalReportCloseout() {
        List<Map<String, String>> flows = entriesByKind("flow").stream()
                .sorted(Comparator.comparingInt(entry -> Integer.parseInt(required(entry, "order"))))
                .collect(Collectors.toList());

        List<String> orderedIds = flows.stream()
                .map(entry -> required(entry, "id"))
                .collect(Collectors.toList());

        assertBefore(orderedIds, "flow.core.smoke", "flow.core.sanity");
        assertBefore(orderedIds, "flow.mcp.sanity", "flow.mcp.package-closeout");
        assertBefore(orderedIds, "flow.mcp.package-closeout", "flow.mcp.final-report-closeout");
        assertBefore(orderedIds, "flow.mcp.final-report-closeout", "flow.mcp.langgraph-final-report-validation");

        Map<String, String> useCase = entry("usecase.generated-package-closeout");
        List<String> suites = values(useCase, "suites");
        assertBefore(suites,
                suite(McpExecutionProfile.PACKAGE_CLOSEOUT),
                suite(McpExecutionProfile.FINAL_REPORT_CLOSEOUT));
        assertBefore(suites,
                suite(McpExecutionProfile.FINAL_REPORT_CLOSEOUT),
                suite(McpExecutionProfile.LANGGRAPH_FINAL_REPORT));
        assertBefore(suites,
                suite(McpExecutionProfile.SANITY),
                suite(McpExecutionProfile.RUN_PREFLIGHT));
        assertBefore(suites,
                suite(McpExecutionProfile.PACKAGE_AUDIT),
                suite(McpExecutionProfile.PACKAGE_CLOSEOUT));
        assertBefore(suites,
                suite(McpExecutionProfile.PACKAGE_CLOSEOUT),
                suite(McpExecutionProfile.POST_SYNC_SMOKE));

        Assert.assertFalse(suites.contains(suite(McpExecutionProfile.CANDIDATE)),
                "html-ex candidate promotion must stay outside generated-package closeout flow");
    }

    @Test
    public void skillArchitectureFlowEntriesReferenceKnownSkillsAndRunnableSuites() {
        List<Map<String, String>> flows = entriesByKind("flow");
        Set<String> skillIds = entries().stream()
                .filter(entry -> isSkillEntry(required(entry, "kind")))
                .map(entry -> required(entry, "id"))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Integer> orders = new LinkedHashSet<>();

        for (Map<String, String> flow : flows) {
            assertKnownSkill(flow, "owner", skillIds);
            Assert.assertTrue(orders.add(Integer.parseInt(required(flow, "order"))),
                    "Flow order values must be unique: " + required(flow, "id"));
            assertRegularFile(flow, "suiteXml");
            Assert.assertFalse(required(flow, "role").isBlank(),
                    "Flow must declare role: " + required(flow, "id"));
            Assert.assertFalse(required(flow, "gate").isBlank(),
                    "Flow must declare gate: " + required(flow, "id"));
        }
    }

    @Test
    public void skillReviewLoopKeepsSignalExploreGovernValidateExecutable() throws IOException {
        List<Map<String, String>> stages = SimpleYamlContractLoader.load(SKILL_REVIEW_LOOP).stream()
                .filter(entry -> "skill-review-stage".equals(requiredFrom(entry, "kind", SKILL_REVIEW_LOOP)))
                .sorted(Comparator.comparingInt(entry -> Integer.parseInt(requiredFrom(entry, "order", SKILL_REVIEW_LOOP))))
                .collect(Collectors.toList());

        Assert.assertEquals(stages.stream()
                        .map(entry -> requiredFrom(entry, "stage", SKILL_REVIEW_LOOP))
                        .collect(Collectors.toList()),
                List.of("Signal", "Explore", "Govern", "Validate"));
        assertReviewStage(stages.get(0), "failing-sanity", "reproducible signal");
        assertReviewStage(stages.get(1), "skill-architecture.yml", "calibration notes alone");
        assertReviewStage(stages.get(2), "do-not-raise-caps", "raising a cap");
        assertReviewStage(stages.get(3), "mcp-sanity", "parallel write-heavy Maven runs");
        Assert.assertEquals(requiredFrom(stages.get(3), "cleanSanityContract", SKILL_REVIEW_LOOP),
                SKILL_CLEAN_SANITY);
        Assert.assertTrue(Files.readString(path("src/main/resources/atf-core/contracts/README.md"))
                        .contains("skill-review-loop.yml"),
                "Core contract README must index skill-review-loop.yml.");
    }

    @Test
    public void skillCleanSanityChainDocumentsCleanCompileAndSequentialSkillGates() throws IOException {
        Map<String, String> profile = SimpleYamlContractLoader.load(SKILL_CLEAN_SANITY).stream()
                .filter(entry -> "skill.clean-sanity.chain".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing skill.clean-sanity.chain contract"));

        Assert.assertTrue(requiredFrom(profile, "cleanCompileCommand", SKILL_CLEAN_SANITY)
                        .contains("mvn -q clean"),
                "Clean sanity must start from clean Maven compilation.");
        Assert.assertEquals(valuesFrom(profile, "requiredOrder"),
                List.of("clean-compile", "core-sanity", "business-understanding-sanity",
                        "reporting-sanity", "mcp-sanity", "atlas-sanity"));
        String commands = requiredFrom(profile, "commands", SKILL_CLEAN_SANITY);
        for (String suite : List.of(
                "src/test/resources/atf/core/suites/core/testng-atf-core-sanity.xml",
                "src/test/resources/skills/business/testng-business-understanding-sanity.xml",
                "src/test/resources/atf/core/reporting/testng-reporting-sanity.xml",
                "src/test/resources/skills/mcp/testng-mcp-sanity.xml",
                "src/test/resources/skills/atlas/testng-atlas-sanity.xml")) {
            Assert.assertTrue(commands.contains(suite), "Clean sanity chain must include " + suite);
        }
        Assert.assertTrue(requiredFrom(profile, "receiptFields", SKILL_CLEAN_SANITY).contains("dirtyState"));
        Assert.assertTrue(requiredFrom(profile, "receiptFields", SKILL_CLEAN_SANITY).contains("overallStatus"));
        Assert.assertTrue(requiredFrom(profile, "receiptFields", SKILL_CLEAN_SANITY).contains("totalDurationMs"));
        Assert.assertTrue(requiredFrom(profile, "receiptFields", SKILL_CLEAN_SANITY).contains("stoppedAtCommand"));
        Assert.assertTrue(requiredFrom(profile, "receiptFields", SKILL_CLEAN_SANITY).contains("failedOwner"));
        Assert.assertTrue(requiredFrom(profile, "antiPattern", SKILL_CLEAN_SANITY).contains("parallel Maven"));
        Assert.assertTrue(Files.readString(path("src/main/resources/atf-core/contracts/README.md"))
                        .contains("skill-clean-sanity.yml"),
                "Core contract README must index skill-clean-sanity.yml.");
    }

    @Test
    public void skillCleanSanityPublishesMachineReadableReviewReceipt() throws IOException {
        Map<String, String> profile = SimpleYamlContractLoader.load(SKILL_CLEAN_SANITY).stream()
                .filter(entry -> "skill.clean-sanity.chain".equals(entry.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing skill.clean-sanity.chain contract"));

        Path receipt = AtfSkillArchitectureReviewReceiptPublisher.publish(
                path(SKILL_ARCHITECTURE_REVIEW_RECEIPT),
                profile);
        String published = Files.readString(receipt);
        Path sidecar = receipt.resolveSibling("skill-architecture-review-receipt.json");
        String sidecarJson = Files.readString(sidecar);

        for (String token : List.of(
                "Skill Architecture Review Receipt",
                "`profile`",
                "`status`",
                "`timestamp`",
                "`commit`",
                "`dirtyState`",
                "`overallStatus`",
                "`totalDurationMs`",
                "`executedCount`",
                "`skippedAfterFailureCount`",
                "`jsonSidecar`",
                "mvn -q clean",
                "src/test/resources/atf/core/suites/core/testng-atf-core-sanity.xml",
                "src/test/resources/skills/mcp/testng-mcp-sanity.xml",
                "src/test/resources/skills/atlas/testng-atlas-sanity.xml")) {
            Assert.assertTrue(published.contains(token),
                    "Generated architecture review receipt must contain " + token);
        }
        for (String token : List.of(
                "\"schemaVersion\": \"1\"",
                "\"status\": \"CONTRACT_RECORDED\"",
                "\"overallStatus\": \"NOT_EXECUTED_BY_RECEIPT\"",
                "\"totalDurationMs\": 0",
                "\"executedCount\": 0",
                "\"passedCount\": 0",
                "\"failedCount\": 0",
                "\"skippedAfterFailureCount\": 0",
                "\"owner\": \"skill.core\"",
                "\"owner\": \"skill.business-understanding\"",
                "\"owner\": \"skill.reporting\"",
                "\"owner\": \"skill.mcp\"",
                "\"owner\": \"skill.atlas\"",
                "\"executionStatus\": \"NOT_EXECUTED_BY_RECEIPT\"",
                "\"exitCode\": null")) {
            Assert.assertTrue(sidecarJson.contains(token),
                    "Generated architecture review JSON sidecar must contain " + token);
        }
        Assert.assertFalse(sidecarJson.contains("\"executionStatus\": \"PASS\""),
                "Contract receipts must not pretend commands were executed.");
    }

    @Test
    public void skillArchitectureUseCasesDeclarePromptReadOrder() {
        for (Map<String, String> useCase : entriesByKind("use-case")) {
            List<String> promptDocs = values(useCase, "promptDocs");
            Assert.assertFalse(promptDocs.isEmpty(),
                    required(useCase, "id") + " must declare canonical prompt/readme documents");
            for (String promptDoc : promptDocs) {
                Assert.assertTrue(Files.isRegularFile(path(promptDoc)),
                        required(useCase, "id") + " references missing prompt/readme document " + promptDoc);
            }
        }

        Map<String, String> mcpCloseout = entry("usecase.generated-package-closeout");
        List<String> mcpDocs = values(mcpCloseout, "promptDocs");
        Assert.assertEquals(mcpDocs.get(0), "docs/prompts/html_ro/mcp/orchestrate-iterative-runs.html",
                "MCP generated-package skill must start from orchestrate-iterative-runs.html");
        assertBefore(mcpDocs,
                "docs/prompts/html_ro/mcp/orchestrate-iterative-runs.html",
                "docs/prompts/html_ro/mcp/execute-and-understand-run.html");
        assertBefore(mcpDocs,
                "docs/prompts/html_ro/mcp/execute-and-understand-run.html",
                "docs/prompts/html_ro/mcp/reporting.html");

        Assert.assertEquals(values(entry("usecase.business-understanding-audit"), "promptDocs"),
                List.of("docs/prompts/html_ro/atlas/business-test-atlas-reference.html"),
                "Atlas use-case must route through the Atlas business-understanding prompt only");
    }

    @Test
    public void skillArchitecturePublishesReadableBoundaryMatrix() throws IOException {
        Path artifact = AtfSkillBoundaryMatrixPublisher.publish(entries(), path(SKILL_BOUNDARY_MATRIX));
        String published = Files.readString(artifact);
        for (String token : List.of(
                "Skill Boundary Matrix",
                "Generated from `atf-core/contracts/skills/skill-architecture.yml`",
                "`skill.core`",
                "`skill.reporting`",
                "`skill.business-understanding`",
                "`skill.mcp`",
                "`skill.atlas`",
                "`prompt.assets`",
                "`src/main/java/com/ai/core/reporting`",
                "`git-diff-extent`",
                "`source-structure`",
                "`flow.core.smoke`",
                "`flow.business-understanding.sanity`",
                "`flow.mcp.final-report-closeout`",
                "`flow.mcp.langgraph-final-report-validation`",
                "`usecase.generated-package-closeout`",
                "`src/main/resources/skills/reporting/contracts/reporting-package-shape.yml`",
                "`src/main/resources/skills/business/contracts/package-shape.yml`",
                "`src/main/resources/skills/mcp/contracts/package-shape.yml`",
                "`src/main/resources/skills/atlas/contracts/package-shape.yml`"
        )) {
            Assert.assertTrue(published.contains(token),
                    "Generated skill boundary matrix must contain " + token);
        }
    }

    private static String suite(McpExecutionProfile profile) {
        return profile.suiteXml();
    }

    private static void assertReviewStage(Map<String, String> stage, String evidenceToken, String antiPatternToken) {
        Assert.assertFalse(requiredFrom(stage, "role", SKILL_REVIEW_LOOP).isBlank(),
                requiredFrom(stage, "id", SKILL_REVIEW_LOOP) + " must explain its role.");
        Assert.assertTrue(requiredFrom(stage, "requiredEvidence", SKILL_REVIEW_LOOP).contains(evidenceToken),
                requiredFrom(stage, "id", SKILL_REVIEW_LOOP) + " must require " + evidenceToken);
        Assert.assertFalse(requiredFrom(stage, "sourceOfTruth", SKILL_REVIEW_LOOP).isBlank(),
                requiredFrom(stage, "id", SKILL_REVIEW_LOOP) + " must declare sourceOfTruth.");
        Assert.assertTrue(requiredFrom(stage, "antiPattern", SKILL_REVIEW_LOOP).contains(antiPatternToken),
                requiredFrom(stage, "id", SKILL_REVIEW_LOOP) + " must reject " + antiPatternToken);
    }

    private static String requiredFrom(Map<String, String> entry, String key, String resource) {
        return SimpleYamlContractLoader.required(entry, key, resource);
    }

    private static List<String> valuesFrom(Map<String, String> entry, String key) {
        return List.of(entry.getOrDefault(key, "").split(",")).stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toList());
    }
}
