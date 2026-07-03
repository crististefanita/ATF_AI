package com.endava.ai.atf.core.reporting.adapters.css;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfTestGroups;
import com.endava.ai.core.reporting.listener.NoFrameworkReporting;
import com.endava.ai.core.reporting.adapters.extent.spark.style.ExtentSparkCollapsePolicy;
import org.testng.Assert;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SPARK_COLLAPSE_POLICY_COMPONENT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SPARK_COLLAPSE_POLICY_COMPONENT_TEST_CLASS)
@NoFrameworkReporting
@Test(groups = AtfTestGroups.EXTENT_ONLY)
public class ExtentSparkCollapsePolicyComponentTest {

    @Test
    public void collapsePolicyKeepsScriptEnvelopeStable() {
        String script = ExtentSparkCollapsePolicy.defaultCollapseScript();

        Assert.assertTrue(script.startsWith("(function(){"),
                "Collapse policy should keep a self-contained IIFE envelope.");
        Assert.assertTrue(script.endsWith("})();"),
                "Collapse policy should close the IIFE envelope.");
    }

    @Test
    public void collapsePolicyComposesBehaviorSectionsInStableOrder() {
        String script = ExtentSparkCollapsePolicy.defaultCollapseScript();

        int bootstrap = script.indexOf("function directChildBodies(card)");
        int menu = script.indexOf("function normalizeStatusMenu()");
        int selection = script.indexOf("function activeDetailBody()");
        int card = script.indexOf("function setCardCollapsed(card,collapsed)");
        int events = script.indexOf("function scheduleCollapse()");

        Assert.assertTrue(bootstrap >= 0, "Bootstrap section must define direct child body discovery.");
        Assert.assertTrue(menu > bootstrap, "Menu normalization must follow bootstrap utilities.");
        Assert.assertTrue(selection > menu, "Runtime status/tag selection must follow menu helpers.");
        Assert.assertTrue(card > selection, "Card collapse behavior must follow selection helpers.");
        Assert.assertTrue(events > card, "Event wiring must remain the final behavior section.");
    }

    @Test
    public void collapsePolicyExposesRuntimeHooksExactlyOnce() {
        String script = ExtentSparkCollapsePolicy.defaultCollapseScript();

        Assert.assertEquals(count(script, "window.collapseExtentTreeByDefault=function()"), 1,
                "Tree collapse hook should be exported once.");
        Assert.assertEquals(count(script, "window.atfSelectExtentStatus=function(status,label)"), 1,
                "Status filter hook should be exported once.");
        Assert.assertEquals(count(script, "window.atfSelectExtentTag=function(tag)"), 1,
                "Tag filter hook should be exported once.");
        Assert.assertEquals(count(script, "new MutationObserver(scheduleCollapse).observe"), 1,
                "Mutation observer should be registered once.");
        Assert.assertEquals(count(script, "scheduleCollapse();"), 1,
                "Collapse scheduling should run immediately after event wiring, not only after load/mutation.");
        Assert.assertTrue(
                script.indexOf("scheduleCollapse();") < script.indexOf("if(window.MutationObserver)"),
                "Immediate collapse scheduling should happen before mutation-observer registration."
        );
    }

    @Test
    public void collapsePolicyRetainsRuntimeMenuAndNavigationContracts() {
        String script = ExtentSparkCollapsePolicy.defaultCollapseScript();

        Assert.assertTrue(script.contains("ensureStatusItem('pass','Pass','success'"),
                "Status menu must synthesize missing pass entries.");
        Assert.assertTrue(script.contains("ensureStatusItem('fail','Fail','danger'"),
                "Status menu must synthesize missing fail entries.");
        Assert.assertTrue(script.contains("ensureStatusItem('skip','Skip','warning'"),
                "Status menu must synthesize missing skip entries.");
        Assert.assertTrue(script.contains("normalizeStatusItem('skip','Skip','warning');"),
                "Status menu must normalize native skipped entries to the agreed Skip label.");
        Assert.assertTrue(script.contains("normalizeClearItem();")
                        && script.contains("fa fa-times"),
                "Status menu must normalize Clear to the agreed close icon contract.");
        Assert.assertTrue(script.contains("showStatusCollection(status,label||status,matchedCards);"),
                "Status filtering must render a test case collection.");
        Assert.assertTrue(script.contains("function itemStatus(item)")
                        && script.contains("itemStatus(item)===status")
                        && script.contains("function statusSelectionClone(node)")
                        && script.contains("matchedCards.push(statusSelectionClone(source));"),
                "Status filtering must fall back to top-level test-item status for small component reports.");
        Assert.assertTrue(script.contains("showTagCollection(tag,matched);"),
                "Tag filtering must render the selected tag collection.");
    }

    private static int count(String text, String needle) {
        int matches = 0;
        int next = text.indexOf(needle);
        while (next >= 0) {
            matches++;
            next = text.indexOf(needle, next + needle.length());
        }
        return matches;
    }
}
