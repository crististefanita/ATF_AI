package com.endava.ai.atf.ui.component.config;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfContractSteps;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.endava.ai.core.support.contract.core.yaml.model.AtfCoreYamlUiEngineSpec;
import com.endava.ai.core.support.config.ConfigManager;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.FRAMEWORK_RUNTIME_UI_DEFAULTS)
@AtfContractRef(AtfContractNarrativeIds.Contract.FRAMEWORK_RUNTIME_UI_DEFAULTS_CLASS)
@Listeners(TestListener.class)
public class FrameworkRuntimeUiDefaultsContractTest {

    private static final Path FRAMEWORK_PROPERTIES = ConfigManager.FRAMEWORK_PROPERTIES_SOURCE;

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.FRAMEWORK_RUNTIME_UI_DEFAULTS_PROPERTIES)
    public void frameworkPropertiesPublishesCanonicalUiRuntimeDefaults() throws IOException {
        AtfContractSteps.ContractDetails contract = AtfContractSteps.contractDetailsFor(
                getClass(),
                "frameworkPropertiesPublishesCanonicalUiRuntimeDefaults"
        );
        AtfContractSteps.expected(contract);

        Properties properties = loadFrameworkProperties();
        StringBuilder observed = new StringBuilder();

        for (Map.Entry<String, String> entry : expectedUiDefaults().entrySet()) {
            String key = entry.getKey();
            String expectedValue = entry.getValue();
            String actualValue = properties.getProperty(key, "").trim();
            observed.append(key).append("=").append(actualValue).append(" | ");

            Assert.assertEquals(
                    actualValue,
                    expectedValue,
                    key + " must remain aligned with the canonical UI runtime defaults"
            );
        }

        AtfContractSteps.observed("source=" + FRAMEWORK_PROPERTIES + " | " + observed);
        AtfContractSteps.verdict(
                "framework.properties keeps the canonical UI runtime defaults explicit and auditable.",
                contract.repairHint()
        );
    }

    private static Properties loadFrameworkProperties() throws IOException {
        Assert.assertTrue(
                Files.exists(FRAMEWORK_PROPERTIES),
                "framework.properties must exist at " + FRAMEWORK_PROPERTIES
        );

        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(FRAMEWORK_PROPERTIES)) {
            properties.load(input);
        }
        return properties;
    }

    private static Map<String, String> expectedUiDefaults() {
        return AtfCoreYamlContractCatalog.uiEngineContracts().stream()
                .filter(spec -> "ui-default".equals(spec.kind()))
                .collect(Collectors.toUnmodifiableMap(
                        AtfCoreYamlUiEngineSpec::key,
                        AtfCoreYamlUiEngineSpec::value
                ));
    }
}
