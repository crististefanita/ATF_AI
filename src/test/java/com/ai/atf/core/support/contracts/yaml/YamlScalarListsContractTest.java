package com.ai.atf.core.support.contracts.yaml;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.yaml.YamlScalarLists;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.YAML_SCALAR_LISTS_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.YAML_SCALAR_LISTS_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class YamlScalarListsContractTest {
    @Test
    public void commaSeparatedTrimsAndDropsBlankParts() {
        Assert.assertEquals(
                YamlScalarLists.commaSeparated(" alpha, , beta ,gamma "),
                List.of("alpha", "beta", "gamma"));
    }

    @Test
    public void commaSeparatedAlternativesKeepsPipeGroups() {
        Assert.assertEquals(
                YamlScalarLists.commaSeparatedAlternatives("a|b, c, d| e "),
                List.of(List.of("a", "b"), List.of("c"), List.of("d", "e")));
    }

    @Test
    public void strictCommaSeparatedAlternativesRejectsBlankCommaGroups() {
        IllegalArgumentException error = Assert.expectThrows(
                IllegalArgumentException.class,
                () -> YamlScalarLists.strictCommaSeparatedAlternatives("a, ,b", "fixture.requiredLinks"));

        Assert.assertTrue(error.getMessage().contains("fixture.requiredLinks"));
        Assert.assertTrue(error.getMessage().contains("position 2"));
    }

    @Test
    public void strictCommaSeparatedAlternativesRejectsBlankPipeAlternatives() {
        IllegalArgumentException error = Assert.expectThrows(
                IllegalArgumentException.class,
                () -> YamlScalarLists.strictCommaSeparatedAlternatives("a||b", "fixture.requiredArtifacts"));

        Assert.assertTrue(error.getMessage().contains("fixture.requiredArtifacts"));
        Assert.assertTrue(error.getMessage().contains("separator '|'"));
    }

    @Test
    public void strictCommaSeparatedAlternativesKeepsValidGroups() {
        Assert.assertEquals(
                YamlScalarLists.strictCommaSeparatedAlternatives("a|b,c", "fixture.valid"),
                List.of(List.of("a", "b"), List.of("c")));
    }

    @Test
    public void pipeSeparatedTrimsAndDropsBlankParts() {
        Assert.assertEquals(
                YamlScalarLists.pipeSeparated(" one | | two |three "),
                List.of("one", "two", "three"));
    }
}
