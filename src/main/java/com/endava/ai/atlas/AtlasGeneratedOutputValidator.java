package com.endava.ai.atlas;

import com.endava.ai.atlas.rules.AtlasValidationRule;
import com.endava.ai.atlas.rules.audit.AtlasTestAuditExtentRule;
import com.endava.ai.atlas.rules.base.AtlasBusinessFlowRule;
import com.endava.ai.atlas.rules.base.AtlasCollapsibleSectionsRule;
import com.endava.ai.atlas.rules.base.AtlasDiagramStyleRule;
import com.endava.ai.atlas.rules.base.AtlasOutputExistsRule;
import com.endava.ai.atlas.rules.base.AtlasRowTemplateRule;
import com.endava.ai.atlas.rules.base.AtlasSharedCssRule;
import com.endava.ai.atlas.rules.base.AtlasSourceReadOnlyRule;
import com.endava.ai.atlas.rules.business.AtlasBusinessObjectCenteredRule;
import com.endava.ai.atlas.rules.business.AtlasBusinessOnlyFlowRule;
import com.endava.ai.atlas.rules.business.AtlasComprehensionVocabularyRule;
import com.endava.ai.atlas.rules.business.AtlasFlowStoryCompletenessRule;
import com.endava.ai.atlas.rules.business.AtlasLayerTraceConcreteEvidenceRule;
import com.endava.ai.atlas.rules.business.AtlasLayerTraceMapRule;
import com.endava.ai.atlas.rules.business.AtlasMappingDepthRule;
import com.endava.ai.atlas.rules.business.AtlasObservedGapsSecondaryRule;
import com.endava.ai.atlas.rules.business.AtlasSourceTraceApplicationBusinessRule;
import com.endava.ai.atlas.rules.business.AtlasSourceTraceBackedBySourceRule;
import com.endava.ai.atlas.rules.business.AtlasSourceTraceMethodLevelRule;
import com.endava.ai.atlas.rules.business.AtlasSubgraphBusinessComplexityRule;
import com.endava.ai.atlas.rules.graph.AtlasBusinessGraphLegendRule;
import com.endava.ai.atlas.rules.graph.AtlasBusinessSubgraphsCollapsedRule;
import com.endava.ai.atlas.rules.graph.AtlasBusinessSubgraphsWidthRule;
import com.endava.ai.atlas.rules.graph.AtlasGraphMinimalStylePerSectionRule;
import com.endava.ai.atlas.rules.graph.AtlasGraphVisualScaleRule;
import com.endava.ai.atlas.rules.graph.AtlasLegendCollapseRule;
import com.endava.ai.atlas.rules.graph.AtlasLegendContentQualityRule;
import com.endava.ai.atlas.rules.graph.AtlasReferenceDiagramPrimitiveRule;
import com.endava.ai.atlas.rules.graph.AtlasReusableGraphCssRule;
import com.endava.ai.atlas.rules.graph.AtlasSupportingGraphsLegendRule;
import com.endava.ai.atlas.rules.scoring.AtlasScoreAxisConsistencyRule;
import com.endava.ai.atlas.rules.scoring.AtlasScoreCardsShapeRule;
import com.endava.ai.atlas.rules.scoring.AtlasScoreCardsDocumentedRule;
import com.endava.ai.atlas.rules.scoring.AtlasScoreEvidenceTraceRule;
import com.endava.ai.atlas.rules.scoring.AtlasScoreFormulaTraceRule;
import com.endava.ai.atlas.rules.scoring.AtlasScoreOpeningAuditPointerRule;
import com.endava.ai.atlas.rules.scoring.AtlasScoreOverallCalculationRule;
import com.endava.ai.atlas.rules.scoring.AtlasScoreCardReadabilityRule;
import com.endava.ai.atlas.rules.scoring.AtlasScoreRationaleAuditRule;
import com.endava.ai.atlas.rules.scoring.AtlasScoreSupportTraceRule;
import com.endava.ai.atlas.rules.structure.AtlasExecutiveContextShapeRule;
import com.endava.ai.atlas.rules.structure.AtlasSectionNumberingRule;
import com.endava.ai.atlas.rules.structure.AtlasSectionNumberPlacementRule;
import com.endava.ai.atlas.rules.structure.AtlasTopLevelSectionContinuityRule;

import java.io.IOException;
import java.util.List;

