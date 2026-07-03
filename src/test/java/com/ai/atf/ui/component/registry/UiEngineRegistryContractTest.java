package com.ai.atf.ui.component.registry;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import com.ai.core.support.config.UiEngineType;
import com.ai.core.reporting.listener.TestListener;
import com.ai.ui.core.engine.UIEngineFactory;
import com.ai.ui.core.engine.UiEngineRegistry;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.util.Set;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.UI_ENGINE_REGISTRY_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.UI_ENGINE_REGISTRY_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class UiEngineRegistryContractTest {

    @Test
    public void registryPublishesSupportedEngineTypes() {
        Assert.assertEquals(
                UiEngineRegistry.defaults().supportedTypes(),
                Set.of(UiEngineType.PLAYWRIGHT, UiEngineType.SELENIUM)
        );
        Assert.assertTrue(UiEngineRegistry.defaults().supportedValues().contains(UiEngineType.PLAYWRIGHT.configValue()));
        Assert.assertTrue(UiEngineRegistry.defaults().supportedValues().contains(UiEngineType.SELENIUM.configValue()));
    }

    @Test
    public void factoryDelegatesToRegistryInsteadOfConcreteSwitch() throws Exception {
        String source = Files.readString(AtfSourcePaths.mainJava(UIEngineFactory.class));

        Assert.assertTrue(source.contains("UiEngineRegistry.defaults()"));
        Assert.assertTrue(source.contains("REGISTRY.create"));
        Assert.assertFalse(source.contains("new PlaywrightEngine"));
        Assert.assertFalse(source.contains("new SeleniumEngine"));
        Assert.assertFalse(source.contains("switch ("));
    }
}
