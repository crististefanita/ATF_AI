package com.ai.atf.ui.component;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.support.config.UiEngineType;
import com.ai.core.support.config.UiScreenshotConfigKeys;
import com.ai.ui.core.engine.UIEngineFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.util.Locale;

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

    @DataProvider(name = "invalidWaits")
    public Object[][] invalidWaits() {
        return new Object[][]{{"not-a-number"}, {"-1"}, {"0"}, {"2147484"}, {"99999999999999999999"}};
    }

    @Test(dataProvider = "invalidWaits")
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_RUNTIME_OPTIONS_INVALID_WAIT)
    public void runtimeOptionsRejectInvalidWaitTimeoutBeforeBrowserStartup(String value) {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "runtimeOptionsRejectInvalidWaitTimeoutBeforeBrowserStartup"
        );
        AtfContractSteps.expected(contract);

        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, value)) {

            IllegalStateException failure = Assert.expectThrows(
                    IllegalStateException.class,
                    UiRuntimeOptions::load
            );

            AtfContractSteps.observed("exception=" + failure.getClass().getSimpleName()
                    + " | message=" + failure.getMessage());
            Assert.assertTrue(failure.getMessage().contains(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY));
            Assert.expectThrows(IllegalStateException.class, UIEngineFactory::create);
        }

        AtfContractSteps.verdict(
                "Invalid runtime wait config fails fast and remains diagnosable.",
                contract.repairHint()
        );
    }

    @DataProvider(name = "invalidBooleans")
    public Object[][] invalidBooleans() {
        String[] keys = {UiRuntimeOptions.UI_HEADLESS_KEY, UiRuntimeOptions.CONSOLE_DETAILS_ENABLED_KEY,
                UiRuntimeOptions.UI_RUNTIME_METADATA_REPORTING_KEY, UiScreenshotConfigKeys.SCREENSHOTS_ENABLED,
                UiScreenshotConfigKeys.FAILURE_ONLY, UiScreenshotConfigKeys.CAPTURE_FINAL_STATE};
        Object[][] cases = new Object[keys.length * 2][2];
        for (int index = 0; index < keys.length; index++) {
            cases[index * 2] = new Object[]{keys[index], "tru"};
            cases[index * 2 + 1] = new Object[]{keys[index], "1"};
        }
        return cases;
    }

    @Test(dataProvider = "invalidBooleans")
    public void runtimeOptionsRejectMalformedBooleanFlags(String key, String value) {
        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(key, value)) {
            IllegalStateException failure = Assert.expectThrows(IllegalStateException.class, UiRuntimeOptions::load);
            Assert.assertTrue(failure.getMessage().contains(key), "The diagnostic names the actual invalid key");
            Assert.assertTrue(failure.getMessage().contains(value), "The diagnostic retains the observed value");
            Assert.expectThrows(IllegalStateException.class, UIEngineFactory::create);
        }
    }

    @Test
    public void runtimeOptionsKeepValidBooleanWaitAndLocaleBoundaries() {
        Locale original = Locale.getDefault();
        try (AtfRuntimeConfigSandbox sandbox = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_ENGINE_KEY, "PLAYWRIGHT")
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_HEADLESS_KEY, " TrUe ")
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "1")) {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Assert.assertEquals(UiRuntimeOptions.load().engineType(), UiEngineType.PLAYWRIGHT);
            Assert.assertTrue(UiRuntimeOptions.load().isHeadless());
            Assert.assertEquals(UiRuntimeOptions.load().waitTimeoutSeconds(), 1);
            sandbox.setConfigAndSystemProperty(UiRuntimeOptions.UI_HEADLESS_KEY, " FaLsE ")
                    .setConfigAndSystemProperty(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "2147483");
            Assert.assertFalse(UiRuntimeOptions.load().isHeadless());
            Assert.assertEquals(UiRuntimeOptions.load().waitTimeoutSeconds(), 2147483);
            sandbox.setConfigAndSystemProperty(UiRuntimeOptions.UI_HEADLESS_KEY, " ");
            Assert.assertTrue(UiRuntimeOptions.load().isHeadless(), "Blank flags retain the documented fallback");
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    public void legacyWaitRejectsInvalidValuesAndKeepsItsOwnDiagnosticKey() {
        try (AtfRuntimeConfigSandbox sandbox = AtfRuntimeConfigSandbox.open()
                .clearConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY)
                .clearSystemProperty(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY)) {
            for (String value : new String[]{"-1", "0", "bad", "2147484"}) {
                sandbox.setConfigAndSystemProperty(UiRuntimeOptions.LEGACY_WAIT_TIMEOUT_SECONDS_KEY, value);
                IllegalStateException failure = Assert.expectThrows(IllegalStateException.class, UiRuntimeOptions::load);
                Assert.assertTrue(failure.getMessage().contains(UiRuntimeOptions.LEGACY_WAIT_TIMEOUT_SECONDS_KEY));
            }
            sandbox.setConfigAndSystemProperty(UiRuntimeOptions.LEGACY_WAIT_TIMEOUT_SECONDS_KEY, "3")
                    .setConfigAndSystemProperty(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "7");
            Assert.assertEquals(UiRuntimeOptions.load().waitTimeoutSeconds(), 7, "The canonical key wins over its legacy fallback");
        }
    }

    @Test
    public void unsupportedEngineFailsDuringOptionLoading() {
        try (AtfRuntimeConfigSandbox ignored = AtfRuntimeConfigSandbox.open()
                .setConfigAndSystemProperty(UiRuntimeOptions.UI_ENGINE_KEY, "unknown-engine")) {
            IllegalArgumentException failure = Assert.expectThrows(IllegalArgumentException.class, UiRuntimeOptions::load);
            Assert.assertTrue(failure.getMessage().contains(UiRuntimeOptions.UI_ENGINE_KEY));
            Assert.assertTrue(failure.getMessage().contains("unknown-engine"));
        }
    }
}
