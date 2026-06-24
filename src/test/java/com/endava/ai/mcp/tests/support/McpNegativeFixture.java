package com.endava.ai.mcp.tests.support;

import com.endava.ai.mcp.context.McpArtifactPaths;
import com.endava.ai.mcp.context.McpProjectLocator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static Path clonePrimaryHtmlExamplePackage() throws IOException {
        return McpFixture.clonePrimaryHtmlExamplePackage().root();
    }

    public static void mutatePackageState(Path packageRoot, String key, String value) throws IOException {
        Path packageState = packageRoot.resolve(McpArtifactPaths.PACKAGE_STATE);
        String json = Files.readString(packageState)
                .replaceAll("(?is)\"" + Pattern.quote(key) + "\"\\s*:\\s*\"?[^\"]+\"?",
                        "\"" + key + "\":\"" + value + "\"");
        Files.writeString(packageState, json);
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
