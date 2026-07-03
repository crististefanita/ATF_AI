package com.endava.ai.skills.mcp.tests.smoke;

import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.skills.mcp.context.McpArtifactPaths;
import com.endava.ai.skills.mcp.context.McpProjectLocator;
import com.endava.ai.skills.mcp.contract.checks.McpCheckRef;
import com.endava.ai.skills.mcp.contract.suites.McpSuiteRef;
import com.endava.ai.skills.mcp.contracts.execution.McpExecutionProfile;
import com.endava.ai.skills.mcp.tests.support.McpPackageAuditAssumptions;
import com.endava.ai.skills.mcp.tests.support.McpTestDigests;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Listeners(TestListener.class)
@McpSuiteRef("mcp.suite.mcp-post-sync-smoke-contract-test")
public class McpPostSyncSmokeContractTest {
    private static final List<String> REQUIRED_MCP_ARTIFACTS = List.of(
            McpArtifactPaths.MCP_EXTENT_REPORT,
            McpArtifactPaths.MCP_EXTENT_SHA256,
            McpArtifactPaths.MCP_EXTENT_ORIGIN,
            McpArtifactPaths.MCP_TESTNG_XML,
            McpArtifactPaths.MCP_ALTERNATE_TESTNG_XML,
            McpArtifactPaths.MCP_METHOD_LEDGER,
            McpArtifactPaths.MCP_GATE_RESULT
    );
    private static final List<String> REQUIRED_KPI_ARTIFACTS = List.of(
            McpArtifactPaths.MCP_KPI_MATRIX,
            McpArtifactPaths.MCP_KPI_MATRIX_HTML,
            McpArtifactPaths.MCP_KPI_FORMULA_TRACE,
            McpArtifactPaths.MCP_KPI_REPAIR_BOARD,
            McpArtifactPaths.MCP_KPI_REPAIR_BOARD_HTML,
            McpArtifactPaths.MCP_SCORE_CAP_RATIONALE
    );
    private static final Pattern JSON_STRING = Pattern.compile("\"%s\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
    private static final Pattern JSON_INT = Pattern.compile("\"%s\"\\s*:\\s*(\\d+)");
    private static final Pattern GATE_COUNT = Pattern.compile("(?im)^%s:\\s*(\\d+)\\s*$");

    @BeforeClass
    public void requirePackageAuditTarget() {
        McpPackageAuditAssumptions.requireExplicitPackageAudit();
    }

    @Test
    @McpCheckRef("mcp.post-sync-smoke.required-frozen-artifacts")
    public void requiredFrozenMcpArtifactsArePresent() throws IOException {
        for (String artifact : REQUIRED_MCP_ARTIFACTS) {
            assertNonEmpty(packageRoot().resolve(artifact), "Missing frozen MCP artifact: " + artifact);
        }
    }

    @Test
    @McpCheckRef("mcp.post-sync-smoke.final-report-links")
    public void finalReportLinksFrozenMcpAndKpiEvidence() throws IOException {
        String report = lower(read(finalReport()));
        for (String artifact : REQUIRED_MCP_ARTIFACTS) {
            assertContains(report, lower(artifact),
                    "Final report must link frozen MCP evidence: " + artifact);
        }
        for (String artifact : REQUIRED_KPI_ARTIFACTS) {
            assertContains(report, lower(artifact),
                    "Final report must link KPI close-out evidence: " + artifact);
        }
    }

    @Test
    @McpCheckRef("mcp.post-sync-smoke.sanitized-command-and-origin-link")
    public void finalReportShowsSanitizedMavenCloseoutCommandAndOriginLink() throws IOException {
        String report = read(finalReport());
        String normalized = lower(report);
        assertContains(normalized, "mcp origin command",
                "Final report must visibly name the sanitized MCP origin command.");
        assertContains(normalized, "mvn",
                "Final report must publish the Maven command family used for MCP Extent generation.");
        assertContains(normalized, McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName(),
                "Final report command must name the final package close-out profile.");
        assertContains(normalized, "-dmcp.package.root=&lt;package-root&gt;",
                "Final report must show a sanitized package root placeholder, not a local absolute path.");
        assertContains(normalized, "-dmcp.project.root=&lt;project-root&gt;",
                "Final report must show a sanitized project root placeholder, not a local absolute path.");
        assertContains(normalized, McpArtifactPaths.MCP_EXTENT_ORIGIN,
                "Final report must link " + fileName(McpArtifactPaths.MCP_EXTENT_ORIGIN)
                        + " for the exact absolute command.");
        Assert.assertFalse(normalized.contains(lower(packageRoot().toString())),
                "Final report must not leak the exact audited package root; extent-origin.json owns that provenance.");
        Assert.assertFalse(normalized.contains(lower(projectRoot().toString())),
                "Final report must not leak the exact audited project root; extent-origin.json owns that provenance.");
    }

    @Test
    @McpCheckRef("mcp.post-sync-smoke.origin-hash-size-exit")
    public void extentOriginMatchesFrozenExtentReportHashSizeAndExit() throws IOException {
        Path extent = packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_REPORT);
        Path shaFile = packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_SHA256);
        String origin = read(packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_ORIGIN));
        String command = jsonString(origin, "command");
        String originSha = jsonString(origin, "sha256");
        long originSize = jsonInt(origin, "size");
        int exit = jsonInt(origin, "exitCode", "exit");
        String actualSha = McpTestDigests.sha256(extent);

        assertContains(lower(command), "mvn", "extent-origin.json command must be a Maven command.");
        assertContains(lower(command), McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName(),
                "extent-origin.json command must name the package close-out profile.");
        assertContains(lower(command), "-dmcp.package.root",
                "extent-origin.json command must audit the package root.");
        assertContains(lower(command), "-dmcp.project.root",
                "extent-origin.json command must audit the project root.");
        Assert.assertEquals(exit, 0, "extent-origin.json must prove the Maven close-out command exited green.");
        Assert.assertEquals(originSha, actualSha,
                fileName(McpArtifactPaths.MCP_EXTENT_ORIGIN)
                        + " sha256 must match " + fileName(McpArtifactPaths.MCP_EXTENT_REPORT) + ".");
        Assert.assertEquals(firstHash(read(shaFile)), actualSha,
                fileName(McpArtifactPaths.MCP_EXTENT_SHA256)
                        + " must match " + fileName(McpArtifactPaths.MCP_EXTENT_REPORT) + ".");
        Assert.assertEquals(originSize, Files.size(extent),
                fileName(McpArtifactPaths.MCP_EXTENT_ORIGIN)
                        + " size must match " + fileName(McpArtifactPaths.MCP_EXTENT_REPORT) + " byte size.");
    }

    @Test
    @McpCheckRef("mcp.post-sync-smoke.testng-xml-green-counts")
    public void frozenTestNgXmlIsGreenAndCounted() throws IOException {
        Counts xml = countsFromSurefireXml(packageRoot().resolve(McpArtifactPaths.MCP_TESTNG_XML));
        Assert.assertTrue(xml.tests() > 0, "MCP close-out XML must execute real tests.");
        Assert.assertEquals(xml.failures(), 0, "MCP close-out XML must have zero failures.");
        Assert.assertEquals(xml.errors(), 0, "MCP close-out XML must have zero errors.");
        Assert.assertEquals(xml.skipped(), 0, "MCP close-out XML must have zero skipped tests.");

        String origin = read(packageRoot().resolve(McpArtifactPaths.MCP_EXTENT_ORIGIN));
        Assert.assertEquals(jsonInt(origin, "testsRun"), xml.tests(),
                "extent-origin.json testsRun must match TEST-TestSuite.xml.");
        Assert.assertEquals(jsonInt(origin, "passed"), xml.tests(),
                "extent-origin.json passed must match TEST-TestSuite.xml when failures/errors/skips are zero.");
    }

    @Test
    @McpCheckRef("mcp.post-sync-smoke.gate-report-count-sync")
    public void gateResultAndFinalReportAgreeWithGreenCloseoutCounts() throws IOException {
        Counts xml = countsFromSurefireXml(packageRoot().resolve(McpArtifactPaths.MCP_TESTNG_XML));
        String gate = read(packageRoot().resolve(McpArtifactPaths.MCP_GATE_RESULT));
        String report = lower(read(finalReport()));

        assertContains(lower(gate), "status: pass", "MCP gate result must publish PASS.");
        assertContains(lower(gate), McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName(),
                "MCP gate result must identify the close-out suite.");
        Assert.assertEquals(gateCount(gate, "Tests run"), xml.tests(), "Gate tests run must match XML.");
        Assert.assertEquals(gateCount(gate, "Passed"), xml.tests(), "Gate passed count must match XML.");
        Assert.assertEquals(gateCount(gate, "Failures"), 0, "Gate failures must be zero.");
        Assert.assertEquals(gateCount(gate, "Errors"), 0, "Gate errors must be zero.");
        Assert.assertEquals(gateCount(gate, "Skipped"), 0, "Gate skipped must be zero for post-sync close-out.");
        Assert.assertEquals(gateCount(gate, "Exit code"), 0, "Gate exit code must be zero.");

        assertContains(report, "mcp close-out pass",
                "Final report must visibly publish the green MCP close-out status.");
        assertContains(report, "pass " + xml.tests() + "/" + xml.tests(),
                "Final report must publish the same MCP passed/total count as XML.");
    }

    private Path projectRoot() {
        return McpProjectLocator.projectRoot();
    }

    private Path packageRoot() throws IOException {
        return McpProjectLocator.packageRoot(projectRoot());
    }

    private Path finalReport() throws IOException {
        return packageRoot().resolve(McpArtifactPaths.FINAL_REPORT);
    }

    private void assertNonEmpty(Path path, String message) throws IOException {
        Assert.assertTrue(Files.isRegularFile(path), message + " at " + path);
        Assert.assertTrue(Files.size(path) > 0, message + " is empty at " + path);
    }

    private String read(Path path) throws IOException {
        assertNonEmpty(path, "Required file");
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private static String lower(String text) {
        return text.toLowerCase(Locale.ROOT).replace('\\', '/');
    }

    private static void assertContains(String text, String token, String message) {
        Assert.assertTrue(text.contains(token), message + " Missing token: " + token);
    }

    private static String jsonString(String json, String key) {
        Matcher matcher = Pattern.compile(String.format(JSON_STRING.pattern(), Pattern.quote(key))).matcher(json);
        Assert.assertTrue(matcher.find(), "Missing JSON string field: " + key);
        return matcher.group(1)
                .replace("\\\\", "\\")
                .replace("\\\"", "\"");
    }

    private static int jsonInt(String json, String key) {
        Matcher matcher = Pattern.compile(String.format(JSON_INT.pattern(), Pattern.quote(key))).matcher(json);
        Assert.assertTrue(matcher.find(), "Missing JSON integer field: " + key);
        return Integer.parseInt(matcher.group(1));
    }

    private static int jsonInt(String json, String firstKey, String secondKey) {
        Matcher first = Pattern.compile(String.format(JSON_INT.pattern(), Pattern.quote(firstKey))).matcher(json);
        if (first.find()) {
            return Integer.parseInt(first.group(1));
        }
        return jsonInt(json, secondKey);
    }

    private static int gateCount(String gate, String label) {
        Matcher matcher = Pattern.compile(String.format(GATE_COUNT.pattern(), Pattern.quote(label))).matcher(gate);
        Assert.assertTrue(matcher.find(), "Missing gate count: " + label);
        return Integer.parseInt(matcher.group(1));
    }

    private static String firstHash(String text) {
        Matcher matcher = Pattern.compile("(?i)\\b([a-f0-9]{64})\\b").matcher(text);
        Assert.assertTrue(matcher.find(), "Missing 64-character SHA-256 in ExtentReport.sha256.");
        return matcher.group(1).toLowerCase(Locale.ROOT);
    }

    private static String fileName(String artifactPath) {
        return McpArtifactPaths.fileName(artifactPath);
    }

    private static Counts countsFromSurefireXml(Path xml)
            throws IOException {
        try (InputStream input = Files.newInputStream(xml)) {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
            var root = document.getDocumentElement();
            return new Counts(
                    integerAttribute(root.getAttribute("tests"), "tests", xml),
                    integerAttribute(root.getAttribute("failures"), "failures", xml),
                    integerAttribute(root.getAttribute("errors"), "errors", xml),
                    integerAttribute(root.getAttribute("skipped"), "skipped", xml)
            );
        } catch (ParserConfigurationException | SAXException exception) {
            throw new AssertionError("Cannot parse MCP close-out TestNG XML: " + xml, exception);
        }
    }

    private static int integerAttribute(String value, String name, Path xml) {
        Assert.assertFalse(value == null || value.isBlank(),
                "Missing XML testsuite attribute " + name + " in " + xml);
        return Integer.parseInt(value);
    }

    @Getter(AccessLevel.PRIVATE)
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Counts {
        private final int tests;
        private final int failures;
        private final int errors;
        private final int skipped;
    }
}
