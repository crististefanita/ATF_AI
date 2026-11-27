package com.ai.ui.core.engine.selenium.network;

import com.ai.ui.core.engine.UiNetworkResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Proxy;

/** Ledger and unsupported-body-reader components; no browser, network or backend is used. */
public class SeleniumNetworkObservationTest {
    @Test
    public void matchingUsesTheObservedRequestIdentityAndRetainsNonSuccessStatus() {
        SeleniumNetworkRequestLedger ledger = new SeleniumNetworkRequestLedger();
        SeleniumResponseBodyReader body = bodyReader();
        JsonObject response = json("{\"requestId\":\"first\",\"response\":{\"url\":\"https://contract.invalid/observed\",\"status\":404}}");
        Assert.assertNull(ledger.responseIfMatches(response, "/observed", "POST", body),
                "A response cannot borrow a method before its request was observed");
        ledger.remember(null);
        ledger.remember(json("{\"requestId\":\"first\",\"request\":{\"method\":\"post\",\"postData\":\"  request  detail  \"}}"));
        Assert.assertNull(ledger.responseIfMatches(null, "/observed", "POST", body));
        Assert.assertNull(ledger.responseIfMatches(response, "/unrelated", "POST", body));
        Assert.assertNull(ledger.responseIfMatches(response, "/observed", "GET", body));
        UiNetworkResponse observed = ledger.responseIfMatches(response, "/observed", "post", body);
        Assert.assertEquals(observed.getUrl(), "https://contract.invalid/observed");
        Assert.assertEquals(observed.getMethod(), "POST");
        Assert.assertEquals(observed.getStatus(), 404);
        Assert.assertFalse(observed.isSuccess());
        Assert.assertEquals(observed.getRequestPreview(), "request detail");
        Assert.assertEquals(observed.getBodyPreview(), "", "An unsupported body reader does not invent a response body");
        Assert.assertEquals(body.read(""), "");
    }

    @Test
    public void independentRequestIdsDoNotBorrowDataAndRepeatedIdsRetainNonblankFields() {
        SeleniumNetworkRequestLedger ledger = new SeleniumNetworkRequestLedger();
        ledger.remember(json("{\"requestId\":\"first\",\"request\":{\"method\":\"POST\",\"postData\":\"first body\"}}"));
        ledger.remember(json("{\"requestId\":\"second\",\"request\":{\"method\":\"GET\",\"postData\":\"second body\"}}"));
        JsonObject second = json("{\"requestId\":\"second\",\"response\":{\"url\":\"/observed\",\"status\":201}}");
        Assert.assertNull(ledger.responseIfMatches(second, "/observed", "POST", bodyReader()));
        Assert.assertEquals(ledger.responseIfMatches(second, "/observed", "GET", bodyReader()).getRequestPreview(), "second body");
        ledger.remember(json("{\"requestId\":\"first\",\"request\":{\"method\":\"PUT\",\"postData\":\"\"}}"));
        JsonObject first = json("{\"requestId\":\"first\",\"response\":{\"url\":\"/observed\",\"status\":202}}");
        UiNetworkResponse updated = ledger.responseIfMatches(first, "/observed", "PUT", bodyReader());
        Assert.assertEquals(updated.getRequestPreview(), "first body",
                "The existing ledger preserves nonblank data for a repeated ID; do not claim empty-body clearing");
        Assert.assertEquals(updated.getMethod(), "PUT");
    }

    private SeleniumResponseBodyReader bodyReader() {
        WebDriver driver = (WebDriver) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class<?>[]{WebDriver.class}, (proxy, method, args) -> {
                    throw new AssertionError("An unsupported body reader must not call the driver");
                });
        return new SeleniumResponseBodyReader(driver);
    }

    private JsonObject json(String value) {
        return JsonParser.parseString(value).getAsJsonObject();
    }
}
