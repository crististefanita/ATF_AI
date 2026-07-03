package com.endava.ai.atf.core.support.architecture;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.support.contract.core.AtfCoreArea;
import com.endava.ai.core.support.contract.core.AtfCoreCheck;
import com.endava.ai.core.support.contract.core.AtfCoreContract;
import com.endava.ai.core.support.contract.core.AtfCoreEvidenceKind;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlBoundarySpec;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.CORE_MCP_BOUNDARY_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.CORE_MCP_BOUNDARY_CONTRACT_TEST_CLASS)
@AtfCoreContract(
        id = "core.architecture.no-mcp-dependency",
        area = AtfCoreArea.ARCHITECTURE_BOUNDARY,
        owner = "ATF core architecture boundary",
        invariant = "core, ui.core and api.core do not depend on MCP package-audit classes or vocabulary.",
        repairHint = "Move MCP/package-audit vocabulary back to com.endava.ai.skills.mcp."
)
@AtfCoreCheck(
        contract = "core.architecture.no-mcp-dependency",
        evidenceKind = AtfCoreEvidenceKind.SOURCE_SCAN,
        evidence = "Scans core, ui.core and api.core source roots for MCP imports and package-audit vocabulary."
)
@Listeners(TestListener.class)
public class CoreMcpBoundaryContractTest {

    private static final String CONTRACT_ID = "core.architecture.no-mcp-dependency";

    @Test
    public void coreDoesNotDependOnMcpOrPackageAuditVocabulary() throws IOException {
        List<String> violations = new ArrayList<>();
        AtfCoreYamlBoundarySpec boundary = boundary();
        for (String rootValue : boundary.scanRoots()) {
            Path root = Path.of(rootValue);
            if (!Files.isDirectory(root)) {
                continue;
            }
            try (Stream<Path> files = Files.walk(root)) {
                files
                        .filter(Files::isRegularFile)
                        .filter(AtfSourcePaths::isJavaSource)
                        .forEach(path -> collectForbiddenTokens(path, boundary.forbidden(), violations));
            }
        }

        Assert.assertTrue(violations.isEmpty(),
                "ATF core must remain independent from MCP/package-audit vocabulary: " + violations);
    }

    private static AtfCoreYamlBoundarySpec boundary() {
        return AtfCoreYamlContractCatalog.boundaries().stream()
                .filter(spec -> CONTRACT_ID.equals(spec.id()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing ATF core YAML boundary: " + CONTRACT_ID));
    }

    private static void collectForbiddenTokens(Path path, List<String> forbiddenTokens, List<String> violations) {
        try {
            String source = Files.readString(path);
            for (String token : forbiddenTokens) {
                if (source.contains(token)) {
                    violations.add(path + " contains " + token);
                }
            }
        } catch (IOException e) {
            violations.add(path + " unreadable: " + e.getMessage());
        }
    }
}
