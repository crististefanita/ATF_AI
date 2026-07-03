package com.endava.ai.atf.core.support.contracts.yaml;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.support.contract.yaml.SimpleYamlContractLoader;
import com.endava.ai.core.support.contract.yaml.YamlScalarLists;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.SIMPLE_YAML_CONTRACT_LOADER_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.SIMPLE_YAML_CONTRACT_LOADER_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class SimpleYamlContractLoaderContractTest {
    @Test
    public void loaderFlattensYamlScalarListsAsCommaSeparatedValues() {
        List<Map<String, String>> entries = SimpleYamlContractLoader.loadText(
                "schemaVersion: 1\n"
                        + "contracts:\n"
                        + "  - id: fixture\n"
                        + "    ruleIds:\n"
                        + "      - alpha.rule\n"
                        + "      - beta.rule\n"
                        + "    nested:\n"
                        + "      artifacts:\n"
                        + "        - one.html\n"
                        + "        - two.xml\n",
                "inline/list-fixture.yml");

        Assert.assertEquals(entries.get(0).get("ruleIds"), "alpha.rule,beta.rule");
        Assert.assertEquals(entries.get(0).get("nested.artifacts"), "one.html,two.xml");
        Assert.assertEquals(YamlScalarLists.commaSeparated(entries.get(0).get("ruleIds")),
                List.of("alpha.rule", "beta.rule"));
    }

    @Test
    public void loaderRejectsNestedObjectsInsideScalarLists() {
        IllegalArgumentException error = Assert.expectThrows(IllegalArgumentException.class,
                () -> SimpleYamlContractLoader.loadText(
                        "schemaVersion: 1\n"
                                + "contracts:\n"
                                + "  - id: fixture\n"
                                + "    ruleIds:\n"
                                + "      - alpha.rule\n"
                                + "      - nested: invalid\n",
                        "inline/invalid-list-fixture.yml"));

        Assert.assertTrue(error.getMessage().contains("ruleIds"));
        Assert.assertTrue(error.getMessage().contains("inline/invalid-list-fixture.yml"));
    }
}
