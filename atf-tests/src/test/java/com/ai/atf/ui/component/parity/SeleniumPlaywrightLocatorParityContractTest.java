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
import com.ai.ui.core.engine.UiNetworkResponse;
import com.ai.ui.support.pages.AccountPage;
import com.ai.ui.support.pages.AdminOrderPage;
import org.testng.Assert;
import org.testng.Reporter;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.CdpInfo;
import org.openqa.selenium.devtools.CdpVersionFinder;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.net.URLEncoder;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ENGINE_ACTION_PARITY)
@AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_ACTION_PARITY_CLASS)
@Listeners(TestListener.class)
public class SeleniumPlaywrightLocatorParityContractTest {
    private AtfRuntimeConfigSandbox config;

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        AtfRuntimeConfigSandbox ownedConfig = config;
        config = null;
        if (ownedConfig != null) ownedConfig.close();
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

    @Test
    public void seleniumInvoiceSelectorsKeepAdjacentInvoiceNumbersSeparate() {
        assertExactInvoiceSelectors(UiEngineType.SELENIUM.configValue());
    }

    @Test
    public void playwrightInvoiceSelectorsKeepAdjacentInvoiceNumbersSeparate() {
        assertExactInvoiceSelectors(UiEngineType.PLAYWRIGHT.configValue());
    }

    @DataProvider(name = "networkEngines")
    public Object[][] networkEngines() {
        return new Object[][] {{UiEngineType.SELENIUM.configValue()}, {UiEngineType.PLAYWRIGHT.configValue()}};
    }

    // The fixture lifetime includes browser startup and cleanup; network waits retain their own deadline.
    @Test(dataProvider = "networkEngines", timeOut = 60_000)
    public void networkWaitReturnsTheActualLocalRequestAndResponse(String engineName) throws Exception {
        long fixtureStarted = System.nanoTime();
        AtomicInteger requests = new AtomicInteger();
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> requestMethod = new AtomicReference<>();
        HttpServer server = localNetworkServer(requests, requestMethod, requestBody);
        UIEngine engine = null;
        boolean bodyCompleted = false;
        try {
            engine = networkEngine(engineName);
            String origin = "http://127.0.0.1:" + server.getAddress().getPort();
            engine.open(origin + "/");
            if (UiEngineType.SELENIUM.configValue().equals(engineName)) {
                assertPinnedSeleniumRuntime(engine);
            }
            Assert.assertEquals(requests.get(), 0, "Opening the page must not submit the probe request.");
            UIEngine activeEngine = engine;
            recordNetworkPhase(engineName, "POST /contract/reply", "setup-complete", fixtureStarted);

            UiNetworkResponse observed = engine.waitForResponse("/contract/reply", "POST",
                    () -> activeEngine.click("#send-network-request"));

            engine.waitForVisible("#network-result", 3);
            Assert.assertEquals(requests.get(), 1, "One trigger must issue exactly one local probe request.");
            Assert.assertEquals(requestMethod.get(), "POST");
            Assert.assertEquals(requestBody.get(), "{\"reference\":\"local-network\"}");
            Assert.assertEquals(observed.getUrl(), origin + "/contract/reply");
            Assert.assertEquals(observed.getMethod(), "POST");
            Assert.assertEquals(observed.getStatus(), 201);
            Assert.assertTrue(observed.isSuccess());
            Assert.assertEquals(observed.getRequestPreview(), "{\"reference\":\"local-network\"}");
            Assert.assertEquals(observed.getBodyPreview(), "{\"accepted\":true,\"reference\":\"local-network\"}",
                    "A successful capability probe requires the actual body, not the adapter's empty fallback.");
            Assert.assertEquals(engine.getText("#network-result"), "{\"accepted\":true,\"reference\":\"local-network\"}");
            bodyCompleted = true;
            recordNetworkPhase(engineName, "POST /contract/reply", "body-complete", fixtureStarted);
        } finally {
            long cleanupStarted = System.nanoTime();
            try {
                if (engine != null) engine.quit();
            } finally {
                try {
                    server.stop(0);
                } finally {
                    recordNetworkPhase(engineName, "POST /contract/reply", "cleanup-exit; bodyCompleted="
                            + bodyCompleted + "; cleanupMs="
                            + Duration.ofNanos(System.nanoTime() - cleanupStarted).toMillis(), fixtureStarted);
                }
            }
        }
    }

    @DataProvider(name = "networkMismatches")
    public Object[][] networkMismatches() {
        return new Object[][] {
                {UiEngineType.SELENIUM.configValue(), "/contract/never-observed", "POST"},
                {UiEngineType.PLAYWRIGHT.configValue(), "/contract/never-observed", "POST"},
                {UiEngineType.SELENIUM.configValue(), "/contract/reply", "PATCH"},
                {UiEngineType.PLAYWRIGHT.configValue(), "/contract/reply", "PATCH"}
        };
    }

