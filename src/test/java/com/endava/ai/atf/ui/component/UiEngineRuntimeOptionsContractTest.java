package com.endava.ai.atf.ui.component;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfContractSteps;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.support.config.UiRuntimeOptions;
import com.endava.ai.core.support.config.UiEngineType;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ENGINE_RUNTIME_OPTIONS)
@AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_RUNTIME_OPTIONS_CLASS)
@Listeners(TestListener.class)
public class UiEngineRuntimeOptionsContractTest {
    private static final String MIXED_CASE_PLAYWRIGHT = "Playwright";
    private static final String DEFAULT_ENGINE_ALIAS = "default";

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_RUNTIME_OPTIONS_TYPED_CONFIG)
    public void runtimeOptionsExposeTypedUiEngineConfig() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "runtimeOptionsExposeTypedUiEngineConfig"
        );
        AtfContractSteps.expected(contract);

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_ENGINE_KEY, MIXED_CASE_PLAYWRIGHT)
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_HEADLESS_KEY, Boolean.FALSE.toString())
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "7")
                .setConfigAndSystemProperty(UiRuntimeOptions.CONSOLE_DETAILS_ENABLED_KEY, Boolean.TRUE.toString())
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_RUNTIME_METADATA_REPORTING_KEY, Boolean.TRUE.toString())
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_PAGE_ZOOM_PERCENT_KEY, "67")) {

            UiRuntimeOptions options = UiRuntimeOptions.load();

            AtfContractSteps.observed("engine=" + options.engineName()
                    + " | headless=" + options.isHeadless()
                    + " | wait=" + options.waitTimeoutSeconds()
                    + " | console.details=" + options.isConsoleDetailsEnabled()
                    + " | runtime.metadata=" + options.isRuntimeMetadataReportingEnabled()
                    + " | configured.zoom=" + options.configuredZoomPercent());

            Assert.assertEquals(options.engineName(), UiEngineType.PLAYWRIGHT.configValue());
            Assert.assertEquals(options.engineType(), UiEngineType.PLAYWRIGHT);
            Assert.assertFalse(options.isHeadless());
            Assert.assertEquals(options.waitTimeoutSeconds(), 7);
            Assert.assertTrue(options.isConsoleDetailsEnabled());
            Assert.assertTrue(options.isRuntimeMetadataReportingEnabled());
            Assert.assertEquals(options.configuredZoomPercent(), "67");
        }

        AtfContractSteps.verdict(
                "UI runtime keys are centralized and typed before engine construction.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_RUNTIME_OPTIONS_FACTORY_DISPATCH)
    public void runtimeOptionsExposeCanonicalEngineTypeForFactoryDispatch() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "runtimeOptionsExposeCanonicalEngineTypeForFactoryDispatch"
        );
        AtfContractSteps.expected(contract);

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_ENGINE_KEY, DEFAULT_ENGINE_ALIAS)) {

            UiRuntimeOptions options = UiRuntimeOptions.load();
            AtfContractSteps.observed("raw.engine=" + options.engineName()
                    + " | canonical.type=" + options.engineType());

            Assert.assertEquals(options.engineName(), DEFAULT_ENGINE_ALIAS);
            Assert.assertEquals(options.engineType(), UiEngineType.SELENIUM);
        }

        AtfContractSteps.verdict(
                "Factory dispatch can use a typed engine model while reports keep the configured raw value.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_RUNTIME_OPTIONS_LEGACY_WAIT)
    public void runtimeOptionsPreserveLegacyWaitTimeoutFallback() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "runtimeOptionsPreserveLegacyWaitTimeoutFallback"
        );
        AtfContractSteps.expected(contract);

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .clearConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY)
                .setConfig(UiRuntimeOptions.LEGACY_WAIT_TIMEOUT_SECONDS_KEY, "3")) {

            UiRuntimeOptions options = UiRuntimeOptions.load();
            AtfContractSteps.observed("legacy.wait.timeout.seconds=3 | resolved.wait="
                    + options.waitTimeoutSeconds());

            Assert.assertEquals(options.waitTimeoutSeconds(), 3);
        }

        AtfContractSteps.verdict(
                "Legacy wait timeout remains compatible without spreading fallback logic into concrete engines.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_RUNTIME_OPTIONS_INVALID_WAIT)
    public void runtimeOptionsRejectInvalidWaitTimeoutBeforeBrowserStartup() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "runtimeOptionsRejectInvalidWaitTimeoutBeforeBrowserStartup"
        );
        AtfContractSteps.expected(contract);

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "not-a-number")) {

            IllegalStateException failure = Assert.expectThrows(
                    IllegalStateException.class,
                    UiRuntimeOptions::load
            );

            AtfContractSteps.observed("exception=" + failure.getClass().getSimpleName()
                    + " | message=" + failure.getMessage());
            Assert.assertTrue(failure.getMessage().contains(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY));
        }

        AtfContractSteps.verdict(
                "Invalid runtime wait config fails fast and remains diagnosable.",
                contract.repairHint()
        );
    }
}
