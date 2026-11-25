package com.ai.core.reporting.adapters.extent;

import com.aventstack.extentreports.ExtentTest;

import java.util.Date;

public final class ExtentNodeTiming {
    private static final long MINIMUM_VISIBLE_DURATION_MILLIS = 1L;

    private ExtentNodeTiming() {
    }

    public static void start(ExtentTest node) {
        if (node == null) {
            return;
        }
        Date start = node.getModel().getStartTime();
        long startedAt = start == null ? System.currentTimeMillis() : start.getTime();
        node.getModel().setStartTime(new Date(startedAt));
        node.getModel().setEndTime(new Date(startedAt + MINIMUM_VISIBLE_DURATION_MILLIS));
    }

    public static void finish(ExtentTest node) {
        if (node == null) {
            return;
        }
        Date start = node.getModel().getStartTime();
        long startedAt = start == null ? System.currentTimeMillis() : start.getTime();
        long endedAt = Math.max(System.currentTimeMillis(), startedAt + MINIMUM_VISIBLE_DURATION_MILLIS);
        node.getModel().setStartTime(new Date(startedAt));
        node.getModel().setEndTime(new Date(endedAt));
    }
}
