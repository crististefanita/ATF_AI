package com.endava.ai.mcp;

import com.endava.ai.mcp.context.McpPackageContext;
import com.endava.ai.mcp.contract.McpProfile;
import com.endava.ai.mcp.rules.McpDefaultRules;
import com.endava.ai.mcp.rules.McpValidationRule;

import java.nio.file.Path;
import java.util.List;

public final class McpGeneratedPackageValidator {
    private final Path projectRoot;
    private final List<McpValidationRule> rules;

    public McpGeneratedPackageValidator(Path projectRoot) {
        this(projectRoot, McpDefaultRules.create());
    }

    public static McpGeneratedPackageValidator htmlExampleCandidate(Path projectRoot) {
        return forProfile(projectRoot, McpProfile.HTML_EXAMPLE_CANDIDATE);
    }

    public static McpGeneratedPackageValidator forProfile(Path projectRoot, McpProfile profile) {
        return new McpGeneratedPackageValidator(projectRoot, McpDefaultRules.rulesFor(profile));
    }

    McpGeneratedPackageValidator(Path projectRoot, List<McpValidationRule> rules) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.rules = List.copyOf(rules);
    }

    public static void main(String[] args) {
        if (args.length != 1 && args.length != 2) {
            System.err.println("Usage: McpGeneratedPackageValidator <project-root> <package-root>");
            System.err.println("   or: McpGeneratedPackageValidator <package-root>  (project root = current directory)");
            System.exit(2);
        }

        Path projectRoot = args.length == 2 ? Path.of(args[0]) : Path.of(".");
        Path packageRoot = args.length == 2 ? Path.of(args[1]) : Path.of(args[0]);
        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);
        result.violations().forEach(System.out::println);
        if (result.hasBlockers()) {
            System.err.println("MCP package validation failed with " + result.blockerCount() + " blocker(s).");
            System.exit(1);
        }
        System.out.println("MCP package validation passed with "
                + result.warningCount() + " warning(s) and "
                + result.recommendationCount() + " recommendation(s).");
    }

    public McpPackageValidationResult validate(Path packageRoot) {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = McpPackageContext.load(projectRoot, packageRoot, result);
        for (McpValidationRule rule : rules) {
            rule.validate(context, result);
        }
        return result;
    }
}
