package com.ai.atf.ui.component.parity;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfRuntimeConfigSandbox;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.config.UiEngineType;
import com.ai.core.support.config.UiRuntimeOptions;
import com.ai.core.reporting.listener.TestListener;
import com.ai.ui.core.engine.UIEngine;
import com.ai.ui.core.engine.UIEngineFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ENGINE_ACTION_PARITY)
@AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_ACTION_PARITY_CLASS)
@Listeners(TestListener.class)
public class SeleniumPlaywrightLocatorParityContractTest {
    private AtfRuntimeConfigSandbox config;

    @AfterMethod
    public void tearDown() {
        if (config != null) {
            config.close();
            config = null;
        }
    }

    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_ACTION_PARITY_ADAPTER_BOUNDARY)
    @Test
    public void seleniumSupportsPlaywrightStyleVisibleTextLocatorsUsedByUiSupport() {
        assertVisibleLocatorBehavior(UiEngineType.SELENIUM.configValue());
    }

    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_ACTION_PARITY_ADAPTER_BOUNDARY)
    @Test
    public void seleniumSupportsTextFallbackInsideCompositeLocators() {
        config = AtfRuntimeConfigSandbox.open()
                .setConfig(UiRuntimeOptions.UI_ENGINE_KEY, UiEngineType.SELENIUM.configValue())
                .setConfig(UiRuntimeOptions.UI_HEADLESS_KEY, Boolean.TRUE.toString())
                .setConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "8");

        UIEngine engine = UIEngineFactory.create();
        try {
            engine.open(localLocatorParityUrl());

            Assert.assertTrue(engine.isVisible("text=Download PDF, button:has-text('Download')"));
            engine.click("text=Download PDF, button:has-text('Download')");
            Assert.assertEquals(engine.getText("#download-result"), "download requested");
        } finally {
            engine.quit();
        }
    }

    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_ACTION_PARITY_ADAPTER_BOUNDARY)
    @Test
    public void playwrightPrefersVisibleControlsWhenDuplicateCheckoutFieldsExist() {
        assertVisibleLocatorBehavior(UiEngineType.PLAYWRIGHT.configValue());
    }

    private void assertVisibleLocatorBehavior(String engineName) {
        config = AtfRuntimeConfigSandbox.open()
                .setConfig(UiRuntimeOptions.UI_ENGINE_KEY, engineName)
                .setConfig(UiRuntimeOptions.UI_HEADLESS_KEY, Boolean.TRUE.toString())
                .setConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "8");

        UIEngine engine = UIEngineFactory.create();
        try {
            engine.open(localLocatorParityUrl());

            Assert.assertTrue(engine.isVisible("text=Invoices"));
            Assert.assertTrue(engine.isVisible("select[data-test='payment-method']:visible"));

            engine.select("select[data-test='payment-method']:visible", "cash-on-delivery");
            Assert.assertEquals(engine.getValue("select[data-test='payment-method']:visible"), "cash-on-delivery");

            engine.select("select[data-test='order-status']", "AWAITING_FULFILLMENT");
            Assert.assertEquals(engine.getValue("select[data-test='order-status']"), "awaiting-fulfillment");
            Assert.assertEquals(engine.getText("#status-result"), "awaiting-fulfillment");

            engine.type("#street", "7 Adapter Street");
            Assert.assertEquals(engine.getValue("#street"), "7 Adapter Street");
            Assert.assertEquals(engine.getText("#street-result"), "7 Adapter Street");

            engine.type("#same-value", "5");
            Assert.assertEquals(engine.getValue("#same-value"), "5");
            Assert.assertEquals(engine.getText("#same-value-events"), "0");

            engine.type("#async-overwrite", "manual street");
            Assert.assertEquals(engine.getValue("#async-overwrite"), "manual street");

            engine.type("input[data-test='postal_code']", "010101");
            engine.type("input[data-test='street']", "Audit Street");
            Assert.assertEquals(engine.getValue("input[data-test='postal_code']"), "010101");
            Assert.assertEquals(engine.getValue("input[data-test='street']"), "Audit Street");

            engine.click("tbody tr:has-text('INV-42') button:has-text('Details')");
            Assert.assertEquals(engine.getText("#result"), "opened INV-42");

            engine.pressKey("#field", "Control+A");
            engine.pressKey("#field", "Delete");
            Assert.assertEquals(engine.getValue("#field"), "");
        } finally {
            engine.quit();
        }
    }

    private static String localLocatorParityUrl() {
        String html = "<!doctype html><html><body>"
                + "<h1>Invoices</h1>"
                + "<select data-test='payment-method' style='display:none'><option value='hidden'>hidden</option></select>"
                + "<select data-test='payment-method'>"
                + "<option value='bank-transfer'>Bank transfer</option>"
                + "<option value='cash-on-delivery'>Cash on delivery</option>"
                + "</select>"
                + "<select data-test='order-status' onchange=\"document.getElementById('status-result').textContent=this.value\">"
                + "<option value='draft'>Draft</option>"
                + "<option value='awaiting-fulfillment'>Awaiting fulfillment</option>"
                + "</select>"
                + "<table><tbody>"
                + "<tr><td>INV-41</td><td><button onclick=\"document.getElementById('result').textContent='wrong'\">Details</button></td></tr>"
                + "<tr><td>INV-42</td><td><button onclick=\"document.getElementById('result').textContent='opened INV-42'\">Details</button></td></tr>"
                + "</tbody></table>"
                + "<a href='#download' onclick=\"document.getElementById('download-result').textContent='download requested'\">Download PDF</a>"
                + "<input id='street' value='old' oninput=\"document.getElementById('street-result').textContent=this.value\">"
                + "<input id='same-value' value='5' oninput=\"document.getElementById('same-value-events').textContent='1'\">"
                + "<input id='async-overwrite' value='' oninput=\"if(!this.dataset.overwrote){this.dataset.overwrote='1';"
                + "setTimeout(()=>{this.value='lookup street'; this.dispatchEvent(new Event('input',{bubbles:true}));},40)}\">"
                + "<input data-test='postal_code' value='hidden-10001' style='display:none'>"
                + "<input data-test='postal_code' value='old-postal'>"
                + "<input data-test='street' value='hidden-street' style='display:none'>"
                + "<input data-test='street' value='old-street'>"
                + "<input id='field' value='replace-me'>"
                + "<div id='result'></div>"
                + "<div id='download-result'></div>"
                + "<div id='status-result'></div>"
                + "<div id='street-result'></div>"
                + "<div id='same-value-events'>0</div>"
                + "</body></html>";
        return "data:text/html;charset=utf-8,"
                + URLEncoder.encode(html, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
