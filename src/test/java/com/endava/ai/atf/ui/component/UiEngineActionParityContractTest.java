package com.endava.ai.atf.ui.component;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.ids.AtfContractSteps;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlUiEngineSpec;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.ui.core.engine.UIEngine;
import com.endava.ai.ui.core.engine.playwright.PlaywrightEngine;
import com.endava.ai.ui.core.engine.selenium.SeleniumEngine;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

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

    private static List<String> requiredActions() {
        return AtfCoreYamlContractCatalog.uiEngineContracts().stream()
                .filter(spec -> "required-action".equals(spec.kind()))
                .map(AtfCoreYamlUiEngineSpec::value)
                .collect(Collectors.toUnmodifiableList());
    }
}
