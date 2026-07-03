package com.endava.ai.atf.core.support.contract;

import org.testng.Assert;

import java.io.File;

public final class AtfReportingArtifactAssertions {

    private AtfReportingArtifactAssertions() {
    }

    public static void assertDirectoryState(File directory, boolean expectedExists, String contractReason) {
        Assert.assertEquals(
                directory.exists(),
                expectedExists,
                contractReason + " | directory=" + directory.getAbsolutePath()
        );
    }
}
