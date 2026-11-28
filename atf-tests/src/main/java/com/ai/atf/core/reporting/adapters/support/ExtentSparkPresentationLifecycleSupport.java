package com.ai.atf.core.reporting.adapters.support;

import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.config.ReportingConfigKeys;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.listener.internal.runtime.publish.ExtentRuntimeAuditRegistry;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.core.reporting.adapters.ExtentAdapter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.atf.core.reporting.adapters.support.ExtentAdapterContractSupport.deleteRecursively;

public abstract class ExtentSparkPresentationLifecycleSupport {

    protected AtfRuntimeConfigSandbox config;
    protected Path contractReportsDir;

    @BeforeMethod
    public void setUp() throws IOException {
        contractReportsDir = Files.createTempDirectory("atf-extent-spark-presentation-");
        config = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTING_ENGINE, ReportingConfigKeys.ENGINE_EXTENT)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_DIR, contractReportsDir.toString())
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_TIMESTAMP_ENABLED, ReportingConfigKeys.TIMESTAMP_ENABLED)
                .setConfigAndSystemProperty(ReportingConfigKeys.REPORTS_FILE_NAME, "");
        resetExtentRuntime();
    }

    @AfterMethod
    public void tearDown() throws IOException {
        ReportingManager.reset();
        StepLogger.clear();
        TestListener.resetForTests();
        ExtentRuntimeAuditRegistry.clear();
        ExtentAdapter.getInstance().resetForTests();
        if (config != null) {
            config.close();
            config = null;
        }
        if (contractReportsDir != null) {
            deleteRecursively(contractReportsDir);
            contractReportsDir = null;
        }
    }

    protected static void resetExtentRuntime() {
        ExtentAdapterContractSupport.resetExtentRuntime();
    }
}
