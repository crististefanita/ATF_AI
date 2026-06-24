package com.endava.ai.mcp.tests.latest;

import com.endava.ai.core.listener.TestListener;
import com.endava.ai.mcp.validation.McpPackageAuditValidation;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(TestListener.class)
public class McpLatestPackageRunQualityContractTest {
    @Test
    public void eachRunXmlMatchesRuntimeTargetAndHasNoFailures() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertEachRunXmlMatchesRuntimeTargetAndHasNoFailures();
    }

    @Test
    public void eachRunStatePublishesTargetTestsAndBusinessStatus() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertEachRunStatePublishesTargetTestsAndBusinessStatus();
    }

    @Test
    public void eachRunScoreSupportMeetsRuntimeDeltaQualityWhenPublished() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertEachRunScoreSupportMeetsRuntimeDeltaQualityWhenPublished();
    }

    @Test
    public void eachRunDiffAndFeedbackAreSubstantiveEnoughToReopen() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertEachRunDiffAndFeedbackAreSubstantiveEnoughToReopen();
    }

    @Test
    public void runDiffHistoryIsColdDistinct() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertRunDiffHistoryIsColdDistinct();
    }

    @Test
    public void runIndependenceIsAuditable() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertRunIndependenceIsAuditable();
    }

    @Test
    public void runXmlMapsToRealJavaTestMethods() throws IOException {
        McpPackageAuditValidation.latestPackage().runs().assertRunXmlMapsToRealJavaTestMethods();
    }
}
