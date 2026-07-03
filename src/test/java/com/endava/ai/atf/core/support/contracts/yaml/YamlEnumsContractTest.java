package com.endava.ai.atf.core.support.contracts.yaml;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.support.contract.yaml.YamlEnums;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Map;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.YAML_ENUMS_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.YAML_ENUMS_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class YamlEnumsContractTest {
    @Test
    public void enumValueNormalizesCaseAndHyphenatedNames() {
        Assert.assertEquals(
                YamlEnums.enumValue(SampleKind.class, "high-value", "kind", "fixture.yml", Map.of("id", "demo")),
                SampleKind.HIGH_VALUE);
    }

    @Test
    public void enumValueReportsContractContextForInvalidValues() {
        IllegalArgumentException error = Assert.expectThrows(
                IllegalArgumentException.class,
                () -> YamlEnums.enumValue(SampleKind.class, "missing", "kind", "fixture.yml", Map.of("id", "demo")));

        Assert.assertTrue(error.getMessage().contains("fixture.yml"));
        Assert.assertTrue(error.getMessage().contains("demo"));
        Assert.assertTrue(error.getMessage().contains("missing"));
    }

    private enum SampleKind {
        HIGH_VALUE
    }
}
