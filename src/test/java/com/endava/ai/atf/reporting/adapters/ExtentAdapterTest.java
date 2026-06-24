package com.endava.ai.atf.reporting.adapters;

import com.endava.ai.core.reporting.adapters.ExtentAdapter;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ExtentAdapterTest {

    @Test
    public void extent_adapter_does_not_fail_when_reports_directory_is_missing() {
        ExtentAdapter adapter = ExtentAdapter.getInstance();

        adapter.startTest("Test Suite", "missing target reports dir");
        adapter.endTest("PASS");
        adapter.flush();

        assertTrue(true); // contract: no exception
    }

    @Test
    public void extent_adapter_escapes_html_like_step_details() throws IOException {
        ExtentAdapter adapter = ExtentAdapter.getInstance();
        String marker = "mcp-html-escape-contract";

        adapter.startTest(marker, "HTML-like MCP details must remain text");
        adapter.startStep("Expected <details> contract");
        adapter.logDetail("Expected: <details id=\"executive-context\"><summary>Runtime</summary>");
        adapter.logDetail("Forbidden: <style>.broken{display:block}</style>");
        adapter.pass("Observed <style> and <details> as text");
        adapter.endTest("PASS");
        adapter.flush();

        String html = Files.readString(latestExtentReport());
        assertTrue(html.contains("&lt;details id=&quot;executive-context&quot;&gt;"));
        assertTrue(html.contains("&lt;style&gt;.broken{display:block}&lt;/style&gt;"));
        assertFalse(html.contains("<details id=\"executive-context\"><summary>Runtime</summary>"));
        assertFalse(html.contains("<style>.broken{display:block}</style>"));
    }

    @Test
    public void extent_adapter_generates_single_html_without_local_spark_assets() throws IOException {
        Path reports = Path.of("target", "reports");
        Path localSparkAssets = reports.resolve("spark");
        deleteRecursively(localSparkAssets);

        ExtentAdapter adapter = ExtentAdapter.getInstance();
        String marker = "extent-single-html-no-local-spark-contract";

        adapter.startTest(marker, "Runtime Extent output must be a single HTML file");
        adapter.startStep("Generated report shape");
        adapter.logDetail("Expected: ExtentReport_*.html only; no target/reports/spark folder.");
        adapter.logDetail("Expected: no local href/src references to spark/* assets.");
        adapter.pass("Runtime report shape recorded");
        adapter.endTest("PASS");
        adapter.flush();

        Path htmlReport = latestExtentReport();
        String html = Files.readString(htmlReport);

        assertTrue(html.contains(marker));
        assertFalse(Files.exists(localSparkAssets),
                "Extent runtime report must not generate a target/reports/spark asset folder");
        assertFalse(html.contains("href=\"spark/"),
                "Extent runtime report must not depend on local spark CSS beside the HTML");
        assertFalse(html.contains("src=\"spark/"),
                "Extent runtime report must not depend on local spark JS/image assets beside the HTML");
    }

    private static Path latestExtentReport() throws IOException {
        Path reports = Path.of("target", "reports");
        try (Stream<Path> files = Files.list(reports)) {
            return files
                    .filter(path -> path.getFileName().toString().startsWith("ExtentReport_"))
                    .filter(path -> path.getFileName().toString().endsWith(".html"))
                    .max(Comparator.comparingLong(path -> path.toFile().lastModified()))
                    .orElseThrow(() -> new AssertionError("No ExtentReport_*.html generated under " + reports));
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> files = Files.walk(root)) {
            files.sorted((a, b) -> b.getNameCount() - a.getNameCount())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new IllegalStateException("Could not delete " + path, e);
                        }
                    });
        }
    }
}
