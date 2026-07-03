package com.ai.skills.mcp.tests.support;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HtmlDetailsMutatorContractTest {
    @Test
    public void replacesTargetSectionAfterMalformedHistoricalDetailsBlock() {
        String html = "<main>"
                + "<details><summary>Historical malformed section</summary><p>legacy text"
                + "<details><summary><span class=\"summary-title\">Run Audit</span></summary>"
                + "<p>old run audit</p></details>"
                + "</main>";

        String mutated = HtmlDetailsMutator.replaceDetailsSection(html, "Run Audit", "<p>new run audit</p>");

        Assert.assertTrue(mutated.contains("<p>new run audit</p>"),
                "Mutator must skip malformed historical blocks and replace the valid target section.");
        Assert.assertFalse(mutated.contains("<p>old run audit</p>"),
                "Target section body must be replaced.");
    }
}
