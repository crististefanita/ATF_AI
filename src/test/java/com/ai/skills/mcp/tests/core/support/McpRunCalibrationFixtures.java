package com.ai.skills.mcp.tests.core.support;

import com.ai.skills.mcp.McpPackageValidationResult;
import com.ai.skills.mcp.context.McpArtifactPaths;
import com.ai.skills.mcp.context.McpPackageContext;
import com.ai.skills.mcp.contracts.execution.McpExecutionProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationArtifactSupport.analysisRoot;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationArtifactSupport.artifactHref;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationArtifactSupport.escapeJson;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationArtifactSupport.fileName;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationArtifactSupport.hashExtentReport;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationArtifactSupport.mcpRoot;
import static com.ai.skills.mcp.tests.core.support.McpRunCalibrationArtifactSupport.packageCloseoutCommand;

public final class McpRunCalibrationFixtures {
    private static final String CLOSEOUT_PROFILE_FILE = McpExecutionProfile.PACKAGE_CLOSEOUT.suiteFileName();

    private McpRunCalibrationFixtures() {
    }

    public static String testNgXml(int realTests, boolean includeConfigMethods) {
        StringBuilder xml = new StringBuilder("<testng-results><suite><test><class name=\"com.example.PositiveUserTests\">");
        if (includeConfigMethods) {
            xml.append("<test-method name=\"setUp\" status=\"PASS\" is-config=\"true\"/>");
            xml.append("<test-method name=\"beforeClass\" status=\"PASS\" is-config=\"true\"/>");
        }
        for (int index = 1; index <= realTests; index++) {
            xml.append("<test-method name=\"test").append(index).append("\" status=\"PASS\"/>");
        }
        if (includeConfigMethods) {
            xml.append("<test-method name=\"tearDown\" status=\"PASS\" is-config=\"true\"/>");
        }
        return xml.append("</class></test></suite></testng-results>").toString();
    }

    public static Path writeReport(String body) throws IOException {
        Path report = Files.createTempFile("mcp-run-calibration-", ".html");
        Files.writeString(report, "<html><body>" + body + "</body></html>");
        return report;
    }

    public static String nativeSparkShellHtml() {
        return "<html><body class=\"spa -report\"><a id=\"nav-dashboard\"></a>"
                + "<script>toggleView('test-view')</script>"
                + "<div class=\"test-wrapper\"><div class=\"test-content\">PASS</div></div>"
                + "<script src=\"spark-script.js\"></script></body></html>";
    }

    public static McpPackageContext temporaryContext(McpPackageValidationResult result) throws IOException {
        Path projectRoot = Files.createTempDirectory("mcp-run-calibration-project-");
        Path runtime = projectRoot.resolve("docs/prompts/html_ro/mcp/agent-runtime.properties");
        Files.createDirectories(runtime.getParent());
        Files.writeString(runtime, ""
                + "RUN_EXPECTED_DELTA_QUALITY=8\n"
                + "ROUND_TARGET_TEST_COUNT=5\n"
                + "META_ITERATION_COUNT=3\n");
        Path packageRoot = Files.createTempDirectory("mcp-run-calibration-package-");
        return McpPackageContext.load(projectRoot, packageRoot, result);
    }

    public static void publishGateXmlAndLedger(Path packageRoot) throws IOException {
        Path mcp = mcpRoot(packageRoot);
        Files.createDirectories(mcp);
        publishPackageState(packageRoot, 92);
        publishFinalReportWithSkippedMcpSummary(packageRoot);
        Files.writeString(mcp.resolve(fileName(McpArtifactPaths.MCP_GATE_RESULT)),
                "# MCP Gate Result\n\n"
                        + "- verdict: PASS\n"
                        + "- command: mvn -q -Dtest=Mcp*Test test -Dmcp.package.root=" + packageRoot.getFileName() + "\n"
                        + "- package_root: " + packageRoot.getFileName() + "\n"
                        + "- Maven full-suite outcome: discovered=177 executed/pass=78 skipped=99 failures=0 errors=0\n"
                        + "- Tests run: 177\n"
                        + "- Passed: 78\n"
                        + "- Failures: 0\n"
                        + "- Errors: 0\n"
                        + "- Skipped: 99\n"
                        + "- latest_extent_report: " + McpArtifactPaths.MCP_EXTENT_REPORT + "\n"
                        + "- latest_extent_sha256: " + McpArtifactPaths.MCP_EXTENT_SHA256 + "\n"
                        + "- latest_extent_origin: " + McpArtifactPaths.MCP_EXTENT_ORIGIN + "\n"
                        + "- complete_testng_xml: " + McpArtifactPaths.MCP_TESTNG_XML + "\n"
                        + "- full_mcp_method_ledger: " + McpArtifactPaths.MCP_METHOD_LEDGER + "\n");
        Files.writeString(mcp.resolve(fileName(McpArtifactPaths.MCP_TESTNG_XML)),
                "<testsuite name=\"MCP\" tests=\"177\" failures=\"0\" errors=\"0\" skipped=\"99\"></testsuite>\n");
        Files.writeString(mcp.resolve(fileName(McpArtifactPaths.MCP_METHOD_LEDGER)),
                "# Full MCP Method Ledger\n\n"
                        + "Tests run: 177\n"
                        + "Passed: 78\n"
                        + "Skipped: 99\n"
                        + "Failures: 0\n"
                        + "Errors: 0\n\n"
                        + "Maven full-suite outcome: discovered=177, executed/pass=78, skipped=99, failures=0, errors=0.\n\n"
                        + "Skipped profile classes:\n"
                        + "- profile/precondition group: package-audit profile methods not active for this validator slice; reason: TestNG profile/precondition skip; accepted=true.\n");
    }

