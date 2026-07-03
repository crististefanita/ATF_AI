package com.endava.ai.skills.mcp;

import com.endava.ai.core.reporting.utils.ConsoleLogger;
import com.endava.ai.skills.mcp.context.McpPackageContext;
import com.endava.ai.skills.mcp.contract.McpProfile;
import com.endava.ai.skills.mcp.rules.McpDefaultRules;
import com.endava.ai.skills.mcp.rules.McpValidationRule;

import java.nio.file.Path;
import java.util.List;

public final class McpGeneratedPackageValidator {
    private static final String USAGE_WITH_PROJECT_ROOT =
            "Usage: McpGeneratedPackageValidator <project-root> <package-root>";
    private static final String USAGE_WITH_CURRENT_DIRECTORY =
            "   or: McpGeneratedPackageValidator <package-root>  (project root = current directory)";
    private static final int INVALID_USAGE_EXIT_CODE = 2;
    private static final int BLOCKER_EXIT_CODE = 1;

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
            console(USAGE_WITH_PROJECT_ROOT);
            console(USAGE_WITH_CURRENT_DIRECTORY);
            System.exit(INVALID_USAGE_EXIT_CODE);
        }

        Path projectRoot = args.length == 2 ? Path.of(args[0]) : Path.of(".");
        Path packageRoot = args.length == 2 ? Path.of(args[1]) : Path.of(args[0]);
        McpPackageValidationResult result = new McpGeneratedPackageValidator(projectRoot).validate(packageRoot);
        result.violations().forEach(violation -> console(String.valueOf(violation)));
        if (result.hasBlockers()) {
            console("MCP package validation failed with " + result.blockerCount() + " blocker(s).");
            System.exit(BLOCKER_EXIT_CODE);
        }
        console("MCP package validation passed with "
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

    private static void console(String message) {
        ConsoleLogger.console(message);
    }
}
