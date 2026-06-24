package com.endava.ai.atlas.tests;

import com.endava.ai.atlas.validation.AtlasAuditValidation;
import com.endava.ai.core.listener.TestListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
public class AtlasGeneratedOutputContractTest {
    @Test
    public void atlasOutputExists() throws IOException {
        AtlasAuditValidation.generatedAtlas().assertRulePasses("atlas.output.exists");
    }

    @Test
    public void atlasUsesSharedCss() throws IOException {
        AtlasAuditValidation.generatedAtlas().assertRulePasses("atlas.html.uses-shared-css");
    }

    @Test
    public void atlasSectionsAreCollapsible() throws IOException {
        AtlasAuditValidation.generatedAtlas().assertRulePasses("atlas.sections.collapsible");
    }

    @Test
    public void atlasContainsBusinessFlowDiagrams() throws IOException {
        AtlasAuditValidation.generatedAtlas().assertRulePasses("atlas.business-flow.present");
    }

    @Test
    public void atlasRowsExposeBusinessMappingTemplate() throws IOException {
        AtlasAuditValidation.generatedAtlas().assertRulePasses("atlas.row-template.complete");
    }

    @Test
    public void atlasKeepsSourceAnalysisReadOnly() throws IOException {
        AtlasAuditValidation.generatedAtlas().assertRulePasses("atlas.source.read-only");
    }

    @Test
    public void atlasDiagramsUseMinimalSharedReportStyle() throws IOException {
        AtlasAuditValidation.generatedAtlas().assertRulePasses("atlas.diagram-style.minimal");
    }
}
