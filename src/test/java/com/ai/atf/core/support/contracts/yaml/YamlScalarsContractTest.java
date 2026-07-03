package com.ai.atf.core.support.contracts.yaml;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.yaml.YamlScalars;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Map;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.YAML_SCALARS_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.YAML_SCALARS_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class YamlScalarsContractTest {
    @Test
    public void optionalOrDefaultReturnsFallbackForMissingOrBlankValues() {
        Assert.assertEquals(YamlScalars.optionalOrDefault(Map.of("id", "demo"), "scanRoots", "src/main"), "src/main");
        Assert.assertEquals(YamlScalars.optionalOrDefault(Map.of("id", "demo", "scanRoots", "  "), "scanRoots", "src/main"), "src/main");
        Assert.assertEquals(YamlScalars.optionalOrDefault(Map.of("id", "demo", "scanRoots", " src/test "), "scanRoots", "src/main"), "src/test");
    }

    @Test
    public void requiredValueReportsSuppliedContractIdWithoutSyntheticMap() {
        IllegalArgumentException error = Assert.expectThrows(
                IllegalArgumentException.class,
                () -> YamlScalars.requiredValue(" ", "owner", "fixture.yml", "contract", "demo.contract"));

        Assert.assertTrue(error.getMessage().contains("fixture.yml"));
        Assert.assertTrue(error.getMessage().contains("owner"));
        Assert.assertTrue(error.getMessage().contains("demo.contract"));
    }

    @Test
    public void contractIdReturnsStableFallback() {
        Assert.assertEquals(YamlScalars.contractId(Map.of("id", "demo")), "demo");
        Assert.assertEquals(YamlScalars.contractId(Map.of()), "<unknown>");
    }

    @Test
    public void optionalBooleanParsesExplicitValues() {
        Assert.assertTrue(YamlScalars.optionalBoolean(Map.of("id", "demo", "enabled", "true"),
                "enabled", false, "fixture.yml"));
        Assert.assertFalse(YamlScalars.optionalBoolean(Map.of("id", "demo", "enabled", "false"),
                "enabled", true, "fixture.yml"));
    }

    @Test
    public void optionalBooleanRejectsAmbiguousValues() {
        IllegalArgumentException error = Assert.expectThrows(
                IllegalArgumentException.class,
                () -> YamlScalars.optionalBoolean(Map.of("id", "demo", "enabled", "yes"),
                        "enabled", false, "fixture.yml"));

        Assert.assertTrue(error.getMessage().contains("fixture.yml"));
        Assert.assertTrue(error.getMessage().contains("demo"));
        Assert.assertTrue(error.getMessage().contains("yes"));
    }

    @Test
    public void requiredIntReportsContractContext() {
        IllegalArgumentException error = Assert.expectThrows(
                IllegalArgumentException.class,
                () -> YamlScalars.requiredInt(Map.of("id", "demo", "maxScore", "high"),
                        "maxScore", "fixture.yml"));

        Assert.assertTrue(error.getMessage().contains("fixture.yml"));
        Assert.assertTrue(error.getMessage().contains("demo"));
        Assert.assertTrue(error.getMessage().contains("high"));
    }

    @Test
    public void optionalNonNegativeIntegerRejectsNegativeValues() {
        IllegalArgumentException error = Assert.expectThrows(
                IllegalArgumentException.class,
                () -> YamlScalars.optionalNonNegativeInteger(Map.of("id", "demo", "threshold", "-1"),
                        "threshold", "fixture.yml"));

        Assert.assertTrue(error.getMessage().contains("threshold"));
        Assert.assertTrue(error.getMessage().contains("-1"));
    }
}
