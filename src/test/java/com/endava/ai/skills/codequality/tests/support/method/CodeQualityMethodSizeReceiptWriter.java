package com.endava.ai.skills.codequality.tests.support.method;

import com.endava.ai.core.support.contract.ids.AtfSourcePaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class CodeQualityMethodSizeReceiptWriter {
    public static final Path METHOD_SIZE_RECEIPT =
            AtfSourcePaths.TARGET_ROOT.resolve("code-quality-method-size.md");

    private CodeQualityMethodSizeReceiptWriter() {
    }

    public static void publish(CodeQualityMethodSizePolicy policy,
                               List<CodeQualityMethodSize> methods,
                               long warningMethods,
                               long overMaxMethods) throws IOException {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Code Quality Method Size\n\n")
                .append("| Metric | Current | Max |\n")
                .append("|---|---:|---:|\n")
                .append("| Methods >= warning threshold ").append(policy.getWarningLines()).append(" | ")
                .append(warningMethods).append(" | ").append(policy.getMaxWarningMethods()).append(" |\n")
                .append("| Methods > default max ").append(policy.getMaxLines()).append(" | ")
                .append(overMaxMethods).append(" | ").append(policy.getMaxOverMaxMethods()).append(" |\n\n")
                .append("## Largest Methods\n\n")
                .append("| Lines | Method | Source |\n")
                .append("|---:|---|---|\n");

        methods.stream().limit(25).forEach(method -> markdown
                .append("| ").append(method.getLines())
                .append(" | `").append(method.getMethod()).append("`")
                .append(" | `").append(method.getSource()).append(":").append(method.getStartLine()).append("` |\n"));

        Files.createDirectories(METHOD_SIZE_RECEIPT.getParent());
        Files.writeString(METHOD_SIZE_RECEIPT, markdown.toString());
    }
}
