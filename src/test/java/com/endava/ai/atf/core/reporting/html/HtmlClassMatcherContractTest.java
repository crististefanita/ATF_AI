package com.endava.ai.atf.core.reporting.html;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.html.HtmlClassMatcher;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.REPORTING_PACKAGE_SHAPE)
@AtfContractRef(AtfContractNarrativeIds.Contract.REPORTING_PACKAGE_SHAPE_CLASS)
@Listeners(TestListener.class)
public final class HtmlClassMatcherContractTest {

    @Test
    public void matches_css_classes_as_tokens_not_substrings() {
        String html = String.join("\n",
                "<section class='score-wall'>",
                "  <article class='score-card featured'>",
                "    <div class='label'>Overall</div>",
                "    <div class='score'>94</div>",
                "  </article>",
                "</section>");

        Assert.assertTrue(HtmlClassMatcher.hasClass(html, "score-card"));
        Assert.assertTrue(HtmlClassMatcher.hasClass(html, "score"));
        Assert.assertFalse(HtmlClassMatcher.attributesContainClass("class='score-card featured'", "score"));
        Assert.assertEquals(HtmlClassMatcher.countClass(html, "score-card"), 1);
        Assert.assertEquals(HtmlClassMatcher.countClass(html, "score"), 1);
    }

    @Test
    public void extracts_first_element_body_by_exact_class() {
        String html = String.join("\n",
                "<article class='score-card'>",
                "  <div class='score-card-label'>wrong</div>",
                "  <div class='label'>Business Substance</div>",
                "  <div class='score'>95</div>",
                "</article>");

        Assert.assertEquals(HtmlClassMatcher.firstElementBodyByClass(html, "div", "label"), "Business Substance");
        Assert.assertEquals(HtmlClassMatcher.firstElementBodyByClass(html, "div", "score"), "95");
        Assert.assertEquals(HtmlClassMatcher.firstElementBodyByClass(html, "div", "missing"), "");
    }
}
