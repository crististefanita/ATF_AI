package com.ai.skills.atlas.report;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public final class AtlasScoreCardsParserContractTest {

    @Test
    public void parses_score_cards_with_exact_css_class_tokens() {
        AtlasReportModel model = AtlasReportModel.parse(String.join("\n",
                "<html><body>",
                "<article class='score-card'>",
                "  <div class='score-card-label'>wrong label</div>",
                "  <div class='label'>Business Substance</div>",
                "  <div class='score'>95</div>",
                "  <p>business-centered</p>",
                "</article>",
                "<article data-atlas-role='score-card'>",
                "  <div class='label'>Overall</div>",
                "  <div class='score'>94</div>",
                "</article>",
                "</body></html>"));

        List<AtlasReportModel.ScoreCard> cards = model.scoreCards();
        Assert.assertEquals(cards.size(), 2);
        Assert.assertEquals(cards.get(0).label(), "Business Substance");
        Assert.assertEquals(cards.get(0).score(), 95);
        Assert.assertEquals(cards.get(0).description(), "business-centered");
        Assert.assertEquals(cards.get(1).label(), "Overall");
        Assert.assertEquals(cards.get(1).score(), 94);
    }
}
