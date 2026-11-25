package com.ai.core.reporting.adapters.extent.patch.component;

import com.ai.core.reporting.adapters.extent.mode.ExtentReportModeType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter(AccessLevel.PACKAGE)
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
final class ExtentComponentInventory {
    private static final Pattern TEST_ITEM = Pattern.compile("(?s)<li class=\"test-item\".*?</li>");
    private static final Pattern TEST_NAME = Pattern.compile("<p class=\"name\">([^<]+)</p>");
    private static final Pattern BADGE = Pattern.compile("<span class=\"badge badge-pill badge-default\">([^<]+)</span>");

    private final Map<String, Integer> classCounts;
    private final Map<String, Integer> packageCounts;

    static ExtentComponentInventory from(String html) {
        Map<String, Integer> classes = new LinkedHashMap<>();
        Map<String, Integer> packages = new LinkedHashMap<>();
        Matcher items = TEST_ITEM.matcher(html);
        while (items.find()) {
            String item = items.group();
            String className = first(TEST_NAME, item);
            if (className.isBlank()) {
                continue;
            }
            classes.merge(className, 1, Integer::sum);
            packages.merge(packageName(item), 1, Integer::sum);
        }
        if (classes.isEmpty()) {
            classes.put("Extent component report", 1);
            packages.put(ExtentReportModeType.COMPONENT_ADAPTER.value(), 1);
        }
        return new ExtentComponentInventory(classes, packages);
    }

    int totalClasses() {
        return classCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    private static String packageName(String item) {
        Matcher badges = BADGE.matcher(item);
        while (badges.find()) {
            String badge = badges.group(1).trim();
            if (badge.contains(".") && !badge.endsWith(".java")) {
                return badge;
            }
        }
        return ExtentReportModeType.COMPONENT_ADAPTER.value();
    }

    private static String first(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? matcher.group(1).trim() : "";
    }
}
