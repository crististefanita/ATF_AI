package com.ai.atf.core.support.contract.core.yaml;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.core.yaml.AtfCoreYamlContractCatalog;
import com.ai.core.support.contract.core.yaml.model.AtfCoreYamlPolicySpec;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.stream.Collectors;

@Listeners(TestListener.class)
@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.YAML_MANIFEST)
@AtfContractRef(AtfContractNarrativeIds.Contract.YAML_MANIFEST_CATALOG)
public class AtfCoreReportingShellManifestContractTest {

    @Test
    public void reportingShellManifestPublishesEveryExtentReportKind() {
        Set<String> tokens = AtfCoreYamlContractCatalog.reportingShellContracts().stream()
                .filter(spec -> "report-kind".equals(spec.kind()))
                .map(AtfCoreYamlPolicySpec::token)
                .collect(Collectors.toUnmodifiableSet());

        Assert.assertEquals(
                tokens,
                Set.of(
                        "RUNTIME_VALIDATION",
                        "ATLAS_VALIDATION",
                        "MCP_CLOSEOUT",
                        "AGGREGATE_VALIDATION",
                        "INTERNAL_COMPONENT_FIXTURE"
                ),
                "reporting-shell.yml must document every ExtentReportKind so final report shape is not implicit."
        );
    }
}
