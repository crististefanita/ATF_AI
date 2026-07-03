package com.endava.ai.atf.core.support.inventory;

import com.endava.ai.core.support.contract.AtfContractProfile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Optional;

@Getter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class AtfCoreTestSource {
    private final String relativePath;
    private final int testCount;
    private final boolean atfContract;
    private final boolean atfContractSuite;
    private final String suiteProfile;
    private final boolean testListener;
    private final boolean noFrameworkReporting;
    private final boolean allureOnlyGroup;
    private final boolean extentOnlyGroup;

    boolean hasTests() {
        return testCount > 0;
    }

    boolean hasAtfContract() {
        return atfContract;
    }

    boolean hasAtfContractSuite() {
        return atfContractSuite;
    }

    boolean isReportableThroughTestListener() {
        return testListener && !noFrameworkReporting;
    }

    boolean isSelfAuditingExtentContract() {
        return noFrameworkReporting
                && hasExtentOnlyGroup()
                && profileMetadata()
                        .map(AtfContractProfile::isExtentRecommended)
                        .orElse(false);
    }

    boolean isExtentAuditableContract() {
        return isReportableThroughTestListener() || isSelfAuditingExtentContract();
    }

    boolean isPureFrameworkContract() {
        return profileMetadata()
                .map(AtfContractProfile::isPureFramework)
                .orElse(false);
    }

    boolean isLiveBoundaryContract() {
        return profileMetadata()
                .map(AtfContractProfile::isLiveBoundary)
                .orElse(false);
    }

    Optional<AtfContractProfile> profileMetadata() {
        try {
            return Optional.of(AtfContractProfile.valueOf(suiteProfile));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    boolean isAllureRecommended() {
        return profileMetadata()
                .map(AtfContractProfile::isAllureOnly)
                .orElse(false);
    }

    boolean hasAllureOnlyGroup() {
        return allureOnlyGroup;
    }

    boolean hasExtentOnlyGroup() {
        return extentOnlyGroup;
    }

    boolean isIntentionalFailureFixture() {
        return relativePath.startsWith("reporting/failurefixtures/")
                || relativePath.startsWith("core/reporting/failurefixtures/");
    }

    boolean isStandaloneNarrativeContract() {
        return relativePath.startsWith("core/reporting/htmlcontract/")
                || relativePath.equals("core/reporting/regression/overview/ExtentOverviewTopCardsTimingContractTest.java")
                || relativePath.equals("core/skills/architecture/AtfSkillCleanSanityRunnerContractTest.java")
                || relativePath.equals("core/skills/architecture/AtfSkillExecutionReceiptValidatorContractTest.java")
                || relativePath.equals("core/skills/architecture/AtfSkillPromptRoutingContractTest.java")
                || relativePath.equals("core/skills/architecture/AtfTestArchitectureContractTest.java")
                || relativePath.startsWith("core/skills/architecture/metrics/")
                || relativePath.startsWith("core/skills/architecture/model/")
                || relativePath.startsWith("business/tests/");
    }

    boolean isIntentionalNonContractExecutable() {
        return isIntentionalFailureFixture() || isStandaloneNarrativeContract();
    }

    String summary() {
        return relativePath
                + " | tests=" + testCount
                + " | contract=" + atfContract
                + " | suite=" + atfContractSuite
                + " | profile=" + suiteProfile
                + " | engine=" + profileMetadata()
                        .map(AtfContractProfile::recommendedEngine)
                        .orElse("unknown")
                + " | allureOnly=" + allureOnlyGroup
                + " | extentOnly=" + extentOnlyGroup
                + " | listener=" + testListener
                + " | noFrameworkReporting=" + noFrameworkReporting;
    }
}
