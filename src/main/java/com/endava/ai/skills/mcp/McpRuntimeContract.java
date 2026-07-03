package com.endava.ai.skills.mcp;

import com.endava.ai.skills.mcp.contracts.catalog.ContractCatalog;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
public final class McpRuntimeContract {
    private final Properties properties;
    private final Path source;

    private McpRuntimeContract(Properties properties, Path source) {
        this.properties = properties;
        this.source = source;
    }

    public static McpRuntimeContract load(Path projectRoot, McpPackageValidationResult result) {
        Path runtime = projectRoot.resolve(McpArtifactPaths.RUNTIME_PROPERTIES).normalize();
        Properties loaded = new Properties();
        if (!Files.isRegularFile(runtime)) {
            result.add(McpPackageViolation.blocker("runtime.missing", "Missing agent-runtime.properties", runtime));
            return new McpRuntimeContract(loaded, runtime);
        }

        try (Reader reader = Files.newBufferedReader(runtime)) {
            loaded.load(reader);
        } catch (IOException exception) {
            result.add(McpPackageViolation.blocker("runtime.unreadable", exception.getMessage(), runtime));
        }
        for (String key : ContractCatalog.packagePolicyTokens("runtime-required-key")) {
            if (loaded.getProperty(key, "").trim().isEmpty()) {
                result.add(McpPackageViolation.blocker("runtime.key.missing",
                        "Missing required runtime key: " + key, runtime));
            }
        }
        for (String key : ContractCatalog.packagePolicyTokens("runtime-decimal-key")) {
            requireDecimal(loaded, runtime, result, key);
        }
        return new McpRuntimeContract(loaded, runtime);
    }

    public String expectedDeltaQuality() {
        return property("RUN_EXPECTED_DELTA_QUALITY");
    }

    public BigDecimal expectedDeltaQualityScore() {
        return normalizeScoreToHundred(expectedDeltaQuality(), BigDecimal.ZERO);
    }

    public int expectedDeltaQualityAsOverallCap() {
        return expectedDeltaQualityScore().intValue();
    }

    public static BigDecimal normalizeScoreToHundred(BigDecimal value) {
        return value.compareTo(BigDecimal.TEN) <= 0 ? value.multiply(BigDecimal.TEN) : value;
    }

    public static BigDecimal normalizeScoreToHundred(String value, BigDecimal fallback) {
        try {
            return normalizeScoreToHundred(new BigDecimal(value.trim()));
        } catch (RuntimeException exception) {
            return fallback;
        }
    }

    public int roundTargetTestCount() {
        return integer("ROUND_TARGET_TEST_COUNT", 0);
    }

    public int metaIterationCount() {
        return integer("META_ITERATION_COUNT", 0);
    }

    public String property(String key) {
        return properties.getProperty(key, "").trim();
    }

    public List<Path> promptReferencePaths(Path projectRoot) {
        return properties.stringPropertyNames().stream()
                .filter(key -> key.endsWith("_PATH"))
                .map(this::property)
                .filter(value -> !value.isBlank())
                .filter(value -> value.endsWith(".html") || value.endsWith(".md") || value.endsWith(".css"))
                .map(value -> resolveProjectPath(projectRoot, value))
                .collect(Collectors.toList());
    }

    private Path resolveProjectPath(Path projectRoot, String value) {
        Path path = Path.of(value);
        return path.isAbsolute() ? path.normalize() : projectRoot.resolve(path).normalize();
    }

    private static void requireDecimal(Properties properties,
                                       Path runtime,
                                       McpPackageValidationResult result,
                                       String key) {
        String value = properties.getProperty(key, "").trim();
        if (value.isEmpty()) {
            return;
        }
        try {
            new BigDecimal(value);
        } catch (NumberFormatException exception) {
            result.add(McpPackageViolation.blocker("runtime.key.invalid-number",
                    "Runtime key must be numeric: " + key + "=" + value,
                    runtime));
        }
    }

    private int integer(String key, int fallback) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
