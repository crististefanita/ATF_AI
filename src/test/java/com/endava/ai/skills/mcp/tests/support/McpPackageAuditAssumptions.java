package com.endava.ai.skills.mcp.tests.support;

import com.endava.ai.skills.mcp.context.McpProjectLocator;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.SkipException;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class McpPackageAuditAssumptions {
    public static final String ALLOW_LATEST_FALLBACK_PROPERTY = "mcp.package.audit.allow.latest";
    public static final int MINIMUM_EXPLICIT_PACKAGE_AUDIT_METHODS = 20;
    private static final Set<String> REQUIRED_EXPLICIT_PACKAGE_AUDIT_CLASSES = Set.of(
            "com.endava.ai.skills.mcp.tests.core.McpPackageAuditPreflightTest",
            "com.endava.ai.skills.mcp.tests.core.McpConfiguredPackageAuditTest",
            "com.endava.ai.skills.mcp.tests.latest.McpRuntimeAndPackageContractTest",
            "com.endava.ai.skills.mcp.tests.latest.McpBusinessEvidenceContractTest",
            "com.endava.ai.skills.mcp.tests.latest.report.McpLatestPackageHtmlReportContractTest",
            "com.endava.ai.skills.mcp.tests.latest.report.McpLatestPackageReportPresentationContractTest"
    );

    private McpPackageAuditAssumptions() {
    }

    public static void requireExplicitPackageAudit() {
        if (hasExplicitPackageAudit()) {
            return;
        }
        throw new SkipException(skipMessage());
    }

    public static boolean hasExplicitPackageAudit() {
        String configured = System.getProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY, "").trim();
        if (!configured.isEmpty() && !"latest".equalsIgnoreCase(configured)) {
            return true;
        }
        return Boolean.getBoolean(ALLOW_LATEST_FALLBACK_PROPERTY);
    }

    public static boolean hasExternalPackageAuditWithoutSourceRoot() {
        String configured = System.getProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY, "").trim();
        if (configured.isEmpty() || "latest".equalsIgnoreCase(configured)) {
            return false;
        }
        if (McpProjectLocator.hasExplicitProjectRoot()) {
            return false;
        }

        Path validatorRoot = McpProjectLocator.projectRoot();
        Path packageRoot = configuredPackageRoot(validatorRoot, configured);
        return !packageRoot.startsWith(validatorRoot.resolve("docs/out").normalize());
    }

    public static String sourceRootRequiredMessage() {
        return "MCP package-audit preflight failed: external package root requires -D"
                + McpProjectLocator.PROJECT_ROOT_PROPERTY
                + "=<audited-project-root>.\n"
                + "Package root: " + System.getProperty(McpProjectLocator.PACKAGE_ROOT_PROPERTY, "").trim() + "\n"
                + "Validator project root: " + McpProjectLocator.projectRoot() + "\n"
                + "MCP refuses to run source-dependent rules against the validator workspace.";
    }

    public static String skipMessage() {
        return "MCP package-audit skipped: missing -D"
                + McpProjectLocator.PACKAGE_ROOT_PROPERTY
                + "=<package-root>.\n"
                + "Use testng-mcp-self.xml for validator health.\n"
                + "Use -D" + ALLOW_LATEST_FALLBACK_PROPERTY
                + "=true only for intentional latest fallback audits.";
    }

    public static List<String> scheduledClassNames(ITestContext context) {
        return Arrays.stream(context.getAllTestMethods())
                .map(ITestNGMethod::getRealClass)
                .map(Class::getName)
                .distinct()
                .collect(Collectors.toUnmodifiableList());
    }

    public static List<String> missingExplicitAuditClasses(ITestContext context) {
        List<String> scheduled = scheduledClassNames(context);
        return REQUIRED_EXPLICIT_PACKAGE_AUDIT_CLASSES.stream()
                .filter(required -> !scheduled.contains(required))
                .collect(Collectors.toUnmodifiableList());
    }

    public static boolean hasCompleteExplicitPackageAuditSchedule(ITestContext context) {
        return context.getAllTestMethods().length >= MINIMUM_EXPLICIT_PACKAGE_AUDIT_METHODS
                && missingExplicitAuditClasses(context).isEmpty();
    }

    private static Path configuredPackageRoot(Path validatorRoot, String configured) {
        Path path = Path.of(configured);
        return path.isAbsolute() ? path.normalize() : validatorRoot.resolve(path).normalize();
    }
}
