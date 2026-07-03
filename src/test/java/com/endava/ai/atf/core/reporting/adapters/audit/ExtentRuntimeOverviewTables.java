package com.endava.ai.atf.core.reporting.adapters.audit;

import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentDataAttributes;
import com.endava.ai.core.reporting.adapters.extent.tokens.ExtentMarkers;
import org.testng.Assert;

public final class ExtentRuntimeOverviewTables {
    private static final int CANONICAL_RUNTIME_TAG_TABLE_COUNT = 3;

    private ExtentRuntimeOverviewTables() {
    }

    public static String attributeEquals(String attribute, String value) {
        return attribute + "=\"" + value + "\"";
    }

    public static String runtimeTagTableAttributePrefix() {
        return ExtentDataAttributes.RUNTIME_TAG_TABLE + "=\"";
    }

    public static String runtimeTagTableAttribute(String kind) {
        return attributeEquals(ExtentDataAttributes.RUNTIME_TAG_TABLE, kind);
    }

    public static String componentTagTableAttributePrefix() {
        return ExtentDataAttributes.COMPONENT_TAG_TABLE + "=\"";
    }

    public static String componentTagTableAttribute(String kind) {
        return attributeEquals(ExtentDataAttributes.COMPONENT_TAG_TABLE, kind);
    }

    public static void assertCanonicalRuntimeTagTableShell(String dashboard) {
        Assert.assertTrue(
                dashboard.contains(ExtentMarkers.RUNTIME_TAG_AUDIT_LABEL),
                "Overview must include the ATF runtime tag audit after the timeline."
        );
        Assert.assertEquals(
                countOccurrences(dashboard, runtimeTagTableAttributePrefix()),
                CANONICAL_RUNTIME_TAG_TABLE_COUNT,
                "Overview must expose exactly three runtime tag tables: classes, packages, and annotations."
        );
        Assert.assertTrue(
                dashboard.contains(runtimeTagTableAttribute(ExtentDataAttributes.TAG_TABLE_CLASSES))
                        && dashboard.contains(runtimeTagTableAttribute(ExtentDataAttributes.TAG_TABLE_PACKAGES))
                        && dashboard.contains(runtimeTagTableAttribute(ExtentDataAttributes.TAG_TABLE_ANNOTATIONS)),
                "Overview must expose canonical runtime tag tables for classes, packages, and annotations."
        );
    }

    public static void assertCanonicalRuntimeTagTables(String dashboard) {
        assertCanonicalRuntimeTagTableShell(dashboard);
        Assert.assertTrue(
                dashboard.contains(runtimeTagTableAttribute(ExtentDataAttributes.TAG_TABLE_CLASSES))
                        && dashboard.contains("Test methods by class percentage total</th><td>4</td><td>100%</td>"),
                "Overview class table must be a 100% distribution over test methods grouped by Java test class."
        );
        Assert.assertTrue(
                dashboard.contains(runtimeTagTableAttribute(ExtentDataAttributes.TAG_TABLE_PACKAGES))
                        && dashboard.contains("Test methods by package percentage total</th><td>4</td><td>100%</td>"),
                "Overview package table must be a 100% distribution over test methods grouped by Java test package."
        );
        Assert.assertTrue(
                dashboard.contains(runtimeTagTableAttribute(ExtentDataAttributes.TAG_TABLE_ANNOTATIONS))
                        && dashboard.contains("ATF_CORE")
                        && dashboard.contains("ATF_UI"),
                "Overview annotation table must expose overlapping semantic tags such as ATF_CORE and ATF_UI."
        );
        Assert.assertFalse(
                dashboard.contains("Other annotations / tags percentage total"),
                "Overview annotation table must not pretend optional overlapping tags sum to 100%."
        );
    }

    public static int countOccurrences(String value, String needle) {
        int count = 0;
        int index = 0;
        while ((index = value.indexOf(needle, index)) >= 0) {
            count++;
            index += needle.length();
        }
        return count;
    }
}
