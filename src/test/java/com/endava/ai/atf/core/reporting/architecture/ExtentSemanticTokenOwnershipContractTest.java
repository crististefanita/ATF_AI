package com.endava.ai.atf.core.reporting.architecture;

import com.endava.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.endava.ai.core.support.contract.AtfContractSuiteRef;
import com.endava.ai.core.support.contract.AtfContractRef;
import com.endava.ai.core.support.contract.ids.AtfSourcePaths;
import com.endava.ai.core.reporting.listener.TestListener;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDashboardLabels;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDomIds;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import com.endava.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;
import com.endava.ai.core.reporting.adapters.extent.spark.ExtentSparkHtmlPostProcessor;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentSemanticToken;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.EXTENT_SEMANTIC_TOKEN_OWNERSHIP_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SEMANTIC_TOKEN_OWNERSHIP_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class ExtentSemanticTokenOwnershipContractTest {

    private static final Path POST_PROCESSOR = AtfSourcePaths.mainJava(ExtentSparkHtmlPostProcessor.class);

    private static final List<Path> REQUIRED_OWNERS = List.of(
            AtfSourcePaths.mainJava(ExtentMarkers.class),
            AtfSourcePaths.mainJava(ExtentDataAttributes.class),
            AtfSourcePaths.mainJava(ExtentDomIds.class),
            AtfSourcePaths.mainJava(ExtentSemanticToken.class),
            AtfSourcePaths.mainJava(ExtentReportModeType.class)
    );

    @Test
    public void postProcessorDoesNotOwnBehavioralExtentTokens() throws IOException {
        String source = Files.readString(POST_PROCESSOR);

        Assert.assertFalse(source.contains(ExtentMarkers.REPORT_MODE));
        Assert.assertFalse(source.contains(ExtentMarkers.LEGACY_COMPONENT_FALLBACK));
        Assert.assertFalse(source.contains(ExtentMarkers.NAVIGATION_GUARD));
        Assert.assertFalse(source.contains(ExtentDataAttributes.REPORT_MODE));
        Assert.assertFalse(source.contains(ExtentReportModeType.RUNTIME_AUDITED.value()));
        Assert.assertFalse(source.contains(ExtentReportModeType.COMPONENT_ADAPTER.value()));

        for (Path owner : REQUIRED_OWNERS) {
            Assert.assertTrue(Files.isRegularFile(owner), "Missing semantic owner: " + owner);
        }
    }

    @Test
    @AtfContractRef(AtfContractNarrativeIds.Contract.EXTENT_SEMANTIC_TOKEN_OWNERSHIP_CONTRACT_TEST_EXTENT_SEMANTIC_TOKENS_HAVE_TYPED_DESCRIPTORS_AND_REPAIR_TARGETS)
    public void extentSemanticTokensHaveTypedDescriptorsAndRepairTargets() {
        List<ExtentSemanticToken> tokens = ExtentSemanticToken.all();
        Set<ExtentSemanticToken.Kind> kinds = tokens.stream()
                .map(ExtentSemanticToken::kind)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ExtentSemanticToken.Kind.class)));
        Set<String> values = tokens.stream()
                .map(ExtentSemanticToken::value)
                .collect(Collectors.toSet());

        Assert.assertTrue(kinds.containsAll(EnumSet.allOf(ExtentSemanticToken.Kind.class)),
                "Every Extent semantic token kind must be represented: " + kinds);
        Assert.assertTrue(values.containsAll(Set.of(
                ExtentMarkers.REPORT_MODE,
                ExtentMarkers.NAVIGATION_GUARD,
                ExtentMarkers.COLLAPSE_SCRIPT,
                ExtentMarkers.LEGACY_COMPONENT_FALLBACK,
                ExtentMarkers.RUNTIME_TAG_AUDIT_START,
                ExtentMarkers.RUNTIME_TAG_AUDIT_END,
                ExtentMarkers.RUNTIME_FAILURE_GROUPS_START,
                ExtentMarkers.RUNTIME_FAILURE_GROUPS_END,
                ExtentMarkers.CORE_FINAL_AUDIT_START,
                ExtentMarkers.CORE_FINAL_AUDIT_END,
                ExtentMarkers.COMPONENT_TAG_AUDIT_START,
                ExtentMarkers.COMPONENT_TAG_AUDIT_END,
                ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL,
                ExtentMarkers.RUNTIME_FAILURE_GROUPS_LABEL,
                ExtentMarkers.COMPONENT_TAG_AUDIT_LABEL,
                ExtentDataAttributes.REPORT_MODE,
                ExtentDataAttributes.RUNTIME_AUDIT,
                ExtentDataAttributes.NAV_BOUND,
                ExtentDataAttributes.RUNTIME_TAG_TABLE,
                ExtentDataAttributes.COMPONENT_TAG_TABLE,
                ExtentDataAttributes.RUNTIME_FAILURE_TABLE,
                ExtentDataAttributes.RUNTIME_FAILURE_GROUP,
                ExtentDataAttributes.RUNTIME_FAILURE_SIGNATURES,
                ExtentDataAttributes.RUNTIME_METHOD,
                ExtentDataAttributes.TEST_CASES_TOTAL,
                ExtentDataAttributes.RUNTIME_TOOLTIP,
                ExtentDomIds.NAV_TEST,
                ExtentDomIds.NAV_CATEGORY,
                ExtentDomIds.NAV_DASHBOARD,
                ExtentDomIds.STATUS_TOGGLE,
                ExtentDomIds.TEST_VIEW,
                ExtentDomIds.CATEGORY_VIEW,
                ExtentDomIds.DASHBOARD_VIEW,
                ExtentReportModeType.RUNTIME_AUDITED.value(),
                ExtentReportModeType.COMPONENT_ADAPTER.value()
        )), "Extent semantic token catalog is missing runtime-owned values: " + values);

        for (ExtentSemanticToken token : tokens) {
            Assert.assertFalse(token.owner().isBlank(), "Token owner must be explicit: " + token);
            Assert.assertFalse(token.responsibility().isBlank(), "Token responsibility must be explicit: " + token);
            Assert.assertFalse(token.repairTarget().isBlank(), "Token repair target must be explicit: " + token);
        }
    }

    @Test
    public void extentSemanticTokensCoverPublishedOverviewVocabulary() {
        Set<String> tokenValues = ExtentSemanticToken.all().stream()
                .map(ExtentSemanticToken::value)
                .collect(Collectors.toSet());

        Assert.assertTrue(tokenValues.containsAll(Set.of(
                ExtentDashboardLabels.STARTED,
                ExtentDashboardLabels.ENDED,
                ExtentDashboardLabels.DURATION,
                ExtentDashboardLabels.TEST_CASES,
                ExtentDashboardLabels.TESTS_PASSED,
                ExtentDashboardLabels.TESTS_FAILED,
                ExtentDashboardLabels.LEGACY_TAGS
        )), "Published Overview labels must be part of the semantic token catalog: " + tokenValues);

        Assert.assertTrue(tokenValues.containsAll(ExtentDashboardLabels.FORBIDDEN_OVERVIEW_SCORE_CARDS),
                "Forbidden native Spark Overview labels must be cataloged so inspectors can explain drift: "
                        + tokenValues);
    }
}
