package com.ai.skills.mcp.tests.candidate;

import com.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.step.StepScope;
import com.ai.skills.mcp.McpGeneratedPackageValidator;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpProjectLocator;
import com.ai.skills.mcp.contract.checks.McpCheckRef;
import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.contracts.candidate.McpHtmlExCorpusSpec;
import com.ai.skills.mcp.validation.profile.html.McpHtmlExampleReference;
import com.ai.skills.mcp.validation.profile.html.McpHtmlExampleReferenceCorpus;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-html-example-corpus-contract-test")
public class McpHtmlExampleCorpusContractTest {
    @Test
    @McpCheckRef("mcp.html-ex-corpus.classification-matrix")
    public void auditsHtmlExampleCorpusAndWritesClassificationMatrix() throws IOException {
        McpHtmlExCorpusSpec spec = corpusSpec();
        McpHtmlExampleReferenceCorpus corpus = McpHtmlExampleReferenceCorpus.load();
        Path root = corpus.root();
        Path matrixArtifact = McpProjectLocator.projectRoot().resolve(spec.matrixArtifact()).normalize();
        Assert.assertTrue(Files.isDirectory(root), "Missing html-ex corpus root: " + root);

        List<Path> examples = corpus.references().stream()
                .map(McpHtmlExampleReference::root)
                .collect(Collectors.toList());
        Assert.assertTrue(examples.size() >= spec.minimumExamples(),
                "Expected at least " + spec.minimumExamples() + " active html-ex corpus examples under " + root);
        Path primaryReport = corpus.primaryReference().report();
        Assert.assertTrue(Files.isRegularFile(primaryReport),
                "Auto-selected html-ex structural reference is missing: " + primaryReport);

        StringBuilder matrix = new StringBuilder()
                .append("# MCP html-ex Corpus Matrix\n\n")
                .append("| Example | Classification | Candidate blockers | Warnings |\n")
                .append("|---|---|---:|---:|\n");
        for (Path example : examples) {
            McpPackageValidationResult result = McpGeneratedPackageValidator
                    .htmlExampleCandidate(McpProjectLocator.projectRoot())
                    .validate(example);
            matrix.append("| ")
                    .append(example.getFileName())
                    .append(" | ")
                    .append(classification(result))
                    .append(" | ")
                    .append(result.blockerCount())
                    .append(" | ")
                    .append(result.warningCount())
                    .append(" |\n");
        }

        Files.createDirectories(matrixArtifact.getParent());
        Files.writeString(matrixArtifact, matrix.toString());

        try (StepScope scope = StepLogger.step("HTML example corpus matrix")) {
            StepLogger.logDetail("Contract: " + spec.id());
            StepLogger.logDetail("Corpus root: " + root);
            StepLogger.logDetail("Auto-selected structural reference: " + primaryReport);
            StepLogger.logDetail("Matrix: " + matrixArtifact.toAbsolutePath().normalize());
            StepLogger.logCodeBlock(matrix.toString());
            scope.pass("Classified " + examples.size() + " html-ex corpus examples");
        }
    }

    private static McpHtmlExCorpusSpec corpusSpec() {
        List<McpHtmlExCorpusSpec> specs = ContractCatalog.htmlExCorpusContracts();
        Assert.assertEquals(specs.size(), 1, "html-ex-corpus.yml must declare exactly one corpus classifier contract.");
        return specs.get(0);
    }

    private static String classification(McpPackageValidationResult result) {
        if (!result.hasBlockers()) {
            return "mcp-green";
        }
        if (result.violations().stream().anyMatch(violation -> violation.code().startsWith("file."))) {
            return "invalid-under-current-contract";
        }
        return "historical-example";
    }
}
