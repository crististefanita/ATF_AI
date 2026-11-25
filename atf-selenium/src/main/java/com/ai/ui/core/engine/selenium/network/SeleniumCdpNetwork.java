package com.ai.ui.core.engine.selenium.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class SeleniumCdpNetwork {
    static final String ENABLE_COMMAND = "Network.enable";
    static final String GET_RESPONSE_BODY_COMMAND = "Network.getResponseBody";
    static final String REQUEST_WILL_BE_SENT_EVENT = "Network.requestWillBeSent";
    static final String RESPONSE_RECEIVED_EVENT = "Network.responseReceived";

    static final String BODY = "body";
    static final String MESSAGE = "message";
    static final String METHOD = "method";
    static final String PARAMS = "params";
    static final String POST_DATA = "postData";
    static final String REQUEST = "request";
    static final String REQUEST_ID = "requestId";
    static final String RESPONSE = "response";
    static final String STATUS = "status";
    static final String URL = "url";

    private SeleniumCdpNetwork() {
    }

    static JsonObject object(JsonObject root, String member) {
        if (root == null) {
            return null;
        }
        if (member == null) {
            return root;
        }
        JsonElement value = root.get(member);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : null;
    }

    static String string(JsonObject root, String member) {
        if (root == null) {
            return "";
        }
        JsonElement value = root.get(member);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    static int intValue(JsonObject root, String member) {
        if (root == null) {
            return 0;
        }
        JsonElement value = root.get(member);
        return value == null || value.isJsonNull() ? 0 : value.getAsInt();
    }
}
