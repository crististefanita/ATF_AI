package com.endava.ai.atf.core.support.contract.core;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.core.AtfCoreArea;
import com.endava.ai.core.support.contract.core.AtfCoreContractCatalog;
import com.endava.ai.core.support.contract.core.AtfCoreContractEvidence;
import com.endava.ai.core.support.contract.core.AtfCoreContractSpec;
import com.endava.ai.atf.core.support.architecture.CoreMcpBoundaryContractTest;
import com.endava.ai.atf.core.reporting.architecture.CoreArchitectureGuardContractTest;
import com.endava.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ATF_CORE_CONTRACT_CATALOG_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ATF_CORE_CONTRACT_CATALOG_TEST_CLASS)
@Listeners(TestListener.class)
public class AtfCoreContractCatalogTest {

    @Test
    public void catalogExtractsGenericCoreContractMetadata() {
        List<AtfCoreContractSpec> contracts = AtfCoreContractCatalog.contractsFor(
                CoreMcpBoundaryContractTest.class
        );
        List<AtfCoreContractEvidence> evidence = AtfCoreContractCatalog.evidenceFor(
                CoreMcpBoundaryContractTest.class
        );

        Assert.assertEquals(contracts.size(), 1);
        Assert.assertEquals(contracts.get(0).id(), "core.architecture.no-mcp-dependency");
        Assert.assertEquals(contracts.get(0).area(), AtfCoreArea.ARCHITECTURE_BOUNDARY);
        Assert.assertTrue(contracts.get(0).repairHint().contains("com.endava.ai.skills.mcp"));
        Assert.assertEquals(evidence.size(), 1);
        Assert.assertEquals(evidence.get(0).contractId(), contracts.get(0).id());
    }

    @Test
    public void catalogExtractsReqsCoreGuardrailMetadata() {
        List<AtfCoreContractSpec> contracts = AtfCoreContractCatalog.contractsFor(
                CoreArchitectureGuardContractTest.class
        );
        List<AtfCoreContractEvidence> evidence = AtfCoreContractCatalog.evidenceFor(
                CoreArchitectureGuardContractTest.class
        );

        Assert.assertEquals(contracts.size(), 1);
        Assert.assertEquals(contracts.get(0).id(), "core.architecture.reqscore-guardrails");
        Assert.assertEquals(contracts.get(0).area(), AtfCoreArea.ARCHITECTURE_BOUNDARY);
        Assert.assertEquals(evidence.size(), 1);
        Assert.assertEquals(evidence.get(0).contractId(), contracts.get(0).id());
    }
}
