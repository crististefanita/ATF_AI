package com.ai.atf.core.reporting.listener;

import com.ai.core.support.contract.ids.AtfContractNarrativeIds;
import com.ai.core.support.contract.AtfContractRef;
import com.ai.core.support.contract.AtfContractSuiteRef;
import com.ai.atf.core.reporting.failurefixtures.LongReferenceFailureSignatureFixture;
import com.ai.core.reporting.listener.TestListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

@AtfContractSuiteRef(AtfContractNarrativeIds.Suite.LISTENER_INTERNAL_FORMATTING_CONTRACT_TEST_SUITE)
@AtfContractRef(AtfContractNarrativeIds.Contract.FAILURE_SUMMARY_PREVIEW_CONTRACT_TEST_CLASS)
@Listeners(TestListener.class)
public class FailureSummaryPreviewContractTest {
    @Test
    public void longRepeatedJavaReferenceListsAreCompactedForFailureSignatureTitles() {
        String summary = "AssertionError: " + LongReferenceFailureSignatureFixture.errorMessage();

        String preview = preview(summary);

        Assert.assertTrue(preview.startsWith("AssertionError: "
                        + LongReferenceFailureSignatureFixture.ACTIONABLE_REASON),
                "Preview must keep the actionable failure reason.");
        Assert.assertTrue(preview.contains(LongReferenceFailureSignatureFixture.FIRST_REFERENCE_TITLE),
                "Preview must keep the first concrete Java reference.");
        Assert.assertTrue(preview.contains("... +"),
                "Preview must disclose that additional references were omitted.");
        Assert.assertFalse(preview.contains(LongReferenceFailureSignatureFixture.REPEATED_SOURCE_PREFIX),
                "Preview must remove repeated source path prefixes from visible root-cause titles.");
        Assert.assertTrue(preview.length() <= 220,
                "Visible root-cause title must stay within the agreed compact display budget: " + preview.length());
    }

    private static String preview(String summary) {
        try {
            Class<?> type = Class.forName("com.ai.core.reporting.listener.internal.runtime.model.FailureSummaryPreview");
            Method from = type.getDeclaredMethod("from", String.class);
            from.setAccessible(true);
            return (String) from.invoke(null, summary);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("FailureSummaryPreview internal contract is no longer reachable.", exception);
        }
    }
}
