package com.ai.atf.core.support.contracts.yaml;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.yaml.ContractIndex;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.CONTRACT_INDEX_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.CONTRACT_INDEX_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class ContractIndexContractTest {
    @Test
    public void indexReportsDuplicateIdsWithoutHidingTheCatalogDiagnostic() {
        ContractIndex<Entry> index = ContractIndex.of(
                "test-contracts.yml",
                List.of(new Entry("alpha"), new Entry("beta"), new Entry("alpha")),
                Entry::id);

        Assert.assertEquals(index.duplicateIds(), List.of("alpha"));
        Assert.assertEquals(index.require(" beta ").id(), "beta");
        Assert.expectThrows(IllegalStateException.class, () -> index.require("alpha"));
    }

    @Test
    public void uniqueIndexFailsFastForRuntimeContractCatalogs() {
        IllegalStateException error = Assert.expectThrows(
                IllegalStateException.class,
                () -> ContractIndex.unique(
                        "runtime-contracts.yml",
                        List.of(new Entry("alpha"), new Entry("beta"), new Entry("alpha")),
                        Entry::id));

        Assert.assertTrue(error.getMessage().contains("Duplicate contract ids [alpha]"));
        Assert.assertTrue(error.getMessage().contains("runtime-contracts.yml"));
    }

    @Test
    public void indexRejectsMissingBlankAndNullIdsWithExplicitFailures() {
        ContractIndex<Entry> index = ContractIndex.of(
                "test-contracts.yml",
                List.of(new Entry("alpha")),
                Entry::id);

        Assert.expectThrows(IllegalArgumentException.class, () -> index.require("missing"));
        Assert.expectThrows(IllegalArgumentException.class, () -> index.require(" "));
        Assert.expectThrows(NullPointerException.class, () -> index.require(null));
        Assert.expectThrows(
                IllegalArgumentException.class,
                () -> ContractIndex.of("test-contracts.yml", List.of(new Entry(" ")), Entry::id));
        Assert.expectThrows(
                NullPointerException.class,
                () -> ContractIndex.of("test-contracts.yml", List.of(new Entry(null)), Entry::id));
    }

    @Getter(AccessLevel.PRIVATE)
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Entry {
        private final String id;
    }
}
