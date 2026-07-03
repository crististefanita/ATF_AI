package com.ai.skills.atlas.rules.audit.manifest;

import com.ai.core.support.config.ConfigManager;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.skills.atlas.AtlasContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AtlasTestAuditManifestPublisher {
    private static final Pattern README_LINK = Pattern.compile(
            "href=\"(validation/[^\"]+\\.README\\.md)\"",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern ATLAS_AUDIT_COUNTS = Pattern.compile(
            "(?is)Total:\\s*(\\d+)\\s*;\\s*Passed:\\s*(\\d+)\\s*;\\s*Failed:\\s*(\\d+)\\s*;\\s*Skipped:\\s*(\\d+)");

    private AtlasTestAuditManifestPublisher() {
    }

    public static void publishFor(AtlasContext context) throws IOException {
        String manifestLink = readmeManifestLink(context.atlasText());
        if (manifestLink.isBlank()) {
            return;
        }

        Path manifest = context.projectRoot()
                .resolve(AtlasContext.ATLAS_DIRECTORY)
                .resolve(manifestLink)
                .normalize();
        Files.createDirectories(manifest.getParent());

        AuditCounts counts = auditCounts(context.atlasText());
        String extentArtifact = manifest.getFileName().toString()
                .replace(AtlasContext.README_SUFFIX, AtlasContext.HTML_SUFFIX);
        List<String> lines = List.of(
                "# Atlas validation manifest",
                "",
                "| Field | Value |",
                "| --- | --- |",
                "| Extent validation artifact | `" + extentArtifact + "` |",
                "| Atlas artifact | `" + context.atlasHtml().getFileName() + "` |",
                "| Source root | `" + context.sourceRoot() + "` |",
                "| UI source root | `" + context.uiSourceRoot() + "` |",
                "| Total invocations | `" + counts.total + "` |",
                "| Passed | `" + counts.passed + "` |",
                "| Failed | `" + counts.failed + "` |",
                "| Skipped | `" + counts.skipped + "` |",
                "| Maven command | `" + mavenCommand(context) + "` |",
                "| Generated at | `" + Instant.now() + "` |",
                "",
                "This manifest is generated from the Atlas Test Audit row for the currently declared atlas artifact."
        );
        Files.write(manifest, lines, StandardCharsets.UTF_8);
    }

    private static String readmeManifestLink(String atlasHtml) {
        Matcher matcher = README_LINK.matcher(atlasHtml);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static AuditCounts auditCounts(String atlasHtml) {
        Matcher matcher = ATLAS_AUDIT_COUNTS.matcher(atlasHtml);
        if (matcher.find()) {
            return new AuditCounts(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)),
                    Integer.parseInt(matcher.group(4)));
        }
        return new AuditCounts(0, 0, 0, 0);
    }

    private static String mavenCommand(AtlasContext context) {
        String suiteXml = System.getProperty("surefire.suiteXmlFiles",
                "src/test/resources/skills/atlas/testng-atlas-business-closeout.xml");
        String reportsDir = ConfigManager.get(ReportingConfigKeys.REPORTS_DIR, "docs/atlas/validation");
        String reportName = ConfigManager.get(ReportingConfigKeys.REPORTS_FILE_NAME, defaultReportName(context));
        return "mvn -q \"-Dsurefire.suiteXmlFiles=" + suiteXml + "\""
                + " \"-Datlas.html=" + context.projectRoot().relativize(context.atlasHtml()).toString().replace('\\', '/') + "\""
                + " \"-Datlas.sourceRoot=" + context.sourceRoot() + "\""
                + " \"-Datlas.uiSourceRoot=" + context.uiSourceRoot() + "\""
                + " \"-Dreporting.engine=extent\""
                + " \"-Dreports.dir=" + reportsDir + "\""
                + " \"-Dreports.file.name=" + reportName + "\""
                + " \"-Dreports.timestamp.enabled=false\""
                + " \"-DforkCount=0\" test";
    }

    private static String defaultReportName(AtlasContext context) {
        return context.atlasHtml().getFileName().toString()
                .replace(AtlasContext.BUSINESS_ATLAS_SUFFIX, AtlasContext.ATLAS_EXTENT_REPORT_SUFFIX);
    }

    private static final class AuditCounts {
        private final int total;
        private final int passed;
        private final int failed;
        private final int skipped;

        private AuditCounts(int total, int passed, int failed, int skipped) {
            this.total = total;
            this.passed = passed;
            this.failed = failed;
            this.skipped = skipped;
        }
    }
}