    // This cap includes fixture cleanup; the two-second wait and its 15-second assertion are unchanged.
    @Test(dataProvider = "networkMismatches", timeOut = 60_000)
    public void networkWaitRejectsAnObservedResponseWithTheWrongUrlOrMethod(
            String engineName, String expectedUrl, String expectedMethod) throws Exception {
        long fixtureStarted = System.nanoTime();
        AtomicInteger requests = new AtomicInteger();
        AtomicReference<String> requestBody = new AtomicReference<>();
        AtomicReference<String> requestMethod = new AtomicReference<>();
        HttpServer server = localNetworkServer(requests, requestMethod, requestBody);
        UIEngine engine = null;
        boolean bodyCompleted = false;
        try {
            engine = networkEngine(engineName);
            engine.open("http://127.0.0.1:" + server.getAddress().getPort() + "/");
            UIEngine activeEngine = engine;
            recordNetworkPhase(engineName, expectedMethod + " " + expectedUrl, "setup-complete", fixtureStarted);
            long started = System.nanoTime();

            RuntimeException failure = Assert.expectThrows(RuntimeException.class,
                    () -> activeEngine.waitForResponse(expectedUrl, expectedMethod,
                            () -> activeEngine.click("#send-network-request")));

            long elapsedMillis = Duration.ofNanos(System.nanoTime() - started).toMillis();
            Assert.assertEquals(requests.get(), 1,
                    "The mismatch must be rejected after a real request, not before the trigger executes.");
            Assert.assertEquals(requestMethod.get(), "POST");
            Assert.assertEquals(requestBody.get(), "{\"reference\":\"local-network\"}");
            Assert.assertNotNull(failure.getMessage());
            String diagnostic = failure.getMessage().toLowerCase(Locale.ROOT);
            Assert.assertTrue(diagnostic.contains("timed out") || diagnostic.contains("timeout"),
                    "A missing match must report timeout, not an unrelated browser or unsupported-capability error.");
            Assert.assertTrue(elapsedMillis < Duration.ofSeconds(15).toMillis(),
                    "A two-second configured network wait must remain bounded; observed " + elapsedMillis + " ms.");
            bodyCompleted = true;
            recordNetworkPhase(engineName, expectedMethod + " " + expectedUrl, "body-complete", fixtureStarted);
        } finally {
            long cleanupStarted = System.nanoTime();
            try {
                if (engine != null) engine.quit();
            } finally {
                try {
                    server.stop(0);
                } finally {
                    recordNetworkPhase(engineName, expectedMethod + " " + expectedUrl,
                            "cleanup-exit; bodyCompleted=" + bodyCompleted + "; cleanupMs="
                                    + Duration.ofNanos(System.nanoTime() - cleanupStarted).toMillis(), fixtureStarted);
                }
            }
        }
    }

    private static void recordNetworkPhase(String engineName, String expectedResponse, String phase, long started) {
        Reporter.log("Network fixture: engine=" + engineName + "; expected=" + expectedResponse + "; phase=" + phase
                + "; elapsedMs=" + Duration.ofNanos(System.nanoTime() - started).toMillis(), true);
    }

