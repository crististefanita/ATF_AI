package com.endava.ai.core.reporting.adapters.extent.inspection;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Accessors(fluent = true)
public final class ExtentStatusGroupSnapshot {
    private final int parentCount;
    private final int passParent;
    private final int failParent;
    private final int skipParent;
    private final int childCount;
    private final int passChild;
    private final int failChild;
    private final int skipChild;
    private final int grandChildCount;
    private final int passGrandChild;
    private final int failGrandChild;
    private final int skipGrandChild;
    private final int eventsCount;

    private ExtentStatusGroupSnapshot(String html) {
        parentCount = value(html, "parentCount");
        passParent = value(html, "passParent");
        failParent = value(html, "failParent");
        skipParent = value(html, "skipParent");
        childCount = value(html, "childCount");
        passChild = value(html, "passChild");
        failChild = value(html, "failChild");
        skipChild = value(html, "skipChild");
        grandChildCount = value(html, "grandChildCount");
        passGrandChild = value(html, "passGrandChild");
        failGrandChild = value(html, "failGrandChild");
        skipGrandChild = value(html, "skipGrandChild");
        eventsCount = value(html, "eventsCount");
    }

    public static ExtentStatusGroupSnapshot fromHtml(String html) {
        return new ExtentStatusGroupSnapshot(html);
    }

    public int parentStatusTotal() { return passParent + failParent + skipParent; }

    public boolean hasZeroChildAndEventTotals() {
        return childCount == 0
                && passChild == 0
                && failChild == 0
                && skipChild == 0
                && grandChildCount == 0
                && passGrandChild == 0
                && failGrandChild == 0
                && skipGrandChild == 0
                && eventsCount == 0;
    }

    private static int value(String html, String key) {
        Matcher matcher = Pattern.compile(Pattern.quote(key) + ":\\s*(\\d+)").matcher(String.valueOf(html));
        if (!matcher.find()) {
            throw new IllegalArgumentException("statusGroup must expose key: " + key);
        }
        return Integer.parseInt(matcher.group(1));
    }
}
