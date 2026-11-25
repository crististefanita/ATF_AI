package com.ai.atf.ui.component;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractSteps;
import com.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlUiEngineSpec;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.reporting.ReportLogger;
import com.ai.core.reporting.ReportingManager;
import com.ai.core.reporting.StepLogger;
import com.ai.atf.ui.component.support.UiEngineThreadLocalTestSupport;
import com.ai.ui.core.engine.UIEngine;
import com.ai.ui.core.engine.playwright.PlaywrightEngine;
import com.ai.ui.core.engine.selenium.SeleniumEngine;
import com.ai.ui.core.runtime.UiInteractionTracker;
import com.ai.ui.support.actions.UIActions;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ENGINE_ACTION_PARITY)
@AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_ACTION_PARITY_CLASS)
@Listeners(TestListener.class)
public class UiEngineActionParityContractTest {

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_ACTION_PARITY_INTERFACE_SURFACE)
    public void uiEngineInterfaceExposesCanonicalActionSurface() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "uiEngineInterfaceExposesCanonicalActionSurface"
        );
        AtfContractSteps.expected(contract);

        List<String> actualMethods = methodNames(UIEngine.class);
        List<String> requiredActions = requiredActions();
        List<String> missing = requiredActions.stream()
                .filter(action -> !actualMethods.contains(action))
                .collect(Collectors.toList());

        AtfContractSteps.observed("required.actions=" + requiredActions
                + " | interface.methods=" + actualMethods
                + " | missing=" + missing);

        Assert.assertTrue(missing.isEmpty(), "UIEngine missing required actions: " + missing);
        AtfContractSteps.verdict(
                "UIEngine remains the single canonical action surface.",
                contract.repairHint()
        );
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_ACTION_PARITY_ADAPTER_BOUNDARY)
    public void concreteEnginesStayBehindUiEngineContract() {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "concreteEnginesStayBehindUiEngineContract"
        );
        AtfContractSteps.expected(contract);

        Assert.assertTrue(UIEngine.class.isAssignableFrom(SeleniumEngine.class));
        Assert.assertTrue(UIEngine.class.isAssignableFrom(PlaywrightEngine.class));

        AtfContractSteps.observed("selenium.assignable=true | playwright.assignable=true");
        AtfContractSteps.verdict(
                "Concrete engines remain interchangeable at test-layer boundaries.",
                contract.repairHint()
        );
    }

    private static List<String> methodNames(Class<?> type) {
        return List.of(type.getMethods()).stream()
                .map(Method::getName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @DataProvider(name = "auditedInputValues")
    public Object[][] auditedInputValues() {
        Object[][] inputs = {
                {"password-label", "#entry", "Password", "TEXT", true},
                {"password-locator", "input[data-test='password']", "Entry", "TEXT", true},
                {"password-type", "input[type='password']", "Entry", "TEXT", true},
                {"secret-label", "#entry", "Client secret", "TEXT", true},
                {"token-camel-case", "#apiToken", "Entry", "TEXT", true},
                {"pin-label", "#entry", "PIN code", "TEXT", true},
                {"password-empty", "#password", "Password", "EMPTY", true},
                {"password-null", "#password", "Password", "NULL", true},
                {"ordinary-text", "#email", "Email address", "TEXT", false},
                {"ordinary-empty", "#email", "Email address", "EMPTY", false},
                {"ordinary-null", "#email", "Email address", "NULL", false},
                {"bounded-password-token", "#passwordlessChoice", "Passwordless option", "TEXT", false},
                {"bounded-pin-token", "#spinner", "Spinner count", "TEXT", false}
        };
        List<Object[]> cases = new ArrayList<>();
        for (Object[] input : inputs) {
            for (boolean autoWait : List.of(false, true)) {
                cases.add(new Object[]{input[0], input[1], input[2], input[3], input[4], autoWait});
            }
        }
        return cases.toArray(new Object[0][]);
    }

    @Test(dataProvider = "auditedInputValues")
    public void typedInputKeepsEngineValuesAndAuditableRedactedDetails(
            String caseId, String selector, String description, String valueKind,
            boolean sensitive, boolean autoWait) {
        String value = "NULL".equals(valueKind) ? null
                : "EMPTY".equals(valueKind) ? "" : "audit-input-sentinel";
        List<String> events = new ArrayList<>();
        AtomicReference<String> typedValue = new AtomicReference<>();
        UIEngine engine = recordingInputEngine(events, typedValue, autoWait, null);
        ReportLogger original = ReportingManager.tryGetLogger();
        StepLogger.clear();
        UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);
        try {
            StepLogger.setDelegate(recordingInputReport(events));
            StepLogger.withConsoleMuted(() -> UIActions.type(selector, description, value));
            Assert.assertTrue(Objects.equals(typedValue.get(), value),
                    "The original input must reach the engine unchanged: " + caseId);
            List<String> expected = new ArrayList<>(List.of(
                    "step:Type into element: " + description,
                    "detail:locator=" + selector,
                    "detail:value=" + (sensitive ? "[REDACTED]" : value)));
            if (!autoWait) expected.addAll(List.of("detail:Wait for visible: " + selector, "engine:wait"));
            expected.addAll(List.of("engine:type", "pass:Typed"));
            Assert.assertEquals(events, expected, "Input audit must retain ordered details and one verdict: " + caseId);
            if (sensitive && value != null && !value.isEmpty()) {
                Assert.assertFalse(String.join("\n", events).contains(value), "Sensitive input must not appear in report events");
            }
            verify(engine).supportsAutoWait();
            verify(engine).type(anyString(), nullable(String.class));
            if (!autoWait) verify(engine).waitForVisible(anyString(), anyInt());
            verifyNoMoreInteractions(engine);
        } finally {
            UiEngineThreadLocalTestSupport.setEngineForCurrentThread(null);
            UiInteractionTracker.clear();
            StepLogger.clear();
            StepLogger.setDelegate(original);
        }
    }

    @Test
    public void redactedInputKeepsOriginalEngineFailureAndSingleFailedVerdict() {
        for (boolean autoWait : List.of(false, true)) {
            List<String> events = new ArrayList<>();
            AtomicReference<String> typedValue = new AtomicReference<>();
            RuntimeException failure = new IllegalStateException("Synthetic provider failure");
            UIEngine engine = recordingInputEngine(events, typedValue, autoWait, failure);
            ReportLogger original = ReportingManager.tryGetLogger();
            StepLogger.clear();
            UiEngineThreadLocalTestSupport.setEngineForCurrentThread(engine);
            try {
                StepLogger.setDelegate(recordingInputReport(events));
                AssertionError observed = Assert.expectThrows(AssertionError.class,
                        () -> StepLogger.withConsoleMuted(() -> UIActions.type(
                                "#password", "Password", "audit-failure-sentinel")));
                Assert.assertSame(observed.getCause(), failure, "The existing provider failure identity must be preserved");
                Assert.assertTrue("audit-failure-sentinel".equals(typedValue.get()), "Failure must follow the original typed input");
                Assert.assertTrue(events.contains("detail:value=[REDACTED]"));
                Assert.assertEquals(events.stream().filter(event -> event.startsWith("fail:")).count(), 1L);
                Assert.assertFalse(events.stream().anyMatch(event -> event.startsWith("pass:")));
                Assert.assertFalse(String.join("\n", events).contains("audit-failure-sentinel"));
                Assert.assertTrue(StepLogger.testHasFailed(), "The failed action must retain its failure state");
            } finally {
                UiEngineThreadLocalTestSupport.setEngineForCurrentThread(null);
                UiInteractionTracker.clear();
                StepLogger.clear();
                StepLogger.setDelegate(original);
            }
        }
    }

    private static UIEngine recordingInputEngine(List<String> events, AtomicReference<String> typedValue,
                                                boolean autoWait, RuntimeException failure) {
        UIEngine engine = mock(UIEngine.class);
        when(engine.supportsAutoWait()).thenReturn(autoWait);
        doAnswer(invocation -> { events.add("engine:wait"); return null; })
                .when(engine).waitForVisible(anyString(), anyInt());
        doAnswer(invocation -> {
            typedValue.set(invocation.getArgument(1));
            events.add("engine:type");
            if (failure != null) throw failure;
            return null;
        }).when(engine).type(anyString(), nullable(String.class));
        return engine;
    }

    private static ReportLogger recordingInputReport(List<String> events) {
        ReportLogger report = mock(ReportLogger.class);
        doAnswer(invocation -> { events.add("step:" + invocation.getArgument(0)); return null; })
                .when(report).startStep(anyString());
        doAnswer(invocation -> { events.add("detail:" + invocation.getArgument(0)); return null; })
                .when(report).logDetail(anyString());
        doAnswer(invocation -> { events.add("pass:" + invocation.getArgument(0)); return null; })
                .when(report).pass(anyString());
        doAnswer(invocation -> { events.add("fail:" + invocation.getArgument(0)); return null; })
                .when(report).fail(anyString(), anyString());
        return report;
    }

    private static List<String> requiredActions() {
        return AtfCoreYamlContractCatalog.uiEngineContracts().stream()
                .filter(spec -> "required-action".equals(spec.kind()))
                .map(AtfCoreYamlUiEngineSpec::value)
                .collect(Collectors.toUnmodifiableList());
    }
}
