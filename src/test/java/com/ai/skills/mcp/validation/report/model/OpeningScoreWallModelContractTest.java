package com.ai.skills.mcp.validation.report.model;

import com.ai.skills.mcp.validation.profile.McpReportTemplateProfile;
import com.ai.skills.mcp.validation.profile.html.McpHtmlExampleReference;
import com.ai.skills.mcp.validation.profile.html.McpReportTemplateConsensus;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;

public final class OpeningScoreWallModelContractTest {

    @Test
    public void parses_opening_score_wall_from_primary_html_example_reference() throws IOException {
        McpReportTemplateConsensus consensus = McpReportTemplateProfile.htmlExConsensus();
        McpHtmlExampleReference primary = consensus.corpus().primaryReference();
        OpeningScoreWallModel model = OpeningScoreWallModel.fromHtml(Files.readString(primary.report()));

        Assert.assertTrue(model.hasHeroBeforeScoreWall());
        Assert.assertTrue(model.hasScoreWallBeforeExecutiveContext());
        Assert.assertEquals(model.scoreCardCount(), consensus.scoreCardLabels().size());
        Assert.assertEquals(model.labelCount(), consensus.scoreCardLabels().size());
        Assert.assertEquals(model.scoreCount(), consensus.scoreCardLabels().size());
        Assert.assertTrue(model.hasScoreCardChildren());
        Assert.assertFalse(model.hasLooseLabelOrScoreBeforeFirstCard());
        Assert.assertFalse(model.hasRuntimeStripBetweenScoreWallAndExecutiveContext());
    }

    @Test
    public void detects_compressed_or_misplaced_opening_score_wall_content() {
        OpeningScoreWallModel model = OpeningScoreWallModel.fromHtml(String.join("\n",
                "<section class=\"hero\"><h2>Verdict</h2></section>",
                "<section class=\"score-wall\">",
                "  <div class=\"label\">Overall</div><div class=\"score\">90</div>",
                "  <article class=\"score-card\"><span>Business Substance</span><strong>90</strong></article>",
                "  <div class=\"runtime-strip\">RUN_EXPECTED_DELTA_QUALITY</div>",
                "</section>",
                "<details id=\"executive-context\"><summary>Executive Context</summary></details>"));

        Assert.assertTrue(model.hasCompressedSpanStrongScoreCard());
        Assert.assertTrue(model.hasLooseLabelOrScoreBeforeFirstCard());
        Assert.assertTrue(model.hasRuntimeStripBetweenScoreWallAndExecutiveContext());
        Assert.assertTrue(model.hasVerdictOrRuntimeBeforeScoreCards());
    }
}
