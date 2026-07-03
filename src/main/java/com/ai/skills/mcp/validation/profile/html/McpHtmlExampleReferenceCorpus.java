package com.ai.skills.mcp.validation.profile.html;

import com.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.ai.skills.mcp.contracts.candidate.McpHtmlExCorpusSpec;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpProjectLocator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public final class McpHtmlExampleReferenceCorpus {
    private static final String AUTO_PRIMARY_REFERENCE = "auto";
    private static final Pattern ACTIVE_EXAMPLE = Pattern.compile("(?m)^- `([^`]+)`\\s*$");
    private static final String ACTIVE_LIBRARY = "## Active Library";
    private static final String CURRENT_RANKING = "## Current Ranking";

    private final McpHtmlExCorpusSpec spec;
    private final Path root;
    private final Path readme;
    private final List<McpHtmlExampleReference> references;

    private McpHtmlExampleReferenceCorpus(McpHtmlExCorpusSpec spec,
                                          Path root,
                                          Path readme,
                                          List<McpHtmlExampleReference> references) {
        this.spec = spec;
        this.root = root;
        this.readme = readme;
        this.references = List.copyOf(references);
    }

    public static McpHtmlExampleReferenceCorpus load() throws IOException {
        McpHtmlExCorpusSpec spec = corpusSpec();
        Path root = McpProjectLocator.projectRoot()
                .resolve(System.getProperty(spec.rootProperty(), spec.corpusRoot()))
                .normalize();
        Path readme = root.resolve(McpArtifactPaths.README);
        if (!Files.isDirectory(root)) {
            throw new IOException("html-ex corpus root is missing: " + root);
        }
        if (!Files.isRegularFile(readme)) {
            throw new IOException("html-ex corpus README is missing: " + readme);
        }

        Set<String> activeExamples = activeExamples(Files.readString(readme));
        List<McpHtmlExampleReference> references = activeExamples.stream()
                .map(example -> root.resolve(example).normalize())
                .map(path -> reference(readme, path))
                .collect(Collectors.toUnmodifiableList());
        return new McpHtmlExampleReferenceCorpus(spec, root, readme, references);
    }

    public List<McpHtmlExampleReference> reusableReferences() {
        return references.stream()
                .filter(McpHtmlExampleReference::hasReusableCloseoutSpine)
                .filter(McpHtmlExampleReference::hasReusableReportShape)
                .collect(Collectors.toUnmodifiableList());
    }

    public McpHtmlExampleReference primaryReference() {
        if (AUTO_PRIMARY_REFERENCE.equalsIgnoreCase(spec.primaryStructuralReference())) {
            return reusableReferences().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No active docs/html-ex example exposes the reusable final-report/package-closeout spine "
                                    + "and current report template shape."));
        }
        return references.stream()
                .filter(reference -> spec.primaryStructuralReference().equals(reference.exampleName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Primary structural reference is not listed in the active html-ex corpus: "
                                + spec.primaryStructuralReference()));
    }

    public List<String> exampleNames() {
        return references.stream()
                .map(McpHtmlExampleReference::exampleName)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<String> directoryNames() throws IOException {
        try (var stream = Files.list(root)) {
            return stream
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    private static McpHtmlExCorpusSpec corpusSpec() {
        List<McpHtmlExCorpusSpec> specs = ContractCatalog.htmlExCorpusContracts();
        if (specs.size() != 1) {
            throw new IllegalStateException("html-ex-corpus.yml must declare exactly one corpus classifier contract.");
        }
        return specs.get(0);
    }

    private static McpHtmlExampleReference reference(Path readme, Path exampleRoot) {
        try {
            return McpHtmlExampleReference.fromExample(readme, exampleRoot);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load html-ex reference: " + exampleRoot, e);
        }
    }

    private static Set<String> activeExamples(String readme) {
        Matcher matcher = ACTIVE_EXAMPLE.matcher(activeLibrarySection(readme));
        Set<String> examples = new LinkedHashSet<>();
        while (matcher.find()) {
            examples.add(matcher.group(1).trim());
        }
        return examples;
    }

    private static String activeLibrarySection(String readme) {
        int start = readme.indexOf(ACTIVE_LIBRARY);
        int end = readme.indexOf(CURRENT_RANKING);
        if (start < 0 || end <= start) {
            throw new IllegalStateException("docs/html-ex README must contain Active Library before Current Ranking.");
        }
        return readme.substring(start, end);
    }
}