    public static void publishFinalReportWithSkippedMcpSummary(Path packageRoot) throws IOException {
        Path analysis = analysisRoot(packageRoot);
        Files.createDirectories(analysis);
        Files.writeString(analysis.resolve(fileName(McpArtifactPaths.FINAL_REPORT)),
                "<html><body>"
                        + "<details open><summary>Artifact Index</summary>"
                        + artifactHref(McpArtifactPaths.MCP_EXTENT_REPORT, "MCP Extent")
                        + artifactHref(McpArtifactPaths.MCP_EXTENT_ORIGIN, "MCP origin")
                        + artifactHref(McpArtifactPaths.MCP_METHOD_LEDGER, "MCP ledger")
                        + artifactHref(McpArtifactPaths.MCP_GATE_RESULT, "MCP gate")
                        + "</details>"
                        + "<section>MCP package-closeout profile summary: " + CLOSEOUT_PROFILE_FILE + ". "
                        + "Tests run 177, passed 78, skipped 99, 0 failures, 0 errors. "
                        + "MCP Maven command for " + fileName(McpArtifactPaths.MCP_EXTENT_REPORT) + ": "
                        + packageCloseoutCommand(packageRoot) + ". "
                        + "Reason: profile/precondition skip for inactive package-audit methods; accepted=true for close-out. "
                        + "Score cap 92 applies; see " + fileName(McpArtifactPaths.MCP_METHOD_LEDGER) + ".</section>"
                        + "</body></html>");
    }

    public static void publishFinalReportWithCleanCloseoutSummary(Path packageRoot) throws IOException {
        Path analysis = analysisRoot(packageRoot);
        Files.createDirectories(analysis);
        Files.writeString(analysis.resolve(fileName(McpArtifactPaths.FINAL_REPORT)),
                "<html><body>"
                        + "<details open><summary>Artifact Index</summary>"
                        + artifactHref(McpArtifactPaths.MCP_EXTENT_REPORT, "MCP Extent")
                        + artifactHref(McpArtifactPaths.MCP_EXTENT_ORIGIN, "MCP origin")
                        + artifactHref(McpArtifactPaths.MCP_METHOD_LEDGER, "MCP ledger")
                        + artifactHref(McpArtifactPaths.MCP_GATE_RESULT, "MCP gate")
                        + "</details>"
                        + "<section>MCP package-closeout profile summary: " + CLOSEOUT_PROFILE_FILE + ". "
                        + "Tests run 89, passed 89, 89/89 all passed, 0 failures, 0 errors, 0 skipped. "
                        + "MCP Maven command for " + fileName(McpArtifactPaths.MCP_EXTENT_REPORT) + ": "
                        + packageCloseoutCommand(packageRoot) + ". "
                        + "This is the final publishability subcollection.</section>"
                        + "</body></html>");
    }

    public static void publishFinalReportWithoutSkippedMcpSummary(Path packageRoot) throws IOException {
        Path analysis = analysisRoot(packageRoot);
        Files.createDirectories(analysis);
        Files.writeString(analysis.resolve(fileName(McpArtifactPaths.FINAL_REPORT)),
                "<html><body><details><summary>Artifact Index</summary>"
                        + artifactHref(McpArtifactPaths.MCP_EXTENT_REPORT, "MCP Extent")
                        + artifactHref(McpArtifactPaths.MCP_METHOD_LEDGER, "MCP ledger")
                        + "</details><section>MCP PASS.</section></body></html>");
    }

