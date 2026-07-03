package com.ai.skills.atlas.tests.candidate;

import com.ai.skills.atlas.AtlasContext;
import com.ai.skills.atlas.contract.AtlasContractSuiteRef;
import com.ai.skills.atlas.contract.AtlasSuiteIds;
import com.ai.skills.atlas.report.AtlasReportModel;
import com.ai.skills.atlas.tests.support.AtlasContractTestSupport;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;

import static com.ai.skills.atlas.contract.AtlasRuleIds.Base.BUSINESS_FLOW_PRESENT;
import static com.ai.skills.atlas.contract.AtlasRuleIds.Base.DIAGRAM_STYLE_MINIMAL;
import static com.ai.skills.atlas.contract.AtlasRuleIds.Base.DECLARED_OUTPUT;
import static com.ai.skills.atlas.contract.AtlasRuleIds.Base.OUTPUT_EXISTS;
import static com.ai.skills.atlas.contract.AtlasRuleIds.Base.ROW_TEMPLATE_COMPLETE;
import static com.ai.skills.atlas.contract.AtlasRuleIds.Base.SECTIONS_COLLAPSIBLE;
import static com.ai.skills.atlas.contract.AtlasRuleIds.Base.SOURCE_READ_ONLY;
import static com.ai.skills.atlas.contract.AtlasRuleIds.Base.USES_SHARED_CSS;

@Listeners(TestListener.class)
@AtlasContractSuiteRef(AtlasSuiteIds.GENERATED_OUTPUT)
public class AtlasGeneratedOutputContractTest extends AtlasContractTestSupport {
    @Test
    public void atlasOutputExists() throws IOException {
        assertBaseRule(OUTPUT_EXISTS);
    }

    @Test
    public void atlasUsesDeclaredOutputArtifact() throws IOException {
        assertBaseRule(DECLARED_OUTPUT);
    }

    @Test
    public void atlasUsesSharedCss() throws IOException {
        assertBaseRule(USES_SHARED_CSS);
    }

    @Test
    public void atlasSectionsAreCollapsible() throws IOException {
        assertBaseRule(SECTIONS_COLLAPSIBLE);
    }

    @Test
    public void atlasContainsBusinessFlowDiagrams() throws IOException {
        assertBaseRule(BUSINESS_FLOW_PRESENT);
    }

    @Test
    public void atlasRowsExposeBusinessMappingTemplate() throws IOException {
        assertBaseRule(ROW_TEMPLATE_COMPLETE);
    }

    @Test
    public void atlasKeepsSourceAnalysisReadOnly() throws IOException {
        assertBaseRule(SOURCE_READ_ONLY);
    }

    @Test
    public void atlasDiagramsUseMinimalSharedReportStyle() throws IOException {
        assertBaseRule(DIAGRAM_STYLE_MINIMAL);
    }

    @Test
    public void atlasContextUsesDeclaredScopedSourceRoot() throws IOException {
        AtlasContext context = AtlasContext.fromWorkingDirectory();
        Assert.assertTrue(
                AtlasReportModel.isDeclaredAtlasArtifactName(context.atlasHtml().getFileName().toString()),
                "Atlas output name must be generic " + AtlasContext.BUSINESS_ATLAS_FILE
                        + " or scoped <ATF_NAME>" + AtlasContext.BUSINESS_ATLAS_SUFFIX + "."
        );

        Assert.assertTrue(
                Files.exists(context.sourceRoot()),
                "Configured atlas source root must exist: " + context.sourceRoot()
        );
        Assert.assertTrue(
                Files.exists(context.uiSourceRoot()),
                "Configured atlas traversed main root must exist: " + context.uiSourceRoot()
        );
    }
}
