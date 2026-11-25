package com.ai.core.reporting.aggregate.parallel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class AggregateProcessCollectionExecutor implements AggregateCollectionExecutor {
    private static final String COMMAND_LOG = "command.out.log";
    private static final String TIMEOUT_SECONDS_PROPERTY = "atf.core.aggregate.collection.timeout.seconds";
    private static final long DEFAULT_TIMEOUT_SECONDS = 900L;

    private final Path workingDirectory;

    public AggregateProcessCollectionExecutor(Path workingDirectory) {
        this.workingDirectory = workingDirectory.toAbsolutePath().normalize();
    }

    @Override
    public AggregateCollectionResult execute(AggregateCollectionSpec spec) throws IOException {
        Files.createDirectories(spec.outputRoot());
        Path log = spec.outputRoot().resolve(COMMAND_LOG);
        Files.deleteIfExists(log);
        long started = Instant.now().toEpochMilli();
        ProcessBuilder builder = new ProcessBuilder(shellCommand(spec.command()));
        builder.directory(workingDirectory.toFile());
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.appendTo(log.toFile()));
        Process process = builder.start();
        int exitCode = waitFor(process, timeoutSeconds());
        long ended = Instant.now().toEpochMilli();
        String output = readLog(log);
        ensureExtentReportForPassedCollection(spec, exitCode);
        List<String> artifacts = artifacts(spec, log);
        return new AggregateCollectionResult(
                spec,
                exitCode,
                started,
                ended,
                artifacts,
                exitCode == 0 ? "" : failureSummary(output, exitCode)
        );
    }

    private static void ensureExtentReportForPassedCollection(AggregateCollectionSpec spec, int exitCode)
            throws IOException {
        if (exitCode != 0 || Files.isRegularFile(spec.extentReport()) || !Files.isRegularFile(spec.testNgResultsXml())) {
            return;
        }
        List<AggregateCollectionTestCase> cases = AggregateTestNgResultParser.parse(spec.testNgResultsXml(), spec);
        if (!cases.isEmpty()) {
            AggregateParallelExtentPublisher.publishMethodCases(spec.extentReport(), cases);
        }
    }

    private static int waitFor(Process process, long timeoutSeconds) {
        try {
            if (process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                return process.exitValue();
            }
            process.destroyForcibly();
            process.waitFor(10, TimeUnit.SECONDS);
            return 124;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            return 130;
        }
    }

    private static List<String> shellCommand(String command) {
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win")) {
            return List.of("powershell", "-NoProfile", "-Command", command);
        }
        return List.of("sh", "-lc", command);
    }

    private static long timeoutSeconds() {
        return Long.getLong(TIMEOUT_SECONDS_PROPERTY, DEFAULT_TIMEOUT_SECONDS);
    }

    private static String readLog(Path log) throws IOException {
        if (!Files.isRegularFile(log)) {
            return "";
        }
        return new String(Files.readAllBytes(log), StandardCharsets.UTF_8);
    }

    private static String failureSummary(String text, int exitCode) {
        if (exitCode == 124) {
            return "Aggregate collection timed out after "
                    + timeoutSeconds()
                    + " seconds. See "
                    + COMMAND_LOG
                    + " for partial Maven output.";
        }
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> line.startsWith("[ERROR]"))
                .findFirst()
                .orElseGet(() -> text.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .filter(line -> !line.startsWith("SLF4J:"))
                        .findFirst()
                        .orElse(""));
    }

    private static List<String> artifacts(AggregateCollectionSpec spec, Path log) {
        List<String> links = new ArrayList<>();
        links.add(workspaceRelative(log));
        addIfExists(links, spec.testNgResultsXml());
        addIfExists(links, spec.extentReport());
        return List.copyOf(links);
    }

    private static void addIfExists(List<String> links, Path artifact) {
        if (Files.isRegularFile(artifact)) {
            links.add(workspaceRelative(artifact));
        }
    }

    private static String workspaceRelative(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        Path workspace = Path.of("").toAbsolutePath().normalize();
        if (normalized.startsWith(workspace)) {
            return portablePath(workspace.relativize(normalized));
        }
        return portablePath(normalized);
    }

    private static String portablePath(Path path) {
        return path.toString().replace('\\', '/');
    }
}
