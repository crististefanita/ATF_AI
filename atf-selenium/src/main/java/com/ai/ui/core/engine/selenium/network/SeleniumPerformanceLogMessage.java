package com.ai.ui.core.engine.selenium.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.openqa.selenium.logging.LogEntry;

import static com.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.MESSAGE;
import static com.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.METHOD;
import static com.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.PARAMS;
import static com.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.object;
import static com.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.string;

final class SeleniumPerformanceLogMessage {
    private final JsonObject message;

    private SeleniumPerformanceLogMessage(JsonObject message) {
        this.message = message;
    }

    static SeleniumPerformanceLogMessage parse(LogEntry entry) {
        try {
            JsonElement root = JsonParser.parseString(entry.getMessage());
            JsonObject message = object(object(root.getAsJsonObject(), MESSAGE), null);
            return message == null ? null : new SeleniumPerformanceLogMessage(message);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    String method() {
        return string(message, METHOD);
    }

    JsonObject params() {
        return object(message, PARAMS);
    }
}
