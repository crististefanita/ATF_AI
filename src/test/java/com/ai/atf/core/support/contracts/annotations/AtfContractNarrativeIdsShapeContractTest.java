package com.ai.atf.core.support.contracts.annotations;

import com.ai.core.reporting.listener.TestListener;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.ids.AtfSourcePaths;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.ANNOTATION_HYGIENE)
@AtfContractRef(AtfContractNarrativeIds.Contract.ANNOTATION_HYGIENE_CATALOG)
@Listeners(TestListener.class)
public class AtfContractNarrativeIdsShapeContractTest {
    private static final Path NARRATIVE_IDS =
            AtfSourcePaths.mainJava("com.ai.core.support.contract.ids.AtfContractNarrativeIds");
    private static final int MAX_REGISTRY_LINES = 640;
    private static final List<String> FORBIDDEN_NARRATIVE_TOKENS = List.of(
            "Expected invariant:",
            "Observed validator result",
            "Repair hint:",
            "Cold verdict");

    @Test
    public void contractNarrativeIdsStayAThinIdRegistry() throws IOException {
        List<String> lines = Files.readAllLines(NARRATIVE_IDS);
        Assert.assertTrue(lines.size() <= MAX_REGISTRY_LINES,
                "AtfContractNarrativeIds must stay a transitional thin id registry. "
                        + "Current lines=" + lines.size() + ", cap=" + MAX_REGISTRY_LINES
                        + ". Move narrative text to contract-narratives.yml or split ids by family before growing.");

        String source = String.join("\n", lines);
        for (String token : FORBIDDEN_NARRATIVE_TOKENS) {
            Assert.assertFalse(source.contains(token),
                    "AtfContractNarrativeIds must not contain report narrative text; keep `" + token
                            + "` in YAML/report narrators.");
        }
    }
}
