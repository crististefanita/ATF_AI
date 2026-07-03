package com.endava.ai.ui.core.engine.selenium.network;

import com.endava.ai.ui.core.engine.UiNetworkResponse;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.METHOD;
import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.POST_DATA;
import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.REQUEST;
import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.REQUEST_ID;
import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.RESPONSE;
import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.STATUS;
import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.URL;
import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.intValue;
import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.object;
import static com.endava.ai.ui.core.engine.selenium.network.SeleniumCdpNetwork.string;

final class SeleniumNetworkRequestLedger {
    private final Map<String, String> requestMethods = new HashMap<>();
    private final Map<String, String> requestBodies = new HashMap<>();

    void remember(JsonObject params) {
        if (params == null) {
            return;
        }
        String requestId = string(params, REQUEST_ID);
        JsonObject request = object(params, REQUEST);
        String method = string(request, METHOD);
        String postData = string(request, POST_DATA);
        if (!requestId.isBlank() && !method.isBlank()) {
            requestMethods.put(requestId, method.toUpperCase(Locale.ROOT));
        }
        if (!requestId.isBlank() && !postData.isBlank()) {
            requestBodies.put(requestId, postData);
        }
    }

    UiNetworkResponse responseIfMatches(
            JsonObject params,
            String expectedUrl,
            String expectedMethod,
            SeleniumResponseBodyReader bodyReader
    ) {
        if (params == null) {
            return null;
        }
        String requestId = string(params, REQUEST_ID);
        JsonObject response = object(params, RESPONSE);
        String url = string(response, URL);
        String actualMethod = requestMethods.getOrDefault(requestId, "");
        if (!url.contains(expectedUrl) || !expectedMethod.equalsIgnoreCase(actualMethod)) {
            return null;
        }
        return new UiNetworkResponse(
                url,
                actualMethod,
                intValue(response, STATUS),
                requestBodies.getOrDefault(requestId, ""),
                bodyReader.read(requestId)
        );
    }
}
