package com.ai.ui.core.engine.selenium.network;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.Map;

import static com.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.BODY;
import static com.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.GET_RESPONSE_BODY_COMMAND;
import static com.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.REQUEST_ID;

final class SeleniumResponseBodyReader {
    private final WebDriver driver;

    SeleniumResponseBodyReader(WebDriver driver) {
        this.driver = driver;
    }

    String read(String requestId) {
        if (!(driver instanceof ChromeDriver) || requestId.isBlank()) {
            return "";
        }
        try {
            Map<String, Object> result = ((ChromeDriver) driver).executeCdpCommand(
                    GET_RESPONSE_BODY_COMMAND,
                    Map.of(REQUEST_ID, requestId)
            );
            Object body = result.get(BODY);
            return body == null ? "" : body.toString();
        } catch (WebDriverException ignored) {
            return "";
        }
    }
}
