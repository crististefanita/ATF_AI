package com.ai.skills.atlas.tests.candidate;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.AtlasValidationResult;
import com.ai.skills.atlas.contract.AtlasContractSuiteRef;
import com.ai.skills.atlas.contract.AtlasSuiteIds;
import com.ai.skills.atlas.rules.base.AtlasCollapsibleSectionsRule;
import com.ai.skills.atlas.tests.support.AtlasContractTestSupport;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ai.skills.atlas.contract.AtlasRuleIds.Base.SECTIONS_COLLAPSIBLE;
import static com.ai.skills.atlas.contract.AtlasRuleIds.Candidate.*;

@Listeners(TestListener.class)
@AtlasContractSuiteRef(AtlasSuiteIds.CANDIDATE_STRUCTURE)
public class AtlasCandidateStructureContractTest extends AtlasContractTestSupport {
    @Test
    public void candidateAtlasReusesSharedReportShell() throws IOException {
        assertCandidateRule(SHARED_REPORT_SHELL);
    }

    @Test
    public void candidateAtlasExecutiveContextIsCollapsedAndStructured() throws IOException {
        assertCandidateRule(EXECUTIVE_CONTEXT_COLLAPSED);
    }

    @Test
    public void candidateAtlasOnlyBusinessFlowGraphIsOpenByDefault() throws IOException {
        AtlasValidationResult result = new AtlasValidationResult();
        new AtlasCollapsibleSectionsRule().validate(AtlasContext.fromWorkingDirectory(), result);
        Assert.assertTrue(result.byCode(SECTIONS_COLLAPSIBLE).isEmpty(),
                "Only 2. Business Flow Graph may be open by default in the generated Atlas report.");
    }

    @Test
    public void candidateAtlasCollapseRuleRejectsOpenSubdetails() throws IOException {
        Path fixture = Path.of("target", "atlas-contract-fixtures", "collapse-policy");
        Path atlas = fixture.resolve("open-subdetails-atlas.html");
        Files.createDirectories(fixture);
        Files.writeString(atlas,
                "<html><body>\n"
                        + "<details><summary>1. Executive Context</summary></details>\n"
                        + "<details open><summary>2. Business Flow Graph</summary></details>\n"
                        + "<details open><summary>Legend</summary></details>\n"
                        + "<details><summary>3. Business Flow Understanding</summary></details>\n"
                        + "<details><summary>4. Business Evidence Tables</summary></details>\n"
                        + "<details><summary>5. Source-to-Business Trace</summary></details>\n"
                        + "</body></html>\n");

        String previous = System.getProperty("atlas.html");
        try {
            System.setProperty("atlas.html", atlas.toString());
            AtlasValidationResult result = new AtlasValidationResult();
            new AtlasCollapsibleSectionsRule().validate(AtlasContext.fromWorkingDirectory(), result);
            Assert.assertFalse(result.byCode(SECTIONS_COLLAPSIBLE).isEmpty(),
                    "Any default-open subdetail besides 2. Business Flow Graph must be rejected.");
            Assert.assertTrue(result.byCode(SECTIONS_COLLAPSIBLE).stream()
                            .anyMatch(violation -> violation.toString().contains("non-primary")),
                    "Violation should name the non-primary default-open details block.");
        } finally {
            if (previous == null) {
                System.clearProperty("atlas.html");
            } else {
                System.setProperty("atlas.html", previous);
            }
        }
    }

    @Test
    public void candidateAtlasBusinessSubgraphsAreCollapsedByDefault() throws IOException {
        assertCandidateRule(BUSINESS_SUBGRAPHS_COLLAPSED);
    }

    @Test
    public void candidateAtlasPublishesSectionToolbarControls() throws IOException {
        assertCandidateRule(SECTION_TOOLBAR);
    }

    @Test
    public void candidateAtlasPublishesRomanianKeySentences() throws IOException {
        assertCandidateRule(REPORT_LANGUAGE_ROMANIAN);
    }

    @Test
    public void candidateAtlasUsesSemanticContractMarkersInsteadOfHiddenKeywords() throws IOException {
        assertCandidateRule(SEMANTIC_CONTRACT_MARKERS);
    }

    @Test
    public void candidateAtlasSectionsAreNumberedForNavigation() throws IOException {
        assertCandidateRule(SECTIONS_NUMBERED);
    }

    @Test
    public void candidateAtlasSectionNumbersStayInTitlesNotHints() throws IOException {
        assertCandidateRule(SECTIONS_NUMBER_PLACEMENT);
    }

    @Test
    public void candidateAtlasTopLevelSectionsRemainContinuous() throws IOException {
        assertCandidateRule(SECTIONS_TOP_LEVEL_CONTINUITY);
    }
}
