package com.ai.skills.mcp.tests.contracts.prompt;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.contracts.execution.McpAgenticExecutionStepSpec;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.HTML_CONTRACT_APPLIES_TO;
import static com.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.HTML_CONTRACT_OWNERS;
import static com.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.HTML_CONTRACT_OWNERSHIP;
import static com.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.HTML_CONTRACT_USAGES;
import static com.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.HTML_EN_PROMPT_ROOT;
import static com.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.HTML_RO_PROMPT_ROOT;
import static com.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.MCP_AGENTIC_PROFILE_PROMPTS;
import static com.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.PROJECT_SHARED_REPORT_CSS;
import static com.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.PROMPT_FILE_REFERENCE;
import static com.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.PROMPT_OWNERSHIP_ROOT;
import static com.ai.skills.mcp.tests.contracts.support.McpYamlContractSchemaExpectations.PROMPT_SHARED_REPORT_CSS;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-prompt-contract-ownership-test")
public class McpPromptContractOwnershipTest {

    @Test
    @McpCheckRef("mcp.yaml-contract.html-contract-ownership")
    public void htmlEnPromptContractsHaveExplicitOwnershipAndSuiteReferences() throws IOException {
        List<Map<String, String>> contracts = htmlContractOwnership();
        Map<String, Map<String, String>> byFile = contracts.stream()
                .collect(Collectors.toUnmodifiableMap(entry -> required(entry, "file"), entry -> entry));
        Set<String> activeFiles = activeHtmlEnPromptFiles();
        Set<String> htmlRoFiles = activePromptFiles(HTML_RO_PROMPT_ROOT);
        Set<String> declaredFiles = byFile.keySet();
        Set<String> referencedFiles = referencedHtmlEnPromptFiles(activeFiles);

        Assert.assertEquals(activeFiles, htmlRoFiles,
                "docs/prompts/html_EN must remain the English representation of docs/prompts/html_ro with the same owner subfolders.");
        Assert.assertEquals(declaredFiles, activeFiles,
                "Every active docs/prompts/html_EN prompt contract file must have one owner.");
        for (Map<String, String> entry : contracts) {
            String file = required(entry, "file");
            String owner = required(entry, "owner");
            Set<String> appliesTo = scalarSet(required(entry, "appliesTo"));
            String usage = required(entry, "usage");

            Assert.assertTrue(HTML_CONTRACT_OWNERS.contains(owner),
                    "Invalid html contract owner for " + file + ": " + owner);
            Assert.assertFalse(appliesTo.isEmpty(), "Missing appliesTo for " + file);
            Assert.assertTrue(HTML_CONTRACT_APPLIES_TO.containsAll(appliesTo),
                    "Invalid appliesTo values for " + file + ": " + appliesTo);
            Assert.assertTrue(appliesTo.contains(owner) || "Shared".equals(owner),
                    "Owner must be part of appliesTo unless the file is Shared: " + file);
            Assert.assertFalse(required(entry, "role").isBlank(), "Missing role for " + file);
            Assert.assertTrue(HTML_CONTRACT_USAGES.contains(usage), "Invalid usage for " + file + ": " + usage);
            if ("referenced".equals(usage)) {
                Assert.assertTrue(referencedFiles.contains(file),
                        "Referenced html contract file must be cited by MCP/Atlas suite metadata: " + file);
            }
        }
        for (String owner : Set.of("MCP", "Atlas", "Shared")) {
            Assert.assertTrue(hasOwnershipReadme(owner),
                    "Prompt owner must have a visible docs/prompts/<owner>/README.md index: " + owner);
        }

        Set<String> registeredReferences = referencedFiles.stream()
                .filter(activeFiles::contains)
                .collect(Collectors.toUnmodifiableSet());
        Assert.assertTrue(declaredFiles.containsAll(registeredReferences),
                "All suite references that point into docs/prompts/html_EN must be owned: " + registeredReferences);
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.prompt-shared-css-asset")
    public void promptHtmlDocumentsUseLanguageNeutralSharedCssAsset() throws IOException {
        Assert.assertTrue(Files.isRegularFile(PROMPT_SHARED_REPORT_CSS),
                "Prompt HTML documents must use a language-neutral stylesheet asset: " + PROMPT_SHARED_REPORT_CSS);
        Assert.assertFalse(Files.exists(Path.of("docs", "prompts", "shared-report-reference.css")),
                "Do not keep the shared stylesheet at docs/prompts root; use docs/prompts/assets instead.");
        Assert.assertEquals(Files.readString(PROMPT_SHARED_REPORT_CSS), Files.readString(PROJECT_SHARED_REPORT_CSS),
                "Prompt stylesheet asset must stay an exact copy of the project canonical report stylesheet.");

        List<Path> htmlFiles = Stream.concat(promptHtmlFiles(HTML_EN_PROMPT_ROOT), promptHtmlFiles(HTML_RO_PROMPT_ROOT))
                .collect(Collectors.toUnmodifiableList());
        Assert.assertFalse(htmlFiles.isEmpty(), "Expected prompt HTML files under html_EN/html_ro.");
        List<String> missingAssetLinks = htmlFiles.stream()
                .filter(path -> !contains(path, "../../assets/shared-report-reference.css"))
                .map(Path::toString)
                .collect(Collectors.toUnmodifiableList());
        Assert.assertTrue(missingAssetLinks.isEmpty(),
                "Prompt HTML files must link ../../assets/shared-report-reference.css: " + missingAssetLinks);
    }

    @Test
    @McpCheckRef("mcp.yaml-contract.prompt-profile-flow-coverage")
    public void mcpPromptDocumentsCoverEveryAgenticExecutionProfile() throws IOException {
        List<String> suiteFileNames = agenticFlowSuiteFileNames();

        for (Path prompt : MCP_AGENTIC_PROFILE_PROMPTS) {
            String text = Files.readString(prompt);
            for (String suiteFileName : suiteFileNames) {
                Assert.assertTrue(text.contains(suiteFileName),
                        "MCP prompt must mention active execution suite " + suiteFileName + ": " + prompt);
            }
        }
    }

    private static List<String> agenticFlowSuiteFileNames() {
        return ContractCatalog.agenticExecutionFlow().stream()
                .map(McpAgenticExecutionStepSpec::suiteXml)
                .map(path -> Path.of(path).getFileName().toString())
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<Map<String, String>> htmlContractOwnership() throws IOException {
        return SimpleYamlContractLoader.loadText(
                Files.readString(HTML_CONTRACT_OWNERSHIP),
                HTML_CONTRACT_OWNERSHIP.toString().replace('\\', '/'));
    }

    private static Set<String> activeHtmlEnPromptFiles() throws IOException {
        return activePromptFiles(HTML_EN_PROMPT_ROOT);
    }

    private static Set<String> activePromptFiles(Path root) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(Files::isRegularFile)
                    .map(root::relativize)
                    .map(Path::toString)
                    .map(path -> path.replace('\\', '/'))
                    .filter(name -> name.endsWith(".html") || name.endsWith(".properties") || name.endsWith(".md"))
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    private static Set<String> referencedHtmlEnPromptFiles(Set<String> activeFiles) {
        Map<String, String> activeByFileName = activeFiles.stream()
                .collect(Collectors.toUnmodifiableMap(McpPromptContractOwnershipTest::fileName, file -> file));
        Stream<String> mcpReferences = ContractCatalog.mcpSuites().stream()
                .flatMap(spec -> promptReferences(spec.htmlReq()));
        Stream<String> atlasReferences = SimpleYamlContractLoader.load("skills/atlas/contracts/suites.yml").stream()
                .flatMap(entry -> promptReferences(entry.getOrDefault("htmlOwner", "")));
        return Stream.concat(mcpReferences, atlasReferences)
                .map(McpPromptContractOwnershipTest::fileName)
                .filter(activeByFileName::containsKey)
                .map(activeByFileName::get)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Stream<String> promptReferences(String text) {
        Matcher matcher = PROMPT_FILE_REFERENCE.matcher(text == null ? "" : text);
        Stream.Builder<String> references = Stream.builder();
        while (matcher.find()) {
            references.add(matcher.group(1));
        }
        return references.build();
    }

    private static Stream<Path> promptHtmlFiles(Path root) throws IOException {
        try (Stream<Path> files = Files.walk(root)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".html"))
                    .collect(Collectors.toUnmodifiableList())
                    .stream();
        }
    }

    private static boolean contains(Path path, String token) {
        try {
            return Files.readString(path).contains(token);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read prompt HTML " + path, e);
        }
    }

    private static String fileName(String path) {
        String normalized = path.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        return slash >= 0 ? normalized.substring(slash + 1) : normalized;
    }

    private static String required(Map<String, String> entry, String key) {
        String value = entry.get(key);
        Assert.assertNotNull(value, "Missing " + key + " in " + HTML_CONTRACT_OWNERSHIP + " for " + entry);
        Assert.assertFalse(value.trim().isEmpty(), "Blank " + key + " in " + HTML_CONTRACT_OWNERSHIP);
        return value.trim();
    }

    private static Set<String> scalarSet(String commaSeparated) {
        return Arrays.stream(commaSeparated.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean hasOwnershipReadme(String owner) {
        return Files.isRegularFile(PROMPT_OWNERSHIP_ROOT
                .resolve(owner.toLowerCase(java.util.Locale.ROOT))
                .resolve(AtfSourcePaths.README_FILE_NAME));
    }
}
