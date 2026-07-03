package com.endava.ai.atf.core.reporting.window;

import com.endava.ai.core.support.contract.wait.AtfTestWaiter;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.adapters.extent.config.ExtentReportFiles;
import com.endava.ai.ui.core.engine.UIEngine;
import com.microsoft.playwright.Page;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ZoomRuntimeReportingTestSupport {

    private ZoomRuntimeReportingTestSupport() {
    }

    static Page page(UIEngine engine) throws ReflectiveOperationException {
        Field field = engine.getClass().getDeclaredField("page");
        field.setAccessible(true);
        return (Page) field.get(engine);
    }

    static String documentZoom(UIEngine engine) throws ReflectiveOperationException {
        return String.valueOf(evaluate(engine, "return document.documentElement.style.zoom;"));
    }

    static Object evaluate(UIEngine engine, String javascript) throws ReflectiveOperationException {
        if (engine.getClass().getName().contains("SeleniumEngine")) {
            WebDriver driver = (WebDriver) readField(engine, "driver");
            return ((JavascriptExecutor) driver).executeScript(javascript);
        }
        return page(engine).evaluate("() => { " + javascript + " }");
    }

    static Object evaluateExpression(UIEngine engine, String javascriptExpression) throws ReflectiveOperationException {
        if (engine.getClass().getName().contains("SeleniumEngine")) {
            WebDriver driver = (WebDriver) readField(engine, "driver");
            return ((JavascriptExecutor) driver).executeScript("return " + javascriptExpression + ";");
        }
        return page(engine).evaluate("() => " + javascriptExpression);
    }

    static void waitUntilUrlContains(UIEngine engine, String fragment) {
        if (AtfTestWaiter.awaitTrue(Duration.ofSeconds(5), () -> engine.getCurrentUrl().contains(fragment))) {
            return;
        }
        Assert.fail("Expected current URL to contain '" + fragment + "', but was: " + engine.getCurrentUrl());
    }

    static String appNavigationSourceUrl() {
        String target = dataUrl("<!doctype html><html><body><h1 id='zoom-target'>zoom-target</h1></body></html>");
        return dataUrl("<!doctype html><html><body><a id='next' href='" + target + "'>Next</a></body></html>");
    }

    static String zoomMetadataProbeUrl() {
        return dataUrl("<!doctype html><html><body><main id='zoom-metadata-probe'>zoom metadata</main></body></html>");
    }

    static Path prepareDirectory(Path relativePath) throws IOException {
        Path directory = relativePath.toAbsolutePath();
        deleteRecursively(directory);
        Files.createDirectories(directory);
        return directory;
    }

    static Path extentReportContaining(String marker, Path preferredRoot) throws IOException {
        Path preferred = findExtentReportContaining(preferredRoot, marker);
        if (preferred != null) {
            return preferred;
        }

        Path fallback = findExtentReportContaining(Path.of(ReportingConfigKeys.DEFAULT_REPORTS_DIR), marker);
        if (fallback != null) {
            return fallback;
        }

        throw new AssertionError("No Extent report containing marker '" + marker
                + "' found under " + preferredRoot + " or " + ReportingConfigKeys.DEFAULT_REPORTS_DIR);
    }

    static String readAllTextFiles(Path root) throws IOException {
        StringBuilder content = new StringBuilder();
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path file : stream.filter(Files::isRegularFile).collect(Collectors.toList())) {
                content.append(Files.readString(file)).append('\n');
            }
        }
        return content.toString();
    }

    static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }

        try (Stream<Path> stream = Files.walk(root)) {
            stream.sorted(Comparator.comparingInt(Path::getNameCount).reversed())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new IllegalStateException("Failed to delete " + path, e);
                        }
                    });
        }
    }

    private static String dataUrl(String html) {
        return "data:text/html;charset=utf-8,"
                + URLEncoder.encode(html, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static Object readField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static Path findExtentReportContaining(Path root, String marker) throws IOException {
        if (!Files.exists(root)) {
            return null;
        }

        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> ExtentReportFiles.isExtentHtmlReport(path.getFileName().toString()))
                    .sorted(Comparator.comparingLong(ZoomRuntimeReportingTestSupport::lastModified).reversed())
                    .filter(path -> fileContains(path, marker))
                    .findFirst()
                    .orElse(null);
        }
    }

    private static boolean fileContains(Path path, String marker) {
        try {
            return Files.readString(path).contains(marker);
        } catch (IOException ignored) {
            return false;
        }
    }

    private static long lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ignored) {
            return Long.MIN_VALUE;
        }
    }
}
