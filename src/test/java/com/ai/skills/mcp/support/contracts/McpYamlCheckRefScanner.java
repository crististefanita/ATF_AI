package com.ai.skills.mcp.support.contracts;

import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.skills.mcp.contracts.governance.McpGovernanceMapEntry;
import com.ai.skills.mcp.contracts.suites.McpSuiteSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class McpYamlCheckRefScanner {
    private static final Path MCP_TEST_ROOT = AtfSourcePaths.TEST_MCP_ROOT;
    private static final Pattern CHECK_REF = Pattern.compile("@McpCheckRef\\(\"([^\"]+)\"\\)");
    private static final Pattern SUITE_REF = Pattern.compile("@McpSuiteRef\\(\"([^\"]+)\"\\)");
    private static final Pattern INLINE_CHECK = Pattern.compile("@McpCheck\\s*\\(");
    private static final Pattern INLINE_SUITE = Pattern.compile("@McpSuite\\s*\\(");
    private static final Pattern TESTNG_CLASS = Pattern.compile("<class\\s+name=\"([^\"]+)\"\\s*/>");

    private McpYamlCheckRefScanner() {
    }

    public static List<String> referencedCheckIds() throws IOException {
        try (Stream<Path> files = Files.walk(MCP_TEST_ROOT)) {
            return files.filter(AtfSourcePaths::isJavaSource)
                    .flatMap(McpYamlCheckRefScanner::refsIn)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    public static List<String> referencedSuiteIds() throws IOException {
        try (Stream<Path> files = Files.walk(MCP_TEST_ROOT)) {
            return files.filter(AtfSourcePaths::isJavaSource)
                    .flatMap(McpYamlCheckRefScanner::suiteRefsIn)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    public static List<String> inlineMcpCheckUsages() throws IOException {
        try (Stream<Path> files = Files.walk(MCP_TEST_ROOT)) {
            return files.filter(AtfSourcePaths::isJavaSource)
                    .flatMap(McpYamlCheckRefScanner::inlineChecksIn)
                    .sorted()
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    public static List<String> inlineMcpSuiteUsages() throws IOException {
        try (Stream<Path> files = Files.walk(MCP_TEST_ROOT)) {
            return files.filter(AtfSourcePaths::isJavaSource)
                    .flatMap(McpYamlCheckRefScanner::inlineSuitesIn)
                    .sorted()
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    public static List<String> testNgClasses(String suiteXml) {
        Path path = Path.of(suiteXml).toAbsolutePath().normalize();
        try {
            Matcher matcher = TESTNG_CLASS.matcher(Files.readString(path));
            Stream.Builder<String> classes = Stream.builder();
            while (matcher.find()) {
                classes.add(matcher.group(1));
            }
            return classes.build().collect(Collectors.toUnmodifiableList());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan MCP TestNG profile classes in " + path, e);
        }
    }

    public static boolean sourceDeclaresSuiteRef(McpSuiteSpec spec) {
        Path path = AtfSourcePaths.testJava(spec.className())
                .toAbsolutePath()
                .normalize();
        try {
            return Files.isRegularFile(path)
                    && Files.readString(path).contains("@McpSuiteRef(\"" + spec.id() + "\")");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan MCP suite class " + path, e);
        }
    }

    public static List<McpGovernanceMapEntry> governanceEntries() throws IOException {
        try (Stream<Path> files = Files.walk(MCP_TEST_ROOT)) {
            return files.filter(AtfSourcePaths::isJavaSource)
                    .flatMap(McpYamlCheckRefScanner::governanceEntriesIn)
                    .sorted()
                    .collect(Collectors.toUnmodifiableList());
        }
    }

    private static Stream<String> refsIn(Path path) {
        try {
            Matcher matcher = CHECK_REF.matcher(Files.readString(path));
            Stream.Builder<String> ids = Stream.builder();
            while (matcher.find()) {
                ids.add(matcher.group(1));
            }
            return ids.build();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan MCP check refs in " + path, e);
        }
    }

    private static Stream<String> suiteRefsIn(Path path) {
        try {
            Matcher matcher = SUITE_REF.matcher(Files.readString(path));
            Stream.Builder<String> ids = Stream.builder();
            while (matcher.find()) {
                ids.add(matcher.group(1));
            }
            return ids.build();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan MCP suite refs in " + path, e);
        }
    }

    private static Stream<McpGovernanceMapEntry> governanceEntriesIn(Path path) {
        try {
            String source = Files.readString(path);
            Matcher matcher = CHECK_REF.matcher(source);
            Stream.Builder<McpGovernanceMapEntry> entries = Stream.builder();
            while (matcher.find()) {
                entries.add(new McpGovernanceMapEntry(
                        matcher.group(1),
                        normalize(path) + ":" + lineNumber(source, matcher.start())));
            }
            return entries.build();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan MCP governance map entries in " + path, e);
        }
    }

    private static Stream<String> inlineChecksIn(Path path) {
        return inlineUsages(path, INLINE_CHECK, "inline MCP checks");
    }

    private static Stream<String> inlineSuitesIn(Path path) {
        return inlineUsages(path, INLINE_SUITE, "inline MCP suites");
    }

    private static Stream<String> inlineUsages(Path path, Pattern pattern, String label) {
        try {
            String source = Files.readString(path);
            Matcher matcher = pattern.matcher(source);
            Stream.Builder<String> usages = Stream.builder();
            while (matcher.find()) {
                usages.add(path + ":" + lineNumber(source, matcher.start()));
            }
            return usages.build();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot scan " + label + " in " + path, e);
        }
    }

    private static long lineNumber(String text, int offset) {
        return text.substring(0, offset).chars().filter(ch -> ch == '\n').count() + 1;
    }

    private static String normalize(Path path) {
        return AtfSourcePaths.unixPath(path.normalize());
    }
}
