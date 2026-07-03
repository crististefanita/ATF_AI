package com.ai.atf.ui.component.engine;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.ui.core.engine.selenium.SeleniumEngine;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ENGINE_CAPABILITY)
@Listeners(TestListener.class)
public class SeleniumEngineInternalContractTest {

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_CAPABILITY_ADAPTER_DIFFERENCES)
    public void seleniumNetworkAndDriverFallbackStayBehindAdapterInternals() throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "seleniumNetworkAndDriverFallbackStayBehindAdapterInternals"
        );
        AtfContractSteps.expected(contract);

        Path browserSession = UiEngineContractSourceSupport.uiEngineSource("selenium.SeleniumBrowserSession");
        Path chromeDriverFactory = UiEngineContractSourceSupport.uiEngineSource("selenium.SeleniumChromeDriverFactory");
        Path chromeDriverCache = UiEngineContractSourceSupport.uiEngineSource("selenium.SeleniumChromeDriverCache");
        Path seleniumEngine = AtfSourcePaths.mainJava(SeleniumEngine.class);
        Path networkWaiter = UiEngineContractSourceSupport.uiEngineSource("selenium.network.SeleniumNetworkWaiter");
        Path cdpNetwork = UiEngineContractSourceSupport.uiEngineSource("selenium.network.SeleniumCdpNetwork");
        Path performanceLogMessage = UiEngineContractSourceSupport.uiEngineSource("selenium.network.SeleniumPerformanceLogMessage");
        Path requestLedger = UiEngineContractSourceSupport.uiEngineSource("selenium.network.SeleniumNetworkRequestLedger");
        Path responseBodyReader = UiEngineContractSourceSupport.uiEngineSource("selenium.network.SeleniumResponseBodyReader");

        AtfContractSteps.observed("selenium.internals=" + browserSession + ", " + chromeDriverFactory
                + ", " + chromeDriverCache + ", " + networkWaiter);

        UiEngineContractSourceSupport.assertSourceContains(browserSession, "SeleniumChromeDriverFactory");
        UiEngineContractSourceSupport.assertSourceContains(chromeDriverFactory, "webdriver.chrome.driver");
        UiEngineContractSourceSupport.assertSourceContains(chromeDriverFactory, "SeleniumChromeDriverCache");
        UiEngineContractSourceSupport.assertSourceContains(chromeDriverFactory, "LogType.PERFORMANCE");
        UiEngineContractSourceSupport.assertSourceContains(chromeDriverFactory, "goog:loggingPrefs");
        UiEngineContractSourceSupport.assertSourceContains(chromeDriverCache, "atf.selenium.cache.root");
        UiEngineContractSourceSupport.assertSourceContains(chromeDriverCache, ".cache");
        UiEngineContractSourceSupport.assertSourceContains(chromeDriverCache, "chromedriver");
        UiEngineContractSourceSupport.assertSourceContains(seleniumEngine, "UIEngine.Capability.NETWORK_RESPONSE_WAIT");
        UiEngineContractSourceSupport.assertSourceContains(seleniumEngine, "waitForResponse(String urlContains, String method, Runnable trigger)");
        UiEngineContractSourceSupport.assertSourceContains(networkWaiter, "SeleniumPerformanceLogMessage.parse");
        UiEngineContractSourceSupport.assertSourceContains(networkWaiter, "requestLedger.responseIfMatches");
        UiEngineContractSourceSupport.assertSourceContains(cdpNetwork, "Network.enable");
        UiEngineContractSourceSupport.assertSourceContains(cdpNetwork, "Network.responseReceived");
        UiEngineContractSourceSupport.assertSourceContains(cdpNetwork, "Network.getResponseBody");
        UiEngineContractSourceSupport.assertSourceContains(performanceLogMessage, "JsonParser.parseString");
        UiEngineContractSourceSupport.assertSourceContains(requestLedger, "new UiNetworkResponse");
        UiEngineContractSourceSupport.assertSourceContains(responseBodyReader, "executeCdpCommand");

        AtfContractSteps.verdict(
                "Selenium-specific CDP, performance logging, and driver fallback stay inside Selenium network internals.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_CAPABILITY_PACKAGE_ARCHITECTURE)
    public void seleniumElementInternalsStayGroupedByResponsibility() throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "seleniumElementInternalsStayGroupedByResponsibility"
        );
        AtfContractSteps.expected(contract);

        Path elementActions = UiEngineContractSourceSupport.uiEngineSource("selenium.element.SeleniumElementActions");
        Path clickActions = UiEngineContractSourceSupport.uiEngineSource("selenium.element.actions.SeleniumClickActions");
        Path typingActions = UiEngineContractSourceSupport.uiEngineSource("selenium.element.actions.SeleniumTypingActions");
        Path formActions = UiEngineContractSourceSupport.uiEngineSource("selenium.element.actions.SeleniumFormActions");
        Path scriptExecutor = UiEngineContractSourceSupport.uiEngineSource("selenium.element.scripts.SeleniumScriptExecutor");
        Path domScripts = UiEngineContractSourceSupport.uiEngineSource("selenium.element.scripts.SeleniumDomScripts");
        Path selectorResolver = UiEngineContractSourceSupport.uiEngineSource("selenium.element.selectors.SeleniumSelectorResolver");
        Path selectorScripts = UiEngineContractSourceSupport.uiEngineSource("selenium.element.selectors.SeleniumSelectorScripts");
        Path elementReader = UiEngineContractSourceSupport.uiEngineSource("selenium.element.readers.SeleniumElementReader");
        Path textReader = UiEngineContractSourceSupport.uiEngineSource("selenium.element.readers.SeleniumTextReader");
        Path valueReader = UiEngineContractSourceSupport.uiEngineSource("selenium.element.readers.SeleniumValueReader");
        Path stateReader = UiEngineContractSourceSupport.uiEngineSource("selenium.element.readers.SeleniumStateReader");

        AtfContractSteps.observed("selenium.element.facade=" + elementActions
                + " | packages=actions, selectors, readers, scripts");

        UiEngineContractSourceSupport.assertSourceContains(elementActions, "SeleniumClickActions");
        UiEngineContractSourceSupport.assertSourceContains(elementActions, "SeleniumTypingActions");
        UiEngineContractSourceSupport.assertSourceContains(elementActions, "SeleniumFormActions");
        UiEngineContractSourceSupport.assertSourceContains(clickActions, "CLICK_ATTEMPTS");
        UiEngineContractSourceSupport.assertSourceContains(clickActions, "element.click();");
        UiEngineContractSourceSupport.assertSourceContains(typingActions, "FILL_STABILITY_POLLS");
        UiEngineContractSourceSupport.assertSourceContains(formActions, "SeleniumSelectStrategy");
        UiEngineContractSourceSupport.assertSourceContains(scriptExecutor, "tryExecute");
        UiEngineContractSourceSupport.assertSourceContains(elementReader, "SeleniumTextReader");
        UiEngineContractSourceSupport.assertSourceContains(elementReader, "SeleniumValueReader");
        UiEngineContractSourceSupport.assertSourceContains(elementReader, "SeleniumStateReader");
        UiEngineContractSourceSupport.assertSourceContains(textReader, "documentBodyText()");
        UiEngineContractSourceSupport.assertSourceContains(valueReader, "getDomProperty");
        UiEngineContractSourceSupport.assertSourceContains(stateReader, "isDisplayed()");
        UiEngineContractSourceSupport.assertSourceContains(domScripts, "DISPATCH_SELECT_EVENTS");
        UiEngineContractSourceSupport.assertSourceContains(selectorResolver, "SeleniumSelectorScripts.BODY_TEXT");
        UiEngineContractSourceSupport.assertSourceContains(selectorScripts, "HIT_TEST_VISIBLE");
        UiEngineContractSourceSupport.assertSourceContains(selectorScripts, "FIND_MATCHES");

        AtfContractSteps.verdict(
                "Selenium element internals are split by action, selector, reader, and script responsibilities.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_CAPABILITY_PACKAGE_ARCHITECTURE)
    public void playwrightElementInternalsStayGroupedByResponsibility() throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "playwrightElementInternalsStayGroupedByResponsibility"
        );
        AtfContractSteps.expected(contract);

        Path elementActions = UiEngineContractSourceSupport.uiEngineSource("playwright.element.PlaywrightElementActions");
        Path clickActions = UiEngineContractSourceSupport.uiEngineSource("playwright.element.actions.PlaywrightClickActions");
        Path typingActions = UiEngineContractSourceSupport.uiEngineSource("playwright.element.actions.PlaywrightTypingActions");
        Path formActions = UiEngineContractSourceSupport.uiEngineSource("playwright.element.actions.PlaywrightFormActions");
        Path selectStrategy = UiEngineContractSourceSupport.uiEngineSource("playwright.element.actions.PlaywrightSelectStrategy");
        Path visibleClickWaiter = UiEngineContractSourceSupport.uiEngineSource("playwright.element.actions.PlaywrightVisibleClickWaiter");
        Path locatorResolver = UiEngineContractSourceSupport.uiEngineSource("playwright.element.selectors.PlaywrightLocatorResolver");
        Path readActions = UiEngineContractSourceSupport.uiEngineSource("playwright.element.readers.PlaywrightReadActions");
        Path domScripts = UiEngineContractSourceSupport.uiEngineSource("playwright.element.readers.PlaywrightDomScripts");

        AtfContractSteps.observed("playwright.element.facade=" + elementActions
                + " | packages=actions, selectors, readers");

        UiEngineContractSourceSupport.assertSourceContains(elementActions, "PlaywrightClickActions");
        UiEngineContractSourceSupport.assertSourceContains(elementActions, "PlaywrightTypingActions");
        UiEngineContractSourceSupport.assertSourceContains(elementActions, "PlaywrightFormActions");
        UiEngineContractSourceSupport.assertSourceContains(elementActions, "PlaywrightReadActions");
        UiEngineContractSourceSupport.assertSourceContains(clickActions, "locator.click();");
        UiEngineContractSourceSupport.assertSourceContains(typingActions, "FILL_STABILITY_POLLS");
        UiEngineContractSourceSupport.assertSourceContains(formActions, "PlaywrightSelectStrategy.values()");
        UiEngineContractSourceSupport.assertSourceContains(selectStrategy, "NORMALIZED_VALUE_OR_TEXT");
        UiEngineContractSourceSupport.assertSourceContains(visibleClickWaiter, "Visible click index is 1-based.");
        UiEngineContractSourceSupport.assertSourceContains(locatorResolver, "UiSelectorSyntax.withVisiblePseudo");
        UiEngineContractSourceSupport.assertSourceContains(readActions, "PlaywrightDomScripts.VISIBLE_TEXTS");
        UiEngineContractSourceSupport.assertSourceContains(domScripts, "VISIBLE_TEXTS");

        AtfContractSteps.verdict(
                "Playwright element internals mirror Selenium's action, selector, and reader package split.",
                contract.repairHint()
        );
    }
}
