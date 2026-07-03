package com.endava.ai.atf.ui.component;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfContractSteps;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.atf.ui.component.support.UiEngineThreadLocalTestSupport;
import com.endava.ai.ui.core.engine.UIEngine;
import com.endava.ai.ui.core.engine.UiNetworkResponse;
import com.endava.ai.ui.support.actions.UIActions;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ENGINE_DELEGATION)
@Listeners(TestListener.class)
public class UiEngineDelegationContractTest {

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_DELEGATION_ACTIONS)
    public void uiActionsDelegateThroughActiveUiEngine() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "uiActionsDelegateThroughActiveUiEngine"
        );
        AtfContractSteps.expected(contract);

        RecordingEngine engine = new RecordingEngine();
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);
        try {
            UIActions.click("#submit", "submit button");
            UIActions.type("#email", "email field", "alice@example.test");
            UIActions.getText("#banner", "status banner");
            UiNetworkResponse response = UIActions.waitForResponse(
                    "save profile",
                    "/api/profile",
                    "POST",
                    () -> engine.operations.add("trigger:save-profile")
            );
            UIActions.clearSession();
            Assert.assertEquals(response.getStatus(), 201);
            Assert.assertTrue(response.isSuccess());
        } finally {
            UiEngineThreadLocalTestSupport.setEngineForCurrentThread(null);
        }

        AtfContractSteps.observed("recorded.engine.operations=" + engine.operations);
        Assert.assertEquals(
                engine.operations,
                List.of(
                        "click:#submit",
                        "type:#email=alice@example.test",
                        "getText:#banner",
                        "waitForResponse:POST /api/profile",
                        "trigger:save-profile",
                        "clearSession"
                )
        );
        AtfContractSteps.verdict(
                "UIActions delegates through the active UIEngine contract and remains browser-adapter agnostic.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_DELEGATION_NETWORK_RESPONSE)
    public void uiNetworkResponseKeepsBoundedAuditPayload() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "uiNetworkResponseKeepsBoundedAuditPayload"
        );
        AtfContractSteps.expected(contract);

        String longBody = "token ".repeat(600);
        UiNetworkResponse response = new UiNetworkResponse(
                "https://api.example.test/orders/42",
                "post",
                201,
                longBody
        );

        AtfContractSteps.observed("method=" + response.getMethod()
                + " | status=" + response.getStatus()
                + " | success=" + response.isSuccess()
                + " | previewLength=" + response.getBodyPreview().length());

        Assert.assertEquals(response.getMethod(), "POST");
        Assert.assertTrue(response.isSuccess());
        Assert.assertTrue(response.urlContains("/orders/42"));
        Assert.assertTrue(response.getBodyPreview().length() <= 2_003);
        AtfContractSteps.verdict(
                "Network response evidence remains bounded and detached from browser-specific response objects.",
                contract.repairHint()
        );
    }

    private static final class RecordingEngine implements UIEngine {
        private final List<String> operations = new ArrayList<>();

        @Override public boolean supportsAutoWait() { return true; }
        @Override public void open(String url) { operations.add("open:" + url); }
        @Override public void click(String cssSelector) { operations.add("click:" + cssSelector); }
        @Override public void type(String cssSelector, String text) { operations.add("type:" + cssSelector + "=" + text); }
        @Override public void select(String cssSelector, String valueOrText) { operations.add("select:" + cssSelector + "=" + valueOrText); }
        @Override public String getText(String cssSelector) { operations.add("getText:" + cssSelector); return "recorded text"; }
        @Override public String getValue(String cssSelector) { operations.add("getValue:" + cssSelector); return "recorded value"; }
        @Override public UiNetworkResponse waitForResponse(String urlContains, String method, Runnable trigger) {
            operations.add("waitForResponse:" + method.toUpperCase() + " " + urlContains);
            trigger.run();
            return new UiNetworkResponse("https://api.example.test" + urlContains, method, 201, "{\"ok\":true}");
        }
        @Override public boolean isVisible(String cssSelector) { operations.add("isVisible:" + cssSelector); return true; }
        @Override public void waitForVisible(String cssSelector, int seconds) { operations.add("waitForVisible:" + cssSelector); }
        @Override public void waitForUrlContains(String fragment, int seconds) { operations.add("waitForUrlContains:" + fragment); }
        @Override public String getCurrentUrl() { return "about:blank"; }
        @Override public void clearSession() { operations.add("clearSession"); }
        @Override public String captureScreenshotAsBase64() { operations.add("captureScreenshot"); return "base64"; }
        @Override public void quit() { operations.add("quit"); }
        @Override public void setWindowSize(int width, int height) { operations.add("setWindowSize:" + width + "x" + height); }
    }
}