    private UIEngine networkEngine(String engineName) {
        config = AtfRuntimeConfigSandbox.open()
                .setConfig(UiRuntimeOptions.UI_ENGINE_KEY, engineName)
                .setConfig(UiRuntimeOptions.UI_HEADLESS_KEY, Boolean.TRUE.toString())
                .setConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "2");
        return UIEngineFactory.create();
    }

    private static void assertPinnedSeleniumRuntime(UIEngine engine) throws Exception {
        String pinnedVersion = "158.0.8127.88";
        Field driverField = engine.getClass().getDeclaredField("driver");
        driverField.setAccessible(true);
        ChromeDriver driver = (ChromeDriver) driverField.get(engine);
        Capabilities capabilities = driver.getCapabilities();
        Assert.assertEquals(capabilities.getBrowserVersion(), pinnedVersion);
        Map<?, ?> chrome = (Map<?, ?>) capabilities.getCapability("chrome");
        Assert.assertNotNull(chrome, "ChromeDriver must report its actual driver version.");
        String driverVersion = String.valueOf(chrome.get("chromedriverVersion")).split(" ", 2)[0];
        Assert.assertEquals(driverVersion, pinnedVersion);

        CdpInfo cdp = new CdpVersionFinder().match(capabilities.getBrowserVersion())
                .orElseThrow(() -> new AssertionError("The actual browser requires a typed CDP provider."));
        Assert.assertEquals(cdp.getMajorVersion(), 158);
        Assert.assertEquals(cdp.getClass().getName(), "org.openqa.selenium.devtools.v158.v158CdpInfo");
        Assert.assertEquals(driver.getDevTools().getDomains().getClass().getName(),
                "org.openqa.selenium.devtools.v158.v158Domains",
                "The actual ChromeDriver DevTools instance must use typed CDP 158.");
        Map<String, Object> browserVersion = driver.executeCdpCommand("Browser.getVersion", Map.of());
        Assert.assertTrue(String.valueOf(browserVersion.get("product")).endsWith("/" + pinnedVersion),
                "The browser's CDP product must agree with the WebDriver capability.");

        String configuredBrowser = System.getenv("SE_BROWSER_PATH");
        Assert.assertNotNull(configuredBrowser, "Set SE_BROWSER_PATH to the pinned Chrome for Testing binary.");
        Assert.assertFalse(configuredBrowser.isBlank());
        Map<String, Object> commandLine = driver.executeCdpCommand("Browser.getBrowserCommandLine", Map.of());
        List<?> arguments = (List<?>) commandLine.get("arguments");
        Assert.assertNotNull(arguments, "The actual browser command line must identify the launched binary.");
        Assert.assertFalse(arguments.isEmpty());
        Path actualBinary = Path.of(String.valueOf(arguments.get(0))).toRealPath();
        Path expectedBinary = Path.of(configuredBrowser).toRealPath();
        Assert.assertEquals(actualBinary, expectedBinary,
                "The session must use pinned Chrome for Testing rather than an installed auto-updating Chrome.");
        Reporter.log("Pinned Selenium runtime: browser=" + capabilities.getBrowserVersion()
                + ", driver=" + driverVersion + ", typedCDP=" + cdp.getMajorVersion()
                + ", binary=" + actualBinary, true);
    }

    private static HttpServer localNetworkServer(AtomicInteger requests, AtomicReference<String> requestMethod,
                                               AtomicReference<String> requestBody)
            throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/contract/reply", exchange -> {
            requestMethod.set(exchange.getRequestMethod());
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            requests.incrementAndGet();
            sendLocalResponse(exchange, 201, "application/json",
                    "{\"accepted\":true,\"reference\":\"local-network\"}");
        });
        server.createContext("/", exchange -> sendLocalResponse(exchange, 200, "text/html",
                "<!doctype html><html><body><button id='send-network-request' onclick=\""
                        + "fetch('/contract/reply',{method:'POST',headers:{'Content-Type':'application/json'},"
                        + "body:JSON.stringify({reference:'local-network'})})"
                        + ".then(response=>response.text()).then(body=>{const result=document.getElementById('network-result');"
                        + "result.textContent=body;result.hidden=false;})\">Send local request</button>"
                        + "<div id='network-result' hidden></div></body></html>"));
        server.start();
        return server;
    }

    private static void sendLocalResponse(HttpExchange exchange, int status, String contentType, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        try {
            exchange.sendResponseHeaders(status, bytes.length);
            try (java.io.OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        } finally {
            exchange.close();
        }
    }

    private void assertExactInvoiceSelectors(String engineName) {
        config = AtfRuntimeConfigSandbox.open()
                .setConfig(UiRuntimeOptions.UI_ENGINE_KEY, engineName)
                .setConfig(UiRuntimeOptions.UI_HEADLESS_KEY, Boolean.TRUE.toString())
                .setConfig(UiRuntimeOptions.UI_WAIT_TIMEOUT_SECONDS_KEY, "8");
        String html = "<html><body><table><tbody>"
                + "<tr><td>INV-424</td><td>Wrong product</td><td><button>Details</button><a>Edit</a></td></tr>"
                + "<tr><td>INV-42</td><td>Correct product</td><td>"
                + "<button onclick=\"document.querySelector('#result').textContent='details 42'\">Details</button>"
                + "<a onclick=\"document.querySelector('#result').textContent='edit 42'\">Edit</a>"
                + "</td></tr></tbody></table><div id='result'></div></body></html>";
        UIEngine engine = UIEngineFactory.create();
        try {
            engine.open("data:text/html;charset=utf-8,"
                    + URLEncoder.encode(html, StandardCharsets.UTF_8).replace("+", "%20"));
            Assert.assertTrue(engine.getText(AccountPage.invoiceRow("INV-42")).contains("Correct product"));
            Assert.assertFalse(engine.getText(AccountPage.invoiceRow("INV-42")).contains("Wrong product"));
            engine.click(AccountPage.invoiceDetailsLink("INV-42"));
            Assert.assertEquals(engine.getText("#result"), "details 42");
            engine.click(AdminOrderPage.editLink("INV-42"));
            Assert.assertEquals(engine.getText("#result"), "edit 42");
        } finally {
            engine.quit();
        }
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
