package com.ai.atf.core.reporting.failurefixtures;

import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.core.support.contract.ids.AtfTestGroups;
import com.ai.atf.core.support.contracts.annotations.ContractAnnotationBestPracticeTest;
import com.ai.atf.core.support.contract.core.AtfCoreContractNarrativeCoverageTest;
import com.ai.atf.core.reporting.adapters.ExtentAdapterTest;
import com.ai.atf.core.reporting.integration.skills.DiagnosticHtmlReportShellContractTest;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
public final class LongReferenceFailureSignatureFixture {
    public static final String ACTIONABLE_REASON =
            "Move bulky @AtfContract/@AtfContractSuite narratives ";
    public static final String FIRST_REFERENCE_TITLE =
            "ContractAnnotationBestPracticeTest.java @AtfContractRef";
    public static final String REPEATED_SOURCE_PREFIX =
            "src\\test\\java\\com\\atf\\ai";
    public static final String DIAGNOSTIC_SHELL_REFERENCE =
            FailureFixtureReferencePaths.ref(
                    DiagnosticHtmlReportShellContractTest.class,
                    AtfContractRef.class
            ).format();

    private static final String LONG_REFERENCE_ERROR =
            ACTIONABLE_REASON
                    + "to contract-narratives.yml via refs; real ATF test refs=["
                    + FailureFixtureReferencePaths.refs(
                            FailureFixtureReferencePaths.ref(ContractAnnotationBestPracticeTest.class,
                                    AtfContractRef.class),
                            FailureFixtureReferencePaths.ref(ContractAnnotationBestPracticeTest.class,
                                    AtfContractSuiteRef.class),
                            FailureFixtureReferencePaths.ref(AtfCoreContractNarrativeCoverageTest.class,
                                    AtfContractRef.class),
                            FailureFixtureReferencePaths.ref(AtfCoreContractNarrativeCoverageTest.class,
                                    AtfContractSuiteRef.class),
                            FailureFixtureReferencePaths.ref(ExtentAdapterTest.class, AtfContractRef.class),
                            FailureFixtureReferencePaths.ref(ExtentAdapterTest.class, AtfContractSuiteRef.class),
                            FailureFixtureReferencePaths.ref(DiagnosticHtmlReportShellContractTest.class,
                                    AtfContractRef.class))
                    + "]";

    public static String errorMessage() {
        return LONG_REFERENCE_ERROR;
    }

    @Test(groups = AtfTestGroups.CONTROLLED_FAILURE_FIXTURE)
    public void long_reference_failure_signature_is_compacted_in_extent_triage() {
        if (!FailureFixtureSwitch.isEnabled()) {
            return;
        }
        Assert.fail(errorMessage());
    }
}
