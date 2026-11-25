package com.ai.atf.ui.component;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.ui.core.engine.UIEngine;
import com.ai.ui.core.engine.UiUnsupportedAction;
import com.ai.ui.core.engine.playwright.PlaywrightEngine;
import com.ai.ui.core.engine.selenium.SeleniumEngine;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ENGINE_CAPABILITY)
@Listeners(TestListener.class)
public class UiEngineCapabilityContractTest {

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_CAPABILITY_ADAPTER_DIFFERENCES)
    public void uiEngineCapabilitiesDocumentIntentionalAdapterDifferences() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "uiEngineCapabilitiesDocumentIntentionalAdapterDifferences"
        );
        AtfContractSteps.expected(contract);

        UIEngine.Capabilities playwright = PlaywrightEngine.CAPABILITIES;
        UIEngine.Capabilities selenium = SeleniumEngine.CAPABILITIES;

        AtfContractSteps.observed("playwright.capabilities=" + playwright.supported()
                + " | selenium.capabilities=" + selenium.supported());

        Assert.assertTrue(playwright.supports(UIEngine.Capability.AUTO_WAIT));
        Assert.assertTrue(playwright.supports(UIEngine.Capability.NETWORK_RESPONSE_WAIT));
        Assert.assertFalse(selenium.supports(UIEngine.Capability.AUTO_WAIT));
        Assert.assertTrue(selenium.supports(UIEngine.Capability.NETWORK_RESPONSE_WAIT));
        Assert.assertTrue(selenium.supports(UIEngine.Capability.VISIBLE_INDEX_CLICK));
        Assert.assertTrue(selenium.supports(UIEngine.Capability.SCREENSHOT_CAPTURE));
        AtfContractSteps.verdict(
                "Capability parity is explicit: business tests may wait for network responses on both Playwright and Selenium.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_CAPABILITY_UNSUPPORTED_DIAGNOSTICS)
    public void optionalUiActionsExposeClearUnsupportedDiagnostics() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "optionalUiActionsExposeClearUnsupportedDiagnostics"
        );
        AtfContractSteps.expected(contract);

        MinimalEngine engine = new MinimalEngine();

        UnsupportedOperationException keyPress = Assert.expectThrows(
                UnsupportedOperationException.class,
                () -> engine.pressKey("#field", "Enter")
        );
        UnsupportedOperationException upload = Assert.expectThrows(
                UnsupportedOperationException.class,
                () -> engine.uploadFile("#file", "fixtures/upload.txt")
        );
        UnsupportedOperationException attribute = Assert.expectThrows(
                UnsupportedOperationException.class,
                () -> engine.getAttribute("#field", "value")
        );
        UnsupportedOperationException network = Assert.expectThrows(
                UnsupportedOperationException.class,
                () -> engine.waitForResponse("/api", "POST", () -> {})
        );
        UnsupportedOperationException visibleClick = Assert.expectThrows(
                UnsupportedOperationException.class,
                () -> engine.clickVisible(".item", 1)
        );

        AtfContractSteps.observed("pressKey.message=" + keyPress.getMessage()
                + " | uploadFile.message=" + upload.getMessage()
                + " | getAttribute.message=" + attribute.getMessage()
                + " | waitForResponse.message=" + network.getMessage()
                + " | clickVisible.message=" + visibleClick.getMessage());

        Assert.assertEquals(keyPress.getMessage(), UiUnsupportedAction.KEY_PRESS.message());
        Assert.assertEquals(upload.getMessage(), UiUnsupportedAction.FILE_UPLOAD.message());
        Assert.assertEquals(attribute.getMessage(), UiUnsupportedAction.ATTRIBUTE_LOOKUP.message());
        Assert.assertEquals(network.getMessage(), UiUnsupportedAction.NETWORK_RESPONSE_WAIT.message());
        Assert.assertEquals(visibleClick.getMessage(), UiUnsupportedAction.VISIBLE_INDEX_CLICK.message());
        AtfContractSteps.verdict(
                "Unsupported optional UI actions fail loudly and readably.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_CAPABILITY_NO_RAW_SCRIPT)
    public void uiEngineDoesNotExposeRawBrowserScriptExecution() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "uiEngineDoesNotExposeRawBrowserScriptExecution"
        );
        AtfContractSteps.expected(contract);

        List<String> methods = List.of(UIEngine.class.getMethods()).stream()
                .map(Method::getName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        AtfContractSteps.observed("interface.methods=" + methods);
        Assert.assertFalse(
                methods.contains("evaluateScript"),
                "UIEngine must not expose raw browser script execution to generated tests."
        );
        AtfContractSteps.verdict(
                "Browser script execution remains behind adapter internals or dedicated audited helpers.",
                contract.repairHint()
        );
    }

    private static final class MinimalEngine implements UIEngine {
        @Override public boolean supportsAutoWait() { return false; }
        @Override public void open(String url) {}
        @Override public void click(String cssSelector) {}
        @Override public void type(String cssSelector, String text) {}
        @Override public void select(String cssSelector, String valueOrText) {}
        @Override public String getText(String cssSelector) { return ""; }
        @Override public String getValue(String cssSelector) { return ""; }
        @Override public boolean isVisible(String cssSelector) { return false; }
        @Override public void waitForVisible(String cssSelector, int seconds) {}
        @Override public void waitForUrlContains(String fragment, int seconds) {}
        @Override public String getCurrentUrl() { return ""; }
        @Override public void clearSession() {}
        @Override public String captureScreenshotAsBase64() { return ""; }
        @Override public void quit() {}
        @Override public void setWindowSize(int width, int height) {}
    }
}