public final class AtlasGeneratedOutputValidator {
    private final List<AtlasValidationRule> rules;
    private final List<AtlasValidationRule> candidateRules;

    public AtlasGeneratedOutputValidator() {
        this(List.of(
                new AtlasOutputExistsRule(),
                new AtlasSharedCssRule(),
                new AtlasCollapsibleSectionsRule(),
                new AtlasBusinessFlowRule(),
                new AtlasRowTemplateRule(),
                new AtlasSourceReadOnlyRule(),
                new AtlasDiagramStyleRule()),
                List.of(
                        new AtlasScoreCardsShapeRule(),
                        new AtlasExecutiveContextShapeRule(),
                        new AtlasBusinessGraphLegendRule(),
                        new AtlasSupportingGraphsLegendRule(),
                        new AtlasReferenceDiagramPrimitiveRule(),
                        new AtlasReusableGraphCssRule(),
                        new AtlasGraphMinimalStylePerSectionRule(),
                        new AtlasGraphVisualScaleRule(),
                        new AtlasBusinessSubgraphsWidthRule(),
                        new AtlasLegendCollapseRule(),
                        new AtlasLegendContentQualityRule(),
                        new AtlasBusinessOnlyFlowRule(),
                        new AtlasBusinessSubgraphsCollapsedRule(),
                        new AtlasScoreAxisConsistencyRule(),
                        new AtlasScoreCardReadabilityRule(),
                        new AtlasScoreOpeningAuditPointerRule(),
                        new AtlasScoreRationaleAuditRule(),
                        new AtlasScoreSupportTraceRule(),
                        new AtlasScoreFormulaTraceRule(),
                        new AtlasScoreOverallCalculationRule(),
                        new AtlasScoreCardsDocumentedRule(),
                        new AtlasScoreEvidenceTraceRule(),
                        new AtlasMappingDepthRule(),
                        new AtlasSectionNumberingRule(),
                        new AtlasSectionNumberPlacementRule(),
                        new AtlasTopLevelSectionContinuityRule(),
                        new AtlasFlowStoryCompletenessRule(),
                        new AtlasLayerTraceMapRule(),
                        new AtlasLayerTraceConcreteEvidenceRule(),
                        new AtlasSourceTraceMethodLevelRule(),
                        new AtlasSourceTraceBackedBySourceRule(),
                        new AtlasSourceTraceApplicationBusinessRule(),
                        new AtlasSubgraphBusinessComplexityRule(),
                        new AtlasBusinessObjectCenteredRule(),
                        new AtlasObservedGapsSecondaryRule(),
                        new AtlasComprehensionVocabularyRule(),
                        new AtlasTestAuditExtentRule()));
    }

    public AtlasGeneratedOutputValidator(List<AtlasValidationRule> rules) {
        this(rules, List.of());
    }

    public AtlasGeneratedOutputValidator(List<AtlasValidationRule> rules, List<AtlasValidationRule> candidateRules) {
        this.rules = List.copyOf(rules);
        this.candidateRules = List.copyOf(candidateRules);
    }

    public AtlasValidationResult validate() throws IOException {
        AtlasContext context = AtlasContext.fromWorkingDirectory();
        AtlasValidationResult result = new AtlasValidationResult();
        rules.forEach(rule -> rule.validate(context, result));
        return result;
    }

    public AtlasValidationResult validateRule(String ruleId) throws IOException {
        return validateRuleFrom(ruleId, rules);
    }

    public AtlasValidationResult validateCandidate() throws IOException {
        AtlasContext context = AtlasContext.fromWorkingDirectory();
        AtlasValidationResult result = new AtlasValidationResult();
        candidateRules.forEach(rule -> rule.validate(context, result));
        return result;
    }

    public AtlasValidationResult validateCandidateRule(String ruleId) throws IOException {
        return validateRuleFrom(ruleId, candidateRules);
    }

    private AtlasValidationResult validateRuleFrom(String ruleId, List<AtlasValidationRule> selectedRules) throws IOException {
        AtlasContext context = AtlasContext.fromWorkingDirectory();
        AtlasValidationResult result = new AtlasValidationResult();
        selectedRules.stream()
                .filter(rule -> rule.id().equals(ruleId))
                .forEach(rule -> rule.validate(context, result));
        return result;
    }

    public List<AtlasValidationRule> rules() {
        return rules;
    }

    public List<AtlasValidationRule> candidateRules() {
        return candidateRules;
    }
}