    public static void publishGateXmlAndLedgerWithoutSkipDisclosure(Path packageRoot) throws IOException {
        Path mcp = mcpRoot(packageRoot);
        Files.createDirectories(mcp);
        publishPackageState(packageRoot, 92);
        Files.writeString(mcp.resolve(fileName(McpArtifactPaths.MCP_GATE_RESULT)),
                "# MCP Gate Result\n\n"
                        + "- verdict: PASS\n"
                        + "- command: mvn -q -Dtest=Mcp*Test test -Dmcp.package.root=" + packageRoot.getFileName() + "\n"
                        + "- package_root: " + packageRoot.getFileName() + "\n"
                        + "- Maven full-suite outcome: discovered=177 executed/pass=78 skipped=99 failures=0 errors=0\n"
                        + "- Tests run: 177\n"
                        + "- Passed: 78\n"
                        + "- Failures: 0\n"
                        + "- Errors: 0\n"
                        + "- Skipped: 99\n"
                        + "- latest_extent_report: " + McpArtifactPaths.MCP_EXTENT_REPORT + "\n"
                        + "- latest_extent_sha256: " + McpArtifactPaths.MCP_EXTENT_SHA256 + "\n"
                        + "- latest_extent_origin: " + McpArtifactPaths.MCP_EXTENT_ORIGIN + "\n"
                        + "- complete_testng_xml: " + McpArtifactPaths.MCP_TESTNG_XML + "\n"
                        + "- full_mcp_method_ledger: " + McpArtifactPaths.MCP_METHOD_LEDGER + "\n");
        Files.writeString(mcp.resolve(fileName(McpArtifactPaths.MCP_TESTNG_XML)),
                "<testsuite name=\"MCP\" tests=\"177\" failures=\"0\" errors=\"0\" skipped=\"99\"></testsuite>\n");
        Files.writeString(mcp.resolve(fileName(McpArtifactPaths.MCP_METHOD_LEDGER)),
                "# Full MCP Method Ledger\n\n"
                        + "Tests run: 177\n"
                        + "Passed: 78\n"
                        + "Skipped: 99\n"
                        + "Failures: 0\n"
                        + "Errors: 0\n\n"
                        + "Maven full-suite outcome: discovered=177, executed/pass=78, skipped=99, failures=0, errors=0.\n");
    }

    public static void publishCompleteMcpExtent(Path packageRoot, int passed, int skipped) throws IOException {
        publishCompleteMcpExtent(packageRoot, passed, skipped, null);
    }

    public static void publishCompleteMcpExtentWithSkippedPackageAuditClass(Path packageRoot, int passed, int skipped) throws IOException {
        publishCompleteMcpExtent(packageRoot, passed, skipped, "McpLatestPackageExtentEvidenceContractTest");
    }

    public static void publishCleanCloseoutEvidence(Path packageRoot, boolean withFinalSummary) throws IOException {
        Path mcp = mcpRoot(packageRoot);
        Files.createDirectories(mcp);
        publishPackageState(packageRoot, 95);
        if (withFinalSummary) {
            publishFinalReportWithCleanCloseoutSummary(packageRoot);
        } else {
            publishFinalReportWithoutSkippedMcpSummary(packageRoot);
        }
        publishCompleteMcpExtent(packageRoot, 89, 0);
        rewriteMcpOriginCommand(packageRoot, packageCloseoutCommand(packageRoot));
        Files.writeString(mcp.resolve(fileName(McpArtifactPaths.MCP_GATE_RESULT)),
                "# MCP Gate Result\n\n"
                        + "Status: PASS\n"
                        + "Command: " + packageCloseoutCommand(packageRoot) + "\n"
                        + "Tests run: 89\n"
                        + "Passed: 89\n"
                        + "Failures: 0\n"
                        + "Errors: 0\n"
                        + "Skipped: 0\n"
                        + "Frozen artifacts: " + fileName(McpArtifactPaths.MCP_EXTENT_REPORT) + ", "
                        + fileName(McpArtifactPaths.MCP_EXTENT_SHA256) + ", "
                        + fileName(McpArtifactPaths.MCP_EXTENT_ORIGIN) + ", "
                        + fileName(McpArtifactPaths.MCP_TESTNG_XML) + ", "
                        + fileName(McpArtifactPaths.MCP_METHOD_LEDGER) + "\n");
        Files.writeString(mcp.resolve(fileName(McpArtifactPaths.MCP_TESTNG_XML)),
                "<testsuite name=\"MCP Package Close-Out\" tests=\"89\" failures=\"0\" errors=\"0\" skipped=\"0\">"
                        + "<properties><property name=\"surefire.suiteXmlFiles\" "
                        + "value=\"" + McpExecutionProfile.PACKAGE_CLOSEOUT.suiteXml() + "\"/></properties>"
                        + "</testsuite>\n");
        Files.writeString(mcp.resolve(fileName(McpArtifactPaths.MCP_METHOD_LEDGER)),
                "# Full MCP Method Ledger\n\n"
                        + "Tests run: 89\n"
                        + "Passed: 89\n"
                        + "Skipped: 0\n"
                        + "Failures: 0\n"
                        + "Errors: 0\n\n"
                        + "Maven package-closeout outcome: total=89, passed=89, failures=0, errors=0, skipped=0.\n");
    }

