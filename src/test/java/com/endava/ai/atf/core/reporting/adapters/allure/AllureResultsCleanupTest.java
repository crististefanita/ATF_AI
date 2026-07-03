package com.endava.ai.atf.core.reporting.adapters.allure;

import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.annotations.Listeners;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.support.contract.ids.AtfContractSteps;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.core.support.config.ConfigManager;
import com.endava.ai.core.support.config.ReportingConfigKeys;
import com.endava.ai.core.reporting.internal.ReportingEngineCleanup;
import com.endava.ai.core.reporting.internal.ReportingPaths;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import static com.endava.ai.atf.core.support.contract.AtfReportingArtifactAssertions.assertDirectoryState;

@SuppressWarnings("ResultOfMethodCallIgnored")
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ALLURE_RESULTS_CLEANUP)
@AtfContractRef(AtfContractNarrativeIds.Contract.ALLURE_RESULTS_CLEANUP_CLASS)
@Listeners(TestListener.class)
@Test(groups = AtfTestGroups.ALLURE_ONLY)
public class AllureResultsCleanupTest {

    private static final String ALLURE_CLEANUP_ROOT_PROPERTY = "atf.allure.cleanup.root";
    private static final String DEFAULT_ALLURE_CLEANUP_ROOT = ".codex-artifacts/allure-cleanup-contract";

    private File allureDir;
    private AtfRuntimeConfigSandbox config;

    @BeforeMethod
    void setUp() {
        config = AtfRuntimeConfigSandbox.open()
                .setSystemProperty(
                        ReportingPaths.ALLURE_RESULTS_DIRECTORY_PROPERTY,
                        System.getProperty(ALLURE_CLEANUP_ROOT_PROPERTY, DEFAULT_ALLURE_CLEANUP_ROOT)
                );
        allureDir = ReportingPaths.allureResultsDirectory();
        deleteDir(allureDir);
        allureDir.mkdirs();
        new File(allureDir, "dummy.txt");
    }

    @AfterMethod
    void tearDown() {
        deleteDir(allureDir);
        ConfigManager.clear(ReportingConfigKeys.REPORTING_ENGINE);
        if (config != null) {
            config.close();
            config = null;
        }
    }

    @DataProvider
    public Object[][] cleanupCases() {
        return new Object[][]{
                {new CleanupCase("extent-deletes-allure-results", ReportingConfigKeys.ENGINE_EXTENT, false)},
                {new CleanupCase("allure-preserves-allure-results", ReportingConfigKeys.ENGINE_ALLURE, true)},
                {new CleanupCase("extent-never-leaves-allure-results", ReportingConfigKeys.ENGINE_EXTENT, false)}
        };
    }

    @Test(dataProvider = "cleanupCases")
    @AtfContractRef(AtfContractNarrativeIds.Contract.ALLURE_RESULTS_CLEANUP_ENGINE_MATRIX)
    public void cleanup_respects_reporting_engine_contract(CleanupCase testCase) {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "cleanup_respects_reporting_engine_contract"
        );
        AtfContractSteps.expected(contract);

        config.setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, testCase.reportingEngine);
        ReportingEngineCleanup.onShutdown();

        AtfContractSteps.observed(testCase.observed(allureDir));
        assertDirectoryState(allureDir, testCase.expectedAllureDirExists, testCase.name);
        AtfContractSteps.verdict(
                "Reporting cleanup case passed: " + testCase.name,
                contract.repairHint()
        );
    }

    private static void deleteDir(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }

        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    file.delete();
                }
            }
        }

        dir.delete();
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class CleanupCase {
        private final String name;
        private final String reportingEngine;
        private final boolean expectedAllureDirExists;

        private String observed(File allureDir) {
            return "case=" + name
                    + " | reporting.engine=" + reportingEngine
                    + " | expected.allure.dir.exists=" + expectedAllureDirExists
                    + " | actual.allure.dir.exists=" + allureDir.exists()
                    + " | directory=" + allureDir.getAbsolutePath();
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
