package com.endava.ai.ui.core.engine.selenium.network;

import com.endava.ai.ui.core.engine.UiNetworkResponse;
import com.endava.ai.ui.core.wait.UiSleeper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;

import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.ENABLE_COMMAND;
import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.REQUEST_WILL_BE_SENT_EVENT;
import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.RESPONSE_RECEIVED_EVENT;

public final class SeleniumNetworkWaiter {
    private static final Duration POLL_INTERVAL = Duration.ofMillis(100);

    private final WebDriver driver;
    private final int timeoutSeconds;
    private final SeleniumNetworkRequestLedger requestLedger = new SeleniumNetworkRequestLedger();
    private final SeleniumResponseBodyReader responseBodyReader;

    public SeleniumNetworkWaiter(WebDriver driver, int timeoutSeconds) {
        this.driver = driver;
        this.timeoutSeconds = timeoutSeconds;
        this.responseBodyReader = new SeleniumResponseBodyReader(driver);
        enableNetworkEvents();
    }

    public UiNetworkResponse waitForResponse(String urlContains, String method, Runnable trigger) {
        String expectedUrl = valueOrEmpty(urlContains);
        String expectedMethod = valueOrEmpty(method).toUpperCase(Locale.ROOT);
        drainPerformanceLog();
        trigger.run();

        Instant deadline = Instant.now().plusSeconds(timeoutSeconds);
        while (Instant.now().isBefore(deadline)) {
            UiNetworkResponse response = matchingResponse(expectedUrl, expectedMethod);
            if (response != null) {
                return response;
            }
            sleep();
        }

        throw new IllegalStateException("Timed out waiting for Selenium network response "
                + expectedMethod + " " + expectedUrl);
    }

    private UiNetworkResponse matchingResponse(String expectedUrl, String expectedMethod) {
        for (LogEntry entry : performanceLogEntries()) {
            SeleniumPerformanceLogMessage message = SeleniumPerformanceLogMessage.parse(entry);
            if (message == null) {
                continue;
            }
            if (REQUEST_WILL_BE_SENT_EVENT.equals(message.method())) {
                requestLedger.remember(message.params());
                continue;
            }
            if (!RESPONSE_RECEIVED_EVENT.equals(message.method())) {
                continue;
            }
            UiNetworkResponse response = requestLedger.responseIfMatches(
                    message.params(),
                    expectedUrl,
                    expectedMethod,
                    responseBodyReader
            );
            if (response != null) {
                return response;
            }
        }
        return null;
    }

    private void enableNetworkEvents() {
        if (!(driver instanceof ChromeDriver)) {
            return;
        }
        try {
            ((ChromeDriver) driver).executeCdpCommand(ENABLE_COMMAND, Map.of());
        } catch (WebDriverException ignored) {
        }
    }

    private void drainPerformanceLog() {
        performanceLogEntries();
    }

    private LogEntries performanceLogEntries() {
        try {
            return driver.manage().logs().get(LogType.PERFORMANCE);
        } catch (WebDriverException failure) {
            throw new IllegalStateException("Selenium network wait requires Chrome performance logs.", failure);
        }
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private static void sleep() {
        if (!UiSleeper.pause(POLL_INTERVAL)) {
            throw new IllegalStateException("Interrupted while waiting for Selenium network response.");
        }
    }
}
