package com.ai.atf.core.reporting.aggregate;

import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.reporting.aggregate.artifact.AggregateReportFiles;

import java.nio.file.Path;

public final class AtfCoreAggregateArtifacts {
    private static final String FRAMEWORK_UNDER_TEST_DIR = "framework-under-test";

    public static final String ATF_TEST_PACKAGE = AtfSourcePaths.ATF_ROOT_PACKAGE;
    public static final String TESTNG_SWEEP_LABEL = ATF_TEST_PACKAGE + " TestNG sweep";
    public static final String EXTENT_ARTIFACT_DIR = AggregateReportFiles.EXTENT_ARTIFACT_DIR;
    public static final String EXTENT_REPORT_FILE = AggregateReportFiles.EXTENT_REPORT_FILE;
    public static final Path DEFAULT_EXTENT_RUNTIME_ROOT =
            AtfSourcePaths.TARGET_ROOT.resolve("atf-core-aggregate-report-extent");
    public static final Path DEFAULT_PUBLISHED_VALIDATION_DIR =
            AtfSourcePaths.TARGET_ROOT.resolve("atf-core-published-validation").resolve(FRAMEWORK_UNDER_TEST_DIR);
    public static final String PUBLISHED_EXTENT_REPORT = "framework-under-test_atf-core-aggregate-extent-report.html";
    public static final String PUBLISHED_MANIFEST = "framework-under-test_atf-core-aggregate-extent-report.README.md";

    private AtfCoreAggregateArtifacts() {
    }

    public static Path defaultExtentRuntimeRoot() {
        return DEFAULT_EXTENT_RUNTIME_ROOT;
    }

    public static Path defaultPublishedValidationDir() {
        return DEFAULT_PUBLISHED_VALIDATION_DIR;
    }

    public static String testNgSweepLabel() {
        return TESTNG_SWEEP_LABEL;
    }

    public static Path extentReport(Path runtimeRoot) {
        return AggregateReportFiles.extentReport(runtimeRoot);
    }

}