    public static void rewriteMcpOriginCommand(Path packageRoot, String command) throws IOException {
        Path origin = packageRoot.resolve(McpArtifactPaths.MCP_EXTENT_ORIGIN);
        String json = Files.readString(origin);
        Files.writeString(origin, json.replaceFirst("(?s)\"command\"\\s*:\\s*\"[^\"]*\"",
                "\"command\": \"" + escapeJson(command) + "\""));
    }

    public static void rewriteMcpGateCommand(Path packageRoot, String command) throws IOException {
        Path gate = packageRoot.resolve(McpArtifactPaths.MCP_GATE_RESULT);
        String text = Files.readString(gate);
        Files.writeString(gate, text.replaceFirst("(?m)^- command: .+$", "- command: " + command));
    }

    public static void publishPackageState(Path packageRoot, int cap) throws IOException {
        Path review = packageRoot.resolve(McpArtifactPaths.REVIEW_ROOT);
        Files.createDirectories(review);
        Files.writeString(review.resolve(fileName(McpArtifactPaths.PACKAGE_STATE)),
                "{\n"
                        + "  \"status\": \"MCP_CONTRACT_PASS\",\n"
                        + "  \"overall\": " + cap + ",\n"
                        + "  \"cap\": " + cap + ",\n"
                        + "  \"runs_completed\": 5,\n"
                        + "  \"tests_produced\": 25\n"
                        + "}\n");
    }

    private static void publishCompleteMcpExtent(Path packageRoot, int passed, int skipped, String firstSkippedName) throws IOException {
        Path mcp = mcpRoot(packageRoot);
        Files.createDirectories(mcp);
        Path extent = mcp.resolve(fileName(McpArtifactPaths.MCP_EXTENT_REPORT));
        StringBuilder html = new StringBuilder("<html><body class=\"spa -report dark\">")
                .append("<a id=\"nav-dashboard\"></a><div class=\"dashboard-view\"><span>Tests run</span>")
                .append("<span>Passed ").append(passed).append("</span><span>Skipped ").append(skipped).append("</span></div>")
                .append("<script>var statusGroup = { parentCount: ").append(passed + skipped)
                .append(", passParent: ").append(passed)
                .append(", failParent: 0, skipParent: ").append(skipped).append(" };</script>")
                .append("<h1>Complete MCP generated package audit</h1>")
                .append("<p>McpGeneratedPackageValidatorTest MCP contract suite complete MCP report</p>")
                .append("<ul class=\"test-list-item\">");
        for (int index = 1; index <= passed; index++) {
            html.append("<li class=\"test-item\" status=\"pass\" test-id=\"").append(index).append("\">")
                    .append("<p class=\"name\">McpGeneratedPackageValidatorTest pass method ").append(index).append("</p>")
                    .append("<span class=\"badge pass-bg log\">Pass</span></li>");
        }
        for (int index = 1; index <= skipped; index++) {
            String name = index == 1 && firstSkippedName != null
                    ? firstSkippedName
                    : "Mcp precondition skipped method " + index;
            html.append("<li class=\"test-item\" status=\"skip\" test-id=\"").append(passed + index).append("\">")
                    .append("<p class=\"name\">").append(name).append("</p>")
                    .append("<span class=\"badge skip-bg log\">Skip</span></li>");
        }
        html.append("</ul><section>Validation axis: MCP full contract suite. Artifacts: package-state.json, run-state.json, root-source-thin-no-index.diff.</section>");
        html.append(" MCP contract test pass skip ".repeat(4000));
        html.append("</body></html>");
        Files.writeString(extent, html.toString());
        String hash = hashExtentReport(extent);
        Files.writeString(mcp.resolve(fileName(McpArtifactPaths.MCP_EXTENT_SHA256)),
                hash + "  " + fileName(McpArtifactPaths.MCP_EXTENT_REPORT) + "\n");
        Files.writeString(mcp.resolve(fileName(McpArtifactPaths.MCP_EXTENT_ORIGIN)),
                "{\n"
                        + "  \"command\": \"mvn -q -Dtest=Mcp*Test test -Dmcp.package.root=" + packageRoot.getFileName() + "\",\n"
                        + "  \"timestamp\": \"2026-06-29T10:00:00+03:00\",\n"
                        + "  \"exit\": 0,\n"
                        + "  \"artifact\": \"" + fileName(McpArtifactPaths.MCP_EXTENT_REPORT) + "\",\n"
                        + "  \"sha256\": \"" + hash + "\",\n"
                        + "  \"size\": " + Files.size(extent) + "\n"
                        + "}\n");
    }

}
