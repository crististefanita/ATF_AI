package com.ai.skills.mcp.tests.support;

import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.context.McpProjectLocator;
import com.ai.skills.mcp.McpGeneratedPackageValidator;
import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.contract.McpProfile;
import com.ai.skills.mcp.rules.McpValidationRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class McpNegativeFixture {
    private McpNegativeFixture() {
    }

    public static Path projectRoot() {
        return McpProjectLocator.projectRoot();
    }

    public static int intRuntimeProperty(String key) throws IOException {
        return McpProjectLocator.intRuntimeProperty(key);
    }

    public static Path cloneLatestPackage() throws IOException {
        return McpFixture.cloneLatestPackage().root();
    }

    public static Path cloneLatestPackageFirstRunOnly() throws IOException {
        return McpFixture.cloneLatestPackageFirstRunOnly().root();
    }

    public static Path cloneLatestPackageSourceSpine() throws IOException {
        return McpFixture.cloneLatestPackageSourceSpine().root();
    }

    public static Path clonePrimaryHtmlExampleFirstRunOnly() throws IOException {
        return McpFixture.clonePrimaryHtmlExampleFirstRunOnly().root();
    }

    public static Path clonePrimaryHtmlExampleReportSpine() throws IOException {
        return McpFixture.clonePrimaryHtmlExampleReportSpine().root();
    }

    public static McpPackageValidationResult validateUiPackage(Path packageRoot) {
        return McpGeneratedPackageValidator
                .forProfile(projectRoot(), McpProfile.UI_PACKAGE)
                .validate(packageRoot);
    }

    public static McpPackageValidationResult validateWithRules(Path packageRoot, McpValidationRule... rules) {
        return validateWithRules(projectRoot(), packageRoot, rules);
    }

    public static McpPackageValidationResult validateWithRules(Path projectRoot,
                                                               Path packageRoot,
                                                               McpValidationRule... rules) {
        McpPackageValidationResult result = new McpPackageValidationResult();
        McpPackageContext context = McpPackageContext.load(projectRoot, packageRoot, result);
        for (McpValidationRule rule : rules) {
            rule.validate(context, result);
        }
        return result;
    }

    public static Path clonePrimaryHtmlExamplePackage() throws IOException {
        return McpFixture.clonePrimaryHtmlExamplePackage().root();
    }

    public static void mutatePackageState(Path packageRoot, String key, String value) throws IOException {
        Path packageState = packageRoot.resolve(McpArtifactPaths.PACKAGE_STATE);
        String json = Files.readString(packageState);
        String replacement = "\"" + key + "\":\"" + value + "\"";
        String mutated = json.replaceAll("(?is)\"" + Pattern.quote(key) + "\"\\s*:\\s*\"?[^\"]+\"?",
                Matcher.quoteReplacement(replacement));
        if (mutated.equals(json)) {
            mutated = appendStringField(json, key, value);
        }
        Files.writeString(packageState, mutated);
    }

    private static String appendStringField(String json, String key, String value) {
        int objectEnd = json.lastIndexOf('}');
        if (objectEnd < 0) {
            throw new IllegalArgumentException("Cannot append package-state field to malformed JSON object.");
        }
        String prefix = json.substring(0, objectEnd).replaceFirst("\\s+$", "");
        String suffix = json.substring(objectEnd);
        String separator = prefix.endsWith("{") ? "" : ",";
        return prefix + separator + System.lineSeparator()
                + "  \"" + key + "\":\"" + value + "\"" + System.lineSeparator()
                + suffix;
    }

    public static void mutateReport(Path packageRoot, TextMutation mutation) throws IOException {
        McpFixture.mutateReport(packageRoot, mutation::apply);
    }

    public static void write(Path path, String content) throws IOException {
        McpFixture.write(path, content);
    }

    public interface TextMutation {
        String apply(String input);
    }
}
