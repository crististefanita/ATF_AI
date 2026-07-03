package com.endava.ai.skills.mcp.validation.diagnostics;

import com.endava.ai.skills.mcp.McpPackageViolation;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class McpViolationFormatter {
    private McpViolationFormatter() {
    }

    public static String format(Path packageRoot, List<McpPackageViolation> blockers) {
        return "\nPackage: " + packageRoot + "\n"
                + "Blockers: " + blockers.size() + "\n"
                + blockers.stream()
                .map(violation -> "- " + violation)
                .collect(Collectors.joining("\n"));
    }
}
