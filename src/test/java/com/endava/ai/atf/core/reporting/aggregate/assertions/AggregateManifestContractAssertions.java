package com.endava.ai.atf.core.reporting.aggregate.assertions;

import com.endava.ai.core.reporting.aggregate.artifact.AggregateManifestInspector;
import org.testng.Assert;

public final class AggregateManifestContractAssertions {
    private AggregateManifestContractAssertions() {
    }

    public static void assertStableManifestFieldMatches(String runtime, String atlas, String field) {
        Assert.assertEquals(
                manifestField(atlas, field),
                manifestField(runtime, field),
                "Atlas validation manifest must stay synchronized with runtime aggregate field: " + field
        );
    }

    public static void assertTimingFieldIsPublishable(String manifest, String manifestName, String field) {
        String value = manifestField(manifest, field);
        Assert.assertTrue(
                value.matches("\\d+(\\.\\d{3})? (ms|s)"),
                manifestName + " aggregate manifest must publish a parseable timing field: " + field + " was " + value
        );
    }

    public static void assertSlowestFieldIsPublishable(String manifest, String manifestName, String field) {
        String value = manifestField(manifest, field);
        Assert.assertTrue(
                value.contains(" - ") || "n/a".equals(value),
                manifestName + " aggregate manifest must publish an auditable slowest field: " + field + " was " + value
        );
    }

    private static String manifestField(String manifest, String field) {
        return AggregateManifestInspector.inspect(manifest).field(field);
    }
}
