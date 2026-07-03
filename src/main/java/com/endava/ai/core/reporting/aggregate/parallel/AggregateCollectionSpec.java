package com.endava.ai.core.reporting.aggregate.parallel;

import java.nio.file.Path;
import java.util.Objects;

public final class AggregateCollectionSpec {
    private final String id;
    private final String displayName;
    private final String command;
    private final Path outputRoot;

    public AggregateCollectionSpec(String id, String displayName, String command, Path outputRoot) {
        this.id = requireText(id, "id");
        this.displayName = requireText(displayName, "displayName");
        this.command = requireText(command, "command");
        this.outputRoot = Objects.requireNonNull(outputRoot, "outputRoot").toAbsolutePath().normalize();
    }

    public static AggregateCollectionSpec of(String id, String displayName, String command, Path outputRoot) {
        return new AggregateCollectionSpec(id, displayName, command, outputRoot);
    }

    public static AggregateCollectionSpec mavenSuite(String id, String displayName, String suiteXml, Path outputRoot) {
        Path root = Objects.requireNonNull(outputRoot, "outputRoot").toAbsolutePath().normalize();
        String command = "mvn -q"
                + quoted("-Datf.build.directory=" + root.resolve("maven-target"))
                + quoted("-Dsurefire.suiteXmlFiles=" + requireText(suiteXml, "suiteXml"))
                + quoted("-Dsurefire.reportsDirectory=" + root.resolve("surefire-reports"))
                + quoted("-Dreporting.engine=extent")
                + quoted("-Dreports.dir=" + root.resolve("reports"))
                + quoted("-Dreports.file.name=ExtentReport.html")
                + quoted("-Dreports.timestamp.enabled=false")
                + quoted("-DforkCount=0")
                + " test";
        return new AggregateCollectionSpec(id, displayName, command, root);
    }

    public static AggregateCollectionSpec mavenSuiteReusingWorkspaceBuild(
            String id,
            String displayName,
            String suiteXml,
            Path outputRoot
    ) {
        Path root = Objects.requireNonNull(outputRoot, "outputRoot").toAbsolutePath().normalize();
        String command = "mvn -q surefire:test"
                + quoted("-Dsurefire.suiteXmlFiles=" + requireText(suiteXml, "suiteXml"))
                + quoted("-Dsurefire.reportsDirectory=" + root.resolve("surefire-reports"))
                + quoted("-Dreporting.engine=extent")
                + quoted("-Dreports.dir=" + root.resolve("reports"))
                + quoted("-Dreports.file.name=ExtentReport.html")
                + quoted("-Dreports.timestamp.enabled=false")
                + quoted("-DforkCount=0");
        return new AggregateCollectionSpec(id, displayName, command, root);
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String command() {
        return command;
    }

    public Path outputRoot() {
        return outputRoot;
    }

    public Path surefireReportsDirectory() {
        return outputRoot.resolve("surefire-reports");
    }

    public Path mavenBuildDirectory() {
        return outputRoot.resolve("maven-target");
    }

    public Path testNgResultsXml() {
        return surefireReportsDirectory().resolve("testng-results.xml");
    }

    public Path extentReportsDirectory() {
        return outputRoot.resolve("reports");
    }

    public Path extentReport() {
        return extentReportsDirectory().resolve("ExtentReport.html");
    }

    public boolean usesIsolatedMavenOutputs() {
        return command.contains("-Dsurefire.reportsDirectory=" + surefireReportsDirectory())
                && command.contains("-Datf.build.directory=" + mavenBuildDirectory())
                && command.contains("-Dreporting.engine=extent")
                && command.contains("-Dreports.dir=" + extentReportsDirectory())
                && command.contains("-Dreports.file.name=ExtentReport.html")
                && command.contains("-Dreports.timestamp.enabled=false");
    }

    private static String requireText(String value, String name) {
        String text = Objects.requireNonNull(value, name).trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Missing aggregate collection " + name);
        }
        return text;
    }

    private static String quoted(String argument) {
        return " '" + argument + "'";
    }
}
